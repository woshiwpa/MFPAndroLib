package com.cyzapps.AdvRtc;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.cyzapps.mfpanlib.MFPAndroidLib;
import com.cyzapps.mfpanlib.R;
import com.cyzapps.EmailService.EmailSignalService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * Created by tony on 23/12/2017.
 */

public class EmailSignalChannelAgent {
    private final static String TAG = "New_AdvRtcapp_Debug";
    public static final String MSGTITLEPREFIX = "#--AnMath==$:000086:"; // prefix followed by 6 digit version code.

    public String acceptedRemoteAddress = ""; // This is the most recent accepted connection's remote address. Because it is a string reference, assignment is atomic

    private int localEmailType = RtcAppClient.USE_SMTP_IMAP;
    private String gmailToken = "";
    private String localAddress = ""; // remote addresses are saved within mapAllMsgs
    private String emailPassword = "";
    private String smtpServer = "";
    private int smtpPort = 0;
    private int smtpSSL = -1;
    private String imapServer = "";
    private int imapPort = 0;
    private int imapSSL = -1;

    private Context context = null;   // the activity associated with EmailSignalChannelAgent

    // only if it is activated we can send and fetch emails. Note that this flag is only useful for
    // ActivityTestConnection because this flag is always set when the service is started but we can
    // not guarantee it is disabled when the service is stopped.
    private AtomicBoolean isBound2Service = new AtomicBoolean();

    List<DataRtcAgent> listRtcAgents = new ArrayList<>();

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "EmailSignalChannelAgent IncomingHandler.handleMessage : msg what is " + msg.what);
            switch (msg.what) {
                case EmailSignalService.MSG_INCOMING_MESSAGE:
                    final long tsSent = msg.getData().getLong("sentTimestamp");
                    final long tsRecv = msg.getData().getLong("receivedTimestamp");
                    final String remoteAddress = msg.getData().getString("remoteAddress");
                    final int agentId = msg.getData().getInt("agentId");
                    final String event = msg.getData().getString("event");
                    final String body = msg.getData().getString("body");
                    final boolean isCtrl = msg.getData().getBoolean("isControlMsg");

                    for (DataRtcAgent dra : listRtcAgents) {
                        if (isCtrl) {   // control message.
                            if ((event.equals(EmailSignalService.CTRL_MSG_SEND_EMAIL) && agentId == dra.mAgentId)
                                    || (event.equals(EmailSignalService.CTRL_MSG_FAIL_TO_SEND) && agentId == dra.mAgentId)
                                    || ((event.equals(EmailSignalService.CTRL_MSG_RECV_EMAIL) || event.equals(EmailSignalService.CTRL_MSG_RECV_STALE_EMAIL))
                                        && agentId != dra.mAgentId)
                                    || event.equals(EmailSignalService.CTRL_MSG_FAIL_TO_FETCH)) {
                                // not related to Peer's internal operations, no need to use RtcAgent.Execute thread.
                                Log.d(TAG, "EmailSignalChannelAgent.fetchEMails : Ctrl msg, event is " + event + ", body is " + body);
                                // only if it is a control msg we show state or error info
                                Date recvDate = new Date(tsRecv);
                                SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                                String dateStr = format.format(recvDate);
                                String stateInfo = dateStr + " : " + event + ", title is " + body + ", remote is " + remoteAddress;
                                if (RtcAgent.mListener != null) {
                                    if (event.equals(EmailSignalService.CTRL_MSG_SEND_EMAIL)
                                            || event.equals(EmailSignalService.CTRL_MSG_RECV_EMAIL)) {
                                        RtcAgent.mListener.onLogInfo(stateInfo, MFPAndroidLib.EMAIL_SEND_RECV_STATE_INFO);
                                    } else if (event.equals(EmailSignalService.CTRL_MSG_FAIL_TO_SEND)) {
                                        RtcAgent.mListener.onLogInfo(stateInfo, MFPAndroidLib.EMAIL_SEND_ERROR_INFO);
                                    } else if (event.equals(EmailSignalService.CTRL_MSG_RECV_STALE_EMAIL)) {
                                        RtcAgent.mListener.onLogInfo(stateInfo, MFPAndroidLib.EMAIL_FETCH_STALE_INFO);
                                    } else {
                                        RtcAgent.mListener.onLogInfo(stateInfo, MFPAndroidLib.EMAIL_FETCH_ERROR_INFO);
                                    }
                                }
                            }
                        } else if (agentId >= 0 && agentId != dra.mAgentId) {  // agentId could be -1, which means initial activation and not handled here.
                            // this is the agent whose AgentId should not be equal to the sender's agent ID.
                            final DataRtcAgent thisDRA = dra;
                            // according to WebRTC doc:
                            // https://webrtc.github.io/webrtc-org/native-code/native-apis/
                            // all the API calls have been mapped to the  signal thread so
                            // that no need to call RtcAgent.EXECUTOR.execute(rRtc);
                            try {
                                Log.d(TAG, "EmailSignalChannelAgent.fetchEMails : DataRtcAgent " + thisDRA.mAgentId + " starts to process received msg : event is " + event + ", body is " + body);
                                JSONObject obj = new JSONObject(body);
                                if (event.equals("signal")) {
                                    String type = "";
                                    if (obj.has("type")) {
                                        type = obj.getString("type");
                                    }
                                    if (type.equals("restart")) {
                                        thisDRA.mmsgHandler.onRestartEvent(remoteAddress, obj);
                                    } else if (type.equals("leave")) {
                                        thisDRA.mmsgHandler.onLeaveEvent(remoteAddress, obj);
                                    } else  {
                                        if (type.equals("offer")) {
                                            // this is the incoming call. We need to clear the notification.
                                            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                            notificationManager.cancel(R.string.there_is_an_incoming_call + remoteAddress.hashCode());
                                        }
                                        thisDRA.mmsgHandler.onMessageEvent(remoteAddress, obj);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    Messenger mService = null;
    Messenger mMessenger = null;    // mMessenger is initialized from new IncomingHandler(). new IncomingHandler() must be called within a UI thread.

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mMessenger = new Messenger(new IncomingHandler());
            isBound2Service.set(true);  // the service has been bound.
            // send register client message only after ActivityTestConnect fully started (i.e. factory is no longer null)
            // so do it (send reister client message) in ActivityTestConnect.
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is only called when the connection with the service has been unexpectedly disconnected - process crashed.
            isBound2Service.set(false);  // the service has been unbound.
            mService = null;
            mMessenger = null;
        }
    };

    public boolean registerClient() {
        try {
            Message msg = Message.obtain(null, EmailSignalService.MSG_REGISTER_CLIENT);
            msg.replyTo = mMessenger;
            mService.send(msg);
            return true;
        }
        catch (RemoteException e) {
            // In this case the service has crashed before we could even do anything with it
            return false;
        }

    }

    public EmailSignalChannelAgent(int localType, String gmailTokenStr, String localAddr, String emailPwd,
                                   String smtpSvr, int smtpPt, int smtpSSLMode, String imapSvr, int imapPt, int imapSSLMode,
                                   DataRtcAgent[] dataRtcAgents) {
        localEmailType = localType;
        gmailToken = gmailTokenStr;
        localAddress = localAddr;
        emailPassword = emailPwd;
        smtpServer = smtpSvr;
        smtpPort = smtpPt;
        smtpSSL = smtpSSLMode;
        imapServer = imapSvr;
        imapPort = imapPt;
        imapSSL = imapSSLMode;

        listRtcAgents.addAll(Arrays.asList(dataRtcAgents));
        for (DataRtcAgent dataRtcAgent : dataRtcAgents) {
            dataRtcAgent.memailSignalChannelAgent = this;
        }
    }

    public void setEmailSignalChannelAgent(int localType, String gmailTokenStr, String localAddr, String emailPwd,
                                           String smtpSvr, int smtpPt, int smtpSSLSetting, String imapSvr, int imapPt, int imapSSLSetting/*,
                                           DataRtcAgent[] dataRtcAgents*/) {
        localEmailType = localType;
        gmailToken = gmailTokenStr;
        localAddress = localAddr;
        emailPassword = emailPwd;
        smtpServer = smtpSvr;
        smtpPort = smtpPt;
        smtpSSL = smtpSSLSetting;
        imapServer = imapSvr;
        imapPort = imapPt;
        imapSSL = imapSSLSetting;

        /*  // dataRtcAgent 0 and 1 will not change so no need to reset them.
        listRtcAgents.clear();
        listRtcAgents.addAll(Arrays.asList(dataRtcAgents));
        for (DataRtcAgent dataRtcAgent : dataRtcAgents) {
            dataRtcAgent.msignalChannelAgent = this;
        }*/
    }

    public void start(Context ctx, int debugLevel) {
        // this is actually restart
        Log.d(TAG, "EmailSignalChannelAgent.start : Email Signal Channel will be started with debugLevel " + debugLevel + ". An email will be sent to local i.e. " + localAddress + " if the service is recreated.");
        context = ctx;
        if (context != null) {
            // this EmailSignalChannelAgent is bound to an activity.
            // start service first if not running.
            Intent intent = new Intent(context, EmailSignalService.class);
            Bundle b = new Bundle();
            b.putInt("localEmailType", localEmailType);
            b.putString("gmailToken", gmailToken);
            b.putString("localAddress", localAddress);
            b.putString("emailPassword", emailPassword);
            b.putString("smtpServer", smtpServer);
            b.putInt("smtpPort", smtpPort);
            b.putInt("smtpSSL", smtpSSL);
            b.putString("imapServer", imapServer);
            b.putInt("imapPort", imapPort);
            b.putInt("imapSSL", imapSSL);
            b.putBoolean("serviceStartActivity", RtcAppClient.getServiceStartActivity());
            b.putInt("debugLevel", debugLevel);
            intent.putExtras(b);
            doBindService(context, intent);
        }
    }

    public Boolean isStarted() {
        return  isBound2Service.get();
    }

    public void stop() {
        Log.d(TAG, "EmailSignalChannelAgent.stop : will be unbound from service.");
        doUnbindService(context);
        context = null;
        mService = null;
    }

    public void doKillService() {
        if (isBound2Service.get()) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, EmailSignalService.MSG_KILL_SERVICE);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            context.unbindService(mConnection);
            isBound2Service.set(false);  // the service has been unbound.
        }
    }

    void doBindService(Context context, Intent intent) { // called in UI thread
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void doUnbindService(Context context) {    // called in UI thread
        if (isBound2Service.get()) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, EmailSignalService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            context.unbindService(mConnection); // this seems will make service call onDestroy
            isBound2Service.set(false);  // the service has been unbound.
        }
    }

    public Boolean send(int agentId, String destAddress, String event, int sessionId, String type, JSONObject payLoad) {
        JSONObject message = new JSONObject();
        try {
            message.put("sessionId", sessionId);
            message.put("type", type);
            message.put("payload", payLoad);
        } catch (JSONException e) {
            e.printStackTrace();    // will not be here.
        }

        if (!isBound2Service.get()) {
            Log.d(TAG, "EmailSignalChannelAgent.send : Email Signal Channel at " + localAddress + " has been stopped, no message can be sent.");
            return false;
        }
        try {
            //Send message with a bundle of parameters.
            Bundle b = new Bundle();
            b.putString("remoteAddress", destAddress);
            b.putInt("agentId", agentId);
            b.putString("event", event);
            b.putString("body", message.toString());
            Message msg = Message.obtain(null, EmailSignalService.MSG_OUTGOING_MESSAGE);
            msg.setData(b);
            msg.replyTo = mMessenger;
            mService.send(msg);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean isDebugEnabled() {
        return false;
    }

    public void enableDebug(Boolean enableDbg) {

    }
}
