package gc.entity;

import java.io.Serializable;

public class LabelAndR implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public byte[] label;
	public byte r; //Point and permute technique: This bit is used to denote which row to be decrypted by Bob (i.e., Evaluator).
}
