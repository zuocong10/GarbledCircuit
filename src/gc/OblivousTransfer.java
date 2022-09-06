package gc;

import entity.Group;
import entity.GroupFactory;

// OT protocol based on Nigel Smart’s "Cryptography Made Simple"

public class OblivousTransfer {
	Group gp = GroupFactory.getGroup("CyclicGroup.txt");
	
	String hello = "";
	
	public static void main(String[] args) {
		OblivousTransfer ot = new OblivousTransfer();
		
		
		System.out.println("p=" + ot.gp.p + " " + ot.gp.p.bitLength());
		System.out.println("q=" + ot.gp.q + " " + ot.gp.q.bitLength());
		System.out.println("g=" + ot.gp.g + " " + ot.gp.g.bitLength());
	}
}
