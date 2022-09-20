package gc.entity;

import java.io.Serializable;

public class Wire implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public LabelAndR[] lar = new LabelAndR[2]; // //Each wire has two possible values (either 0 or 1).
	
	public Wire() {
		for(int i=0; i<lar.length; i++)
			lar[i] = new LabelAndR();
	}
}
