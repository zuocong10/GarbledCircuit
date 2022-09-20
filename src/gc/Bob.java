package gc;

import java.io.IOException;
import java.net.UnknownHostException;

import communication.Client;
import gc.entity.LabelAndR;

public class Bob {
	
	public LabelAndR BobEva(byte b) {
		try {
			Client client = new Client();
			
			byte[][] garbledTable = (byte[][]) client.oin.readObject();
			LabelAndR alice = (LabelAndR) client.oin.readObject();
			
			
			
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
	} 
}
