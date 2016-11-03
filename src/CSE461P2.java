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
		if (args.length > 1) {
			System.err.println("too many args");
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
			byte[] remainData = null;
			// byte[] payload;
			boolean header_over = false;
			boolean r1 = false;
			boolean r2 = false;
			boolean n1 = false;
			boolean n2 = false;
			StringBuilder clientData = new StringBuilder();
			String readDataText;
			while ((read = clientSocket.getInputStream().read(buffer)) > -1) {
			    // when we haven't done reading the header
			    if (!header_over) {
			    	int get_int = read;
			    	for (int i = 0; i < read; i++) {
			    		// we see a \r maybe the end
				    	if (buffer[i] == 13 && !r1) {
				    		r1 = true;
				    	} else if (buffer[i] == 13 && r1 && n1 && !r2) {
				    		// we see a \r immediately after \r\n, may be the end
				    		r2 = true;
				    	} else if (buffer[i] == 10 && r1 && !n1) {
				    		// we see a \n immediately after \r, may be the end
				    		n1 = true;
				    	} else if (buffer[i] == 13 && r1 && n1 && r2 && !n2) {
				    		// we see a \n immediately after \r\n\r, must be the end
				    		n2 = true;
				    		header_over = true;
				    		// set the header distance
				    		get_int = i + 1;
				    	} else if (r1 && buffer[i] != 10) {
				    		// after a \r is not a \n, not the end
				    		r1 = false;
				    	} else if (r1 && n1 && buffer[i] != 13) {
				    		// after a \r\n is not a \r, not the end
				    		r1 = false;
				    		n1 = false;
				    	} else if (r1 && n1 && r2 && buffer[i] != 10) {
				    		// after a \r\n\r is not a \n, not the end
				    		r1 = false;
				    		n1 = false;
				    		r2 = false;
				    	}
				    }
				    //buffer contains request header
				    readData = new byte[get_int];
				    if (read - get_int > 0) {  // there is leftover for the payload part, header is done
				    	remainData = new byte[read - get_int];
				    }
				    System.arraycopy(buffer, 0, readData, 0, get_int);
				    if (read - get_int > 0) {  // copy the payload to payload part
				    	System.arraycopy(buffer, get_int, remainData, 0, read - get_int);
				    }
				    // put the header into a string
				    readDataText = new String(readData,"US-ASCII"); // assumption that client sends ASCII encoded
				    // CR 13; LF 10 in ASCII
				    clientData.append(readDataText);
			    } else {
			    	// the header is over, put data directly into the payload
			    	byte[] payload_buf = new byte[read];
			    	System.arraycopy(buffer, 0, payload_buf, 0, read);
			    	// combine the payload segments
			    	remainData = twoArrayToOne(remainData, payload_buf);
			    }
			    
			}
			String clientString = clientData.toString();
			
			// find the destination (Can be put into a separate method)
			// a lower case version of the client data, so that it will be case insensitive
			String clientString_h = clientString.toLowerCase();
			// find the host of the destination
			int start_host = clientString_h.indexOf("host");
			int end_host = get_end_line_index(clientString_h, start_host);
			String host_name = clientString_h.substring(start_host, end_host);
			// get host name			
			// get rid of "Host:"
			int start_name = host_name.indexOf(":");
			String name = host_name.substring(start_name + 1);
			// trim out white space
			name = name.trim();
			// see if port exists
			int port_start = name.indexOf(":");
			// default port num is 80
			int port_num = 80;
			String port = "";
			// the user specified port num
			if (port_start != -1) {
				// get the client specified port number instead of using default
				port = name.substring(port_start + 1).trim();
				port_num = Integer.parseInt(port);
				name = name.substring(0, port_start).trim();
			}
			// end get host name
			
			// find the version of http
			// get the line with the command GET
			int change_version = clientString_h.indexOf("get");
			int end_version = get_end_line_index(clientString_h, change_version);
			String get_line = clientString_h.substring(change_version, end_version);
			// check if it's https. if it's https, default port_num is 443 unless
			// the user specified the port number already
			if (get_line.indexOf("https") != -1 && port_num == 80) {
				port_num = 443;
			}
			
			// change version number
			int version_num = get_line.indexOf("1.1");
			if (version_num != -1) {
				version_num += change_version;
			}
			clientString = clientString.substring(0, version_num + 2) + "0" + clientString.substring(version_num + 3);
			// end change version number
			
			// change keep alive
			int status_start = clientString_h.indexOf("connection");
			int status_end = get_end_line_index(clientString_h, status_start);
			String new_status = "Connection: close";
			clientString = clientString.substring(0, status_start) + new_status + clientString.substring(status_end);
			// end change keep alive
			// end finding info about server and changing info
			// NOTE: Host Name: name
			//	 	 Port     : port_num
			// 		 Header   : clientString
			//		 Payload  : remainData
			//
			// To Do: 1. Establish connection with the host
			//		  2. If succeeded, send a 200 back to client, otherwise send 502
			//		  3. Send clientString to host via a SocketChannel (DNS of name and port at port_num)
			//		  4. Receive host's response via SocktChannel
			
			
			
			// send back 200 OK or 502 Bad Gateway based on whether or not we can establish a connection with the host
			
			String okMessage = new String("HTTP/1.0 200 OK\r\n\r\n");
			String notOkMessage = new String("HTTP/1.0 502 Bad Gateway\r\n\r\n"); 
			
			return null;
		}
		
		// get data from the server and send that to client
		public void proxy_to_client(StringBuilder sb) throws IOException {
			
		}
		
		// connect two arrays together
		public static byte[] twoArrayToOne(byte[] first, byte[] second) {
			int flen = first.length;
			int slen = second.length;
			byte[] res = new byte[flen + slen];
			System.arraycopy(first, 0, res, 0, flen);
			System.arraycopy(second, 0, res, flen, slen);
			return res;
		}
		
		// a method that tells the ending of each line in the header
		public static int get_end_line_index(String s, int start) {
			int cand1 = s.indexOf("\r", start);
			System.out.println(cand1);
			int cand2 = s.indexOf("\n", start);
			System.out.println(cand2);
			int end_host = s.indexOf("\r\n", start);
			System.out.println(end_host);
			if (cand1 != end_host || cand2 != end_host +2) {
				end_host = Math.min(cand1, cand2);
			}
			return end_host;
		}
	}

}
