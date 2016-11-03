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
		String test_string_h = test_string.toLowerCase();
		int start_host = test_string_h.indexOf("host");
		int end_host = get_end_line_index(test_string_h, start_host);
		System.out.println(test_string_h.substring(start_host, end_host));
		int change_version = test_string_h.indexOf("get");
		System.out.println(change_version);
		int end_version = get_end_line_index(test_string_h, change_version);
		
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
