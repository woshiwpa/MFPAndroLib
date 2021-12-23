package com.cyzapps.AnMFPApp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Properties;

import com.cyzapps.AdvRtc.RtcAppClient;
import com.cyzapps.JPlatformHW.PlatformHWManager;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.OSAdapter.MFP4AndroidFileMan;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import com.cyzapps.adapter.MFPAdapter;
import com.cyzapps.mfpanlib.MFPAndroidLib;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.util.Log;

public class AppAnMFP extends androidx.multidex.MultiDexApplication {
	public static final String STRING_APP_FOLDER = "MFPAndroLibTester";
	// support single line statment, multiple line session and quick help statment
	public static final String STRING_COMMANDS_TO_RUN
			= "\n\nplot_exprs(\"x**2+y**2+z**2==9\")\ngdi_test::game_test::super_bunny::run( )\n";//::gdi_test::game_test::chess::main()";

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

		MFPAndroidLib mfpLib = MFPAndroidLib.getInstance();
		mfpLib.initialize(mContext, "", true);	// we don't have any settings to load. So stick to default values

		// load predefined libs when app starts
		MFP4AndroidFileMan mfp4AnFileMan = new MFP4AndroidFileMan(getAssets());
		// platform hardware manager has to be early initialized as it is needed to
		// analyze anotations in the code at loading stage.
		// other managers are loaded later at the first command.
		FuncEvaluator.msPlatformHWMgr = new PlatformHWManager(mfp4AnFileMan);
		MFPAdapter.clear(CitingSpaceDefinition.CheckMFPSLibMode.CHECK_EVERYTHING);
		mfp4AnFileMan.loadPredefLibs();
    }

    public static Context getContext(){
        return mContext;
    }

    public String getStringFromAssetFile(String strAssetFile) {
        
		AssetManager am = getAssets();
		InputStream inputStream = null;
		if (am != null)	{
			// MFPApp.cfg is an asset file
			try {
				inputStream = am.open(strAssetFile);
			} catch(IOException e)	{
			}
		}

	    final StringBuilder out = new StringBuilder();
		if (inputStream != null) {
			final char[] buffer = new char[1024];
			Reader reader = null;
		    try {
		    	reader = new InputStreamReader(inputStream, "UTF-8");
		        while (true) {
		            int rsz = reader.read(buffer, 0, buffer.length);
		            if (rsz < 0)
		                break;
		            out.append(buffer, 0, rsz);
		        }
		    }
		    catch (UnsupportedEncodingException ex) {
		        if (reader != null) {
		        	try {
		        		reader.close();
		        	} catch (IOException ex1) {
		        		
		        	}
		        }
		    }
		    catch (IOException ex) {
		    	if (reader != null) {
		        	try {
		        		reader.close();
		        	} catch (IOException ex1) {
		        		
		        	}
		        }
		    } finally {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	    return out.toString();
    }

	public static String getLocalLanguage()
	{
		Locale l = Locale.getDefault();
		String strLanguage = l.getLanguage();
		if (strLanguage.equals("en"))	{
			return "English";
		} else if (strLanguage.equals("fr"))	{
			return "French";
		} else if (strLanguage.equals("de"))	{
			return "German";
		} else if (strLanguage.equals("it"))	{
			return "Itanian";
		} else if (strLanguage.equals("ja"))	{
			return "Japanese";
		} else if (strLanguage.equals("ko"))	{
			return "Korean";
		} else if (strLanguage.equals("zh"))	{
			if (l.getCountry().equals("TW") || l.getCountry().equals("HK"))	{
				return "Traditional_Chinese";
			} else	{
				return "Simplified_Chinese";
			}
		} else {
			return "";	// unknown language
		}
	}
}
