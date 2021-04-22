package com.pignier.instagramdm.DirectMessages;

import com.pignier.instagramdm.R;
import com.pignier.instagramdm.Utils.Functions;
import com.pignier.instagramdm.Utils.DatabaseHelper;

import android.util.Log;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CursorAdapter;

import android.content.Context;
import android.database.Cursor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

// Useful sources
// https://guides.codepath.com/android/Populating-a-ListView-with-a-CursorAdapter#attaching-the-adapter-to-a-listview
// https://stackoverflow.com/questions/21501316/what-is-the-benefit-of-viewholder-pattern-in-android

public class DirectMessagesAdapter extends CursorAdapter {
	Functions f = new Functions();
	String TAG = "INSTAGRAMDM";
	String LOCALTAG = "DirectMessagesAdapter : ";


	public DirectMessagesAdapter(Context context, Cursor cursor) {
		super(context, cursor, 0);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.single_item_conv, parent ,false); 
		
		ViewHolder holder = new ViewHolder();
		holder.text = (TextView) view.findViewById(R.id.txt_view);
		holder.image = (ImageView) view.findViewById(R.id.icon);
		holder.has_newer = (ImageView) view.findViewById(R.id.has_newer);

		view.setTag(holder);
		
		return view;
	}
	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		ViewHolder holder = (ViewHolder) view.getTag();
		
		boolean isRequested = (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.THREAD_IS_REQUESTED)) == 1);
		boolean showView = DirectMessagesActivity.DMSHOWED != isRequested;

		// get element of the threads
		String title = cursor.getString(cursor.getColumnIndex(DatabaseHelper.THREAD_TITLE));
		byte[] blob = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.THREAD_IMAGE));
		Bitmap bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.length);
		boolean newer = (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.THREAD_HAS_NEWER)) == 1);

		
		
		// show item in accordance with the will of activity
		/** set values to null is better than change the visibility under condition of {@link showView} each time */
		holder.text.setText(showView ? title : null);
		holder.image.setImageBitmap(showView ? f.BitmapCircularCroper(bitmap) : null);
		holder.has_newer.setVisibility((showView && newer) ? View.VISIBLE : View.INVISIBLE);
		
	}
	static class ViewHolder {
		TextView text;
		ImageView image;
		ImageView has_newer;
	}
}