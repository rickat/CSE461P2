import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * 
 */

/**
 * @author Yilun Hua (1428927) Shen Wang (1571169)
 *
 */
public class CSE461P2 {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("hello world");
		if (args.length > 1) {
			System.err.println("too many args");;
			return;
		}
		int port_num = Integer.parseInt(args[0]);
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port_num);
		} catch (IOException e) {
			System.out.println("Could not listen on port " + port_num);
			System.exit(-1);
		}
		while(true){
			Socket client = null;
			try {
				client = serverSocket.accept();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			new Thread(
					new Client_handler(serverSocket, client)
						).start(); 
		  	}
	}
	
	static class Client_handler implements Runnable {
		
		public ServerSocket serverSocket;
		public Socket socket;
		
		public Client_handler(ServerSocket serverSocket, Socket socket) {
			serverSocket = serverSocket;
			socket = socket;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			//generate_secret();
			
		}

		// get request from the client
		public void client_to_proxy() throws IOException {
			
		}
		
		// get client's request to the server
		public void proxy_to_server() throws IOException {
			
		}
		
		// get data from the server and send that to client
		public void proxy_to_client() throws IOException {
			
		}
		
		// verify header
		public boolean verify_header(ByteBuffer head_buf) {
			return true;
		}

		// generate http header
		public void generate_header() {
		}
		
		// may need to pad
		public int padding_bytes(int length) {
			if (length % 4 == 0) {
				return 0;
			} else {
				return 4 - length % 4;
			}
		}

	}

}
