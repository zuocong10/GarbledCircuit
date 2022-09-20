package gc.entity;

import java.io.Serializable;

public class Gate implements Serializable{
	private static final long serialVersionUID = 1L;
	
	//Here we consider a gate with two inputs (w1,w2) and one output (w3).
	
	public Wire[] w = new Wire[3];
	
	public Gate() {
		for(int i=0; i<w.length; i++) {
			w[i] = new Wire();
		}
		
	}
}
