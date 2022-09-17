package ot;

import java.io.IOException;
import java.math.BigInteger;
import java.net.UnknownHostException;

import communication.Client;
import entity.CEE;

public class Bob {
	
	public byte[] BobCom(byte b) {
		try {
			Client client = new Client();
			
			BigInteger c = (BigInteger) client.oin.readObject();
			
			BigInteger x = OT.R_x();
			BigInteger h0 = OT.R_h0(c, x, b);
			client.oout.writeObject(h0);
			client.oout.flush();
			
			CEE cee = (CEE) client.oin.readObject();
			return OT.R_m(x, b, cee);
			
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
		Bob bob = new Bob();
		
		byte b = 1;
		
		byte[] mb = bob.BobCom(b);
		
		for(int i=0; i<mb.length; i++) {
			System.out.print(mb[i] + " ");
		}
	}
}
