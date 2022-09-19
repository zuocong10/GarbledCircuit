package gc;

import java.util.Random;

import gc.entity.Gate;

public class YaoGC {
	private int labelLength = 16; //The length of the label (bytes)
	
	public byte[] randomBytes(int n, Random random) {
		byte[] arr = new byte[n];
		random.nextBytes(arr);
		
		return arr;
	}
	
	public byte[][] AliceGarble(String gateType){
		byte[][] garbledTable = new byte[4][];
		
		Gate gate = new Gate();
		
		//Initialize the gate (Generate random labels)
		
		Random rnd = new Random();
		for(int i=0; i<2; i++) {
			gate.a[i] = this.randomBytes(labelLength, rnd);
			gate.b[i] = this.randomBytes(labelLength, rnd);
			gate.c[i] = this.randomBytes(labelLength, rnd);
		}
		
		
		
		return garbledTable;
	}
	
	public static void main(String[] args) {
		YaoGC gc = new YaoGC();
		gc.AliceGarble("AND");
	}
}
