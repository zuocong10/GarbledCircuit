package util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class AES {
	
	public static byte[] encrypt(byte[] key, byte[] msg) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(key);
            kgen.init(128, sr);
            SecretKey sk = kgen.generateKey();
            byte[] ef = sk.getEncoded();
            SecretKeySpec keySpec = new SecretKeySpec(ef, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] bt = cipher.doFinal(msg);
            return bt;
        } catch (BadPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }
	
	public static byte[] decrypt(byte[] key, byte[] value) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(key);
            kgen.init(128, sr);
            SecretKey sk = kgen.generateKey();
            byte[] ef = sk.getEncoded();
            SecretKeySpec keySpec = new SecretKeySpec(ef, "AES");
            Cipher cipher = Cipher.getInstance("AES");
        	cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return cipher.doFinal(value);
        } catch (BadPaddingException e) {
            return null;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static void main(String[] args){

	    byte[] key = {1,2,3};
    	byte[] value = {1};

    	byte[] cipher = AES.encrypt(value, key);

    	byte[] plain = AES.decrypt(cipher, key);
    	System.out.println(plain[0]);
    }
}
