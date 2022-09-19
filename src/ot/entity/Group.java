package ot.entity;

import java.math.BigInteger;
import java.util.Random;

public class Group {
	public BigInteger p;
	public BigInteger q;
	public BigInteger g;
	
	
	public BigInteger minLimit = new BigInteger("1000");
	
	public BigInteger genRandomZq() {
		
		Random rand = new Random();
		
		BigInteger r = new BigInteger(q.bitLength(), rand);
		r = r.mod(q); //防止r太大
		
		if(r.compareTo(minLimit) == -1)
			r = r.add(minLimit);
		
		return r;
	}
	
	public BigInteger genRandomG() {
		
		BigInteger r = this.genRandomZq();
		BigInteger h = g.modPow(r, p);
		
		return h;
	}
}
