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
					new Client_handler(client, serverSocket)
						).start(); 
		  	}
	}
	
	static class Client_handler implements Runnable {
		
		public ServerSocket serverSocket;
		public Socket clientSocket;
		
		public Client_handler(Socket socket, ServerSocket serverSocket) {
			clientSocket = socket;
			serverSocket = serverSocket;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			//generate_secret();
			StringBuilder sb;
			try {
				sb = client_to_proxy_to_server();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// get request from the client and then
		// get client's request to the server and receive the server's respond
		// then get the server's respond
		public StringBuilder client_to_proxy_to_server() throws IOException {
			int read = -1;
			byte[] buffer = new byte[5*1024]; // a read buffer of 5KiB
			byte[] readData;
			StringBuilder clientData = new StringBuilder();
			String readDataText;
			boolean first = true;
			String first_line = null;
			while ((read = clientSocket.getInputStream().read(buffer)) > -1) {
			    readData = new byte[read];
			    System.arraycopy(buffer, 0, readData, 0, read);
			    readDataText = new String(readData,"US-ASCII"); // assumption that client sends data UTF-8 encoded
			    // System.out.println("message part recieved:" + redDataText);
			    // CR 13; LF 10 in ASCII
			    if (first) {
			    	int idx = readDataText.indexOf("\r\n\r\n");
			    	// change 1.1 to 1.0
			    	readDataText = readDataText.substring(0, idx - 1) + "0" + readDataText.substring(idx);
			    	// get the first line
			    	first_line = readDataText.substring(0, idx);
			    	System.out.println(first_line);
			    	first = false;
			    }
			    clientData.append(readDataText);
			}
			// find the destination
			int dns_start = first_line.indexOf("http");
			int dns_end = first_line.indexOf(" ", dns_start);
			String dest = first_line.substring(dns_start, dns_end);
			InetAddress IPAddress = InetAddress.getByName(dest);
			
			return null;
		}
		
		// get data from the server and send that to client
		public void proxy_to_client(StringBuilder sb) throws IOException {
			
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
