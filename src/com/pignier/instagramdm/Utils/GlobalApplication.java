package com.pignier.instagramdm.Utils;

import com.pignier.instagramdm.Model.*;
import android.util.Log;
import java.util.ArrayList;
import android.app.Application;


public class GlobalApplication extends Application{
	public static String NEW_BUBLE = "addNewBuble";
	public static String NEW_MESSAGE_ON_THE_FLY = "newMessageOnTheFly";
	public static String RESTART_SERVICE = "RestartService";


	private String activityRunning = "none"; 

	/**
		To know which activity is on UI. Useful to DMservice to know if notification is needed
	*/
	public void setActivityRunning(String activityName){
		this.activityRunning = activityName;
	}
	public String getActivityRunning(){
		return activityRunning;
	}


}