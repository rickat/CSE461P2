import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class runwoco {

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
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		if (args.length > 1) {
			System.err.println("too many args");
			return;
		}
		// int port_num = Integer.parseInt(args[0]);
		InetSocketAddress port_num = new InetSocketAddress(22233);
		ServerSocketChannel scs = ServerSocketChannel.open();
		Selector selector = Selector.open();

		try {
			System.out.println("yyyy");
			scs.socket().bind(port_num);
			System.out.println("xxxx");
			scs.configureBlocking(false);
			// scs.register(selector, SelectionKey.OP_ACCEPT); 
		} catch (IOException e) {
			System.out.println("Could not listen on port " + port_num);
			System.exit(-1);
		}
		while(true){
			SocketChannel client = scs.accept();
			// System.out.println("aaa");
			if (client != null) {
				System.out.println(client.isConnected());
				new Thread(new Client_handler(client, scs, selector)).start();
			}
			// System.out.println("bbb");
		}
	}

	static class Client_handler implements Runnable {

		// public ServerSocket serverSocket;
		public SocketChannel clientSocket;
		public ServerSocketChannel scs;
		public Selector sel;

		public Client_handler(SocketChannel socket, ServerSocketChannel serverSocket, Selector sel) {

			this.sel = sel;
			clientSocket = socket;
			try {
				clientSocket.configureBlocking(false);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				clientSocket.register(sel, SelectionKey.OP_READ /*| SelectionKey.OP_WRITE*/);
			} catch (ClosedChannelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// serverSocket = serverSocket;
			scs = serverSocket;
		}

		@Override
		public void run() {
			ByteBuffer sb;
			try {
				sb = client_to_proxy_to_server();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// get request from the client and then
		// get client's request to the server and receive the server's respond
		// then get the server's respond
		public ByteBuffer client_to_proxy_to_server() throws IOException {
			StringBuffer sb = new StringBuffer();
			StringBuffer clientData = new StringBuffer();
			while (true) {
				ByteBuffer bb = ByteBuffer.allocate(1);
				clientSocket.read(bb);
				byte b = bb.get(0);
				sb.append((char)b);
				if (b == '\n') {
					String s = sb.toString();
					System.out.println(s);
					clientData.append(s);
					if (s.equals("\r\n")) {
						break;
					} else {
						sb = new StringBuffer();
					}
				}
			}
			String clientString = clientData.toString();
			System.out.println(clientString);
			System.out.println("Apple");
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
			System.out.println(name);
			name = name.trim();
			// end get host name

			// find the version of http
			// get the line with the command GET

			// int change_version = clientString_h.indexOf("get");
			int change_version = 0;
			int end_version = get_end_line_index(clientString_h, change_version);
			String request_line = clientString_h.substring(change_version, end_version);
			request_line = request_line.trim();




			int https = request_line.indexOf("https");
			int http = request_line.indexOf("http");
			int http_version = request_line.indexOf("http/1.1");
			port_start = request_line.lastIndexOf(":");
			if(port_start != -1) {
				// if start with https
				if (https != -1) {
					// the ":" is not the one after https
					if (port_start != https + 5) {
						String port = name.substring(port_start + 1, http_version).trim();
						port_num = Integer.parseInt(port);
					}
				} else if(http != -1) {
					if (port_start != http + 4) {
						String port = name.substring(port_start + 1, http_version).trim();
						port_num = Integer.parseInt(port);
					}
				} else { //ip address instead of host name
					String port = name.substring(port_start + 1, http_version).trim();
					port_num = Integer.parseInt(port);
				}
			}
			// get time
			DateFormat dateFormat = new SimpleDateFormat("dd MMM HH:mm:ss");
			Calendar cal = Calendar.getInstance();

			// if start with http
			// check if it's https. if it's https, default port_num is 443 unless
			// the user specified the port number already

			if (https != -1 && port_num == 80) {
				port_num = 443;
			}
			//print request line
			InetAddress address = InetAddress.getByName(name); 
			System.out.println(dateFormat.format(cal.getTime()) + " - Proxy listening on " + address.getHostAddress() + ":" + port_num);
			String request_line_2 = request_line.substring(change_version, end_version - 8);
			System.out.println(dateFormat.format(cal.getTime()) + " - >>>" + request_line_2.trim());
			// change http version number
			int version_num = clientString_h.indexOf("http/1.1");
			assert(version_num != -1);
			clientString = clientString.substring(0, version_num + 7) + "0" + clientString.substring(version_num + 8);
			// end change version number

			// change keep alive
			int status_start = clientString_h.indexOf("keep-alive");
			clientString = clientString.substring(0, status_start) + "close" + clientString.substring(status_start + 10);
			// end change keep alive
			
			System.out.println("\n" + clientString);
			System.out.println(name);
			System.out.println(host_name);
			System.out.println(port_num);

			// NOTE: Host Name: name
			//	 	 Port     : port_num
			// 		 Header   : clientString
			//		 Payload  : remainData
			//
			// To Do: 1. Establish connection with the host
			//		  2. If succeeded, send a 200 back to client, otherwise send 502
			//		  3. Send clientString to host via a SocketChannel (DNS of name and port at port_num)
			//		  4. Receive host's response via SocktChannel

			// string -> bytebuffer
			ByteBuffer sendData = ByteBuffer.allocate(clientString.length());
			for(int i = 0; i < clientString.length();i++){
				char cur_char = clientString.charAt(i);
				sendData.put((byte) cur_char);
			}

			// UNSOLVED PART STARTS FROM HERE!!!!!!
			//send the data
			// @SuppressWarnings("resource")
			SocketChannel scc = SocketChannel.open();
			// Socket proxy_to_server = new Socket(name, port_num);
			scc.configureBlocking(false);
			scc.connect(new InetSocketAddress(name, port_num));
			System.out.println(scc.isConnected());
			while(!scc.isConnected()){
				if (!scc.isConnectionPending()) {
					scc.connect(new InetSocketAddress(name, port_num));
				} else {
					if (scc.finishConnect()) {
						break;
					}
				}

			}
			System.out.println(scc.isConnected());
			System.out.println(scc == null);
			System.out.println(sel == null);
			assert(sel != null);
			scc.register(sel, /*SelectionKey.OP_WRITE | */SelectionKey.OP_READ);
			// scc.register(sel, SelectionKey.OP_READ);
			// OutputStream out = proxy_to_server.getOutputStream(); 
			// DataOutputStream dos = new DataOutputStream(out);
			// dos.write(sendData.array(), 0, clientString.length());
			
			String return_message;
			// If the request is connect
			// send back 200 OK or 502 Bad Gateway based on whether or not we can establish a connection with the host
			if(request_line.indexOf("connect") == 0) {
				if(scc.isConnected()) {
					System.out.println("connect");
					return_message = new String("HTTP/1.0 200 OK\r\n\r\n");

				} else {
					return_message = new String("HTTP/1.0 502 Bad Gateway\r\n\r\n"); 
				}

				System.out.println("out");
				// OutputStream out_to_client = clientSocket.getOutputStream(); 
				// DataOutputStream dos_to_client = new DataOutputStream(out_to_client);
				ByteBuffer send_data_client = ByteBuffer.allocate(return_message.length());
				for(int i = 0;i<return_message.length();i++){
					char temp = return_message.charAt(i);
					send_data_client.put((byte) temp);
				}

				// dos_to_client.write(send_data_client.array(), 0, return_message.length());
				clientSocket.write(send_data_client);
			}
			System.out.println("here!!!!");
			// read any remaining data and directly send to the server
			ByteBuffer buffer = ByteBuffer.allocate(1024 * 5);
			ByteBuffer bb2 = ByteBuffer.allocate(1024 * 5);
			while (true) {
				int readyChannels = sel.select();
				System.out.println("here!!");
				boolean end = false;
				if(readyChannels == 0) {
//					if (!scc.isConnected() && !clientSocket.isConnected()) {
//						break;
//					}
					continue;
				}
				System.out.println("here!!!");
				Set<SelectionKey> selectedKeys = sel.selectedKeys();
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
				while(keyIterator.hasNext()) {
					
					SelectionKey key = keyIterator.next();
					if (key.isReadable()) {
						SocketChannel sc = (SocketChannel) key.channel();
						SocketChannel oc = scc;
						if (sc.equals(scc)) {
							oc = clientSocket;
						}
						int readlen;
						while ((readlen = sc.read(bb2)) > 0) {
							System.out.println("aaaa" + readlen);
							bb2.flip();
							try{
								oc.write(bb2);
							} catch (IOException e) {
								end = true;
								break;
							}
							System.out.println(new String(bb2.array()));
							bb2.compact();
						}
					}
					keyIterator.remove();
					if (end) {
						break;
					}
				}
				if (end) {
					break;
				}
			}
			System.out.println("here!!!!!!!!!!!!!!!!");
//			int con_len_pos = clientString_h.indexOf("content-length");
//			if (con_len_pos != -1) {
//				int end_con = get_end_line_index(clientString_h, con_len_pos);
//				String s = clientString_h.substring(con_len_pos + 16, end_con).trim();
//				long content_len = Long.parseLong(s);
//				while (content_len > 0) {
//					int readlen;
//					while ((readlen = clientSocket.read(bb2)) > -1 && scc.isOpen() && scc.isConnected() && !scc.socket().isInputShutdown()) {
//						System.out.println("aaaa" + readlen);
//						bb2.flip();
//						scc.write(bb2);
//						bb2.clear();
//						System.out.println(new String(bb2.array()));
//						content_len -= readlen;
//					}
//				}
//			}
						
//			int con_len_pos_2 = clientString_h.indexOf("content-length");
//			if (con_len_pos_2 != -1) {
//				int end_con = get_end_line_index(clientString_h, con_len_pos);
//				String s = clientString_h.substring(con_len_pos + 16, end_con).trim();
//				long content_len = Long.parseLong(s);
//				// starts to get message from the server
//				int readlen;
//				while ((readlen = scc.read(buffer)) > -1) {
//					// dos_to_client.write(buffer, 0, readlen);
//					String s2 = new String(buffer.array());
//					System.out.println(s2);
//					clientSocket.write(buffer);
//					content_len -= readlen;
//				}
//			}
//			
			
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




