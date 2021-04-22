package com.pignier.instagramdm.Network;

import java.lang.Thread;
import java.lang.Runnable;
import android.app.Activity;

// An alternative to the deprecated class AsyncTask : https://stackoverflow.com/a/65108879

public abstract class BackGroundTask {

	private Activity activity;
	public BackGroundTask(Activity activity) {
		this.activity = activity;
	}

	private void startBackground() {
		new Thread(new Runnable() {
			public void run() {

				doInBackground();
				activity.runOnUiThread(new Runnable() {
					public void run() {

						onPostExecute();
					}
				});
			}
		}).start();
	}
	public void execute(){
		startBackground();
	}

	public abstract void doInBackground();

	public abstract void onPostExecute();

}