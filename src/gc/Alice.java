package gc;

import communication.Server;
import gc.entity.LabelAndR;
import ot.OTAlice;
import util.WRObject;

public class Alice {
	public YaoGC gc = new YaoGC();
	
	public void AliceGarbledGen(String gateType){
		gc.AliceGarbledTableGen(gateType);
	}
	
	public void AliceSend(byte[][] garbledTable, LabelAndR alice, LabelAndR bob0, LabelAndR bob1) {
		try {
			Server server = new Server();
			
			server.oout.writeObject(garbledTable);
			server.oout.flush();
			
			server.oout.writeObject(alice);
			server.oout.flush();
			
			new OTAlice().AliceCom(server, WRObject.writeObjectToByteArray(bob0), WRObject.);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
