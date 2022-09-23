package gc;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import communication.Server;
import gc.entity.Circuit;
import gc.entity.GarbledGate;
import gc.entity.LabelAndR;
import gc.entity.Wire;
import ot.OTAlice;
import util.WRFile;

public class YaoAlice {
	
	public void AliceGarbledGen(String circuitPath){
		YaoGC.AliceGarbledTablesGen(circuitPath);
	}
	
	public void OTAliceSendWire(Server server, Wire wire) {
		try {
			server.oout.writeObject(wire.wire_id);
			server.oout.flush();
			server.oout.writeObject(wire.lar[0].r);	//Point and permute, only send r0 to bob, and since bob can generate r1 from r0 
			server.oout.flush();
			
			OTAlice.AliceCom(server, wire.lar[0].label, wire.lar[1].label);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void AliceSend(Server server, GarbledGate[] ggs, Map<Integer, LabelAndR> alice_inputs, Wire[] bob_wires) {
		try {
			
			server.oout.writeObject(ggs);
			server.oout.flush();
			
			server.oout.writeObject(alice_inputs);
			server.oout.flush();
			
			for(int i=0; i<bob_wires.length; i++)
				OTAliceSendWire(server, bob_wires[i]);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Server server = new Server();
		YaoAlice yaoAlice = new YaoAlice();
		
		yaoAlice.AliceGarbledGen("testcircuit.json");
		
		Circuit cir = JSON.parseObject(WRFile.readAll("testcircuit_ranLabels.json"), Circuit.class);
		GarbledGate[] ggs = JSON.parseObject(new String(WRFile.readAll("testcircuit_garbledGates.json")), new TypeReference<GarbledGate[]>(){});
		
		byte[] alice_bs = {1, 1};
		Map<Integer, LabelAndR> alice_inputs = new HashMap<>();
		
		for(int i=0; i<alice_bs.length; i++) {
			int wire_id = cir.alice_inputs[i];
			alice_inputs.put(wire_id, cir.wires[wire_id].lar[alice_bs[i]]);
		}
		
		Wire[] bob_wires = new Wire[cir.bob_inputs.length];
		for(int i=0; i<cir.bob_inputs.length; i++) {
			bob_wires[i] = cir.wires[cir.bob_inputs[i]];
		}
		
		yaoAlice.AliceSend(server, ggs, alice_inputs, bob_wires);
		
		try {
			LabelAndR lar = (LabelAndR) server.oin.readObject();
			
			if(Arrays.equals(lar.label, cir.wires[cir.final_output].lar[0].label)) {
				server.oout.writeObject(0);
				server.oout.flush();
				System.out.println("The final output is: 0");
			}
			else if(Arrays.equals(lar.label, cir.wires[cir.final_output].lar[1].label)) {
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
