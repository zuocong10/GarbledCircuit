package gc;

import java.math.BigInteger;

import entity.CEE;
import entity.Group;
import entity.GroupFactory;
import util.Hash;

// OT protocol based on Nigel Smart’s "Cryptography Made Simple"

public class OblivousTransfer {
	Group gp = GroupFactory.getGroup("CyclicGroup.txt");
	
	public BigInteger S_c() {
		return gp.genRandomG();
	}
	
	public BigInteger R_x() {
		return gp.genRandomZq();
	}
	
	public BigInteger R_h0(BigInteger c, BigInteger x, byte b) {
		
		BigInteger h_b = gp.g.modPow(x, gp.p);
		BigInteger h_b1 = h_b.modInverse(gp.p).multiply(c).mod(gp.p);
		
		if(b == 0)
			return h_b;
		
		return h_b1;
	}
	
	public CEE S_cee(BigInteger c, BigInteger h0, byte[] m0, byte[] m1) {
		CEE cee = new CEE();
		
		BigInteger h1 = h0.modInverse(gp.p).multiply(c).mod(gp.p);
		BigInteger k = gp.genRandomZq();
		
		cee.c1 = gp.g.modPow(k, gp.p);
		cee.e0 = Hash.XOR(m0, Hash.Sha256(h0.modPow(k, gp.p).toByteArray()));
		cee.e1 = Hash.XOR(m1, Hash.Sha256(h1.modPow(k, gp.p).toByteArray()));
		
		return cee;
	}
	
	public byte[] R_m(BigInteger x, byte b, CEE cee) {
		if(b == 0) {
			byte[] m0 = Hash.XOR(cee.e0, Hash.Sha256(cee.c1.modPow(x, gp.p).toByteArray()));
			return m0;
		}else if(b == 1){
			byte[] m1 = Hash.XOR(cee.e1, Hash.Sha256(cee.c1.modPow(x, gp.p).toByteArray()));
			return m1;
		}
		
		System.out.println("b is not valid! It can be either 0 or 1!");
		return null;
	}
	
	public static void main(String[] args) {
		OblivousTransfer ot = new OblivousTransfer();
		
		byte[] m0 = new byte[32];  //256 bits
		byte[] m1 = new byte[32];
		byte b = 0;
		
		for(int i=0; i<m0.length; i++) {
			m0[i] = 0;
			m1[i] = 1;
		}
		
		BigInteger c = ot.S_c();
		
		BigInteger x = ot.R_x();
		BigInteger h0 = ot.R_h0(c, x, b);
		
		CEE cee = ot.S_cee(c, h0, m0, m1);
		
		byte[] m_b = ot.R_m(x, b, cee);
		
		for(int i=0; i<m_b.length; i++)
			System.out.print(m_b[i]);
	}
}
