package com.pignier.instagramdm.Utils;

import com.pignier.instagramdm.R;
import com.pignier.instagramdm.Model.*;

import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;  
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;

import android.database.Cursor;

import android.content.res.Resources;

import java.net.URL;

import java.util.ArrayList;

import java.io.InputStream;

import java.io.ByteArrayOutputStream;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;



import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;






public class Functions{
	String TAG = "INSTAGRAMDM";
	String LOCALTAG = "Functions : ";



	/**
		Get one Thread from JSON from web
		@param res ressources needed to group's picture
		@param json thread in json format from web
		@return ThreadModel
	*/
	public ThreadModel getThreadFromJSON(Resources res, JSONObject json){
		ThreadModel thread = new ThreadModel();
		try {
			thread.setTitle(json.getString("thread_title"));
			thread.setInstaID(json.getString("thread_v2_id"));

			Bitmap bitmap;
			if (json.getBoolean("is_group")){
				bitmap = BitmapFactory.decodeResource(res, R.drawable.group);
			}else{
				String imgURL = json.getJSONArray("users").getJSONObject(0).getString("profile_pic_url");
				bitmap = getImageFromURL(imgURL);
			}
			thread.setProfilPic(bitmap);
			
		}catch(Exception e){
			Log.e(TAG, LOCALTAG+Log.getStackTraceString(e));
		}
		return thread;
	}


	/**
		Get one Thread from Cursor from database
		@param cursor actual access to database
		@return ThreadModel without messages. They will be add after the call of this method by DBHelper
	*/
	public ThreadModel getThreadFromCursor(Cursor cursor){
		ThreadModel thread = new ThreadModel();
		thread.setTitle(cursor.getString(cursor.getColumnIndex(DatabaseHelper.THREAD_TITLE)));
		byte[] blob = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.THREAD_IMAGE));
		Bitmap bitmap=BitmapFactory.decodeByteArray(blob, 0, blob.length);
		thread.setProfilPic(bitmap);
		thread.setHasNewer(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.THREAD_HAS_NEWER)) == 1);
		thread.setIs_group(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.THREAD_IS_GROUP)) == 1);
		String threadID = cursor.getString(cursor.getColumnIndex(DatabaseHelper.THREAD_INSTA_ID));
		thread.setInstaID(threadID);
		return thread;
	}

	/**
		Get one Message from JSON from Web
		@param json thread in json format from web
		@param instaID the instagram_thread_id of the thread which contains this message 
		@return MessageModel 
	*/
	public MessageModel getMessageFromJson(JSONObject json,String instaID){
		MessageModel message = new MessageModel();
		try{
			String messageType = json.getString("item_type");
			message.setType(messageType);
			message.setThreadID(instaID);
			switch(messageType){
				case "text" : 
					message.setText(json.getString("text"));
					break;
				case "media" :
					JSONArray candidates = json.getJSONObject("media").getJSONObject("image_versions2").getJSONArray("candidates");
					String imgurl = candidates.getJSONObject(candidates.length()-1).getString("url");
					Bitmap bitmap = getImageFromURL(imgurl);
					// Some image are no loger available and throw an error 
					if (bitmap == null){
						// Replace image by text
						message.setType("text");
						message.setText("IMAGE NO LONGER AVAILABLE");

					}else{
						message.setImage(bitmap);
					}
					
					break;
				case "voice_media" :
					String audioUrl = json.getJSONObject("voice_media").getJSONObject("media").getJSONObject("audio").getString("audio_src");
					byte[] audio = getAudioFromURL(audioUrl);
					message.setAudio(audio);
					message.setText("VOCAL");
					break;
				default:
					message.setText("unsupported");
			}
			message.setOwner(json.getString("user_id"));
			message.setItemID(json.getString("item_id"));
		}catch(Exception e){
			Log.e(TAG, LOCALTAG+Log.getStackTraceString(e));
		}
		return message;
	}
	/**
		Get one Message from Cursor from database
		@param cursor actual access to database
		@return MessageModel
	*/
	public MessageModel getMessageFromCursor(Cursor cursor){
		MessageModel message = new MessageModel();
		String type = cursor.getString(cursor.getColumnIndex(DatabaseHelper.MESSAGE_TYPE));
		message.setType(type);
		switch(type){
			case "text":
				message.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.MESSAGE_TEXT)));
				break;
			case "media":
				byte[] blob = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.MESSAGE_IMAGE));
				Bitmap bitmap=BitmapFactory.decodeByteArray(blob, 0, blob.length);
				message.setImage(bitmap);
		}
		message.setAudio(cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.MESSAGE_AUDIO)));
		message.setOwner(cursor.getString(cursor.getColumnIndex(DatabaseHelper.MESSAGE_OWNER)));
		message.setItemID(cursor.getString(cursor.getColumnIndex(DatabaseHelper.MESSAGE_INSTA_ID)));
		message.set_audio_played(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MESSAGE_AUDIO_PLAYED)) == 1);
		return message;
	}
	
	/**
		Get all Message of conversation from Cursor from database
		@param json message array in json format
		@return an array list of all MessageModel
	*/
	public ArrayList<MessageModel> getMessagesArrayFromJson(JSONArray json,String instaID){
		ArrayList<MessageModel> messages = new ArrayList<MessageModel>();
		try{
			for (int i = 0; i < json.length(); i++){
				JSONObject jsonMessage = json.getJSONObject(i);
				messages.add(getMessageFromJson(jsonMessage,instaID));
			}
		}catch(Exception e){
			Log.e(TAG, LOCALTAG+Log.getStackTraceString(e));
		}
		return messages;
	}

	/**
		@param json instagram inbox in json format
		@return all threads in json format
	*/
	public JSONArray getThreadsJSON(JSONObject json){
		JSONArray threads = new JSONArray();
		try{
			threads = json.getJSONObject("inbox").getJSONArray("threads");
		}catch (JSONException e){
			Log.e(TAG, LOCALTAG+Log.getStackTraceString(e));
		}
		return threads;
	}

	/**
		@param imageURL the url of image
		@return image in Bitmap format
	*/
	public Bitmap getImageFromURL(String imageURL){
		URL url = null;
		Bitmap image = null;
		try {
			url = new URL(imageURL);
			image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
		}catch (Exception e){
			Log.e(TAG, LOCALTAG+Log.getStackTraceString(e));
		}
		return image;
	}


	/**
		@param audioURL the url of audio
		@return audio as an array of byte
	*/
	//https://stackoverflow.com/a/1264737
	public byte[] getAudioFromURL(String audioURL){
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		try {
			URL url = new URL(audioURL);
			InputStream in = url.openConnection().getInputStream();
			int bufferSize = 1024;
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = in.read(buffer)) != -1) {
				byteBuffer.write(buffer, 0, len);
			}
		}catch (Exception e){
			Log.e(TAG, LOCALTAG+Log.getStackTraceString(e));
		}
		return byteBuffer.toByteArray();
	}


	/**
		@param squareBitmap the original Bitmap
		@return the new cropped Bitmap
	*/
	//https://stackoverflow.com/a/12089127
	public Bitmap BitmapCircularCroper(Bitmap squareBitmap){
		Bitmap newBitmap = Bitmap.createBitmap(squareBitmap.getWidth(),squareBitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(newBitmap);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, squareBitmap.getWidth(),squareBitmap.getHeight());
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawCircle(squareBitmap.getWidth() / 2,squareBitmap.getHeight() / 2, squareBitmap.getWidth() / 2, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(squareBitmap, rect, rect, paint);
		return newBitmap;
	}
	/**
		@param session give credentials for connection
		@return json as result of connection
	*/
	public JSONObject getDmJSONFromWeb(IGSession session){
		String url = String.format("https://i.instagram.com/api/v1/direct_v2/inbox/?thread_message_limit=%d",10);
		return 	getJSONFromUrl(session,url);
	}
	/**
		@param session give credentials for connection
		@return json as result of connection
	*/
	public JSONObject getRequestedJSONFromWeb(IGSession session){
		String url = "https://i.instagram.com/api/v1/direct_v2/pending_inbox/";
		return getJSONFromUrl(session,url);	
	}

	/**
		@param session give credentials for connection
		@param url of instagram web page to grab json

		@return json as result of connection
	*/

	// http://www.java2s.com/Tutorials/Java/Network_How_to/URL/Get_JSON_from_URL.htm
	public JSONObject getJSONFromUrl(IGSession session,String url){
		String sessionid = session.getSessionID();
		String accountID = session.getAccountID();
		String csrfToken = session.getCsrfToken();

		JSONObject allJson = null;
		try{
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("X-IG-App-ID", "936619743392459");
			String c1 = "mid=YHmFegALAAFN2N29eUE9mOvCYoeO; ";
			String c2 = "ig_did=0AB48E1A-ABBD-4BF0-B93B-60AFA188020F; ";
			String c3 = "rur=FRC; ";
			String c4 = "shbid=528; ";
			String c5 = "shbts=1618576779.6327314; ";
			String c6 = String.format("csrftoken=%s;",csrfToken);
			String c7 = String.format("ds_user_id=%s; ",accountID);
			String c8 = String.format("sessionid=%s; ",sessionid);
			con.setRequestProperty("Cookie", c1+c2+c3+c4+c5+c6+c7+c8);
			con.connect();
			if (con.getResponseCode() == 200){
				InputStream ins = con.getInputStream();
				InputStreamReader insr = new InputStreamReader(ins);
				String in = new BufferedReader(insr).readLine();
				allJson = new JSONObject(in);
			}
			con.disconnect();
		}catch(Exception e){
			Log.e(TAG, LOCALTAG+Log.getStackTraceString(e));

		}
		return allJson;
	}

	
}