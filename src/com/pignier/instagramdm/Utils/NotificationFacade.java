package com.pignier.instagramdm.Utils;

import com.pignier.instagramdm.R;
import com.pignier.instagramdm.Model.ThreadModel;
import com.pignier.instagramdm.DirectMessages.DirectMessagesActivity;

import android.content.Context;
import android.content.Intent;

import android.media.RingtoneManager;
import android.media.AudioAttributes;

import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.Notification;
import android.app.PendingIntent;

import android.net.Uri;

import android.graphics.Color;

import org.json.JSONObject;

public class NotificationFacade {
	private static final String EMAIL_NOTIFICATION_CHANNEL_ID = "notfication";
	private static final long[] VIBRATION_PATTERN = {100, 200, 100, 200};
	private static final int NOTIFICATION_ID = 0;
	public static final int SERVICE_NOTIFICATION_ID = 1;

	NotificationManager notificationManager;
	Context context;
	
	public NotificationFacade(Context context) {
		this.context = context;
		this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	

	public Notification getServiceNotification(){
		
		Notification notification = new Notification.Builder(this.context,EMAIL_NOTIFICATION_CHANNEL_ID)
		 .setContentTitle("DM Sync started")
		 .setAutoCancel(true)
		 .setSmallIcon(R.drawable.notification_icon)
		 .build();
		 return notification;
	}


	public void createNotificationChannel(){
		
		NotificationChannel notificationChannel = new NotificationChannel(
			EMAIL_NOTIFICATION_CHANNEL_ID,
			"New DM received",
			NotificationManager.IMPORTANCE_DEFAULT);
		notificationChannel.setShowBadge(true);
		Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		AudioAttributes att = new AudioAttributes.Builder()
			.setUsage(AudioAttributes.USAGE_NOTIFICATION)
			.setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
			.build();
		notificationChannel.setSound(ringtoneUri, att);
		notificationChannel.setVibrationPattern(VIBRATION_PATTERN);
		notificationChannel.enableLights(true);
		notificationChannel.setLightColor(Color.RED);
		notificationChannel.setShowBadge(true);
		notificationManager.createNotificationChannel(notificationChannel);
	}
	public void cancelNotification(){
		notificationManager.cancel(NOTIFICATION_ID);
	}

	public void Notify(JSONObject lastInstagramItem, ThreadModel thread,String instaAccoundID){

		Intent openThreadIntent = new Intent(this.context, DirectMessagesActivity.class);
		openThreadIntent.putExtra("sender", "notification");
		openThreadIntent.putExtra("instaAccoundID", instaAccoundID);


		PendingIntent pendingIntent = PendingIntent.getActivity(
			context.getApplicationContext(),
			0,
			openThreadIntent,
			PendingIntent.FLAG_UPDATE_CURRENT);
		

		String textOnNofification;
		String threadTitle = thread.getTitle();
		String lastInstagramItemType;
		try { lastInstagramItemType = lastInstagramItem.getString("item_type"); }catch( Exception e){ lastInstagramItemType = null; }

		switch(lastInstagramItemType){
			case "action_log":
				//Log.d(TAG,LOCALTAG+"NEW Action log");
				textOnNofification = threadTitle+" liked a message";
				break;
		   case "text":
				String messageText;
				try { messageText = lastInstagramItem.getString("text"); }catch( Exception e){ messageText = null; }
		       
		       
				textOnNofification = threadTitle+" : "+messageText;
				//Log.d(TAG,LOCALTAG+"NEW Message : " + lastInstagramItem.getString("text"));
				break;
		   case "raven_media":
				//Log.d(TAG,LOCALTAG+"NEW video");
				textOnNofification = threadTitle+" sent a video";
				break;
		   case "voice_media":
				//Log.d(TAG,LOCALTAG+"NEW vocal");
				textOnNofification = threadTitle+" sent a vocal";
				break;
		   case "reel_share":
				//Log.d(TAG,LOCALTAG+"NEW reel share");
				textOnNofification = threadTitle+" shared a reel";
				break;
		   default :
				//Log.d(TAG,LOCALTAG+"what is it ? : "+lastInstagramItemType);
				textOnNofification = threadTitle+" did a unknown action : "+lastInstagramItemType;
	
		}
	
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		
	
		Notification notification = new Notification.Builder(this.context,EMAIL_NOTIFICATION_CHANNEL_ID)
		 .setContentTitle("New message from "+threadTitle)
		 .setContentText(textOnNofification)
		 .setAutoCancel(true)
		 .setSmallIcon(R.drawable.notification_icon)
		 .setContentIntent(pendingIntent)
		 .setLargeIcon(thread.getProfilPic())
		 .build();
		
		notificationManager.notify(NOTIFICATION_ID, notification);

	}
}