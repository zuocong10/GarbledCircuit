package gc.entity;

import java.io.Serializable;

public class Gate implements Serializable{
	private static final long serialVersionUID = 1L;
	
	//Here we consider a gate with two inputs (a,b) and one output (c).
	
	public byte[][] a;
	public byte[][] b;
	public byte[][] c;
	
	public Gate() {
		//Each wire has two possible values (either 0 or 1),
		//and each value is associated with a label (usually 128 bits or 16 bytes)
		a = new byte[2][];
		b = new byte[2][];
		c = new byte[2][];
	}
}
