package gc.entity;

import java.io.Serializable;

public class GarbledGate implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public int gate_id;
	public int[] input_wire_ids;
	public int output_wire_id;
	public byte[][] garbledTable;
}
