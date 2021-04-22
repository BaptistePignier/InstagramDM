package com.pignier.instagramdm.Model;

import com.pignier.instagramdm.Utils.DatabaseHelper;
import android.content.ContentValues;

public class IGSession{
	private String accountID;
	private String sessionID;
	private String csrfToken;

	public IGSession(String sessionID,String csrfToken){
		this.accountID = sessionID.split("%")[0];
		this.sessionID = sessionID;
		this.csrfToken = csrfToken;
	}
	public IGSession(String[] credentials){
		this(credentials[0],credentials[1]);
	}
	public String getAccountID(){
		return accountID;
	}
	public String getSessionID(){
		return sessionID;
	}
	public String getCsrfToken(){
		return csrfToken;
	}
	public ContentValues extract(){
		ContentValues cValues = new ContentValues();
		cValues.put(DatabaseHelper.IGSESSION_SESSION_ID, sessionID);
		cValues.put(DatabaseHelper.IGSESSION_CSRF_TOKEN, csrfToken);
		return cValues;
	}
	public String[] toIntent(){
		String[] out = new String[2];
		out[0] = sessionID;
		out[1] = csrfToken;
		return out;
	}

}