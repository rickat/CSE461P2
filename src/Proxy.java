import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Set;


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
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		if (args.length > 1) {
			System.err.println("too many args");
			return;
		}
		int port_num_enter = Integer.parseInt(args[0]);
		// InetSocketAddress port_num = new InetSocketAddress(22233);
		InetSocketAddress port_num = new InetSocketAddress(port_num_enter);
		ServerSocketChannel scs = ServerSocketChannel.open();


		try {
			System.out.println("yyyy");
			scs.socket().bind(port_num);
			// System.out.println("xxxx");
			//			scs.configureBlocking(false);
			// scs.register(selector, SelectionKey.OP_ACCEPT); 
		} catch (IOException e) {
			// System.out.println("Could not listen on port " + port_num);
			System.exit(-1);
		}
		while(true){
			SocketChannel client = scs.accept();
			if (client != null) {
				new Thread(new Client_handler(client)).start();
			}		}
	}

	static class Client_handler implements Runnable {
		public SocketChannel clientSocket;
		public Selector sel;

		public Client_handler(SocketChannel socket) throws IOException {
			this.sel = Selector.open();
			clientSocket = socket;
		}

		@Override
		public void run() {
			ByteBuffer sb;
			try {
				sb = client_to_proxy_to_server();
			}  catch (UnknownHostException u) {
				u.printStackTrace();
			}catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// get request from the client and then
		// get client's request to the server and receive the server's respond
		// then get the server's respond
		public ByteBuffer client_to_proxy_to_server() throws Exception {
			StringBuffer sb = new StringBuffer();
			StringBuffer clientData = new StringBuffer();
			while (true) {
				ByteBuffer bb = ByteBuffer.allocate(1);
				clientSocket.read(bb);
				byte b = bb.get(0);
				sb.append((char)b);
				if (b == '\n') {
					String s = sb.toString();
					clientData.append(s);
					if (s.equals("\r\n")) { // find header
						break;
					}
					sb = new StringBuffer(); // flush StringBuffer

				}
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
			name = name.trim(); // this is the host name
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
				name = name.substring(0, port_start);
			}
			// System.out.println(name);
			name = name.trim();
			// end get host name

			// int change_version = clientString_h.indexOf("get");
			int change_version = 0;
			int end_version = get_end_line_index(clientString_h, change_version);
			String request_line = clientString_h.substring(change_version, end_version);
			request_line = request_line.trim();

			int https = request_line.indexOf("https://");
			if (https != -1 && port_num == 80) {
				port_num = 443;
			}			
			// get time
			DateFormat dateFormat = new SimpleDateFormat("dd MMM HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			//print request line
			try{
				InetAddress address = InetAddress.getByName(name); 
				System.out.println(dateFormat.format(cal.getTime()) + " - Proxy listening on " + address.getHostAddress() + ":" + port_num);
				int version_num = clientString_h.indexOf("http/1.1");
				assert(version_num != -1);
				String request_line_2 = request_line.substring(change_version, version_num).trim();
				System.out.println(dateFormat.format(cal.getTime()) + " - >>>" + request_line_2);
				// change http version number
				clientString = clientString.substring(0, version_num + 7) + "0" + clientString.substring(version_num + 8);
				// end change version number

				// change keep alive
				int status_start = clientString_h.indexOf("keep-alive");
				clientString = clientString.substring(0, status_start) + "close" + clientString.substring(status_start + 10);
				// end change keep alive

				// NOTE: Host Name: name
				//	 	 Port     : port_num
				// 		 Header   : clientString
				//		 Payload  : remainData
				//
				// To Do: 1. Establish connection with the host
				//		  2. If succeeded, send a 200 back to client, otherwise send 502
				//		  3. Send clientString to host via a SocketChannel (DNS of name and port at port_num)
				//		  4. Receive host's response via SocktChannel

				assert(sel != null);
				// scc.register(sel, SelectionKey.OP_READ);

				// If the request is connect
				// send back 200 OK or 502 Bad Gateway based on whether or not we can establish a connection with the host
				//System.out.println(clientString);
				int cpos = request_line.indexOf("connect");
				if (cpos != -1) {
					//System.out.println("handling connect");
					handleConnect(name, port_num);
				} else {
					//System.out.println("handling non connect");
					try {
						handleNonConnect(clientString,clientString_h, name, port_num);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return null;
			}catch(UnknownHostException u) {
				return null;
			}
		}

		public void handleNonConnect(String clientString, String clientString_h, String name, int port_num) throws IOException {
			Socket ss = new Socket(name, port_num);
			ss.getOutputStream().write(clientString.getBytes());
			int contentLengthIndex = clientString_h.indexOf("content-length");
			int contentLength = 0;
			if (contentLengthIndex != -1) {
				int i = get_end_line_index(clientString_h, contentLengthIndex);
				String trimed_1 = clientString_h.substring(contentLengthIndex, i);
				int length_index = trimed_1.indexOf(":");
				assert(length_index != -1);
				String trimed_2 = trimed_1.substring(length_index+1);
				contentLength = Integer.parseInt(trimed_2.trim());
			}

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
				try {
					browserOutput.write(buf, 0, byteRead);
					byteRead = serverInput.read(buf);
				}catch(IOException i) {
					continue;
				}
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
					try{
						if (sc.equals(clientSocket)) {
							// browser case

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
					}catch(IOException i){
						// abort quietly
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
			//System.arraycopy(first, 0, res, 0, flen);
			//System.arraycopy(second, 0, res, flen, slen);
			return res;
		}

		// a method that tells the ending of each line in the header
		public static int get_end_line_index(String s, int start) {
			int cand1 = s.indexOf("\r", start);
			int cand2 = s.indexOf("\n", start);
			int end_host = s.indexOf("\r\n", start);
			if (cand1 != end_host || cand2 != end_host +2) {
				end_host = Math.min(cand1, cand2);
			}
			return end_host;
		}
	}
}

