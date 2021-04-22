package com.pignier.instagramdm.DirectMessages;

import com.pignier.instagramdm.R;
import com.pignier.instagramdm.Model.*;
import com.pignier.instagramdm.Thread.ThreadActivity;
import com.pignier.instagramdm.Login.LoginMainActivity;
import com.pignier.instagramdm.Utils.GlobalApplication;
import com.pignier.instagramdm.Utils.NetworkUtil;
import com.pignier.instagramdm.Utils.Functions;
import com.pignier.instagramdm.Utils.DatabaseHelper;
import com.pignier.instagramdm.Utils.NotificationFacade;
import com.pignier.instagramdm.Network.DMService;
//import com.pignier.instagramdm.Receivers.Restarter;

import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.ListView;
import android.widget.Button;
import android.widget.TextView;

import android.app.Activity;

import android.view.View;

import android.util.Log;
import java.util.ArrayList;


public class DirectMessagesActivity extends Activity implements OnItemClickListener{
	private String TAG = "INSTAGRAMDM", LOCALTAG = "MainActivity : ";

	private ProgressBar progressBar;
	private Button disconnectButton, showRequestsButton;
	private TextView noRequest;
	private GlobalApplication appContext;
	private DirectMessagesManager directmesagesmanager;
	private DirectMessagesAdapter adapter;
	private ListView listView;
	private DatabaseHelper dbHelper;
	private IGSession instaSession;


	public static boolean DMSHOWED = true;
	
	/**
		This BroadcastReceiver receive the new message's broadcast to update the UI
	*/
	// Useful source
	// https://www.vogella.com/tutorials/AndroidBroadcastReceiver/article.html
	private BroadcastReceiver newMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (adapter != null && dbHelper != null){
				adapter.changeCursor(newCursorForAdapter());
				adapter.notifyDataSetChanged();
				
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		disconnectButton = (Button) findViewById(R.id.disconnectButton);
		showRequestsButton = (Button) findViewById(R.id.showRequestsButton);
		noRequest = (TextView) findViewById(R.id.noRequest);
		progressBar = (ProgressBar) findViewById(R.id.pBar);
		listView = (ListView) findViewById(R.id.list_view);

		
		Log.d(TAG,LOCALTAG+"Activity created");

		
		Intent intent = getIntent();
		String sender =  intent.getStringExtra("sender");
		if (sender.equals("notification")){
			Intent intentToSend = new Intent(this,ThreadActivity.class);
			// Just a relay
			intentToSend.putExtra("instaAccoundID", intent.getStringExtra("instaAccoundID"));

			startActivity(intentToSend);
		}
		if (sender.equals("login")){
			//String[] credentials = intent.getStringArrayExtra("session");
			//Log.d(TAG,LOCALTAG+"credentials null ? : "+String.valueOf(credentials));
			instaSession = new IGSession(intent.getStringArrayExtra("session"));
		}

		appContext = (GlobalApplication) getApplicationContext();
		dbHelper = DatabaseHelper.getInstance(appContext);
		

		

		directmesagesmanager = new DirectMessagesManager(this,instaSession,appContext,new DirectMessagesManager.DMInterface(){
			@Override
			public void OnThreads(ArrayList<ThreadModel> threads){
				Log.d(TAG,LOCALTAG+"got response from direct manager : Threads");
				quitDownloadingState();
				dbHelper.threadsWriter(threads);
				
				CreateService();

				showThreadsOnUI();
				showRequestsButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// while downloading
						if (adapter != null){
							DMSHOWED = ! DMSHOWED;
							showRequestsButton.setText(DMSHOWED ? "Requests" :  "DM Messages");
							//Reload all views
							adapter.notifyDataSetChanged();
							noRequest.setVisibility(( !DMSHOWED && !dbHelper.AreThereRequestedConversations()) ? View.VISIBLE : View.INVISIBLE);
						}
					}
				});
			}
			@Override
			public void OnFailed(){
				Log.d(TAG,LOCALTAG+"got response from direct manager : fail ");
				quitDownloadingState();
				setContentView(R.layout.webError);
			}
		});
		
		if (! dbHelper.AreThereConversations()){
			if (NetworkUtil.internetIsReachable(appContext)){
				Log.d(TAG,LOCALTAG+"there aren't any conversations, so downloading");
				enterInDownloadingState();
				directmesagesmanager.getThreads(progressBar);
			}else { setContentView(R.layout.no_database); }
		}
		
			
		disconnectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
					quitDownloadingState();		
					directmesagesmanager.cancelNetworkOperation();
					dbHelper.flushAll();
					DatabaseHelper.removeInstance();
					DestroyService();
					backToLogin();
					
			}
		});
		
	}
	private void backToLogin(){
		startActivity(new Intent(appContext,LoginMainActivity.class));
	}
	
	private void CreateService(){
		Intent intent = new Intent(DirectMessagesActivity.this, DMService.class);
		intent.putExtra("sender", "DirectMessagesActivity");
		startForegroundService(intent);
	}
	private void DestroyService(){
		stopService(new Intent(DirectMessagesActivity.this,DMService.class));
	}

	private Cursor newCursorForAdapter(){
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor newCursor = db.rawQuery("SELECT  * FROM "+dbHelper.THREAD_TABLE_NAME, null);
		return newCursor;
	}

	private void showThreadsOnUI(){
		Log.d(TAG,LOCALTAG+"Threads are show");
		Cursor newCursor = newCursorForAdapter();
		adapter = new DirectMessagesAdapter(this, newCursor);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}

	private void quitDownloadingState(){
		progressBar.setVisibility(View.INVISIBLE);
	}
	private void enterInDownloadingState(){
		progressBar.setVisibility(View.VISIBLE);
		progressBar.setProgress(0);
	}
	@Override
	public void onItemClick(AdapterView<?> l, View v,int position, long id){
		// items are clickable even if they are hidden 
		if (DMSHOWED){
			NotificationFacade notificationFacade = new NotificationFacade(this);
			notificationFacade.cancelNotification();
			Intent intent = new Intent(this,ThreadActivity.class);
			intent.putExtra("ListViewPosition",position); 
			intent.putExtra("instaAccoundID", instaSession.getAccountID());
			startActivity(intent);
		}
			
	}
	@Override
	public void onBackPressed(){
		finish();
	}

	@Override
	protected void onResume() {
		registerReceiver(newMessageReceiver, new IntentFilter(GlobalApplication.NEW_BUBLE));
		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(newMessageReceiver);
		super.onPause();
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
	}
	@Override
	protected void onStart(){
		super.onStart();
		if (dbHelper.AreThereConversations()){
			showThreadsOnUI();
		}
		appContext.setActivityRunning("DirectMessagesActivity");
	}
}