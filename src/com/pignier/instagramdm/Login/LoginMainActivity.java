package com.pignier.instagramdm.Login;

import com.pignier.instagramdm.R;
import com.pignier.instagramdm.Utils.GlobalApplication;
import com.pignier.instagramdm.Utils.NetworkUtil;
import com.pignier.instagramdm.Utils.DatabaseHelper;
import com.pignier.instagramdm.DirectMessages.DirectMessagesActivity;
import com.pignier.instagramdm.Model.IGSession;

import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.text.InputType;
import java.util.Random;

import android.widget.CheckBox;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

public class LoginMainActivity extends Activity{
	String TAG = "INSTAGRAMDM", LOCALTAG = "LoginMainActivity : ";
	GlobalApplication appContext;
	CheckBox show_password;
	EditText username, password;
	Button login_button, webview_button;

	ProgressBar progressBar;
	TextView errorTextview;
	LoginManager loginManager;
	DatabaseHelper dbHelper;
	IGSession igsession;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG,"----------------------------------------------------------------------");
		Log.d(TAG,LOCALTAG+"Application created");
		appContext = (GlobalApplication) getApplicationContext();

		
		dbHelper = DatabaseHelper.getInstance(appContext);
		
		Log.d(TAG,LOCALTAG+"SESSION ? : "+String.valueOf(dbHelper.isThereSession()));
		if (dbHelper.isThereSession()){
			Log.d(TAG,LOCALTAG+"Session found, start MainActivity");
			
			igsession = dbHelper.sessionReader();
			launchMainActivity(igsession);
			
		}else{
			setContentView(R.layout.loginCrypto);
			
			Log.d(TAG,LOCALTAG+"Session not found, start Login page ");
			
			
			progressBar = (ProgressBar) findViewById(R.id.pBarlogin);
			show_password = (CheckBox) findViewById(R.id.checkBox);
			errorTextview = (TextView) findViewById(R.id.errorTextview);
			
			username = (EditText) findViewById(R.id.editTextTextEmailAddress);
			password = (EditText) findViewById(R.id.editTextTextPassword);
			login_button = (Button) findViewById(R.id.button_login);
			webview_button = (Button) findViewById(R.id.webviewButton);




			View.OnClickListener login_button_click_listener = new View.OnClickListener() {
        		@Override
        		public void onClick(View v) {
					
					String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";
					Log.d(TAG,LOCALTAG+"Button login pressed "+ALLOWED_CHARACTERS.charAt(new Random().nextInt(ALLOWED_CHARACTERS.length())));
					if (! NetworkUtil.internetIsReachable(getApplicationContext())) {
						errorTextview.setVisibility(View.VISIBLE);
						errorTextview.setText("No internet connection");
					}else{
						login_button.setOnClickListener(null);
						// If you want test the app,
						// put raw login data here to avoid to connect each time. Instagram may become angry
						//String ig_session_ID = "<insert sessionID here>";
						//String ig_csrf_token = "<insert csrf token here>";
						//IGSession session = new IGSession(ig_session_ID,ig_csrf_token);
						//saveSessionInStorage(session);
						//launchMainActivity(session);

						String username_input = username.getText().toString();
        				String password_input = password.getText().toString();

						progressBar.setVisibility(View.VISIBLE);
						errorTextview.setVisibility(View.INVISIBLE);
						loginManager.connect(username_input,password_input);
					}		
				}
			};

			loginManager = new LoginManager(LoginMainActivity.this,new LoginManager.LMInterface(){
				@Override
				public void OnConnected(String ig_session_ID,String ig_csrf_token){
					Log.d(TAG,LOCALTAG+"SessionID : "+ig_session_ID);
					Log.d(TAG,LOCALTAG+"CSRF Token : "+ig_csrf_token);

					progressBar.setVisibility(View.INVISIBLE);
					IGSession session = new IGSession(ig_session_ID,ig_csrf_token);
					saveSessionInStorage(session);
					launchMainActivity(session);

				}
				@Override
				public void OnFailed(){
					Log.d(TAG,LOCALTAG+"OnFailed");
					progressBar.setVisibility(View.INVISIBLE);
					errorTextview.setVisibility(View.VISIBLE);
					errorTextview.setText("Wrong Password");
					login_button.setOnClickListener(login_button_click_listener);

				}

			});
			show_password.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           		@Override
           		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
           		  		if(isChecked) {
           		       		password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
           		   		} else {
           		       		password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
           		   		}
           		   		// to keep the cursor at the end of the input
           		   		password.setSelection(password.length());
           		}
        	});
        	login_button.setOnClickListener(login_button_click_listener);
			webview_button.setOnClickListener(new View.OnClickListener() {
        		@Override
        		public void onClick(View v) {
					Intent intent = new Intent(LoginMainActivity.this,LoginWebViewActivity.class);
					startActivity(intent);
					finish();
				}
			});
		}
	}
	public void launchMainActivity(IGSession igsession){
		
		Intent intent = new Intent(this,DirectMessagesActivity.class);
		intent.putExtra("sender", "login");
		intent.putExtra("session",igsession.toIntent());
		startActivity(intent);
		finish();
	}
	public void saveSessionInStorage(IGSession session){
		dbHelper.sessionWriter(session);

	}
	@Override
	public void onStart(){
		super.onStart();
		appContext.setActivityRunning("LoginMainActivity");
	}
}