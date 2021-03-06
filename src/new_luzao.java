import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 */

/**
 * @author ylh96
 *
 */
public class new_luzao {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if (args.length > 1) {
			System.err.println("too many args");
			return;
		}
		// int port_num = Integer.parseInt(args[0]);
		InetSocketAddress port_num = new InetSocketAddress(22233);
		ServerSocketChannel scs = ServerSocketChannel.open();
		

		try {
			//System.out.println("yyyy");
			scs.socket().bind(port_num);
		} catch (IOException e) {
			//System.out.println("Could not listen on port " + port_num);
			System.exit(-1);
		}
		while(true){
			SocketChannel client = scs.accept();
			if (client != null) {
				new Thread(new Client_handler(client, scs)).start();
			}
		}
	}
	
	static class Client_handler implements Runnable {
		// public ServerSocket serverSocket;
		public SocketChannel clientSocket;
		public Selector sel;

		public Client_handler(SocketChannel socket, ServerSocketChannel serverSocket) throws IOException {
			this.sel = Selector.open();
			clientSocket = socket;
		}

		@Override
		public void run() {
			try {
				client_to_proxy_to_server();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// get request from the client and then
		// get client's request to the server and receive the server's respond
		// then get the server's respond
		public void client_to_proxy_to_server() throws IOException {
			StringBuffer sb = new StringBuffer();
			StringBuffer clientData = new StringBuffer();
			ArrayList<String> alist = new ArrayList<String>();
			while (true) {
				ByteBuffer bb = ByteBuffer.allocate(1);
				clientSocket.read(bb);
				byte b = bb.get(0);
				sb.append((char)b);
				if (b == '\n') {
					String s = sb.toString();
					clientData.append(s);
					alist.add(s);
					if (s.equals("\r\n")) {
						break;
					} else {
						sb = new StringBuffer();
					}
				}
			}
			String clientString = clientData.toString();
			String clientString_h = clientString.toLowerCase();
			//System.out.println(clientString);
			String connection = null;
			String request = alist.get(0);
			String host = null;
			int conn_index = -1;
			for (int i = 0; i < alist.size(); i++) {
				String s = alist.get(i);
				String sx = s.toLowerCase();
				if (sx.contains("connection")) {
					connection = s;
					conn_index = i;
				} else if (sx.contains("host")) {
					host = s;
				}
			}
			// reqA[0] = request type; reqA[1] = host + port; reqA[2] = http version
			String[] reqA = request.split("\\s+");
			// hostA[0] = HOST:; hostA[1] = host + port
			String[] hostA = host.split("\\s+");
			// connA[0] = CONNECTION:; connA[1] = keep-alive
			String[] connA = connection.split("\\s+");
			// new connection line
			String new_conn = connA[0] + " close";
			// new request line
			String new_vers = reqA[0] + " " + reqA[1] + " " + "HTTP/1.0\r\n";
			alist.set(conn_index, new_conn);
			alist.set(0, new_vers);
			
			int hostport = 80;
			String hostname = null;
			
			// get host name
			// hR[0] = http; hR[1] = hostname; hR[2] = port num
			System.out.println("======================================" + reqA[1]);
			String[] hostname_R = reqA[1].split(":");
			if (hostname_R[0].contains("https")) {
				hostport = 443;
			}
//			if (hostname_R.length > 2) {
//				System.out.println("-------------------------"+hostname_R[2].trim());
//				hostport = Integer.parseInt(hostname_R[2].trim());
//			}
			// hh[0] = hostname; hh[1] = port num
			String[] hostname_H = hostA[1].split(":");
			if (hostname_H.length > 1) {
				hostport = Integer.parseInt(hostname_H[1].trim());
			}
			hostname = hostname_H[0].trim();
			//System.out.println(hostname);
			//System.out.println(hostport);
			for (String s : alist) {
				//System.out.println(s);
			}
			System.out.println("++++++++++++++++++++++++++++++" + hostname);
			System.out.println("------------------------------" + hostport);
			
			
			
			
			
			
			
			


			// If the request is connect
			// send back 200 OK or 502 Bad Gateway based on whether or not we can establish a connection with the host

			if (reqA[0].trim().toLowerCase().equals("connect")) {
				System.out.println("handling connect");
				handleConnect(hostname, hostport);
			} else {
				//System.out.println("handling non connect");
				try {
					handleNonConnect(/*clientString*/alist, hostname, hostport, clientString_h);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// //System.out.println("Connect pos" + cpos);
		}
		
		public void handleNonConnect(/*String clientString*/ArrayList<String> alist, String hostname, int hostport, String clientString_h) throws Exception {

//			String clientString_h = clientString.toLowerCase();
//			// find the host of the destination
//			int start_host = clientString_h.indexOf("host");
//			int end_host = get_end_line_index(clientString_h, start_host);
//			String host_name = clientString_h.substring(start_host, end_host); //ex: host:xxxxxxxxxxx
//			// get host name			
//			// get rid of "Host:"
//			int start_name = host_name.indexOf(":");
//			String name = host_name.substring(start_name + 1); // include white space
//			// trim out white space
//			name = name.trim();
//			// default port num is 80
//			int port_num = 80;
//			// see if port exists
//			int port_start = name.indexOf(":");
//			// if we didn't find ":", then using default port num
//			// otherwise, using specified port num and update name
//			if (port_start != -1) {
//				// get the client specified port number instead of using default
//				String port = name.substring(port_start + 1).trim();
//				port_num = Integer.parseInt(port);
//				name = name.substring(0, port_start).trim(); // update host name
//			}
//			//System.out.println(name);
//			// end get host name
//
//			// find the version of http
//			// get the line with the command GET
//			int change_version = 0;
//			int end_version = get_end_line_index(clientString_h, change_version);
//			String request_line = clientString_h.substring(change_version, end_version);
//			request_line = request_line.trim(); // get the request line
//            int https = request_line.indexOf("https"); // get the position of "https" if any
//			int http = request_line.indexOf("http"); // get position of "http" if any
//			int http_version = request_line.indexOf("http/1.1"); // get position of http version for finding potential port num
//			port_start = request_line.lastIndexOf(":"); // find the last appearance of ":"
//			if(port_start != -1) { // if ":" apears in request line
//				// check with https first
//				if (https != -1) {
//					// the ":" is not the one after https
//					if (port_start != https + 5) {
//						String port = name.substring(port_start + 1, http_version).trim();
//						port_num = Integer.parseInt(port); // update port
//					}
//				} else if(http != -1) { // check with http
//					if (port_start != http + 4) { // ":" is not the one after "http"
//						String port = name.substring(port_start + 1, http_version).trim();
//						port_num = Integer.parseInt(port); // update port
//					}
//				} else { // no appearance of "http" or "https"; IP address instead of host name
//					String port = name.substring(port_start + 1, http_version).trim();
//					port_num = Integer.parseInt(port); // update port
//				}
//			}
//			
			
			int contentLengthIndex = clientString_h.indexOf("content-length");
			int contentLength = 0;
			if (contentLengthIndex != -1) {
				int i = get_end_line_index(clientString_h, contentLengthIndex);
				String trimed = clientString_h.substring(contentLengthIndex, i).split(":")[1].trim();
				contentLength = Integer.parseInt(trimed);
			}
//			
//			if (https != -1 && port_num == 80) {
//				port_num = 443;
//			}
//			//print request line
////			InetAddress address = InetAddress.getByName(name); 
//
//			// change http version number
//			int version_num = clientString_h.indexOf("http/1.1");
//			assert(version_num != -1);
//			clientString = clientString.substring(0, version_num + 7) + "0" + clientString.substring(version_num + 8);
//			// end change version number
//
//			// change keep alive
//			int status_start = clientString_h.indexOf("keep-alive");
//			clientString = clientString.substring(0, status_start) + "close" + clientString.substring(status_start + 10);
			
			
			String clientString = alist.get(0);
			for (int i = 1; i < alist.size(); i++) {
				clientString += alist.get(i);
			}
			Socket ss = new Socket(hostname, hostport);
			ss.getOutputStream().write(clientString.getBytes());
			byte[] buf = new byte[1024];
			if (contentLength > 0) {
				while (contentLength > 0) {
					int byteRead = clientSocket.socket().getInputStream().read(buf);
					ss.getOutputStream().write(buf, 0, byteRead);
					contentLength -= byteRead;
					
				}
			}
			
			InputStream serverInput = ss.getInputStream();
			OutputStream browserOutput = clientSocket.socket().getOutputStream();
			int byteRead = serverInput.read(buf);
			while (byteRead != -1) {
				browserOutput.write(buf, 0, byteRead);
				byteRead = serverInput.read(buf);
			}
		}
		
		public void handleConnect(String name, int port_num) throws IOException{
			String return_message = null;
			ByteBuffer send_data_client = ByteBuffer.allocate(1024);
			SocketChannel scc = SocketChannel.open();
			try {
				scc.connect(new InetSocketAddress(name, port_num));
				return_message = new String("HTTP/1.0 200 OK\r\n\r\n");
				send_data_client.put(return_message.getBytes());
				clientSocket.write(send_data_client);
			} catch (Exception e) {
				return_message = new String("HTTP/1.0 502 Bad Gateway\r\n\r\n");
				send_data_client.put(return_message.getBytes());
				clientSocket.write(send_data_client);
				return;
			}
			
			//System.out.println("return message is:" + return_message);
			scc.configureBlocking(false);
			clientSocket.configureBlocking(false);
			scc.register(sel, SelectionKey.OP_READ);
			clientSocket.register(sel, SelectionKey.OP_READ);
			ByteBuffer bb2 = ByteBuffer.allocate(1024);
			// //System.out.println("ah!");
			boolean isClosed = false;
			while (!isClosed) {
				int readyChannels = sel.select();
				if (readyChannels == 0) continue;
				Set<SelectionKey> selectedKeys = sel.selectedKeys();
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
				while(keyIterator.hasNext()) {
					bb2.clear();
					
					SelectionKey key = keyIterator.next();
					SocketChannel sc = (SocketChannel)key.channel();
					if (sc.equals(clientSocket)) {
						// broswer case
						int readlen = sc.read(bb2);
						if (readlen == -1) {
							isClosed = true;
							break;
						}
						bb2.flip();
						while (bb2.hasRemaining()) {
							try {
								scc.write(bb2);
							} catch(IOException e) {
								isClosed = true;
								break;
							}
						}
						
					} else if (sc.equals(scc)) {
						// server case
						int readlen = sc.read(bb2);
						if (readlen == -1) {
							isClosed = true;
							break;
						}
						bb2.flip();
						while (bb2.hasRemaining()) {
							try {
								clientSocket.write(bb2);
							} catch(IOException e) {
								isClosed = true;
								break;
							}
						}
					}
				}
				selectedKeys.clear();
			}
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
			// //System.out.println(cand1);
			int cand2 = s.indexOf("\n", start);
			// //System.out.println(cand2);
			int end_host = s.indexOf("\r\n", start);
			// //System.out.println(end_host);
			if (cand1 != end_host || cand2 != end_host +2) {
				end_host = Math.min(cand1, cand2);
			}
			return end_host;
		}
	}

}
