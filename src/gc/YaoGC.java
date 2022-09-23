package gc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

import gc.entity.Circuit;
import gc.entity.GarbledGate;
import gc.entity.LabelAndR;
import util.AES;
import util.WRFile;
import util.WRObject;

public class YaoGC {
	private static int labelLength = 16; //The length of the label (bytes)
	
	public static byte[] randomBytes(int n, Random random) {
		byte[] arr = new byte[n];
		random.nextBytes(arr);
		
		return arr;
	}
	
	public static byte Operation(byte a, byte b, String gateType) {
		
		byte r=-1;
		
		switch(gateType) {
			case "AND":
				r = (byte)(a&b);
				break;
			case "XOR":
				r = (byte)(a^b);
				break;
		}
		
		return r;
	}
	
	public static void AliceGarbledTablesGen(String circuitPath){
		Circuit cir = JSON.parseObject(WRFile.readAll(circuitPath), Circuit.class);
	
		Random rnd = new Random(); //This is used to initialize the gate (Generate random labels)
		
		for(int i=0; i<cir.wires.length; i++) {
			byte r = Math.random() > 0.5 ? (byte)0 : (byte)1; //Point and Permute	
			for(byte j=0; j<cir.wires[i].lar.length; j++) {
				cir.wires[i].lar[j].label = randomBytes(labelLength, rnd);
				cir.wires[i].lar[j].r = (byte) (r^j);
			}
		}
		
		String text = JSON.toJSONString(cir, SerializerFeature.PrettyFormat);
		WRFile.writeTxt(circuitPath.split(".json")[0] + "_ranLabels.json", text);
		
		GarbledGate[] gg = new GarbledGate[cir.gates.length];
		for(int i=0; i<cir.gates.length; i++) {
			gg[i] = new GarbledGate();
			gg[i].gate_id = cir.gates[i].gate_id;
			gg[i].input_wire_ids = cir.gates[i].input_wire_ids;
			gg[i].output_wire_id = cir.gates[i].output_wire_id;
			
			byte[][] garbledTable = new byte[4][];
			
			LabelAndR[] lr_input0 = cir.wires[cir.gates[i].input_wire_ids[0]].lar; //The position of wires array is equal to the wire id.
			LabelAndR[] lr_input1 = cir.wires[cir.gates[i].input_wire_ids[1]].lar;
			LabelAndR[] lr_output = cir.wires[cir.gates[i].output_wire_id].lar;
			
			for(byte j=0; j<lr_input0.length; j++) {
				for(byte k=0; k<lr_input1.length; k++) {
						
					garbledTable[lr_input0[j].r*2+lr_input1[k].r] = AES.encrypt(lr_input0[j].label, AES.encrypt(lr_input1[k].label,
							WRObject.writeObjectToByteArray(lr_output[Operation(j, k, cir.gates[i].type)])));
				}
			}
			
			gg[i].garbledTable = garbledTable;
		}
		
		String text_gg = JSON.toJSONString(gg, SerializerFeature.PrettyFormat);
		WRFile.writeTxt(circuitPath.split(".json")[0] + "_garbledGates.json", text_gg);
	}
	
	public static Map<Integer, LabelAndR> BobEva(GarbledGate[] garbledGates, Map<Integer, LabelAndR> lars) {
		
		for(int i=0; i<garbledGates.length; i++) {
			LabelAndR input0_lr = lars.get(garbledGates[i].input_wire_ids[0]);
			LabelAndR input1_lr = lars.get(garbledGates[i].input_wire_ids[1]);
			
			LabelAndR output_lr = (LabelAndR)WRObject.readObjectFromByteArray(AES.decrypt(input1_lr.label, AES.decrypt(input0_lr.label, garbledGates[i].garbledTable[input0_lr.r*2+input1_lr.r])));
			lars.put(garbledGates[i].output_wire_id, output_lr);
		}
		
		return lars;
	}
	
	public static void main(String[] args) {
		
		YaoGC.AliceGarbledTablesGen("testcircuit.json");
		
		Circuit cir = JSON.parseObject(WRFile.readAll("testcircuit_ranLabels.json"), Circuit.class);
		GarbledGate[] ggs = JSON.parseObject(new String(WRFile.readAll("testcircuit_garbledGates.json")), new TypeReference<GarbledGate[]>(){});
		
		Map<Integer, LabelAndR> eva_wires = new HashMap<>();
		
		int[] alice_inputs = cir.alice_inputs;
		int[] bob_inputs = cir.bob_inputs;
		
		for(int i=0; i<alice_inputs.length; i++) {
			eva_wires.put(alice_inputs[i], cir.wires[alice_inputs[i]].lar[1^i]);
			eva_wires.put(bob_inputs[i], cir.wires[bob_inputs[i]].lar[i]);
		}
		
		Map<Integer, LabelAndR> lars = YaoGC.BobEva(ggs, eva_wires);
		
		byte[] label_final = lars.get(cir.final_output).label;
		
		if(Arrays.equals(label_final, cir.wires[cir.final_output].lar[0].label))
			System.out.println(0);
		else if(Arrays.equals(label_final, cir.wires[cir.final_output].lar[1].label))
			System.out.println(1);
		
		YaoGC.AliceGarbledTablesGen("AND.json");
		Circuit cir2 = JSON.parseObject(WRFile.readAll("AND_ranLabels.json"), Circuit.class);
		GarbledGate[] ggs2 = JSON.parseObject(new String(WRFile.readAll("AND_garbledGates.json")), new TypeReference<GarbledGate[]>() {});
		
		Map<Integer, LabelAndR> eva_wires2 = new HashMap<>();
		
		int[] alice_inputs2 = cir2.alice_inputs;
		int[] bob_inputs2 = cir2.bob_inputs;
		
		for(int i=0; i<alice_inputs2.length; i++) {
			eva_wires2.put(alice_inputs2[i], cir2.wires[alice_inputs2[i]].lar[1]);
			eva_wires2.put(bob_inputs2[i], cir2.wires[bob_inputs2[i]].lar[0]);
		}
		
		Map<Integer, LabelAndR> lars2 = YaoGC.BobEva(ggs2, eva_wires2);
		byte[] label_final2 = lars2.get(cir2.final_output).label;
		
		if(Arrays.equals(label_final2, cir2.wires[cir2.final_output].lar[0].label))
			System.out.println(0);
		else if(Arrays.equals(label_final2, cir2.wires[cir2.final_output].lar[1].label))
			System.out.println(1);
	}
}
