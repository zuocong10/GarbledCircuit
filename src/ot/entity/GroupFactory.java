package ot.entity;

import java.math.BigInteger;

import util.WRFile;

public class GroupFactory {
	public static Group getGroup(String path) {
		Group gp = new Group();
		
		String[] pqg = WRFile.readTxt(path, 3);
		
		gp.p = new BigInteger(pqg[0].substring(pqg[0].indexOf(":") + 1));
		gp.q = new BigInteger(pqg[1].substring(pqg[1].indexOf(":") + 1));
		gp.g = new BigInteger(pqg[2].substring(pqg[2].indexOf(":") + 1));
		
		return gp;
	}
}
