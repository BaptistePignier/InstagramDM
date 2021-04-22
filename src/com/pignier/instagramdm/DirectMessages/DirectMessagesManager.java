package com.pignier.instagramdm.DirectMessages;

import com.pignier.instagramdm.Model.*;
import com.pignier.instagramdm.Utils.GlobalApplication;
import com.pignier.instagramdm.Network.BackGroundTask;
import com.pignier.instagramdm.Utils.Functions;
import com.pignier.instagramdm.Utils.NetworkUtil;

import android.content.res.Resources;
import android.widget.ProgressBar;
import android.util.Log;
import android.app.Activity;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;

public class DirectMessagesManager{
	String TAG = "INSTAGRAMDM";
	String LOCALTAG = "DMManager : ";
	Resources res;
	GlobalApplication appContext;
	DMInterface dmInterface;
	Activity activity;
	JSONObject dmJson, requestedJson;
	boolean canceled = false, failed = false;
	IGSession sessionToUse;
	Functions f = new Functions();
	int progressOfProgressBar;
	
	
	public DirectMessagesManager(Activity activity,IGSession sessionToUse,GlobalApplication appContext,DMInterface dmInterface){
		super();
		this.activity = activity;
		this.res = appContext.getResources();
		this.appContext = appContext;
		this.dmInterface = dmInterface;
		this.sessionToUse = sessionToUse;

	}
	
	public void cancelNetworkOperation(){
		this.canceled = true;
	}


	public void getThreads(ProgressBar progressBar){
		ArrayList<ThreadModel> threads = new ArrayList<ThreadModel>();
		
		new BackGroundTask(activity) {
			@Override
			public void doInBackground() {
				Log.d(TAG,LOCALTAG+"get threads from web ... ");
				if (! canceled){
					dmJson = f.getDmJSONFromWeb(sessionToUse);
					requestedJson = f.getRequestedJSONFromWeb(sessionToUse);
					if (dmJson == null || requestedJson == null){
						failed = true;
					}else{
						progressOfProgressBar = 0;
						
						JSONArray threadsDMJson = f.getThreadsJSON(dmJson);
						JSONArray threadsRequestedJson = f.getThreadsJSON(requestedJson);

						progressBar.setMax(threadsDMJson.length() + threadsRequestedJson.length());

						ArrayList<ThreadModel> dm = fetchDM(threadsDMJson,progressBar);
						ArrayList<ThreadModel> requests = fetchRequests(threadsRequestedJson,progressBar);
						threads.addAll(dm);
						threads.addAll(requests);
					}
				}
			}
			@Override
			public void onPostExecute(){
				if (failed){
					canceled = false;
					failed = false;
					dmInterface.OnFailed();

				}else{
					dmInterface.OnThreads(threads);
					
				}
				
			}
		}.execute();
	}
	public ArrayList<ThreadModel> fetchRequests(JSONArray threadsRequestedJson,ProgressBar progressBar){
		ArrayList<ThreadModel> requests = new ArrayList<ThreadModel>();
		for (int i=0;i<threadsRequestedJson.length();i++){
			if (canceled || ! NetworkUtil.internetIsReachable(appContext)){
				failed = true;
				break;
			}
			try{
				progressBar.setProgress(progressOfProgressBar+1+i);
				JSONObject threadRequestedJson = threadsRequestedJson.getJSONObject(i);
				JSONArray messagesArray = threadRequestedJson.getJSONArray("items");
				ThreadModel thread = f.getThreadFromJSON(res,threadRequestedJson);
				if (messagesArray.length() != 0){
					ArrayList<MessageModel> messages = new ArrayList<MessageModel>();
					messages.add(f.getMessageFromJson(messagesArray.getJSONObject(0),thread.getInstaID())); // Only get the first message for requested
					thread.setMessages(messages);
					thread.setIsRequested(true);
					requests.add(thread);
				}
			}catch(JSONException e){
				Log.e(TAG, LOCALTAG+Log.getStackTraceString(e));
			}
		}
		return requests;
	}

	public ArrayList<ThreadModel> fetchDM(JSONArray threadsDMJson,ProgressBar progressBar){
		ArrayList<ThreadModel> dm = new ArrayList<ThreadModel>();
		for (int i=0;i<threadsDMJson.length();i++){
			if (canceled || ! NetworkUtil.internetIsReachable(appContext)){
				failed = true;
				break;
			}
			try{
				progressOfProgressBar = i;
				progressBar.setProgress(progressOfProgressBar);
				JSONObject threadDMJson = threadsDMJson.getJSONObject(i);
				JSONArray messagesArray = threadDMJson.getJSONArray("items");
				ThreadModel thread = f.getThreadFromJSON(res,threadDMJson);
				if (messagesArray.length() != 0){
					ArrayList<MessageModel> messages = f.getMessagesArrayFromJson(messagesArray,thread.getInstaID());
					Collections.reverse(messages);
					thread.setMessages(messages);
					thread.setIsRequested(false);
					dm.add(thread);
				}
			}catch(JSONException e){
				Log.e(TAG, LOCALTAG+Log.getStackTraceString(e));
			}
		}
		return dm;

	}
	public interface DMInterface {
		void OnThreads(ArrayList<ThreadModel> threads);
		void OnFailed();
	}

}