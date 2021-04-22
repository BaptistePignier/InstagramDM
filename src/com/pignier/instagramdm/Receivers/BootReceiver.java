package com.pignier.instagramdm.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.content.Context;
import com.pignier.instagramdm.Network.DMService;

public class BootReceiver extends BroadcastReceiver {
	String TAG = "INSTAGRAMDM";
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.d(TAG,"Boot completed intent received");
        Intent startServiceIntent = new Intent(context, DMService.class);
        context.startForegroundService(startServiceIntent);
    }
}