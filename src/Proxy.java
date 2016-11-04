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

public class Proxy {

	/**
	 * 
	 */

	/**
	 * @author Yilun Hua (1428927) Shen Wang (1571169)
	 *
	 */

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
				e1.printStackTrace();
			}
			new Thread(new Client_handler(client, serverSocket)).start(); 
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
			StringBuilder sb;
			try {
				sb = client_to_proxy_to_server();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// get request from the client and then
		// get client's request to the server and receive the server's respond
		// then get the server's respond
		public StringBuilder client_to_proxy_to_server() throws IOException {
			byte[] buffer = new byte[1]; // read header one by one until reaching /r/n/r/n
			boolean header_over = false; // header_over = r1&n1&r2&n2
			boolean r1 = false; //check the first "\r" in "\r\n\r\n"
			boolean n1 = false; //check the first "\n" in "\r\n\r\n"
			boolean r2 = false; //check the second "\r" in "\r\n\r\n"
			boolean n2 = false; //check the second "\n" in "\r\n\r\n"
			// StringBuilder to build header
			// reasons why using StringBuilder: thread safe
			StringBuilder clientData = new StringBuilder();
			// read until header_over turn into true, send header ASAP
			// Then, we can read and send payload
			while (!header_over && clientSocket.getInputStream().read(buffer) > -1) {
				// we see a \r maybe the end
				if (buffer[0] == 13 && !r1) {
					r1 = true;
				} else if (buffer[0] == 13 && r1 && n1 && !r2) {
					// we see a \r immediately after \r\n, may be the end
					r2 = true;
				} else if (buffer[0] == 10 && r1 && !n1) {
					// we see a \n immediately after \r, may be the end
					n1 = true;
				} else if (buffer[0] == 13 && r1 && n1 && r2 && !n2) {
					// we see a \n immediately after \r\n\r, must be the end
					n2 = true;
					header_over = true; //now we have all the header!
				} else if (r1 && buffer[0] != 10) {
					// after a \r is not a \n, not the end
					r1 = false;
				} else if (r1 && n1 && buffer[0] != 13) {
					// after a \r\n is not a \r, not the end
					r1 = false;
					n1 = false;
				} else if (r1 && n1 && r2 && buffer[0] != 10) {
					// after a \r\n\r is not a \n, not the end
					r1 = false;
					n1 = false;
					r2 = false;
				}
				// convert cur byte -> cur string
				// append cur string -> string builder
				String curString = new String(buffer, "US-ASCII"); // assumption that client sends ASCII encoded
				clientData.append(curString);
			}
			String clientString = clientData.toString();

			// find the destination (Can be put into a separate method)
			// a lower case version of the client data, so that it will be case insensitive
			String clientString_h = clientString.toLowerCase();
			// find the host of the destination
			int start_host = clientString_h.indexOf("host");
			int end_host = get_end_line_index(clientString_h, start_host);
			String host_name = clientString_h.substring(start_host, end_host); //ex: host:xxxxxxxxxxx
			// get host name			
			// get rid of "Host:"
			int start_name = host_name.indexOf(":");
			String name = host_name.substring(start_name + 1); // include white space
			// trim out white space
			name = name.trim();
			// default port num is 80
			int port_num = 80;
			// see if port exists
			int port_start = name.indexOf(":");
			// if we didn't find ":", then using default port num
			// otherwise, using specified port num and update name
			if (port_start != -1) {
				// get the client specified port number instead of using default
				String port = name.substring(port_start + 1).trim();
				port_num = Integer.parseInt(port);
				name = name.substring(0, port_start).trim();
			}
			// end get host name

			// find the version of http
			// get the line with the command GET
			int change_version = clientString_h.indexOf("get");
			int end_version = get_end_line_index(clientString_h, change_version);
			String request_line = clientString_h.substring(change_version, end_version);
			request_line = request_line.trim();
			int https = request_line.indexOf("https");
			int http = request_line.indexOf("http");
			int http_version = request_line.indexOf("http/1.1");
			port_start = request_line.lastIndexOf(":");
			if(port_start != -1) {
				String port;
				// if start with https
				if (https != -1) {

					// the ":" is not the one after https
					if (port_start != https + 5) {
						port = name.substring(port_start + 1, http_version).trim();
					}
				} else if(http != -1) {
					if (port_start != https + 4) {
						port = name.substring(port_start + 1, http_version).trim();
					}
				} else { //ip address instead of host name
					port = name.substring(port_start + 1, http_version).trim();
				}
				port_num = Integer.parseInt(port);
			}
			
			// if start with http
			// check if it's https. if it's https, default port_num is 443 unless
			// the user specified the port number already

			if (https != -1 && port_num == 80) {
				port_num = 443;
			}

			// change http version number
			int version_num = clientString_h.indexOf("http/1.1");
			assert(version_num != -1);
			clientString = clientString.substring(0, version_num + 7) + "0" + clientString.substring(version_num + 4);
			// end change version number

			// change keep alive
			int status_start = clientString_h.indexOf("keep-alive");
			clientString = clientString.substring(0, status_start) + "close" + clientString.substring(status_start + 10);
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



