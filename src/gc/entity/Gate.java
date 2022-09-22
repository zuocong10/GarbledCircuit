package gc.entity;

import java.io.Serializable;

public class Gate implements Serializable{
	private static final long serialVersionUID = 1L;
	
	//Here we consider a gate with two inputs and one output.
	
	public String type = ""; //It could be "AND" or "XOR".
	public int gate_id;
	public int[] input_wire_ids = new int[2];
	public int output_wire_id;
}
