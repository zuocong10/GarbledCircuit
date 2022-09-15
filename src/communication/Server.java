package communication;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private static final int server_port = 57571;
	
	private ServerSocket server = null;
	private Socket socket = null;
	public ObjectInputStream oin = null;
	public ObjectOutputStream oout = null;
	
	public Server() throws Exception{
		server = new ServerSocket(server_port);
		System.out.println("等待连接...");
		socket = server.accept();
		System.out.println("ip: " + socket.getInetAddress() + " 已连接");
		
		oin = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
		oout = new ObjectOutputStream(socket.getOutputStream());
	}
	
	/*
	 * 使用线程处理每个客户端传输的文件
	 */
	
//	public void Communicate() throws Exception {
//		
//		while(true) {
//			System.out.println("等待连接...");
//			Socket socket = server.accept();
//			System.out.println("ip: " + socket.getInetAddress() + " 已连接");
//			new Thread(new MessageHandler(socket)).start();
//		}
//	}
//	
//	class MessageHandler implements Runnable{
//		private Socket socket;
//		private ObjectInputStream oin = null;
//		private ObjectOutputStream oout = null;
//		
//		public MessageHandler(Socket socket) {
//			this.socket = socket;
//		}
//		
//		public void run() {
//			try {
//				oin = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
//				oout = new ObjectOutputStream(socket.getOutputStream());
//				
//				BigInteger big = (BigInteger) oin.readObject();
//					
//				System.out.println("客户端大数为:" + big);
//				oout.writeObject(BigInteger.ONE);
//				
//				oout.flush();
//			} catch (IOException | ClassNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
	
	public static void main(String[] args) {
		try {
			Server server = new Server();
			
			server.oout.writeObject(BigInteger.ONE);
			server.oout.flush();
			
			BigInteger big = (BigInteger) server.oin.readObject();
			System.out.println("客户端大数为:" + big);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
