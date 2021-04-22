package com.pignier.instagramdm.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

public class NetworkUtil {
	public static boolean internetIsReachable(Context context) {
		boolean status = false;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		Network networks = cm.getActiveNetwork();
		if (networks != null) {
			status = cm.getNetworkCapabilities(networks).hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
		} else {
			status = false;
		}
		return status;
	}
}