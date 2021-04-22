package com.pignier.instagramdm.Login;

import com.pignier.instagramdm.Network.BackGroundTask;

import android.widget.ProgressBar;
import android.util.Log;
import android.app.Activity;

import org.json.JSONObject;
import java.util.Map;
import java.util.List;

import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

//https://stackoverflow.com/a/65108879

public class LoginManager{
	String TAG = "INSTAGRAMDM";
	String LOCALTAG = "LoginManager : ";
	

	Activity activity;
	String sessionId;
	String csrf_token;
	LMInterface lmInterface;
	
	public LoginManager(Activity activity,LMInterface lmInterface){
		super();
		this.activity = activity;
		this.lmInterface = lmInterface;
	}

	public void connect(String username_input,String password_input){
		new BackGroundTask(activity) {
			boolean connectionSucceed = false;
			@Override
			public void doInBackground() {
				String publickey = null;
				int key_id = 0;
				int app_id = 0;
				csrf_token = null;
				try {
					//Get crypto data
					URL instagram_url = new URL(String.format("https://www.instagram.com/data/shared_data/"));
					HttpURLConnection con = (HttpURLConnection) instagram_url.openConnection();
					con.setRequestMethod("GET");
					String in = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
					JSONObject json = new JSONObject(in);
					csrf_token = json.getJSONObject("config").getString("csrf_token");
					Log.d(TAG,LOCALTAG+"csrf_token : "+csrf_token);
					
					publickey = json.getJSONObject("encryption").getString("public_key");
					key_id = Integer.parseInt(json.getJSONObject("encryption").getString("key_id"));
					app_id = Integer.parseInt(json.getJSONObject("encryption").getString("version"));
					con.disconnect();
					
					
					URL instagram_url_POST = new URL("https://www.instagram.com/accounts/login/ajax/");
					HttpURLConnection conn = (HttpURLConnection) instagram_url_POST.openConnection();
					conn.setRequestMethod("POST");
					conn.setDoOutput(true);
					conn.setRequestProperty("Host", "www.instagram.com");
					conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; rv:78.0) Gecko/20100101 Firefox/78.0");
					conn.setRequestProperty("Accept", "*/*");
					conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
					conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
					conn.setRequestProperty("X-CSRFToken", csrf_token);
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
 					String cookie = "mid=YHmFegALAAFN2N29eUE9mOvCYoeO; ig_did=0AB48E1A-ABBD-4BF0-B93B-60AFA188020F; rur=FRC; shbid=528; shbts=1618576779.6327314; csrftoken="+csrf_token;
					conn.setRequestProperty("Cookie", cookie);
					conn.setRequestProperty("Sec-GPC", "1");
					conn.setRequestProperty("TE", "Trailers");


					String usernameURL = URLEncoder.encode(username_input, StandardCharsets.UTF_8.name());
					String enc_password = CryptoUtil.encrypt_password(password_input,publickey,key_id,app_id);
					String enc_passwordURL = URLEncoder.encode(enc_password, StandardCharsets.UTF_8.name());
					String payload = "username="+usernameURL+"&enc_password="+enc_passwordURL+"&queryParams=%7B%7D&optIntoOneTap=false";

					//Log.d(TAG,LOCALTAG+"payload : "+payload);
					
					byte[] bytesPayload = payload.getBytes( StandardCharsets.UTF_8);
					DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
					wr.write(bytesPayload);
					
					String responsecookie = conn.getHeaderField("set-cookie"); 
					
					if (responsecookie != null){
						connectionSucceed = responsecookie.contains("sessionid");
						sessionId = responsecookie.split(";")[0].split("sessionid=")[1];
					}
				} catch (Exception e) {
					Log.d(TAG,LOCALTAG+Log.getStackTraceString(e));
				}
			}
					
			@Override
			public void onPostExecute(){
				if (connectionSucceed){
					lmInterface.OnConnected(sessionId,csrf_token);
				}else{
					lmInterface.OnFailed();
				}
				
			}
		}.execute();
	}
	public interface LMInterface {
		void OnConnected(String ig_session_ID,String ig_csrf_token);
		void OnFailed();

	}
}
