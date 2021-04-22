import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets ;
import java.net.URLEncoder;
import java.lang.StringBuilder;

import java.lang.StringBuffer ;
import java.io.InputStream;

import java.util.Scanner;



public class Main {
	public static void main(String[] args){
		//login();
		get_pending();
		get_message();
    }
		
	
	public static void get_presence(){
		try{
			String threads_id = "";
			String item_id = "";
			URL instagram_url_get = new URL(String.format("https://i.instagram.com/api/v1/direct_v2/threads/%s/items/%s/seen/",threads_id,item_id));
			HttpURLConnection con = (HttpURLConnection) instagram_url_get.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Accept", "*/*");
			con.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setRequestProperty("Connection", "keep-alive");
			con.setRequestProperty("Cookie", "ig_did=D7DE46A1-26D8-4FF3-B350-EEE87BBCF767; csrftoken=FUs9AYbOnvZAvTEOihco5WWOqgScTpAc; mid=X-ObsAALAAGQPCpCkAi8OTzD7ufu; rur=FTW; ds_user_id=44513817883; sessionid=44513817883%3Aj6D0cml2j2BkIh%3A11; urlgen=\"{\\\"2a01:e0a:354:d590:d059:1283:9faa:cd5f\\\": 12322}:1ks9vj:x-cKaaOorMlhc1HMn8xGmIhP9MM\"");
			con.setRequestProperty("DNT", "1");
			con.setRequestProperty("Host", "i.instagram.com");
			con.setRequestProperty("Origin", "https://www.instagram.com");
			con.setRequestProperty("Referer", "https://www.instagram.com/");
			con.setRequestProperty("TE", "Trailers");
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; rv:78.0) Gecko/20100101 Firefox/78.0");
			con.setRequestProperty("X-IG-App-ID", "936619743392459");
			con.setRequestProperty("X-IG-WWW-Claim", "hmac.AR1Ki_at200r-cCXxo9LPof1KVD6DTRMjgq2VdRJA-WTLsWp");
			con.connect();
			String in = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
			System.out.println(in);
			con.disconnect();
		}catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void login(){
		try{


			URL instagram_url_POST = new URL("https://www.instagram.com/accounts/login/ajax/");
			HttpURLConnection conn = (HttpURLConnection) instagram_url_POST.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Host", "www.instagram.com");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; rv:78.0) Gecko/20100101 Firefox/78.0");
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
			conn.setRequestProperty("X-CSRFToken", "wRK8udxHWcAmt5DddRa6hEqVHqUaNcUY");
			conn.setRequestProperty("X-Instagram-AJAX", "43faacaaef00");
			conn.setRequestProperty("X-IG-App-ID", "936619743392459");
			conn.setRequestProperty("X-IG-WWW-Claim", "hmac.AR1f6mgq5ufcLbyQrD9Rsq5u4AvcnO4ZP97mGwDqxd7FX6I_");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
			conn.setRequestProperty("Content-Length", "300");
			conn.setRequestProperty("Origin", "https://www.instagram.com");
			conn.setRequestProperty("DNT", "1");
			conn.setRequestProperty("Connection", "keep-alive");
			conn.setRequestProperty("Referer", "https://www.instagram.com/accounts/login/");
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Accept", "*/*");
 			String cookie = "mid=YHmFegALAAFN2N29eUE9mOvCYoeO; ig_did=0AB48E1A-ABBD-4BF0-B93B-60AFA188020F; rur=FRC; shbid=528; shbts=1618576779.6327314; csrftoken=wRK8udxHWcAmt5DddRa6hEqVHqUaNcUY";
			conn.setRequestProperty("Cookie", cookie);
			conn.setRequestProperty("Sec-GPC", "1");
			conn.setRequestProperty("TE", "Trailers");
	
	
			String payload = "username=pignier.baptiste%40tutanota.com&enc_password=%23PWD_INSTAGRAM_BROWSER%3A10%3A1618593090%3AAa9QAOHFFRMmQJuYferyruISF0XYycih%2FnXBGRj8DrwQvt1S5uhE44EF%2BsV3WMmAXeJrKHSNnImESqYB7drRibUOiJ7lkDNSxd8d6on90312sVWhqgw4zzfhtGwnp7rhK1uN5h3fZyhcuSCueubHotsJ&queryParams=%7B%7D&optIntoOneTap=false";
			                  
			byte[] postData = payload.getBytes( StandardCharsets.UTF_8);
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.write( postData );
			for (Map.Entry<String, List<String>> entries : conn.getHeaderFields().entrySet()) {
    			String values = "";
    			for (String value : entries.getValue()) {
        			values += value + ",";
    			}
    			System.out.println(entries.getKey() + "- "+values);
			}
			String responsecookie = conn.getHeaderField("set-cookie"); 
    		System.out.println("responsecookie : "+responsecookie);
		}catch(Exception e){
			System.out.println(e);
		}


	}

	public static void get_message(){
		try {
			URL instagram_url_get = new URL(String.format("https://i.instagram.com/api/v1/direct_v2/inbox/?thread_message_limit=%d",10));
			HttpURLConnection con = (HttpURLConnection) instagram_url_get.openConnection();
			con.setRequestMethod("GET");
			
			con.setRequestProperty("X-IG-App-ID", "936619743392459");

			
			String c1 = "mid=YHmFegALAAFN2N29eUE9mOvCYoeO; ";

			String c2 = "ig_did=0AB48E1A-ABBD-4BF0-B93B-60AFA188020F; ";

			String c3 = "rur=FRC; ";

			String c4 = "shbid=528; ";
			String c5 = "shbts=1618576779.6327314; ";
			String c6 = "csrftoken=2pOJWojWzby3v381t9J6lCLa25l8ESXQ;    ";

			
			String c7 = "ds_user_id=4925020518; ";
			String c8 = "sessionid=4925020518%3Aqvhn0RASdcAKnc%3A20; ";
			

			
			con.setRequestProperty("Cookie", c1+c2+c3+c4+c5+c6+c7+c8);

			
			con.connect();
			String in = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
			System.out.println(in);
			con.disconnect();
		} catch (Exception e) {
			System.out.println(e);
		}
		
		
	}
	public static void get_pending(){
		try {
			URL instagram_url_get = new URL("https://i.instagram.com/api/v1/direct_v2/pending_inbox/");
			HttpURLConnection con = (HttpURLConnection) instagram_url_get.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Host", "i.instagram.com");
			con.setRequestProperty("X-IG-App-ID", "936619743392459");
			con.setRequestProperty("X-IG-WWW-Claim", "hmac.AR1f6mgq5ufcLbyQrD9Rsq5u4AvcnO4ZP97mGwDqxd7FX-Pl");
			String c1 = "ig_did=0AB48E1A-ABBD-4BF0-B93B-60AFA188020F; mid=YHmFegALAAFN2N29eUE9mOvCYoeO; rur=FRC;";

			String c2 = "urlgen=\"{\"2a01:e0a:354:d590:8464:c1a3:84a6:f2e3\": 12322}:1l3jdj:Weh4sszkUCHWzEUcC-t0p94DDYw\";"; 
			String c3 = "csrftoken=9ahYM3kpQYZa3ZlJ7rtqOvISpATyNDK9; ds_user_id=4925020518; sessionid=4925020518:JdyZE7x8dZ0joF:2; shbid=528; shbts=1618576779.6327314";

			//String c1 = "ig_did=DB3956DF-5131-43EE-81B7-B06DA7B9B1FB; mid=X9cxBQALAAHkWvgiD0iWrIS3V5lq; rur=FRC; shbid=528; ";
			//String c2 = "shbts=1607938349.690737; urlgen=\"{\"2a01:e0a:354:d590:6c0a:62a8:3928:e15e\": 12322}:1kokLp:eYDvmS7tOwiJBQswjmwDXy6ON8o\"; ";
			//String c3 = "csrftoken=ZYQgAA2Elm1G1e9KQ2KW1C1nKSY0DUm9; ds_user_id=44513817883; sessionid=44513817883%3A53CaXi08Nfyjzx%3A23";
			con.setRequestProperty("Cookie", c1+c2+c3);
			con.setRequestProperty("TE", "Trailers");
			con.connect();
			String in = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
			System.out.println(in);
			con.disconnect();
		} catch (Exception e) {
			System.out.println(e);
		}
		
		
	}
}