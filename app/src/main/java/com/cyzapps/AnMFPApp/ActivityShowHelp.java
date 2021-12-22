package com.cyzapps.AnMFPApp;


import java.util.Locale;
import com.cyzapps.MFPLibTester.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ActivityShowHelp extends Activity {
	/** Called when the activity is first created. */
	String mstrLanguage = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.help));
		setContentView(R.layout.help_info);

        String strHelpFileStart = "index";
        
        WebView webviewHelp = (WebView)findViewById(R.id.webview_help);
        webviewHelp.setVerticalScrollBarEnabled(true);
        webviewHelp.setHorizontalScrollBarEnabled(true);
        webviewHelp.getSettings().setBuiltInZoomControls(true);
        webviewHelp.setWebViewClient(new WebViewClient(){
			@Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if( url.startsWith("mailto:") )
				{
					try	{
						// use try...catch block to quench the famous android.util.AndroidRuntimeException: Calling
						// startActivity() from outside of an Activity context requires the FLAG_ACTIVITY_NEW_TASK flag.
						// Is this really what you want? exception.
						Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
				        emailIntent.setType("plain/text");
				        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{url.substring(7)});
				        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
				        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
			            emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				        startActivity(Intent.createChooser(emailIntent, ""));
					} catch(Exception e)	{
						
					}
			        return true;	//intercept it.
				}
		        /*if (url.startsWith("mailto:")) {
		            MailTo mt = MailTo.parse(url);
		            Intent intent = new Intent();
		            intent.setType("text/html");
		            intent.putExtra(Intent.EXTRA_EMAIL, new String[] {mt.getTo()});
		            intent.putExtra(Intent.EXTRA_SUBJECT, mt.getSubject());
		            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		            startActivity(Intent.createChooser(intent, "Email ..."));
		            return true;
		        }*/
				return false;
		          }
		});
	    Locale l = Locale.getDefault();
	    mstrLanguage = String.format("%s-%s", l.getLanguage(), l.getCountry());
	    String strIndexAddr;
	    if (mstrLanguage.equals("zh-CN") || mstrLanguage.equals("zh-SG"))	{
	    	strIndexAddr = "file:///android_asset/zh-CN/" + strHelpFileStart + ".html";
	    } else if (mstrLanguage.equals("zh-TW") || mstrLanguage.equals("zh-HK"))	{
		    	strIndexAddr = "file:///android_asset/zh-TW/" + strHelpFileStart + ".html";
	    } else	{
	    	strIndexAddr = "file:///android_asset/en/" + strHelpFileStart + ".html";
	    	mstrLanguage = "en";
	    }

        //String strIndexAddr = "file:///android_asset/help/" + strHelpFileStart + ".html";

		webviewHelp.loadUrl(strIndexAddr);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(event.getAction() == KeyEvent.ACTION_DOWN){
			switch(keyCode)
			{
			case KeyEvent.KEYCODE_BACK:
				WebView webviewHelp = (WebView)findViewById(R.id.webview_help);
				if(webviewHelp.canGoBack() == true){
					webviewHelp.goBack();
				}else{
					finish();
				}
				return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}
	
}
