package com.pignier.instagramdm.Model;

import android.graphics.Bitmap;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import android.content.ContentValues;
import com.pignier.instagramdm.Utils.DatabaseHelper;

public class ThreadModel{
	private String title;
	private Bitmap profilPic;
	private boolean has_newer = false;
	private boolean is_group = false;
	private ArrayList<MessageModel> messages = new ArrayList<MessageModel>();
	private String insta_ID;
	private boolean isRequested;

	//Getters
	public String getTitle(){
		return title;
	}
	public Bitmap getProfilPic(){
		return profilPic;
	}
	public ArrayList<MessageModel> getMessages(){
		return messages;
	}
	public String getInstaID(){
		return insta_ID;
	}

	//Setters
	public void setTitle(String title){
		this.title = title;
	}
	public void setProfilPic(Bitmap profilPic){
		this.profilPic = profilPic;
	}
	public void setHasNewer(boolean has_newer){
		this.has_newer = has_newer;
	}
	public void setIs_group(boolean is_group){
		this.is_group = is_group;
	}
	public void setMessages(ArrayList<MessageModel> messages){
		this.messages = messages;
	}
	public void setInstaID(String insta_ID){
		this.insta_ID = insta_ID;
	}

	public void setIsRequested(boolean status){
		this.isRequested = status;
	}
	public String getLastItemID(){
		return messages.get(messages.size() - 1 ).getItemID();
	}
	public ContentValues extract() {
		ContentValues cValues = new ContentValues();
		cValues.put(DatabaseHelper.THREAD_TITLE, title);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		profilPic.compress(Bitmap.CompressFormat.PNG, 100, bos);
		byte[] bArray = bos.toByteArray();
		cValues.put(DatabaseHelper.THREAD_IMAGE, bArray);
		cValues.put(DatabaseHelper.THREAD_HAS_NEWER, (has_newer) ? 1 : 0);
		cValues.put(DatabaseHelper.THREAD_IS_GROUP, (is_group) ? 1 : 0);
		cValues.put(DatabaseHelper.THREAD_INSTA_ID, insta_ID);
		cValues.put(DatabaseHelper.THREAD_IS_REQUESTED, isRequested ? 1 : 0);
		return cValues;
	}
}