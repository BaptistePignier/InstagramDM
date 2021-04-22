package com.pignier.instagramdm.Model;

import android.graphics.Bitmap;
import android.content.ContentValues;
import com.pignier.instagramdm.Utils.DatabaseHelper;
import java.io.ByteArrayOutputStream;

public class MessageModel{
	private String type;
	private String owner;
	private String itemID;
	private String text;
	private Bitmap image;
	private byte[] audio;
	private boolean audio_played;
	private String threadID;
	

	public String getType(){
		return type;
	}
	public String getItemID(){
		return itemID;
	}
	public byte[] getAudio(){
		return audio;
	}
	



	public void setType(String type){
		this.type = type;
	}
	public void setOwner(String owner){
		this.owner = owner;
	}
	public void setItemID(String itemID){
		this.itemID = itemID;
	}
	public void setText(String text){
		this.text = text.replace("\"","'");
	}
	public void setImage(Bitmap image){
		this.image = image;
	}
	public void setAudio(byte[] audio){
		this.audio = audio;
	}
	public void set_audio_played(boolean bool){
		this.audio_played = bool;
	}
	public void setThreadID(String id){
		this.threadID = id;
	}
	
	public ContentValues extract(){
		ContentValues cValues = new ContentValues();
		cValues.put(DatabaseHelper.MESSAGE_TYPE, type);
		switch(type){
			case "text" :
				cValues.put(DatabaseHelper.MESSAGE_TEXT, text);
				break;
			case "media" :
				//https://www.worldbestlearningcenter.com/tips/Android-save-image-sqlite-database.htm
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				image.compress(Bitmap.CompressFormat.PNG, 100, bos);
				byte[] bArray = bos.toByteArray();
				cValues.put(DatabaseHelper.MESSAGE_IMAGE, bArray);
				break;  
			case "voice_media":
				cValues.put(DatabaseHelper.MESSAGE_TEXT, text);
				cValues.put(DatabaseHelper.MESSAGE_AUDIO, audio);
				cValues.put(DatabaseHelper.MESSAGE_AUDIO_PLAYED, (audio_played) ? 1 : 0);
				break;
		}
		
		cValues.put(DatabaseHelper.MESSAGE_OWNER, owner);
		cValues.put(DatabaseHelper.MESSAGE_INSTA_ID, itemID);
		cValues.put(DatabaseHelper.MESSAGE_THREAD_ID, threadID);
		
		return cValues;
	}


}