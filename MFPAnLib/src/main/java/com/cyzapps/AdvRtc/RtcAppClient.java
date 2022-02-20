package com.cyzapps.AdvRtc;

import android.content.Context;
import android.content.SharedPreferences;

import com.cyzapps.mfpanlib.MFPAndroidLib;

/**
 * Created by tony on 4/02/2018.
 */

public class RtcAppClient {
    public static final String TAG = "New_AdvRtcapp_Debug";
    public static final String CONNECT_SETTINGS = "AnMath_connect_settings";

    public static final int MAX_NUMBER_PEER_CONNECTION = 4;

    public static final int USE_SMTP_IMAP = 0;
    public static final int USE_GMAIL_API = 1;
    public static final int USE_EXCHANGE = 2;

    public static final String LOCAL_EMAIL_TYPE = "local_email_type";
    public static final String GMAIL_TOKEN = "gmail_token";
    public static final String EMAIL_ADDRESS = "email_address";
    public static final String EMAIL_PASSWORD = "email_password";
    public static final String SMTP_SERVER = "smtp_server";
    public static final String SMTP_PORT = "smtp_port";
    public static final String SMTP_SSL = "smtp_ssl";
    public static final String IMAP_SERVER = "imap_server";
    public static final String IMAP_PORT = "imap_port";
    public static final String IMAP_SSL = "imap_ssl";
    public static final String KEEP_SIGNAL_SERVICE_RUNNING = "keep_signal_service_running";
    public static final String SERVICE_START_ACTIVITY = "service_start_activity";
    public static final String SERVICE_START_DEST_ADDR = "service_start_dest_addr";

    public DataRtcAgent dataClient0;
    public DataRtcAgent dataClient1;

    private static int mlocalEmailType = 0; // 0 means smtp/imap, 1 means gmail API, 2 means Microsoft Exchange.
    public static int getLocalEmailType() { return mlocalEmailType; }
    public static void setLocalEmailType(int type) { mlocalEmailType = type; }

    public static String mgmailToken = "";  // gmail API token should be refreshed everytime the App is restarted. so no need to save calculator_settings.

    private static String mlocalAddress = "";
    public static String getLocalAddress() { return mlocalAddress; }
    public static void setLocalAddress(String localAddress) { mlocalAddress = localAddress; }
    private static String memailPassword = "";
    public static String getEmailPassword() { return memailPassword; }
    public static void setEmailPassword(String emailPassword) { memailPassword = emailPassword; }
    private static String msmtpServer = "";
    public static String getSmtpServer() { return msmtpServer; }
    public static void setSmtpServer(String smtpServer) { msmtpServer = smtpServer; }
    private static int msmtpPort = 0;
    public static int getSmtpPort() { return msmtpPort; }
    public static void setSmtpPort(int smtpPort) { msmtpPort = smtpPort; }
    private static int msmtpSSL = -1;
    public static int getSmtpSSL() { return msmtpSSL; }
    public static void setSmtpSSL(int smtpSSL) { msmtpSSL = smtpSSL; }
    private static String mimapServer = "";
    public static String getImapServer() { return mimapServer; }
    public static void setImapServer(String imapServer) { mimapServer = imapServer; }
    private static int mimapPort = 0;
    public static int getImapPort() { return mimapPort; }
    public static void setImapPort(int imapPort) { mimapPort = imapPort; }
    private static int mimapSSL = -1;
    public static int getImapSSL() { return mimapSSL; }
    public static void setImapSSL(int imapSSL) { mimapSSL = imapSSL; }
    private static boolean mkeepSignalServiceRunning = false;
    public static boolean getKeepSignalServiceRunning() { return mkeepSignalServiceRunning; }
    public static void setKeepSignalServiceRunning(boolean keepSignalServiceRunning) { mkeepSignalServiceRunning = keepSignalServiceRunning; }

    public static boolean msignalServiceStarted = false;    // this is not a part of setting. This is simply a state flag.

    private static boolean mserviceStartActivity = true;
    public static boolean getServiceStartActivity() { return mserviceStartActivity; }
    public static void setServiceStartActivity(boolean serviceStartActivity) { mserviceStartActivity = serviceStartActivity; }
    private static boolean mserviceStartDestAddr = false;
    public static boolean getServiceStartDestAddr() { return mserviceStartDestAddr; }
    public static void setServiceStartDestAddr(boolean serviceStartDestAddr) { mserviceStartDestAddr = serviceStartDestAddr; }

    public static boolean isLocalAddrPasswdEmpty() {
        return mlocalAddress.trim().length() == 0 && memailPassword.trim().length() == 0;
    }

    static {
        // read calculator_settings when application initialized.
        readSettings();
    }

    public EmailSignalChannelAgent emailSignalChannelAgent = null;

    public RtcAppClient(boolean startMMediaRtc) {
        dataClient0 = new DataRtcAgent(0, startMMediaRtc);
        dataClient1 = new DataRtcAgent(1, startMMediaRtc);
        emailSignalChannelAgent = new EmailSignalChannelAgent(mlocalEmailType, mgmailToken, mlocalAddress, memailPassword,
                                                msmtpServer, msmtpPort, msmtpSSL, mimapServer, mimapPort, mimapSSL,
                                                new DataRtcAgent[] {dataClient0, dataClient1});
    }

    public static boolean readSettings()	{
        //Read preferences
        SharedPreferences settings = MFPAndroidLib.getContext().getSharedPreferences(CONNECT_SETTINGS, 0);
        if (settings != null) {
            mlocalEmailType = settings.getInt(LOCAL_EMAIL_TYPE, mlocalEmailType);
            mgmailToken = settings.getString(GMAIL_TOKEN, mgmailToken);
            mlocalAddress = settings.getString(EMAIL_ADDRESS, mlocalAddress);
            memailPassword = settings.getString(EMAIL_PASSWORD, memailPassword);
            msmtpServer = settings.getString(SMTP_SERVER, msmtpServer);
            msmtpPort = settings.getInt(SMTP_PORT, msmtpPort);
            msmtpSSL = settings.getInt(SMTP_SSL, msmtpSSL);
            mimapServer = settings.getString(IMAP_SERVER, mimapServer);
            mimapPort = settings.getInt(IMAP_PORT, mimapPort);
            mimapSSL = settings.getInt(IMAP_SSL, mimapSSL);
            mkeepSignalServiceRunning = settings.getBoolean(KEEP_SIGNAL_SERVICE_RUNNING, mkeepSignalServiceRunning);
            mserviceStartActivity = settings.getBoolean(SERVICE_START_ACTIVITY, mserviceStartActivity);
            mserviceStartDestAddr = settings.getBoolean(SERVICE_START_DEST_ADDR, mserviceStartDestAddr);
            return true;
        } else	{
            return false;
        }
    }

    public static boolean saveSettings()	{
        //Save preferences
        SharedPreferences settings = MFPAndroidLib.getContext().getSharedPreferences(CONNECT_SETTINGS, 0);
        if (settings != null) {
			/*
			 * Note: we cannot write like
			 * calculator_settings.edit().putInt(BITS_OF_PRECISION, nBitsofPrecision);
			 * calculator_settings.edit().putInt(NUMBER_OF_RECORDS, nNumberofRecords);
			 * calculator_settings.edit().commit();
			 * because calculator_settings.edit() returns different editor each time.
			 */
            settings.edit().putInt(LOCAL_EMAIL_TYPE, mlocalEmailType)
                    .putString(EMAIL_ADDRESS, mlocalAddress)
                    .putString(EMAIL_PASSWORD, memailPassword)
                    .putString(SMTP_SERVER, msmtpServer)
                    .putInt(SMTP_PORT, msmtpPort)
                    .putInt(SMTP_SSL, msmtpSSL)
                    .putString(IMAP_SERVER, mimapServer)
                    .putInt(IMAP_PORT, mimapPort)
                    .putInt(IMAP_SSL, mimapSSL)
                    .putBoolean(KEEP_SIGNAL_SERVICE_RUNNING, mkeepSignalServiceRunning)
                    .putBoolean(SERVICE_START_ACTIVITY, mserviceStartActivity)
                    .putBoolean(SERVICE_START_DEST_ADDR, mserviceStartDestAddr)
                    .commit();

            return true;
        }
        return false;
    }

    public void setRtcListener(RtcListener rtcListener, MmediaPeerConnectionParams params) {
        RtcAgent.setRtcListener(rtcListener, params);
        if (RtcAgent.factoryDC == null) {
            // we only initialize factory once
            RtcAgent.factoryDC = RtcAgent.initWebRtcFactory(null);
        }
    }

    // this function should only be called in UI thread so that it is self-synchronized.
    // And this function is called in an initialized RtcAppClient
    // This means emailSignalChannelAgent cannot be null.
    public void startSignalService(Context context, Boolean stopEmailAgent1st, Boolean stopDCAgent1st, Boolean enableDbg) {
        // stop old SignalChannelAgent
        if (stopEmailAgent1st) {
            emailSignalChannelAgent.stop();
        }
        // we do not create a new emailSignalChannelAgent because if we create a new emailSignalChannelAgent,
        // message coming in may be directed to the old emailSignalChannelAgent and get exception there since
        // the old emailSignalChannelAgent has been stopped
        emailSignalChannelAgent.setEmailSignalChannelAgent(mlocalEmailType, mgmailToken, mlocalAddress, memailPassword,
                msmtpServer, msmtpPort, msmtpSSL, mimapServer, mimapPort, mimapSSL/*, new DataRtcAgent[] {dataClient0, dataClient1}*/);
        emailSignalChannelAgent.start(context, enableDbg);
        msignalServiceStarted = true;
    }

    public void stopSignalService() {
        msignalServiceStarted = false;
        emailSignalChannelAgent.doKillService();
    }

    public void onPause() {
        if(dataClient0 != null) {
            dataClient0.onPause();
        }
        if(dataClient1 != null) {
            dataClient1.onPause();
        }
    }

    public void onResume() {
        if(dataClient0 != null) {
            dataClient0.onResume();
        }
        if(dataClient1 != null) {
            dataClient1.onResume();
        }
    }

    public void onDestroy() {
        if(dataClient0 != null) {
            dataClient0.onDestroy(false);
        }
        if(dataClient1 != null) {
            dataClient1.onDestroy(true);
        }
    }
}
