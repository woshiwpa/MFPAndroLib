package com.cyzapps.mfpanlib;

import android.content.Context;

import com.cyzapps.AdvRtc.RtcAppClient;
import com.cyzapps.adapter.AndroidStorageOptions;
import com.cyzapps.adapter.MFPAdapter;

public class MFPAndroidLib {
    public static final int RTC_STATE_CHANGE_INFO = 1;
    public static final int EMAIL_SEND_ERROR_INFO = 2;
    public static final int EMAIL_FETCH_STALE_INFO = 3;
    public static final int EMAIL_FETCH_ERROR_INFO = 4;
    public static final int EMAIL_SEND_RECV_STATE_INFO = 5;

    private static MFPAndroidLib singleObject;
    private void MFPAndroidLib() {}


    private static Context context;
    private static String settingsConfig;
    private static boolean readScriptsResFromAsset;
    public boolean initialize(Context c, String settingsCfg, boolean readCodesResFromAsset) {
        if (context == null) {
            context = c;
            settingsConfig = settingsCfg;
            readScriptsResFromAsset = readCodesResFromAsset;
            return true;
        }
        return false;
    }
    public boolean initialize(Context c, String settingsCfg) {
        if (context == null) {
            context = c;
            settingsConfig = settingsCfg;
            readScriptsResFromAsset = false;
            return true;
        }
        return false;
    }

    public static MFPAndroidLib getInstance() {
        if (singleObject == null) {
            singleObject = new MFPAndroidLib();
        }
        return singleObject;
    }


    public static Context getContext() {
        return context;
    }
    public static String getSettingsConfig() { return settingsConfig; }
    public static boolean getReadScriptsResFromAsset() { return readScriptsResFromAsset; }

    private static RtcAppClient rtcAppClient = null;    // cannot initialize it here because it needs App's context so it has to be initalized after App is initialized.

    public static RtcAppClient getRtcAppClient() {
        if (null == rtcAppClient) {
            rtcAppClient = new RtcAppClient(false);
        }
        return rtcAppClient;
    }
}
