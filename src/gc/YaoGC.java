package gc;

import java.util.Arrays;
import java.util.Random;

import gc.entity.Gate;
import util.AES;
import util.WRObject;

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
		
		WRObject.writeObjectToFile("AND_Gate.bin", gate);
		
		for(int i=0; i<2; i++) {
			for(int j=0; j<2; j++) {
				garbledTable[i*2+j] = AES.encrypt(gate.a[i], AES.encrypt(gate.b[i], gate.c[i&j]));
				
				System.out.println("i=" + i + ",j=" + j);
			}
		}
		
		return garbledTable;
	}
	
	public byte[] AliceAndBobOT(byte[] b0, byte[] b1, byte b) {
		byte[] a = {};
		
		return a;
	}
	
	public byte[] BobEva(byte[][] garbledTable, Gate gate, int aliceLabel, int bobLabel) {
		byte[] output =  AES.decrypt(gate.b[bobLabel], AES.decrypt(gate.a[aliceLabel], garbledTable[aliceLabel*2+bobLabel]));
		
		for(int i=0; i<output.length; i++) {
			System.out.print(output[i] + " ");
		}
		
		System.out.println();
		
		return output;
	}
	
	public static void main(String[] args) {
		YaoGC gc = new YaoGC();
		byte[][] garbledTable = gc.AliceGarble("AND");
		
		Gate gate = (Gate) WRObject.readObjectFromFile("AND_Gate.bin");
		
		for(int i=0; i<2; i++) {
			System.out.println("c" + i + " ");
			for(int j=0; j<gate.c[i].length; j++) {
				System.out.print(gate.c[i][j] + " ");
			}
			System.out.println();
		}
		
		byte[] output = gc.BobEva(garbledTable, gate, 1, 0);
		
		if(Arrays.equals(output, gate.c[0]))
			System.out.println(0);
		else if(Arrays.equals(output, gate.c[1]))
			System.out.println(1);
	}
}
