package gc;

import java.io.IOException;
import java.util.Arrays;

import communication.Server;
import gc.entity.Gate;
import gc.entity.LabelAndR;
import ot.OTAlice;
import util.WRObject;

public class YaoAlice {
	
	public void AliceGarbledGen(String gateType){
		YaoGC.AliceGarbledTableGen(gateType);
	}
	
	public void AliceSend(Server server, byte[][] garbledTable, LabelAndR alice, LabelAndR bob0, LabelAndR bob1) {
		try {
			
			server.oout.writeObject(garbledTable);
			server.oout.flush();
			
			server.oout.writeObject(alice);
			server.oout.flush();
			
			server.oout.writeObject(bob0.r);
			server.oout.flush();
			
			OTAlice.AliceCom(server, bob0.label, bob1.label);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Server server = new Server();
		YaoAlice yaoAlice = new YaoAlice();
		
		String gateType = "XOR";
		yaoAlice.AliceGarbledGen(gateType);
		
		byte[][] garbledTable = (byte[][]) WRObject.readObjectFromFile("garbledTable.bin");
		Gate gate = (Gate) WRObject.readObjectFromFile(gateType + ".bin");
		
		byte alice_b = 0;
		
		yaoAlice.AliceSend(server, garbledTable, gate.w[0].lar[alice_b], gate.w[1].lar[0], gate.w[1].lar[1]);
		
		try {
			LabelAndR lar = (LabelAndR) server.oin.readObject();
			
			if(Arrays.equals(lar.label, gate.w[2].lar[0].label)) {
				server.oout.writeObject(0);
				server.oout.flush();
				System.out.println("The final output is: 0");
			}
			else if(Arrays.equals(lar.label, gate.w[2].lar[1].label)) {
				server.oout.writeObject(1);
				server.oout.flush();
				System.out.println("The final output is: 1");
			}		
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
