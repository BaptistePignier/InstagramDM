package com.pignier.instagramdm.Network;

import com.pignier.instagramdm.R;
import com.pignier.instagramdm.Model.*;
import com.pignier.instagramdm.Utils.Functions;
import com.pignier.instagramdm.Utils.NotificationFacade;
import com.pignier.instagramdm.Utils.DatabaseHelper;
import com.pignier.instagramdm.Utils.GlobalApplication;
import com.pignier.instagramdm.Utils.NetworkUtil;

import android.os.IBinder;
import android.app.Service;

import android.content.Intent;

import android.util.Log;

import java.util.Random;
import java.util.ArrayList;
import java.lang.Thread;
import java.lang.Runnable;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DMService extends Service {
	private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";
	static String TAG = "INSTAGRAMDM";
	static String LOCALTAG;

	static boolean canIRun;
	private GlobalApplication appContext; 
	IBinder mBinder;
	IGSession session;
	DatabaseHelper dbHelper;
	ArrayList<ThreadModel> threads;
	private Functions f = new Functions();
	NotificationFacade notificationFacade;
	ScheduledExecutorService scheduler;
	boolean isWorking;

	@Override
	public void onCreate(){
		super.onCreate();
		LOCALTAG = "DMService "+ ALLOWED_CHARACTERS.charAt(new Random().nextInt(ALLOWED_CHARACTERS.length())) +": ";
		Log.d(TAG,LOCALTAG+"Created");
		scheduler = Executors.newSingleThreadScheduledExecutor();
		
	}
	@Override
	public void onDestroy(){
		super.onDestroy();
		scheduler.shutdown();
		Log.d(TAG,LOCALTAG+"destroy");

	}
	public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
		Log.d(TAG,LOCALTAG+"onStartCommand");
		startWorking();
		
			
			
		startForeground(NotificationFacade.SERVICE_NOTIFICATION_ID,notificationFacade.getServiceNotification());
		Log.d(TAG,LOCALTAG+"service notif launch");
	
		return Service.START_NOT_STICKY;
        
    }

	private boolean ICanUpdate(){
		return (threads != null && session != null && !isWorking && NetworkUtil.internetIsReachable(appContext));
	}
	public void getNews(){
		new Thread(new Runnable() {
			public void run(){
				isWorking = false;
				Log.d(TAG,LOCALTAG+String.valueOf(threads != null)+" "+String.valueOf(session != null)+" "+String.valueOf( !isWorking) +" "+String.valueOf(NetworkUtil.internetIsReachable(appContext)));
				if (ICanUpdate()){
				   	isWorking = true;
					
					Log.d(TAG,LOCALTAG+"getNews : "+ ALLOWED_CHARACTERS.charAt(new Random().nextInt(ALLOWED_CHARACTERS.length())));
					
					JSONObject jsonResponse = f.getDmJSONFromWeb(session);
					if (jsonResponse != null){
						JSONArray inbox_JSON = f.getThreadsJSON(jsonResponse);
						for (int i=0;i<inbox_JSON.length();i++){
							try{
								
								

								JSONObject instagramThread = inbox_JSON.getJSONObject(i);
								JSONObject lastInstagramItem = instagramThread.getJSONArray("items").getJSONObject(0);
								
								
								String instaThreadID = instagramThread.getString("thread_v2_id");
								
								ThreadModel savedThread = getThreadByID(instaThreadID);
								
								String lastSavedItemId = savedThread.getLastItemID();
								String lastInstagramItemId = lastInstagramItem.getString("item_id");
								boolean newMessageOnConversation = ! lastInstagramItemId.equals(lastSavedItemId);
								
								String lastInstagramItemOwner = lastInstagramItem.getString("user_id");
								String myAccoundID = session.getAccountID();
								boolean isMine = lastInstagramItemOwner.equals(myAccoundID);
								
								if (newMessageOnConversation && ! isMine){
									
									//update data before and after manipulation

									getThreadsFromStorage();
									

									String activityRunning = appContext.getActivityRunning();
									Log.d(TAG,LOCALTAG+"activityRunning : "+activityRunning);

									if (! (activityRunning.equals("ThreadActivity:"+instaThreadID))){
										Log.d(TAG,LOCALTAG+"Thread insta id : "+instaThreadID);
										notificationFacade.Notify(lastInstagramItem,savedThread,myAccoundID);
										
										savedThread.setHasNewer(true);
										
										Log.d(TAG,LOCALTAG+savedThread.getTitle()+" updated has newer = true ");



									}else{
										Log.d(TAG,LOCALTAG+" No Notify nNofification because user is on conversation");
										addMessageOnThreadUI(instaThreadID);

									}

									//Remove thread which has newer if already exists
									removeThreadByID(instaThreadID);
									//Add hread which has newer at first = on top
									threads.add(0,savedThread);
										

									dbHelper.writeTitle(threads);
									Log.d(TAG,LOCALTAG+"threads wroted");

								
									MessageModel message = f.getMessageFromJson(lastInstagramItem,instaThreadID);

									dbHelper.addMessageTo(message);
									Log.d(TAG,LOCALTAG+"message added");

									
									getThreadsFromStorage();


									Log.d(TAG,LOCALTAG+"local data updated");
									updateUI(instaThreadID);
									Log.d(TAG,LOCALTAG+"ui updated");
									dbHelper.close();
									
								}

								
							}catch(JSONException e){
								Log.e(TAG, LOCALTAG+Log.getStackTraceString(e));
							}
						}
					}else{
						Log.d(TAG,LOCALTAG+"JSON Response is null");

					}
					isWorking = false;
				}else{
					Log.d(TAG,LOCALTAG+"Can't update now. Either 'cause there's no data stored or 'cause one operation is working");
				}
			}
		}).start();
	}
	public void getSessionFromStorage(){
		session = dbHelper.sessionReader();
		
	}
	public void getThreadsFromStorage(){
		Log.d(TAG,LOCALTAG+"Threads are read");
		if (dbHelper.AreThereConversations()){
			threads = dbHelper.threadsReader();
		}
	}
	// Useful source
	// https://androidexperinz.com/2012/02/14/communication-between-service-and-activity-part-1/
	public void updateUI(String instaThreadID){
		Intent new_intent = new Intent();
		new_intent.setAction(GlobalApplication.NEW_BUBLE);
		new_intent.putExtra("instaThreadID",instaThreadID);
		sendBroadcast(new_intent);
	}
	public void addMessageOnThreadUI(String instaThreadID){
		Intent new_intent = new Intent();
		new_intent.setAction(GlobalApplication.NEW_MESSAGE_ON_THE_FLY);
		new_intent.putExtra("instaThreadID",instaThreadID);
		sendBroadcast(new_intent);
	}
	public ThreadModel getThreadByID(String instaThreadID){
		ThreadModel out = null;
		for (ThreadModel thread : threads){
			if (thread.getInstaID().equals(instaThreadID)){
				out = thread;
				break;
			}
		}
		return out;
	}
	public void removeThreadByID(String instaThreadID){
		for (ThreadModel thread : threads){
			if (thread.getInstaID().equals(instaThreadID)){
				threads.remove(thread);
				break;
			}
		}
	}



    public void startWorking(){
    	Runnable runnable = new Runnable(){
			public void run() {
				getNews();
			

			}
		};
    	scheduler.scheduleAtFixedRate(runnable, 0,1, TimeUnit.SECONDS); 
		
		appContext = (GlobalApplication) getApplicationContext();

		dbHelper = DatabaseHelper.getInstance(appContext);
		
		notificationFacade = new NotificationFacade(this);
		notificationFacade.createNotificationChannel();

		

		
		
		getSessionFromStorage();
		getThreadsFromStorage();
    }

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	

}