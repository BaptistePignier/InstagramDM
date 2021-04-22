package com.pignier.instagramdm.Thread;

import com.pignier.instagramdm.R;
import com.pignier.instagramdm.Utils.DatabaseHelper;
import com.pignier.instagramdm.Utils.Functions;

import android.content.Context;

import android.database.Cursor;


import android.util.Log;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;

import android.widget.RelativeLayout;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.ImageView;

import java.lang.Math;

//https://guides.codepath.com/android/Populating-a-ListView-with-a-CursorAdapter#attaching-the-adapter-to-a-listview
//https://stackoverflow.com/questions/21501316/what-is-the-benefit-of-viewholder-pattern-in-android
public class ThreadsAdapter extends CursorAdapter {
	Functions f = new Functions();
	String TAG = "INSTAGRAMDM";
	String LOCALTAG = "ThreadsAdapter : ";
	String accountID;
	private String COLOR_FROM_ME = "#00838F";
	private String COLOR_FROM_OTHER = "#F1AF28";


	public ThreadsAdapter(Context context, Cursor cursor,String accountID) {
		super(context, cursor, 0);
		this.accountID = accountID;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.single_item_mess, parent ,false); 

		
		ViewHolder holder = new ViewHolder();
		holder.text = (TextView) view.findViewById(R.id.txt_view);
		holder.image = (ImageView) view.findViewById(R.id.img_view);
		holder.layout = (RelativeLayout) view.findViewById(R.id.relative_layout);


		view.setTag(holder);
		
		return view;
	}
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		
		String message_type = cursor.getString(cursor.getColumnIndex(DatabaseHelper.MESSAGE_TYPE));
		String message_owner = cursor.getString(cursor.getColumnIndex(DatabaseHelper.MESSAGE_OWNER));

		//Draw messages of each users in conversation
		boolean isMine = message_owner.equals(accountID);
		
		holder.layout.setGravity(isMine ? Gravity.RIGHT : Gravity.LEFT);
	
		Drawable in = context.getResources().getDrawable(R.drawable.messageIn, context.getTheme());
		Drawable out = context.getResources().getDrawable(R.drawable.messageOut, context.getTheme());
		holder.text.setBackground(isMine ? out : in);
		
		
		String messageText = cursor.getString(cursor.getColumnIndex(DatabaseHelper.MESSAGE_TEXT));
		switch(message_type){
			case "voice_media":
				boolean isplayed = (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MESSAGE_AUDIO_PLAYED)) == 1);
				
				if (isplayed){
					Drawable played = context.getResources().getDrawable(R.drawable.vocalPlayed, context.getTheme());
					holder.text.setBackground(played);
				}
				holder.text.setVisibility(View.VISIBLE);
				
				holder.text.setText(messageText);
				break;
			case "media":
				holder.image.setVisibility(View.VISIBLE);
				
				byte[] blob = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.MESSAGE_IMAGE));
				Bitmap original = BitmapFactory.decodeByteArray(blob, 0, blob.length);

				float aspectRatio = original.getWidth() / (float) original.getHeight();
				int width = 480;
				int height = Math.round(width / aspectRatio);
	
				Bitmap newBitmap = Bitmap.createScaledBitmap(original, width, height, false);
				holder.image.setImageBitmap(newBitmap);
				break;
			default : //Text
				holder.text.setVisibility(View.VISIBLE);
				holder.text.setText(messageText);
				break;
		}

	}
	static class ViewHolder {
		TextView text;
		ImageView image;
		RelativeLayout layout;
	}
}