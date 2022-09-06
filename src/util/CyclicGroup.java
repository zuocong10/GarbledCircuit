package util;

import java.math.BigInteger;
import java.util.Random;

/**
 * Generate security parameters p q g of cyclic group G, G is a subgroup of multiplicative group Zp*
 * p is a big prime number, q is a big prime number, and q | (p - 1), g is the generator
 * This method is a implementation of Schnorr Group(https://en.wikipedia.org/wiki/Schnorr_group), you can get a designated p_bit and q_bit parameters
 * For security, p is recommended (1024-3072bits) and q is recommended (160-256bits)
 * Since p is generated from q, so may be p's bit length is not exactly as what you set, try more times!
 * @param p_bits, p's length in bit
 * @param q_bits, q's length in bit
 * @param certainty, the probability of the prime is 1-2^(-certainty)
 */

public class CyclicGroup {
	
	public BigInteger p = null;
	public BigInteger q = null;
	public BigInteger g = null;
	
	public void CycGen(int p_bits, int q_bits, int certainty) {
		//p = kq + 1
		BigInteger k = null;
		BigInteger h = null;
		
		Random r = new Random();
		
		//1. Generate q
		do {
			q = BigInteger.probablePrime(q_bits, r);
		}while(!q.isProbablePrime(certainty));
		
		//2. Generate p
	
		do {
			k = new BigInteger(p_bits-q_bits+1, r);
			p = q.multiply(k).add(BigInteger.ONE);
		}while(!p.isProbablePrime(certainty));
		
		//3. Generate g
		do {
			h = new BigInteger(200, r);
			g = h.modPow(p.subtract(BigInteger.ONE).divide(q), p);
		}while(g.compareTo(BigInteger.ONE) == 0);
	}
	
	public static void main(String[] args) {
		CyclicGroup pg = new CyclicGroup();
		
		int p_bits = 1024, q_bits = 160, certainty = 256;
		
		pg.CycGen(p_bits, q_bits, certainty);
		
		System.out.println("p=" + pg.p + " " + pg.p.bitLength());
		System.out.println("q=" + pg.q + " " + pg.q.bitLength());
		System.out.println("g=" + pg.g + " " + pg.g.bitLength());
		
		System.out.println("g^q mod p = " + pg.g.modPow(pg.q, pg.p));
		
		String str = "p:" + pg.p.toString() + "\n" + "q:" + pg.q.toString() + "\n" + "g:" + pg.g.toString();
		
		WRFile.writeTxt("CyclicGroup.txt", str);
		
	}
}
