package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
	public static byte[] Sha256(byte[] message) {
		   try {
	            MessageDigest md = MessageDigest.getInstance("SHA-256"); // The output of SHA-256 is 32 bytes (256 bits).
	            md.update(message);
	            return md.digest();
	        } catch (NoSuchAlgorithmException e) {
	            e.printStackTrace();
	            return null;
	        }
	}
	
	public static byte[] XOR(byte[] m1, byte[] m2) {
		
		if(m1.length == m2.length && m1.length > 0) {
			
			byte[] result = new byte[m1.length];
			
			for(int i=0; i<m1.length; i++) {
				result[i] = (byte) (m1[i] ^ m2[i]);
			}
			
			return result;
			
		}else {
			System.out.println("You need to input two byte arrays with same length!");
			return null;
		}
	}
}
