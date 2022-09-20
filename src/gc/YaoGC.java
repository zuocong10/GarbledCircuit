package gc;

import java.util.Arrays;
import java.util.Random;

import gc.entity.Gate;
import gc.entity.LabelAndR;
import util.AES;
import util.WRObject;

public class YaoGC {
	private int labelLength = 16; //The length of the label (bytes)
	
	public byte[] randomBytes(int n, Random random) {
		byte[] arr = new byte[n];
		random.nextBytes(arr);
		
		return arr;
	}
	
	public byte Operation(byte a, byte b, String gateType) {
		
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
	
	public void AliceGarbledTableGen(String gateType){
		byte[][] garbledTable = new byte[4][];
		
		Gate gate = new Gate();
		
		//Initialize the gate (Generate random labels)
		
		Random rnd = new Random();
		
		for(int i=0; i<gate.w.length; i++) {
			byte r = Math.random() > 0.5 ? (byte)0 : (byte)1; //Point and Permute	
			for(byte j=0; j<gate.w[i].lar.length; j++) {
				gate.w[i].lar[j].label = this.randomBytes(labelLength, rnd);
				gate.w[i].lar[j].r = (byte) (r^j);
			}
		}
		
		WRObject.writeObjectToFile(gateType + ".bin", gate);
		
		for(byte i=0; i<gate.w[0].lar.length; i++) {
			for(byte j=0; j<gate.w[1].lar.length; j++) {
					
				garbledTable[gate.w[0].lar[i].r*2+gate.w[1].lar[j].r] = AES.encrypt(gate.w[0].lar[i].label, AES.encrypt(gate.w[1].lar[j].label,
						WRObject.writeObjectToByteArray(gate.w[2].lar[this.Operation(i, j, gateType)])));
			}
		}
		
		WRObject.writeObjectToFile("garbledTable.bin", garbledTable);
	}
	
	public byte[] AliceAndBobOT(byte[] label0, byte[] label1, byte b) {
		byte[] a = {};
		
		return a;
	}
	
	public byte[] BobEva(byte[][] garbledTable, LabelAndR alice, LabelAndR bob) {
		return AES.decrypt(bob.label, AES.decrypt(alice.label, garbledTable[alice.r*2+bob.r]));
	}
	
	public static void main(String[] args) {
		YaoGC gc = new YaoGC();
		
		String gateType = "XOR";
		
		gc.AliceGarbledTableGen(gateType);
		
		byte[][] garbledTable = (byte[][])WRObject.readObjectFromFile("garbledTable.bin");
		
		Gate gate = (Gate) WRObject.readObjectFromFile(gateType + ".bin");
		
		LabelAndR alice = gate.w[0].lar[1];
		LabelAndR bob = gate.w[1].lar[0];
		
		LabelAndR output = (LabelAndR) WRObject.readObjectFromByteArray(gc.BobEva(garbledTable, alice, bob));
		
		System.out.println(WRObject.writeObjectToByteArray(gate.w[2].lar[0]).length);
		System.out.println(WRObject.writeObjectToByteArray(gate.w[2].lar[1]).length);
		
		if(Arrays.equals(output.label, gate.w[2].lar[0].label))
			System.out.println(0);
		else if(Arrays.equals(output.label, gate.w[2].lar[1].label))
			System.out.println(1);
	}
}
