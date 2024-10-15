from __future__ import division
from __future__ import print_function

import os
import time
from datetime import datetime

from sklearn.cluster import KMeans
import numpy as np
import pandas as pd
import torch
import torch.nn.functional as F
import torch.optim as optim
from sklearn.metrics import adjusted_rand_score, normalized_mutual_info_score
from sklearn.manifold import TSNE
import matplotlib.pyplot as plt
import scipy.sparse as sp
import torch.nn.functional as F

from utils_sssnet import get_powers_sparse, get_powers_sparse_neg, impor_bridge_set # scipy_sparse_to_torch_sparse
from utils_sssnet import split_labels, write_log # extract_edges, getClassMean
from metrics_sssnet import triplet_loss_InnerProduct_alpha, Prob_Balanced_Ratio_Loss, Prob_Balanced_Normalized_Loss, imp_Prob_Balanced_Normalized_Loss
from metrics_sssnet import label_size_ratio, print_performance_mean_std, get_cut_and_distribution, link_sign_loss_function
from models_sssnet_nsvgd import SSSNET_nsvgd
from imp_encoder_mlp_models_sssnet import SSSNET
from cluster_sssnet import Cluster
from preprocess_sssnet import load_data
from param_parser_sssnet import parameter_parser
from evaluation import eva
from utils_sssnet import spectral_adjacency_reg, signed_Laplacian_features

args = parameter_parser()

device = args.device
# print('old dataset', args.dataset) # default dataset is SSBM/.
if args.dataset[-1] != '/':
    args.dataset += '/'
if args.cuda:
    torch.cuda.manual_seed(args.seed)
no_magnet = True
compare_names = []
if 'spectral' in args.all_methods: # ['spectral','SSSNET']
    compare_names = ['A','sns','dns','L','L_sym','BNC','BRC','SPONGE','SPONGE_sym']
num_gnn = 0
if 'SSSNET' in args.all_methods:
    num_gnn += 1
    compare_names.append('SSSNET')
    compare_names_all = []
    compare_names_all.extend(compare_names[:-1]) # # remove the last one
    for feat_opt in args.feature_options: # ['A_reg']
        compare_names_all.append(compare_names[-1]+'_'+feat_opt)
else:
    compare_names_all = compare_names

def setup_seed(seed):
    torch.manual_seed(seed)
    torch.cuda.manual_seed_all(seed)
    np.random.seed(seed)
    # random.seed(seed)
    torch.backends.cudnn.deterministic = True


class SSSNET_Trainer(object):
    """
    Object to train and score different models.
    """

    def __init__(self, args, random_seed):
        """
        Constructing the trainer instance.
        :param args: Arguments object.
        """
        self.args = args
        self.device = args.device
        self.reconstruction_loss = True


        label, comb = load_data(args, args.load_only, random_seed) #  self.train_mask,

        # normalize label, the minimum should be 0 as class index
        _label_ = label - np.amin(label) # 0/1-0
        self.label = torch.from_numpy(_label_[np.newaxis]).to(device) # np.newaxis=None
        self.cluster_dim = np.amax(_label_) + 1 # 1+1

        self.num_clusters = self.cluster_dim

        self.feat_adj_reg, self.feat_L, self.feat_given, self.A_p_scipy, self.A_n_scipy = comb


        #-------------------------------------Rectify the fliped sign-------------------------------------
        aa = 1
        if args.eta >= 0 and aa == -1:
            A_P, A_N = impor_bridge_set(self.A_p_scipy, self.A_n_scipy, label=label)
            self.A_p_scipy = sp.csc_matrix(A_P)
            self.A_n_scipy = sp.csc_matrix(A_N)




        self.A_p, self.A_p_power = get_powers_sparse(self.A_p_scipy,                                 hop=1, tau=self.args.tau)  # oran:
        self.A_n, self.A_n_power = get_powers_sparse_neg(self.A_n_scipy, self.A_p_scipy,             hop=3, tau=0)  # oran: tau=0
        self.A_pt, _ = get_powers_sparse(self.A_p_scipy.transpose(),                                 hop=1, tau=self.args.tau)  # oran: self.args.tau
        self.A_nt, _ = get_powers_sparse_neg(self.A_n_scipy.transpose(), self.A_p_scipy.transpose(), hop=3, tau=0)  # hop 代表了对邻接矩阵的增强程度


        self.A_p = self.A_p.to(self.args.device)
        self.A_n = self.A_n.to(self.args.device)
        self.A_pt = self.A_pt.to(self.args.device)
        self.A_pt = self.A_pt.to(self.args.device)
        self.adj_label = (self.A_p - self.A_n).to_dense()
        self.adj_p_label = (-1) * self.A_p.to_dense()
        self.adj_n_label = (-1) * self.A_n.to_dense()
        self.A_n = -self.A_n
        print('-self.A_n')




        if self.args.dense:
            print('runing the analysis of edges within/without cluster')
            self.A_p = self.A_p.to_dense()
            self.A_n = self.A_n.to_dense()
            self.A_pt = self.A_pt.to_dense()
            self.A_nt = self.A_nt.to_dense()

            # print('self.A_n, min, max', self.A_n, torch.min(self.A_n), torch.max(self.A_n))
            #---------------------------------------analysis of edges within/without cluster------------------



        self.c = Cluster((0.5*(self.A_p_scipy+self.A_p_scipy.transpose()), 0.5*(self.A_n_scipy+self.A_n_scipy.transpose()), int(self.num_clusters)))

        date_time = datetime.now().strftime('%m-%d-%H:%M:%S')
        if args.dataset[:-1].lower() == 'ssbm': # SSBM/[:-1]=SSBM
            default_values = [args.p, args.K, args.N, args.seed_ratio, args.train_ratio, args.test_ratio, args.size_ratio, args.eta, args.num_trials]
        elif args.dataset[:-1].lower() == 'polarized':
            default_values = [args.total_n, args.num_com, args.p, args.K, args.N, args.seed_ratio, args.train_ratio, args.test_ratio, args.size_ratio, args.eta, args.num_trials]
        else:
            default_values = [args.K, args.seed_ratio,args.train_ratio, args.test_ratio, args.num_trials]
        save_name = '_'.join([str(int(100*value)) for value in default_values])
        save_name += 'Seed' + str(random_seed)
        self.log_path = os.path.join(os.path.dirname(os.path.realpath(__file__)), args.log_root, args.dataset[:-1], save_name, date_time)

        if os.path.isdir(self.log_path) == False:
            try:
                os.makedirs(self.log_path)
            except FileExistsError:
                print('Folder exists!')

        # self.splits = self.train_mask.shape[1] # 2
        self.splits = 1
        write_log(vars(args), self.log_path)  # write the setting
        print('N_nodes:::::::::::::', args.N)
        print('num_clus:::::::::::::', args.K)
        print('prob_edge_in_clus:::::::::::::', args.p)
        print('prob_flip:::::::::::::', args.eta)
        print('lr::::::::::::', args.lr)

    def SSSNET(self, feat_choice):
        #################################
        # SSSNET
        #################################

        if feat_choice == 'A_reg':
            print('feat_choice == A_reg')
            self.features = self.feat_adj_reg
        elif feat_choice == 'L':
            self.features = self.feat_L
            print('feat_choice == L')
        elif feat_choice == 'given':
            self.features = self.feat_given
            print('feat_choice == given')
        elif feat_choice == 'None':
            self.features = torch.eye(self.A_p_scipy.shape[0]).to(self.args.device)
            print('feat_choice == None')

        res_all_full_latest = np.zeros([self.splits, 1]) # [2,1]
        NMI_all_full_latest = np.zeros([self.splits, 1]) # [2,1]
        balanced_cuts_full =  np.zeros([self.splits, 1, 3])
        balanced_cuts_full_latest = balanced_cuts_full.copy()
        labels_distribution_full = np.zeros([self.splits, 1, self.num_clusters]) # [2,1,2]
        labels_distribution_full_latest = labels_distribution_full.copy()
        args = self.args
        labels = self.label.view(-1) # [[1000]]-->[1000]



        for split in range(self.splits):
            setup_seed(args.set_seed)
            if self.args.sssnet_unsup:
                print('SSSNET_unsup', SSSNET)
                graphmodel = SSSNET(nfeat=self.features.shape[1],
                                    hidden=self.args.hidden,
                                    nclass=self.num_clusters,
                                    dropout=self.args.dropout,
                                    hop=self.args.hop,
                                    div_alpha=self.args.div_alpha,
                                    directed=self.args.directed).to(self.args.device)
            else:
                print('SSSNET_nsvgd', SSSNET_nsvgd)
                graphmodel = SSSNET_nsvgd(nfeat=self.features.shape[1],
                                    hidden=self.args.hidden,
                                    nclass=self.num_clusters,
                                    dropout=self.args.dropout,
                                    hop=self.args.hop,
                                    div_alpha=self.args.div_alpha,
                                    directed=self.args.directed).to(self.args.device)


            model = graphmodel

            # print(i for i in model.parameters())
            opt = optim.Adam(model.parameters(), lr=self.args.lr,weight_decay=self.args.weight_decay)

            # kmeans = KMeans(n_clusters=5, n_init=20)
            log_str_full = ''

            # loss_func_pbnc_train = Prob_Balanced_Normalized_Loss(A_p=self.A_p_scipy, A_n=self.A_n_scipy) #
            loss_func_pbnc_train = Prob_Balanced_Normalized_Loss(A_p=self.A_p_power, A_n=self.A_n_power)
            loss_func_pbnc_train_imp = imp_Prob_Balanced_Normalized_Loss(A_p=self.A_p_power, A_n=self.A_n_power)
            sss_n_p = []
            epoch_all = []
            perf = []
            max_acc, max_nmi, max_ari, max_f1 = 0, 0, 0, 0
            for epoch in range(args.epochs): # 300
                start_time = time.time()
                epoch_all.append(epoch)

                model.train()
                z4, z_p, z_n, logits, pred_label, prob = model(self.A_p, self.A_n, self.features, self.A_pt, self.A_nt)






                if epoch == 0:
                    log_str_full += '\n' + f'svgd1+pbnc+res-n_cuda memory allocated, {torch.cuda.memory_allocated()/1024/1024}, reserved, {torch.cuda.memory_reserved()/1024/1024}'

                loss_pbnc = loss_func_pbnc_train(prob=prob)# prob[train_index] --> prob

                train_loss = args.pbnc_loss * loss_pbnc
                # train_loss = args.pbnc_loss * loss_pbnc + args.recon_loss_ratio * res_loss# 1 * loss_pbnc + 0 * supervised_loss


                opt.zero_grad()
                train_loss.backward()
                opt.step()

                acc, nmi, ari, f1 = eva(self.label.view(-1).to('cpu').numpy(), pred_label.view(-1).to('cpu').numpy(), epoch=epoch)
                # perf.append(acc)
                if acc > max_acc:
                    max_acc = acc
                    max_nmi = nmi
                    max_ari = ari
                    max_f1 = f1
                print('Epoch: %.0f, Train loss:, %.6f, loss_pbnc:, %.6f,  acc: ,%.3f, nmi: ,%.3f, ari: ,%.3f, f1: ,%.3f' % (epoch, train_loss.detach().item(), loss_pbnc,  acc, nmi, ari, f1,)) # supervised_loss:, %.6f,  supervised_loss, train_ARI: ,%.3f, train_NMI: ,%.3f, train_ARI, train_NMI,  , ac_train ,%.3f, nm_train ,%.3f, ar_train ,%.3f, f_train ,%.3f, ac_train, nm_train, ar_train, f_train,
            print('acc: ,%.3f, nmi: ,%.3f, ari: ,%.3f, f1: ,%.3f' % (max_acc, max_nmi, max_ari, max_f1))
            all_ARI_latest = adjusted_rand_score(pred_label.view(-1).to('cpu'), self.label.view(-1).to('cpu'))
            all_NMI_latest = normalized_mutual_info_score(pred_label.view(-1).to('cpu'), self.label.view(-1).to('cpu'))
            brc, bnc, unhappy_ratio, labels_distribution = get_cut_and_distribution(pred_label.view(-1).to('cpu'), self.num_clusters, self.A_p_scipy, self.A_n_scipy)
            balanced_cuts_full_latest[split, 0] = [brc, bnc, unhappy_ratio]
            labels_distribution_full_latest[split, 0] = labels_distribution


            res_all_full_latest[split] = all_ARI_latest
            NMI_all_full_latest[split] = all_NMI_latest
            results = (res_all_full_latest)
            NMIs = (NMI_all_full_latest)
            cuts = (balanced_cuts_full_latest)
            labels_distributions = (labels_distribution_full_latest)



        return results, NMIs, cuts, labels_distributions


    def gen_results_spectral(self):

        num_clusters = self.num_clusters  # 2
        balanced_cuts_full = np.zeros([self.splits, len(compare_names) - num_gnn, 3])  # [2,9,3]
        labels_distribution_full = np.zeros([self.splits, len(compare_names) - num_gnn, self.num_clusters])  # [2,9,2]
        for split in range(self.splits):
            res_all = []
            acc_all = []
            nm_all = []
            ar_all = []
            f_all = []
            labels_cpu = self.label.view(-1).to('cpu')

            # now append results for comparison methods
            for i, pred in enumerate([self.c.spectral_cluster_adjacency_reg(k=num_clusters, normalisation='none'),
                                      self.c.spectral_cluster_sns(k=num_clusters),
                                      self.c.spectral_cluster_dns(k=num_clusters),
                                      self.c.spectral_cluster_laplacian(k=num_clusters, normalisation='none'),
                                      self.c.spectral_cluster_laplacian(k=num_clusters, normalisation='sym'),
                                      self.c.spectral_cluster_bnc(k=num_clusters, normalisation='sym', labels_cpu=labels_cpu),
                                      self.c.spectral_cluster_bnc(k=num_clusters, normalisation='none', labels_cpu=labels_cpu),
                                      self.c.SPONGE(k=num_clusters, labels_cpu=labels_cpu), self.c.SPONGE_sym(k=num_clusters, labels_cpu=labels_cpu)]):
                brc, bnc, unhappy_ratio, labels_distribution = get_cut_and_distribution(pred, self.num_clusters, self.A_p_scipy, self.A_n_scipy)
                balanced_cuts_full[split, i] = [brc, bnc, unhappy_ratio]
                labels_distribution_full[split, i] = labels_distribution
                res_all.append(adjusted_rand_score(pred, labels_cpu))

                ac_spe, nm_spe, ar_spe, f_spe = eva(labels_cpu.numpy(), pred)

                acc_all.append(round(ac_spe, 4))
                nm_all.append(round(nm_spe, 4))
                ar_all.append(round(ar_spe, 4))
                f_all.append(round(f_spe, 4))


            print('All data ACC for methods to compare:{}'.format(acc_all))
            print('All data NM for methods to compare:{}'.format(nm_all))
            print('All data AR for methods to compare:{}'.format(ar_all))
            print('All data F for methods to compare:{}'.format(f_all))

        return balanced_cuts_full, labels_distribution_full  # NMI_all_full, res_all_full,






trainer = SSSNET_Trainer(args, random_seed=args.set_seed)
if 'spectral' in args.all_methods:
    print('spectral methods')
    trainer.gen_results_spectral()

if 'SSSNET' in args.all_methods:
    for feat_choice in args.feature_options:
        results, NMIs, cuts, labels_distributions = trainer.SSSNET(feat_choice)  # A_reg






