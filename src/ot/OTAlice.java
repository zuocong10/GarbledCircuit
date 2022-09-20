package ot;

import java.math.BigInteger;

import communication.Server;
import ot.entity.CEE;

public class OTAlice {
	
	public void AliceCom(Server server, byte[] m0, byte[] m1) {
		try {
			BigInteger c = OT.S_c();
			server.oout.writeObject(c);
			server.oout.flush();
			
			BigInteger h0 = (BigInteger) server.oin.readObject();
			
			CEE cee = OT.S_cee(c, h0, m0, m1);
			server.oout.writeObject(cee);
			server.oout.flush();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		OTAlice alice = new OTAlice();
		try {
			Server server = new Server();
			byte[] m0 = new byte[16];
			byte[] m1 = new byte[16];
			
			for(int i=0; i<m0.length; i++) {
				m0[i] = 0;
				m1[i] = (byte) i;
			}
			
			alice.AliceCom(server, m0, m1);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
