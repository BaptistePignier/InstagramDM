package com.pignier.instagramdm.Login;

import com.pignier.instagramdm.R;
import com.pignier.instagramdm.Utils.GlobalApplication;
import com.pignier.instagramdm.Utils.DatabaseHelper;
import com.pignier.instagramdm.DirectMessages.DirectMessagesActivity;
import com.pignier.instagramdm.Model.IGSession;
import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import android.webkit.WebViewClient;
import android.webkit.WebView;
import android.graphics.Bitmap;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;



public class LoginWebViewActivity extends Activity{
	String TAG = "INSTAGRAMDM";
	String LOCALTAG = "LoginWebViewActivity : ";
	GlobalApplication appContext;
	DatabaseHelper dbHelper;
	IGSession igsession;
	WebView webView;
	private boolean logged = false;
	private final WebChromeClient webChromeClient = new WebChromeClient();

	private final WebViewClient webViewClient = new WebViewClient() {

		@Override
		public void onPageFinished(final WebView view, final String url) {

			final String mainCookie = CookieManager.getInstance().getCookie(url);
			if (mainCookie != null && mainCookie.length() != 0 && ! logged){
				Log.d(TAG,LOCALTAG+"Page finished");
				logged = true;
				String ig_csrf_token = mainCookie.split("csrftoken=")[1].split(";")[0];
				String ig_session_ID = mainCookie.split("sessionid=")[1].split(";")[0];
				IGSession new_session = new IGSession(ig_session_ID,ig_csrf_token);
				saveSessionInStorage(new_session);
				launchMainActivity(new_session);
			}	
			
		}
	};



	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginWebView);
		webView = (WebView) findViewById(R.id.webview);

		appContext = (GlobalApplication) getApplicationContext();
		dbHelper = DatabaseHelper.getInstance(appContext);
		

		webView.setWebViewClient(webViewClient);
		final WebSettings webSettings = webView.getSettings();
		if (webSettings != null) {
			webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.105 Mobile Safari/537.36");
			webSettings.setJavaScriptEnabled(true);
			webSettings.setDomStorageEnabled(true);
			webSettings.setSupportZoom(true);
			webSettings.setBuiltInZoomControls(true);
			webSettings.setDisplayZoomControls(false);
			webSettings.setLoadWithOverviewMode(true);
			webSettings.setUseWideViewPort(true);
			webSettings.setAllowFileAccessFromFileURLs(true);
			webSettings.setAllowUniversalAccessFromFileURLs(true);
			webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}
		CookieManager.getInstance().removeAllCookies(null);
		CookieManager.getInstance().flush();
		webView.loadUrl("https://instagram.com/");
		Log.d(TAG,LOCALTAG+"WebView started");
	}

	public void launchMainActivity(IGSession igsession){
		Log.d(TAG,LOCALTAG+"Main Activity launched");
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
		appContext.setActivityRunning("LoginWebViewActivity");
	}
	@Override
	protected void onPause() {
		if (webView != null) webView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (webView != null) webView.onResume();
	}

	@Override
	protected void onDestroy() {
		if (webView != null) webView.destroy();
		super.onDestroy();
	}
	@Override
	public void onBackPressed(){
		Intent intent = new Intent(this,LoginMainActivity.class);
		startActivity(intent);
		finish();
	}
	
}