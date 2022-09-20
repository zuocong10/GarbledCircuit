package ot;

import java.io.IOException;
import java.math.BigInteger;
import java.net.UnknownHostException;

import communication.Client;
import ot.entity.CEE;

public class OTBob {
	
	public byte[] BobCom(Client client, byte b) {
		
			try {
				BigInteger c = (BigInteger) client.oin.readObject();
				BigInteger x = OT.R_x();
				BigInteger h0 = OT.R_h0(c, x, b);
				client.oout.writeObject(h0);
				client.oout.flush();
				
				CEE cee = (CEE) client.oin.readObject();
				return OT.R_m(x, b, cee);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		return null;
	}
	
	public static void main(String[] args) {
		OTBob bob = new OTBob();

		try {
			Client client = new Client();
			byte b = 1;
			
			byte[] mb = bob.BobCom(client, b);
			
			for(int i=0; i<mb.length; i++) {
				System.out.print(mb[i] + " ");
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
