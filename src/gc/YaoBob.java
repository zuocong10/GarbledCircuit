package gc;

import java.io.IOException;
import java.net.UnknownHostException;

import communication.Client;
import gc.entity.LabelAndR;
import ot.OTBob;
import util.WRObject;

public class YaoBob {
	
	public LabelAndR BobEva(Client client, byte b) {
		try {
			byte[][] garbledTable = (byte[][]) client.oin.readObject();
			LabelAndR alice = (LabelAndR) client.oin.readObject();
			byte r = (byte) client.oin.readObject();
			
			byte[] bob_label = OTBob.BobCom(client, b);
			
			return (LabelAndR) WRObject.readObjectFromByteArray(YaoGC.BobEva(garbledTable, alice.label, alice.r, bob_label, (byte)(r^b)));
			
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
		
		byte bob_b = 1;
		LabelAndR lar = yaoBob.BobEva(client, bob_b);
		
		try {
			client.oout.writeObject(lar);
			client.oout.flush();
			
			System.out.println("The final output is: " + (int)client.oin.readObject());
			
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
