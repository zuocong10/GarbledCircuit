package gc;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

import communication.Client;
import gc.entity.GarbledGate;
import gc.entity.LabelAndR;
import ot.OTBob;

public class YaoBob {
	
	public void OTBobReceiveWire(Client client, Map<Integer, LabelAndR> lrs, byte b) {
		try {
			int wire_id = (int) client.oin.readObject();
			byte r = (byte) client.oin.readObject();
			
			byte[] b_label = OTBob.BobCom(client, b);
			
			lrs.put(wire_id, new LabelAndR(b_label, (byte)(r^b)));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Map<Integer, LabelAndR> BobEva(Client client, byte[] bob_bs) {
		try {
			GarbledGate[] ggs = (GarbledGate[]) client.oin.readObject();
			
			@SuppressWarnings("unchecked")
			Map<Integer, LabelAndR> lrs = (Map<Integer, LabelAndR>) client.oin.readObject();
			
			for(int i=0; i<bob_bs.length; i++)
				OTBobReceiveWire(client, lrs, bob_bs[i]);
			
			return YaoGC.BobEva(ggs, lrs);
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	} 
	
	public static void main(String[] args) {
		Client client = new Client();
		YaoBob yaoBob = new YaoBob();
		
		byte[] bob_bs = {1, 1};
		Map<Integer, LabelAndR> lrs = yaoBob.BobEva(client, bob_bs);
		
		LabelAndR lr = lrs.get(lrs.size()-1);
		
		try {
			client.oout.writeObject(lr);
			client.oout.flush();
			
			System.out.println("The final output is: " + (int)client.oin.readObject());
			
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
