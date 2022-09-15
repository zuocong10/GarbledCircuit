package communication;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	private final String server_IP = "localhost";
	private final int server_port = 57571;
	
	public ObjectInputStream oin = null;
	public ObjectOutputStream oout = null;
	
	private Socket socket = null;
	
	public Client() throws UnknownHostException, IOException {
		socket = new Socket(server_IP, server_port);
		System.out.println("成功连接到服务器 " + server_IP);
		oout = new ObjectOutputStream(socket.getOutputStream());
		oin = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
	}
	
	public static void main(String[] args) {
		try {
			Client client = new Client();
			
			client.oout.writeObject(BigInteger.TWO);
			client.oout.flush();
			
			BigInteger big = (BigInteger) client.oin.readObject();
			System.out.println("服务器断大数为:" + big);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}
