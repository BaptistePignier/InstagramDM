package com.pignier.instagramdm.Thread;

import com.pignier.instagramdm.R;
import com.pignier.instagramdm.Model.*;
import com.pignier.instagramdm.Utils.GlobalApplication;
import com.pignier.instagramdm.Utils.DatabaseHelper;
import com.pignier.instagramdm.Utils.AudioHelper;

import android.database.sqlite.SQLiteDatabase;

import com.pignier.instagramdm.Utils.ByteArrayMediaDataSource;
import android.content.Context;
import android.content.Intent;

import android.media.AudioAttributes; 
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.media.AudioFocusRequest;

import android.widget.AdapterView;
import android.widget.ListView;
import android.database.Cursor;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.util.Log;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
 
public class ThreadActivity extends Activity {
	public GlobalApplication appContext;
	String TAG = "INSTAGRAMDM", LOCALTAG = "ThreadActivity : ";
	ListView listView;
	ThreadsAdapter arrayAdapter;
	ThreadModel thread;
	int threadNumber;
	String instaThreadID, instaAccoundID;



	DatabaseHelper dbHelper;

	// pop up on tread direcly
	//https://www.vogella.com/tutorials/AndroidBroadcastReceiver/article.html
	private BroadcastReceiver activityReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			String instaThreadID = intent.getStringExtra("instaThreadID");
			if (arrayAdapter != null && thread != null){
				if (thread.getInstaID().equals(instaThreadID)){
					Log.d(TAG,LOCALTAG+"New message pop on conversation");
					updateUI();
				}
				
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item);
		
		appContext = (GlobalApplication) getApplicationContext();
		Intent intent = getIntent();
		

		threadNumber = intent.getIntExtra("ListViewPosition",0);


		instaAccoundID = intent.getStringExtra("instaAccoundID");	

		listView = (ListView) findViewById(R.id.list_view_item);
		Log.d(TAG,LOCALTAG+"ThreadNumber : "+String.valueOf(threadNumber));


		dbHelper = DatabaseHelper.getInstance(appContext);
	   	
		// ListView start at 0, db start at 1
		thread = dbHelper.getThreadByPosition(threadNumber + 1);
		
		instaThreadID = thread.getInstaID();
		thread.setHasNewer(false);
		Log.d(TAG,LOCALTAG+thread.getTitle()+" updated has newer = false ");
		
		dbHelper.updateConversation(thread);
		
		Log.d(TAG,LOCALTAG+"Thread insta id : "+instaThreadID);
		
		

		setListView();
		
		AudioHelper audioHelper  = new AudioHelper();
		
		AudioAttributes attributes = audioHelper.createAttributes();
		
		AudioFocusRequest audioFocusRequest = audioHelper.createFocusRequest(attributes);
		AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		
		ArrayList<MessageModel> messages = thread.getMessages();
		
		MediaPlayer mediaPlayer = audioHelper.createPlayer(attributes);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,long arg3){

				mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer player) {
						
						MessageModel messagePlayed = messages.get(audioHelper.get_audio_played_position());
						messagePlayed.set_audio_played(false);
						
						dbHelper.updateMessage(messagePlayed);
						dbHelper.close();


						updateUI();
						am.abandonAudioFocusRequest(audioFocusRequest);
					}
				});
				MessageModel message = messages.get(position);
				String type = message.getType();
				if (type.equals("voice_media")){
					if (! mediaPlayer.isPlaying()) {
						int result = am.requestAudioFocus(audioFocusRequest); // to stop other audio from others apps
						
						audioHelper.set_audio_played_position(position);
						
						message.set_audio_played(true);
						dbHelper.updateMessage(message);
						dbHelper.close();

					
						updateUI();
						try {
							mediaPlayer.reset();
							mediaPlayer.setDataSource(new ByteArrayMediaDataSource(message.getAudio()));
							mediaPlayer.prepare();
							mediaPlayer.start();
						} catch (Exception e) {
							Log.d(TAG,Log.getStackTraceString(e));
						}
					}		
				}

			}

		});

		if (activityReceiver != null) {
			IntentFilter intentFilter = new IntentFilter(GlobalApplication.NEW_MESSAGE_ON_THE_FLY);

			registerReceiver(activityReceiver, intentFilter);
		}
	}
	public void setListView(){
		arrayAdapter = new ThreadsAdapter(this, newCursorForAdapter(), instaAccoundID);
		listView.setAdapter(arrayAdapter);
		listView.setSelection(arrayAdapter.getCount()-1);
	}


	public void updateUI(){
		Cursor newMessageCursor = newCursorForAdapter();
		arrayAdapter.changeCursor(newMessageCursor);
		arrayAdapter.notifyDataSetChanged();
		// to always have the new message on screen
		listView.setSelection(arrayAdapter.getCount()-1);
	}
	public Cursor newCursorForAdapter(){
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor messageCursor = db.rawQuery("SELECT * FROM "+ DatabaseHelper.MESSAGE_TABLE_NAME+" WHERE "+DatabaseHelper.MESSAGE_THREAD_ID+"="+instaThreadID,null);

		return messageCursor;
	}

	@Override
	protected void onDestroy(){
		if (activityReceiver != null) {
			unregisterReceiver(activityReceiver);
			activityReceiver = null;
		}
		super.onDestroy();
		
	}
	@Override
	public void onStart(){
		super.onStart();
		appContext.setActivityRunning("ThreadActivity:"+instaThreadID);
	}
}