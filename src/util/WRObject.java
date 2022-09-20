package util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class WRObject {
    public static void writeObjectToFile(String filename, Object obj)
    {
        ObjectOutputStream out;
        try {
            out = new ObjectOutputStream(new FileOutputStream(filename));
            out.writeObject(obj);
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println("write object failed");
            e.printStackTrace();
        }
    }
    
    public static Object readObjectFromFile(String filename)
    {
        Object temp=null;
        ObjectInputStream in;
        try {
            in = new ObjectInputStream(new FileInputStream(filename));
            temp= in.readObject();
            in.close();
        } catch (IOException e) {
            System.out.println("read object failed");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return temp;
    }
    
    
    public static byte[] writeObjectToByteArray(Object obj) {
    	try {
    		ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bout);
			out.writeObject(obj);
			out.flush();
			byte[] b = bout.toByteArray();
			out.close();
			bout.close();
			return b;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }
    
    public static Object readObjectFromByteArray(byte[] b) {
    	try {
    		ByteArrayInputStream bin = new ByteArrayInputStream(b);
			ObjectInputStream in = new ObjectInputStream(bin);
			Object obj = in.readObject();
			in.close();
			bin.close();
			return obj;
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }
}
