package gc.entity;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import util.WRFile;

public class Circuit implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public Wire[] wires;
	public Gate[] gates;
	
	public int[] alice_inputs;
	public int[] bob_inputs;
	public int final_output;
	
	public Circuit() {}
	
	public Circuit(int w, int g) {
		wires = new Wire[w];
		for(int i=0; i<w; i++)
			wires[i] = new Wire();
		
		gates = new Gate[g];
		for(int i=0; i<g; i++)
			gates[i] = new Gate();
	}
	
	public static void main(String[] args) {
		
		int w = 7; //number of wires
		int g = 3; //number of gates
		
		Circuit cir = new Circuit(w, g);
		
		int[] ainputs = {0, 2};
		int[] binputs = {1, 3};
		
		int[] g0_inputs = {0, 1};
		int[] g1_inputs = {2, 3};
		int[] g2_inputs = {4, 5};
		
		cir.alice_inputs = ainputs;
		cir.bob_inputs = binputs;
		cir.final_output = 6;
		
		for(int i=0; i<cir.wires.length; i++) {
			cir.wires[i].wire_id = i;
		}
		
		cir.gates[0].gate_id = 0;
		cir.gates[0].input_wire_ids = g0_inputs;
		cir.gates[0].output_wire_id = 4;
		cir.gates[0].type = "AND";
		
		cir.gates[1].gate_id = 1;
		cir.gates[1].input_wire_ids = g1_inputs;
		cir.gates[1].output_wire_id = 5;
		cir.gates[1].type = "XOR";
		
		cir.gates[2].gate_id = 2;
		cir.gates[2].input_wire_ids = g2_inputs;
		cir.gates[2].output_wire_id = 6;
		cir.gates[2].type = "AND";
		
		String text = JSON.toJSONString(cir, SerializerFeature.PrettyFormat);
		WRFile.writeTxt("testcircuit.json", text);
	}
}
