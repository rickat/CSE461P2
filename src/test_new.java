import java.io.IOException;

/**
 * 
 */

/**
 * @author ylh96
 *
 */
public class test_new {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String test_string = "GET  https://www.my.example.page.com/  :  43332 HTTP/1.1 \r\n";
		test_string += "Host:  www.my.example.page.com :  43332\n";
		test_string += "User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:26.0) Gecko/20100101 Firefox/26.0\r\n";
		test_string += "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n";
		test_string += "Accept-Language: en-US,en;q=0.5\r\n";
		test_string += "Accept-Encoding: gzip, deflate\r\n";
		test_string += "Connection: keep-alive   \r\n\r\n";
		System.out.println(test_string);
		
		String clientString = test_string;

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
			// if start with https
			if (https != -1) {
                // the ":" is not the one after https
				if (port_start != https + 5) {
					String port = name.substring(port_start + 1, http_version).trim();
					port_num = Integer.parseInt(port);
				}
			} else if(http != -1) {
				if (port_start != https + 4) {
					String port = name.substring(port_start + 1, http_version).trim();
					port_num = Integer.parseInt(port);
				}
			} else { //ip address instead of host name
				String port = name.substring(port_start + 1, http_version).trim();
				port_num = Integer.parseInt(port);
			}
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
		clientString = clientString.substring(0, version_num + 7) + "0" + clientString.substring(version_num + 8);
		// end change version number

		// change keep alive
		int status_start = clientString_h.indexOf("keep-alive");
		clientString = clientString.substring(0, status_start) + "close" + clientString.substring(status_start + 10);
		// end change keep alive
		// end finding info about server and changing info
		System.out.println(clientString);
		System.out.println(name);
		System.out.println(host_name);
		System.out.println(port_num);
	}
	
	// get data from the server and send that to client
	public void proxy_to_client(StringBuilder sb) throws IOException {
		
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
