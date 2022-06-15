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
import com.cyzapps.Jfcalc.IOLib;
import com.cyzapps.OSAdapter.MFP4AndroidFileMan;
import com.cyzapps.OSAdapter.ParallelManager.MFP4AndroidCommMan;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import com.cyzapps.adapter.AndroidStorageOptions;
import com.cyzapps.adapter.MFPAdapter;
import com.cyzapps.mfpanlib.MFPAndroidLib;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.util.Log;
import android.widget.Toast;

public class AppAnMFP extends androidx.multidex.MultiDexApplication {
	public static final String STRING_APP_FOLDER = "MFPAndroLibTester";
	// support single line statment, multiple line session and quick help statment
	public static final String STRING_COMMANDS_TO_RUN
			/*= "\n\nplot_exprs(\"x**2+y**2+z**2==9\")\ngdi_test::game_test::super_bunny::run( )\n";
			= "Plot_3d_surfaces(\"3dBox\", \"3D Box\", \"x\", \"y\", \"z\", "
			+ "\"\",false,\"red\",\"red\",null,\"red\",\"red\",null,\"u\",-1,1,2,\"v\",-1,1,2,\"u\",\"v\",\"1\", "
			+ "\"\",false,\"green\",\"green\",null,\"green\",\"green\",null,\"u\",-1,1,2,\"v\",-1,1,2,\"u\",\"1\",\"v\", "
			+ "\"\",false,\"blue\",\"blue\",null,\"blue\",\"blue\",null,\"u\",-1,1,2,\"v\",-1,1,2,\"1\",\"u\",\"v\", "
			+ "\"\",false,\"yellow\",\"yellow\",null,\"yellow\",\"yellow\",null,\"u\",-1,1,2,\"v\",-1,1,2,\"u\",\"v\",\"-1\", "
			+ "\"\",false,\"cyan\",\"cyan\",null,\"cyan\",\"cyan\",null,\"u\",-1,1,2,\"v\",-1,1,2,\"u\",\"-1\",\"v\", "
			+ "\"\",false,\"magenta\",\"magenta\",null,\"magenta\",\"magenta\",null,\"u\",-1,1,2,\"v\",-1,1,2,\"-1\",\"u\",\"v\")\n"
			+ "gdi_test::game_test::super_bunny::run( )\n";*/
	= "print(\"你\\\"好\")\nreturn \"你\\\"好\"";
	//= "::gdi_test::game_test::chess::main()";
	// = "::gdi_test::remote_ctrl::remote_monitor::main()";

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

		MFPAndroidLib mfpLib = MFPAndroidLib.getInstance();

		String settingsName = "AppAnMFP_settings";
		SharedPreferences settings = AppAnMFP.getContext().getSharedPreferences(settingsName, 0);
		if (settings != null) {
			/*
			 * Note: we cannot write like
			 * calculator_settings.edit().putInt(BITS_OF_PRECISION, nBitsofPrecision);
			 * calculator_settings.edit().putInt(NUMBER_OF_RECORDS, nNumberofRecords);
			 * calculator_settings.edit().commit();
			 * because calculator_settings.edit() returns different editor each time.
			 */
			//settings.edit().putInt(MFP4AndroidCommMan.WEBRTC_DEBUG_LEVEL, 3)
			//		.commit();
		}
		// initialize function has three parameters. The first one is application's context,
		// the second one is your app's shared preference name, and the last one is a boolean
		// value, with true means your MFP scripts and resources are saved in your app's
		// assets and false means your MFP scripts and resources are saved in your Android
		// device's local storage.
		// The following code is for the situation to save MFP scripts and resources in assets
		// of app. However, if developer wants to run scripts from local storage, uncomment
		// the following line and pass false to the third parameter of mfpLib.initialize function.
		// MFP4AndroidFileMan.msstrAppFolder = STRING_APP_FOLDER;
		mfpLib.initialize(mContext, settingsName, true);	// we don't have any settings to load. So stick to default values

		MFP4AndroidFileMan mfp4AnFileMan = new MFP4AndroidFileMan(getAssets());
		// platform hardware manager has to be early initialized as it is needed to
		// analyze anotations in the code at loading stage.
		// other managers are loaded later at the first command.
		FuncEvaluator.msPlatformHWMgr = new PlatformHWManager(mfp4AnFileMan);
		MFPAdapter.clear(CitingSpaceDefinition.CheckMFPSLibMode.CHECK_EVERYTHING);
		// load predefined libs when app starts
		mfp4AnFileMan.loadPredefLibs();
    }

    public static Context getContext(){
        return mContext;
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
