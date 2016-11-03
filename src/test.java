/**
 * 
 */

/**
 * @author ylh96
 *
 */
public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String test_string = "GET http://www.my.example.page.com/ HTTP/1.1\r\n";
		test_string += "Host: www.my.example.page.com\n";
		test_string += "User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:26.0) Gecko/20100101 Firefox/26.0\r\n";
		test_string += "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n";
		test_string += "Accept-Language: en-US,en;q=0.5\r\n";
		test_string += "Accept-Encoding: gzip, deflate\r\n";
		test_string += "Connection: keep-alive\r\n\r\n";
		System.out.println(test_string);
		
		// code starts here
		String test_string_h = test_string.toLowerCase();
		int start_host = test_string_h.indexOf("host");
		int end_host = get_end_line_index(test_string_h, start_host);
		String host_name = test_string_h.substring(start_host, end_host);
		System.out.println(host_name);  // testing output
		// get host name
		int start_name = host_name.indexOf(":");
		String name = host_name.substring(start_name + 1);
		name = name.trim();
		System.out.println(name);  // testing output
		// see if port exists
		int port_start = name.indexOf(":");
		// default port num is 80
		int port_num = 80;
		String port = "";
		// the user specified port num
		if (port_start != -1) {
			port = name.substring(port_start + 1).trim();
			port_num = Integer.parseInt(port);
			name = name.substring(0, port_start).trim();
		}
		System.out.println(name);  // testing output
		System.out.println(port);  // testing output
		// end get host name
		int change_version = test_string_h.indexOf("get");
		System.out.println(change_version);  // testing output
		int end_version = get_end_line_index(test_string_h, change_version);
		String get_line = test_string_h.substring(change_version, end_version);
		System.out.println(get_line);  // testing output
		// it's https, default port_num is 443
		if (get_line.indexOf("https") != -1 && port_num == 80) {
			port_num = 443;
		}
		
		// change version number
		int version_num = get_line.indexOf("1.1");
		if (version_num != -1) {
			version_num += change_version;
		}
		test_string = test_string.substring(0, version_num + 2) + "0" + test_string.substring(version_num + 3);
		System.out.println(test_string);  // testing output
		// end change version number
		
		// change keep alive
		int status_start = test_string_h.indexOf("connection");
		int status_end = get_end_line_index(test_string_h, status_start);
		String new_status = "Connection: close";
		test_string = test_string.substring(0, status_start) + new_status + test_string.substring(status_end);
		// end change keep alive
		
		System.out.println(test_string);  // testing output
		System.out.println(port_num);  // testing output
		System.out.println("end");  // testing output
	}
	
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
	
	public static String getFrist(String s) {
		String res = "";
		
		return res;
	}

}
