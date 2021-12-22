package com.cyzapps.EmailService;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.util.Log;

import com.cyzapps.AdvRtc.RtcAppClient;
import com.cyzapps.mfpanlib.R;
import com.cyzapps.mfpanlib.MFPAndroidLib;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tony on 29/04/2018.
 */

public class EmailSignalService extends Service {

    private final static String TAG = "New_AdvRtcapp_Debug";

    // if an email was sent 180 seconds ago, it is stale. Originally use 60 seconds because
    // we do not delete fetched email and intervals between fetch email can be as large as 38s.
    // However, during peak hours some emails may see a very large delay and now the fetch function
    // includes more codes and takes longer time to run.
    public static final int STALETHRESHHOLD = 180000;
    public static int StaleThreshHold = STALETHRESHHOLD;

    public static final String CTRL_MSG_FAIL_TO_FETCH = "fail to fetch email";
    public static final String CTRL_MSG_FAIL_TO_SEND = "fail to send email";
    public static final String CTRL_MSG_RECV_EMAIL = "recv email";
    public static final String CTRL_MSG_RECV_STALE_EMAIL = "recv stale email";
    public static final String CTRL_MSG_SEND_EMAIL = "send email";

    private PowerManager.WakeLock wl = null;
    private Boolean serviceStartActivity = false;
    private Boolean onCreateCalled = false;
    private NotificationManager nm;
    private String notificationChannelId = "";
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_INCOMING_MESSAGE = 3;
    public static final int MSG_OUTGOING_MESSAGE = 4;
    public static final int MSG_KILL_SERVICE = 5;

    public static final String GOOGLE_MAIL_ACCOUNT_TYPE = "com.google";

    final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.
    // Keeps track of all current registered clients. But note that only the first client is active.
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();

    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "EmailSignalService IncomingHandler.handleMessage : msg what is " + msg.what);
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.remove(msg.replyTo);   // remove the msg.replyTo if it is in the mClients list.
                    mClients.add(0, msg.replyTo);   // add it to the front. Now it is the active client.
                    sendMessageToApp(new Date());   // use now as reference time to identify stale messages.
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_OUTGOING_MESSAGE:
                    //int agentId, String destAddress, String event, String messageStr
                    int agentId = msg.getData().getInt("agentId");
                    String destAddress = msg.getData().getString("remoteAddress");
                    String event = msg.getData().getString("event");
                    String messageStr = msg.getData().getString("body");
                    if (localAddress.equals(destAddress)) {
                        // send to itself, we do not pass the message to email.
                        Date currentTime = Calendar.getInstance().getTime();
                        ReceivedMsg msgRecv = new ReceivedMsg(currentTime, currentTime, destAddress, agentId, event, messageStr, false);
                        allRecvMsgs.msgsReceived.add(msgRecv);  // add the message in the queue. note that the message could be disordered.
                    } else {
                        // different destination from local
                        send(agentId, destAddress, event, messageStr);
                    }
                    break;
                case MSG_KILL_SERVICE:
                    stopForeground(true);
                    stopSelf();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onCreate() {
        // The service is being created
        Log.d(TAG, "EmailSignalService.onCreate");
        super.onCreate();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EmailSignalService:KeepCPUOn");
        wl.acquire();   //  lock screen will not stop the service.
        onCreateCalled = true;

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.service_started);
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MFPAndroidLib.getContext().getClass()), 0);
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannelId = createChannel();
            // Set the icon, scrolling text and timestamp
            builder = new Notification.Builder(this, notificationChannelId)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle(text) // Set the info for the views that show in the notification panel.
                    .setWhen(System.currentTimeMillis());
        } else {
            // Set the icon, scrolling text and timestamp
            builder = new Notification.Builder(this)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle(text) // Set the info for the views that show in the notification panel.
                    .setWhen(System.currentTimeMillis());
        }
        Notification notification = builder.build();

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        startForeground(R.string.service_started, notification);
    }

    private void onServiceStartOrBind(Intent intent) {
        // first of all, let's obtain the parameters from intent
        boolean bParametersChanged = false;
        Bundle b = intent.getExtras();
        if (b != null) {
            // note that strings (except gmail token) have been trimmed already.
            if (localEmailType != b.getInt("localEmailType", RtcAppClient.USE_SMTP_IMAP)) {
                bParametersChanged = true;
            } else if (!gmailToken.equals(b.getString("gmailToken", ""))) {
                bParametersChanged = true;
            } else if (!localAddress.equals(b.getString("localAddress", ""))) {
                bParametersChanged = true;
            } else if (!emailPassword.equals(b.getString("emailPassword", ""))) {
                bParametersChanged = true;
            } else if (!smtpServerAddr.equals(b.getString("smtpServer", ""))) {
                bParametersChanged = true;
            } else if (smtpServerPort != b.getInt("smtpPort", 0)) {
                bParametersChanged = true;
            } else if (smtpSSL != b.getInt("smtpSSL", -1)) {
                bParametersChanged = true;
            } else if (!imapServerAddr.equals(b.getString("imapServer", ""))) {
                bParametersChanged = true;
            } else if (imapServerPort != b.getInt("imapPort", 0)) {
                bParametersChanged = true;
            } else if (imapSSL != b.getInt("imapSSL", 0)) {
                bParametersChanged = true;
            }
            serviceStartActivity = b.getBoolean("serviceStartActivity", false); // service start activty has to be set here otherwise it may not be set.
        }

        if (!bParametersChanged && !onCreateCalled) {
            // exit if parameters do not change and onCreate was not called
            return;
        } else {
            if (bParametersChanged) {
                // stop the service first coz we may update its parameters.
                stop();
            }
            onCreateCalled = true;
            if (b != null) {
                localEmailType = b.getInt("localEmailType", RtcAppClient.USE_SMTP_IMAP);
                gmailToken = b.getString("gmailToken", "");
                if (localEmailType == RtcAppClient.USE_GMAIL_API) {
                    // now use gmail API to send email.
                    GoogleCredential credential = new GoogleCredential().setAccessToken(gmailToken);
                    JsonFactory jsonFactory = new JacksonFactory();
                    HttpTransport httpTransport = new NetHttpTransport();

                    gmailService = new Gmail.Builder(httpTransport, jsonFactory, credential)
                            .setApplicationName(MFPAndroidLib.getContext().getResources().getString(R.string.app_name)).build();
                } else {
                    gmailService = null;
                }
                localAddress = b.getString("localAddress", "");
                emailPassword = b.getString("emailPassword", "");
                smtpServerAddr = b.getString("smtpServer", "");
                smtpServerPort = b.getInt("smtpPort", 0);
                smtpSSL = b.getInt("smtpSSL", -1);
                imapServerAddr = b.getString("imapServer", "");
                imapServerPort = b.getInt("imapPort", 0);
                imapSSL = b.getInt("imapSSL", -1);
            }
            if (localEmailType == RtcAppClient.USE_GMAIL_API) {
                Log.d(TAG, "EmailSignalService.onServiceStartOrBind : localAddr : " + localAddress + " token : " + gmailToken);
            } else {
                Log.d(TAG, "EmailSignalService.onServiceStartOrBind : localAddr : " + localAddress + " emailPassword : " + emailPassword
                        + " smtpServerAddr : " + smtpServerAddr + " smtpServerPort : " + smtpServerPort + " smtpSSL : " + smtpSSL + " imapServerAddr : " + imapServerAddr
                        + " imapServerPort : " + imapServerPort + " imapSSL : " + imapSSL + " serviceStartActivity : " + serviceStartActivity);
            }
            // restart it.
            start(false);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private synchronized String createChannel() {
        String name = "EmailSignalService";
        int importance = NotificationManager.IMPORTANCE_LOW;

        String id = "com.cyzapps.advremote.EmailSignalService";
        NotificationChannel channel = new NotificationChannel(id, name, importance);

        channel.enableLights(true);
        channel.setLightColor(Color.BLUE);
        if (nm != null) {
            nm.createNotificationChannel(channel);
        } else {
            stopSelf();
        }
        return id;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {  // if there is no startService called, this function will not be called.
        Log.d(TAG, "EmailSignalService.onStartCommand");
        // The service is starting, due to a call to startService()
        onServiceStartOrBind(intent);
        return START_STICKY; // run until explicitly stopped.
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "EmailSignalService.onBind");
        onServiceStartOrBind(intent);
        return mMessenger.getBinder();
    }

    // note that we still need to remove stale messages in this function because this function may
    // be called when we bind the service to a new client. So it is possible that when a message is
    // added into allRecvMsgs.msgsReceived it is still fresh. But when sendMessageToApp it is stale.
    // so if the refTime is not null, we remove stale messages, otherwise, we do not.
    private void sendMessageToApp(Date refTime) {
        ReceivedMsg thisRecvMsg = null;
        while (refTime != null && (thisRecvMsg = allRecvMsgs.msgsReceived.peek()) != null) {
            if (thisRecvMsg.sentTimestamp < refTime.getTime() - StaleThreshHold) {
                Date recvDate = new Date(thisRecvMsg.recvTimestamp);
                Log.d(TAG, "sendMessageToApp removes stale message "
                        + ", message received time: " + recvDate.toString()
                        + ", message title : " + thisRecvMsg.event);
                allRecvMsgs.msgsReceived.remove();  // remove staled msg.
            } else {
                // ok, seems that from now beyond the messages are fresh (but actually the messages
                // could be disordered so that some messages can still be stale)..
                break;
            }
        }
        while ((thisRecvMsg = allRecvMsgs.msgsReceived.peek()) != null) {
            if (refTime != null && thisRecvMsg.sentTimestamp < refTime.getTime() - StaleThreshHold) {
                Date recvDate = new Date(thisRecvMsg.recvTimestamp);
                Log.d(TAG, "sendMessageToApp removes stale message "
                        + ", message received time: " + recvDate.toString()
                        + ", message title : " + thisRecvMsg.event);
                allRecvMsgs.msgsReceived.remove();  // remove staled msg.
                continue;
            }
            Log.d(TAG, "EmailSignalService.sendMessageToApp client size is " + mClients.size()
                    + ", message received time : " + thisRecvMsg.recvTimestamp
                    + ", agentId : " + thisRecvMsg.agentId
                    + ", message is control : " + thisRecvMsg.isControlMsg
                    + ", message title : " + thisRecvMsg.event
                    + ", message body :\n" + thisRecvMsg.body);
            if (mClients.size() > 0) {
                // consume the message only if we have clients, and only send message to the first, i.e. active, client.
                try {
                    //Send message with a bundle of two strings.
                    Bundle b = new Bundle();
                    b.putLong("sentTimestamp", thisRecvMsg.sentTimestamp);
                    b.putLong("receivedTimestamp", thisRecvMsg.recvTimestamp);
                    b.putString("remoteAddress", thisRecvMsg.remoteAddress);
                    b.putInt("agentId", thisRecvMsg.agentId);
                    b.putString("event", thisRecvMsg.event);
                    b.putString("body", thisRecvMsg.body);
                    b.putBoolean("isControlMsg", thisRecvMsg.isControlMsg);
                    Message msg = Message.obtain(null, MSG_INCOMING_MESSAGE);
                    msg.setData(b);
                    mClients.get(0).send(msg);
                    allRecvMsgs.msgsReceived.remove();  // remove consumed msg.
                }
                catch (RemoteException e) {
                    // The client is dead. Remove it from the list;
                    Log.d(TAG, "EmailSignalService.sendMessageToApp remoteException, client removed");
                    mClients.remove(0);
                    break;  // do not send any more message.
                }
            } else {
                break;  // no client is ready, so quit.
            }
        }
    }

    // do not implement OnUnbind (using default) so that OnRebind is not needed.

    @Override
    public void onDestroy() {
        Log.d(TAG, "EmailSignalService.onDestroy");
        // The service is no longer used and is being destroyed
        stop(); // stop email service first.
        stopForeground(true);
        nm.cancel(R.string.service_started); // Cancel the persistent notification.
        if (wl != null) {
            wl.release();   // now lock screen will stop CPU and the service.
        }
        super.onDestroy();

        // cannot figure out a way to auto restart service
    }

    public static final String MAILSTORETYPE = "imaps";

    private int servicePID = -1;
    private String serviceCallerName = "";

    private int localEmailType = RtcAppClient.USE_SMTP_IMAP;
    private String gmailToken = "";
    private Gmail gmailService = null;
    private String localAddress = ""; // remote addresses are saved within mapAllMsgs
    private String emailPassword = "";
    private String smtpServerAddr = "";
    private int smtpServerPort = 0;
    private int smtpSSL = -1;
    private String imapServerAddr = "";
    private int imapServerPort = 0;
    private int imapSSL = -1;

    private AtomicBoolean isActivated = new AtomicBoolean();  // only if it is activated we can send and fetch emails
    Boolean enableDbg = false;
    public ExecutorService executor = Executors.newSingleThreadExecutor();

    SortedSet<FetchEmail.MsgEvent> sortedMsgEvents = new TreeSet<FetchEmail.MsgEvent>();  // all the signal channel agent share same listmsgevents
    Date lastFetchDate = new Date();

    public static String toCodedTitle(String originalTitle) {
        if (originalTitle == null) {
            return null;
        } else if (originalTitle.equals("activate")) {
            return "a";
        } else if (originalTitle.equals("signal")) {
            return "s";
        } else if (originalTitle.equals("leave")) {
            return "l";
        } else if (originalTitle.equals("restart")) {
            return "r";
        } else {
            return originalTitle;
        }
    }

    public static String toOriginalTitle(String codedTitle) {
        if (codedTitle == null) {
            return null;
        } else if (codedTitle.equals("a")) {
            return "activate";
        } else if (codedTitle.equals("s")) {
            return "signal";
        } else if (codedTitle.equals("l")) {
            return "leave";
        } else if (codedTitle.equals("r")) {
            return "restart";
        } else {
            return codedTitle;
        }
    }

    public static String encodeBody(String body) {
        String encodedBody = "";
        for (int idx = 0; idx < body.length(); idx ++) {
            if (body.charAt(idx) >= (char)(32) && body.charAt(idx) <= (char)(126)) {
                encodedBody += (char)(126 + 32 - (int)(body.charAt(idx)));
            } else {
                encodedBody += body.charAt(idx);
            }
        }
        return encodedBody;
    }

    public static String decodeBody(String encodedBody) {
        return encodeBody(encodedBody);
    }

    public class ReceivedMsg {
        public long sentTimestamp = -1;
        public long recvTimestamp = -1;
        public String remoteAddress = "";
        public int agentId = -1;
        public String event = null;
        public String body = null;
        public boolean isControlMsg = false;
        public ReceivedMsg(Date sentTime, Date rcvTime, String remoteAddr, int aId, String evnt, String msg, boolean isControl) {
            sentTimestamp = sentTime.getTime();
            recvTimestamp = rcvTime.getTime();
            remoteAddress = remoteAddr;
            agentId = aId;
            event = evnt;
            body = msg;
            isControlMsg = isControl;
        }
    }

    public class ReceivedMsgList {
        public Queue<ReceivedMsg> msgsReceived = new ConcurrentLinkedQueue<ReceivedMsg>();
    }

    public ReceivedMsgList allRecvMsgs = new ReceivedMsgList();
    public void fetchEmails() {
        Log.d(TAG, "EmailSignalService start to fetch from " + localAddress);
        String recvHost = imapServerAddr;
        int recvPort = imapServerPort;
        int ssl = imapSSL;

        // we do not clear listMsgEvents before call fetch because, it is possible, that fetch was not
        // able to delete received message(s) last time. And as a result, this time fetch receive the
        // message again. If the message hasn't been expired, we need a mechanism to exclude the duplicates.
        // so we keep last fetched messages until they expire as such we are able to compare them with
        // emails from this fetch to see if they have been processed or not.
        Date now = new Date();
        SortedSet<FetchEmail.MsgEvent> toRemoveMsgEvents = new TreeSet<FetchEmail.MsgEvent>();
        Iterator it = sortedMsgEvents.iterator();
        while (it.hasNext()) {
            FetchEmail.MsgEvent me = (FetchEmail.MsgEvent) it.next();
            if (me.sentTime.getTime() < now.getTime() - StaleThreshHold) { // if stale, delete it.
                Log.d(TAG, "FetchEmails remove (" + me.hashCode()
                        + "), time: " + me.receiveTime
                        + ", title : " + me.title);
                toRemoveMsgEvents.add(me);
            }
        }
        sortedMsgEvents.removeAll(toRemoveMsgEvents);
        FetchEmail.FetchReturnInfo fetchReturnInfo;
        if (localEmailType == RtcAppClient.USE_GMAIL_API) {
            fetchReturnInfo = FetchEmail.fetch(gmailService, lastFetchDate, sortedMsgEvents, true, true);
            if (fetchReturnInfo.err instanceof com.google.api.client.googleapis.json.GoogleJsonResponseException) {
                // This means token is no longer valid.
                if (refreshAccessToken()) {
                    // token and gmail service has been refreshed. Now fetch emails again.
                    fetchReturnInfo = FetchEmail.fetch(gmailService, lastFetchDate, sortedMsgEvents, true, true);
                }
            }
        } else {
            fetchReturnInfo = FetchEmail.fetch(recvHost, recvPort, ssl, MAILSTORETYPE, localAddress, emailPassword, lastFetchDate, sortedMsgEvents, true, false);
        }
        lastFetchDate = fetchReturnInfo.dateFetched;
        Exception err = fetchReturnInfo.err;
        if (err != null) {
            // fail to receive. Note that for control message, the sent date is the same as received date.
            ReceivedMsg msgCtrl = new ReceivedMsg(lastFetchDate, lastFetchDate, localAddress, -1,
                    CTRL_MSG_FAIL_TO_FETCH, err.getMessage(), true); // we dont have agent Id here.
            allRecvMsgs.msgsReceived.add(msgCtrl);  // add the message in the queue. note that the message could be disordered.
        }
        it = sortedMsgEvents.iterator();
        while (it.hasNext()) {
            FetchEmail.MsgEvent me = (FetchEmail.MsgEvent) it.next();
            Log.d(TAG, "EmailSignalService.fetchEMails : get email from remote, sent time is " + me.sentTime + ", recv time is " + me.receiveTime + ", title is " + me.title + " ; body is " + me.body);
            if (me.processed) {
                // if it is processed
                continue;
            }
            me.processed = true;
            /* // allow send message to itself.
            if (me.from.equals(localAddress)) {
                // it is sent by myself. so skip it.
                continue;
            } */
            Date sentDate = me.sentTime; // the staled msg will be deleted based on sent time..
            Date receivedDate = me.receiveTime; // don't use send time coz the msgs will be queued based on receive time.
            // assuming title has been squeezed, which means no space in the text.
            String[] titleSplitParts = me.title.split(":");
            LinkedList<String> agentIdEventsList = new LinkedList<String>();
            for (int titlePartIdx = 0; titlePartIdx < titleSplitParts.length; titlePartIdx ++) {
                if (titleSplitParts[titlePartIdx].contains(")")) {
                    String[] subParts = titleSplitParts[titlePartIdx].split("\\)");
                    if (subParts.length == 2 && subParts[0].startsWith("(") && subParts[1].length() > 0) {
                        int repeatTimes = 0;
                        try {
                            repeatTimes = Integer.parseInt(subParts[1]);
                        } catch(NumberFormatException e) {
                            // repeatTimes = 0;
                        }
                        for (int idx1 = 0; idx1 < repeatTimes; idx1 ++) {
                            agentIdEventsList.add(subParts[0].substring(1));
                        }
                    } else if (subParts[0].startsWith("(")) {
                        agentIdEventsList.add(subParts[0].substring(1));
                    } else if (subParts[0].length() > 0) { // subParts[0] could be an empty string so we add a condition here.
                        agentIdEventsList.add(subParts[0]);
                    }
                }
            }
            String[] agentIdEvents = agentIdEventsList.toArray(new String[0]);  //was me.title.split(":"); but I have changed title format.
            String[] infos = me.body.split("\n");
            final String from = me.from;
            if (agentIdEvents.length != infos.length) {
                // should continue?
                Log.d(TAG, "EmailSignalService.fetchEMails : Fetch get weird message title doesn't match body, title is " + me.title + " ; body is " + me.body);
                throw new RuntimeException("Number of events doesn't match number of messages!");
            }

            if (agentIdEvents.length > 0) {
                int ctrlMsgAgentId = -1;
                for (int idx = 0; idx < agentIdEvents.length; ++idx) {
                    String[] titleParts = agentIdEvents[idx].split("->");
                    if (titleParts.length < 2) {
                        continue; // not a valid message.
                    }
                    try {
                        ctrlMsgAgentId = Integer.parseInt(titleParts[0]);   // use the first title part's id as ctrl msg agent id.
                        break;
                    } catch (Exception e) {
                        // ctrlMsgAgentId = -1;
                    }
                }

                SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
                String ctrlMsgTSStr = " (" + format.format(sentDate) + " => " + format.format(receivedDate) + ")";
                // add a control message in the message queue which will be shown as state info
                if (sentDate.getTime() < lastFetchDate.getTime() - StaleThreshHold) {
                    // use sentDate to identify if it is stale, use recvDate to order
                    // for control message, sentDate is the same as received date.
                    ReceivedMsg msgCtrl = new ReceivedMsg(lastFetchDate, lastFetchDate, from, ctrlMsgAgentId, CTRL_MSG_RECV_STALE_EMAIL, me.title + ctrlMsgTSStr, true); // we dont care about agent Id here.
                    allRecvMsgs.msgsReceived.add(msgCtrl);  // add the message in the queue. note that the message could be disordered.
                    continue;   // skip this email.
                } else {
                    // for control message, sentDate is the same as received date.
                    ReceivedMsg msgCtrl = new ReceivedMsg(lastFetchDate, lastFetchDate, from, ctrlMsgAgentId, CTRL_MSG_RECV_EMAIL, me.title + ctrlMsgTSStr, true); // we dont care about agent Id here.
                    allRecvMsgs.msgsReceived.add(msgCtrl);  // add the message in the queue. note that the message could be disordered.
                }
            }

            for (int idx = 0; idx < agentIdEvents.length; ++idx) {
                if (agentIdEvents[idx].length() == 0) {
                    continue;
                }
                String[] titleParts = agentIdEvents[idx].split("->");
                if (titleParts.length < 2) {
                    continue; // not a valid message.
                }
                int agentId = -1;
                try {
                    agentId = Integer.parseInt(titleParts[0]);
                } catch (Exception e) {
                    // agentId = -1;
                }
                String title = titleParts[1];
                String event = toOriginalTitle(title);
                String body = infos[idx];
                String remoteAddress = from;

                if (event.equals("signal")) {
                    try {
                        JSONObject obj = new JSONObject(body);
                        if (obj.has("from")) {
                            remoteAddress = obj.get("from").toString(); // update remoteAddress. remoteAddress must have been to lower cased and trimmed.
                        }
                        String type = "";
                        if (obj.has("type")) {
                            type = obj.getString("type");
                        }
                        if (type.equals("offer")) {
                            // this is the incoming call.
                            //startActivityOrNotificationWhenMsg(remoteAddress);
                        }
                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                }
                ReceivedMsg msg = new ReceivedMsg(sentDate, receivedDate, remoteAddress, agentId, event, body, false);
                allRecvMsgs.msgsReceived.add(msg);  // add the message in the queue. note that the message could be disordered.
            }
        }
        sendMessageToApp(null); // we do not check stale again in sendMessageToApp.
    }

    public void sendEmails() {
        Log.d(TAG, "EmailSignalService start to send");
        synchronized (mapAllMsgs) {
            Iterator it = mapAllMsgs.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, BufferedMsgList> pair = (Map.Entry<String, BufferedMsgList>) it.next();
                BufferedMsg bufferedMsg = null;
                Map<String, Integer> titlePartCntMap = new HashMap<String, Integer>();
                String title = "";
                String body = "";
                int ctrlMsgAgentId = -1;
                while (null != (bufferedMsg = pair.getValue().msgs2Send.poll())) {
                    String messageStr = bufferedMsg.message;
                    Log.d(TAG, "EmailSignalService.sendEMails : " + localAddress + " send to " + pair.getKey() + " event : " + bufferedMsg.event + " , message : " + messageStr);
                    String thisTitlePart = bufferedMsg.agentId + "->" + toCodedTitle(bufferedMsg.event);
                    // was title += bufferedMsg.agentId + "->" + toCodedTitle(bufferedMsg.event) + ":";
                    // but repeated 1->s:1->s:1->s: ... or 0->s:0->s:0->s:... will be rejected by outlook
                    // so have to use zipped format, e.g. (1->s)7:
                    if (!titlePartCntMap.containsKey(thisTitlePart)) {
                        titlePartCntMap.put(thisTitlePart, 1);
                    } else {
                        titlePartCntMap.put(thisTitlePart, titlePartCntMap.get(thisTitlePart) + 1);
                    }
                    if (ctrlMsgAgentId == -1) {
                        ctrlMsgAgentId = bufferedMsg.agentId;   // the agent id used in the control message is the first buffered msg agent id.
                    }
                    body += messageStr + "\n";
                }
                for (Map.Entry<String,Integer> entry : titlePartCntMap.entrySet())  {
                    title += "(" + entry.getKey() + ")" + entry.getValue() + ":";
                }
                if (title.length() != 0) {
                    Exception e;
                    if (localEmailType == RtcAppClient.USE_GMAIL_API) {
                        e = SendEmail.send(gmailService, localAddress, new String[]{localAddress}, new String[]{pair.getKey()}, title, body);
                        if (e instanceof com.google.api.client.googleapis.json.GoogleJsonResponseException) {
                            // This means token is no longer valid.
                            if (refreshAccessToken()) {
                                // token and gmail service has been refreshed. Now send the email again.
                                e = SendEmail.send(gmailService, localAddress, new String[]{localAddress}, new String[]{pair.getKey()}, title, body);
                            }
                        }
                    } else {
                        e = SendEmail.send(smtpServerAddr, smtpServerPort, smtpSSL, localAddress, new String[]{localAddress},
                                emailPassword, new String[]{pair.getKey()}, title, body);
                    }
                    // add a control message in the message queue which will be shown as state info
                    Date currentTime = Calendar.getInstance().getTime();
                    if (e == null) {
                        // send message successfully.
                        ReceivedMsg msgCtrl = new ReceivedMsg(currentTime, currentTime, pair.getKey(), ctrlMsgAgentId, CTRL_MSG_SEND_EMAIL, title, true); // we do care about agent Id here.
                        allRecvMsgs.msgsReceived.add(msgCtrl);  // add the message in the queue. note that the message could be disordered.
                    } else {
                        // fail to send
                        ReceivedMsg msgCtrl = new ReceivedMsg(currentTime, currentTime, pair.getKey(), ctrlMsgAgentId,
                                CTRL_MSG_FAIL_TO_SEND, title + "\n" + e.getMessage(), true); // we do care about agent Id here.
                        allRecvMsgs.msgsReceived.add(msgCtrl);  // add the message in the queue. note that the message could be disordered.
                    }

                }
                // it.remove(); // it may cause package loss although stack overflow claims that it avoids a ConcurrentModificationException
            }
        }
    }

    // this function refresh expired access token.
    public boolean refreshAccessToken() {
        Log.d(TAG, "EmailSignalService.refreshAccessToken : start to refresh token, local address is " + localAddress + " current token is " + gmailToken + ".");
        boolean retVal = false;
        //invalidate current token
        AccountManager am = AccountManager.get(MFPAndroidLib.getContext());
        if (gmailToken != null && gmailToken.trim().length() != 0) {
            // invalidate current gmail token if gmail token hasn't been initialized.
            am.invalidateAuthToken(GOOGLE_MAIL_ACCOUNT_TYPE, gmailToken);
        }
        Account[] googleAccts = am.getAccountsByType(GOOGLE_MAIL_ACCOUNT_TYPE);
        for (Account acct : googleAccts) {
            if (acct.name.equalsIgnoreCase(localAddress)) {
                // get it.
                String newToken = null;
                try {
                    newToken = am.blockingGetAuthToken(acct, "oauth2:" + GmailScopes.GMAIL_SEND
                            + " " + GmailScopes.GMAIL_READONLY + " " + GmailScopes.GMAIL_MODIFY, true);
                } catch (OperationCanceledException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (AuthenticatorException e) {
                    e.printStackTrace();
                } finally {
                    if (newToken != null) {
                        gmailToken = newToken;
                        GoogleCredential credential = new GoogleCredential().setAccessToken(gmailToken);
                        JsonFactory jsonFactory = new JacksonFactory();
                        HttpTransport httpTransport = new NetHttpTransport();

                        gmailService = new Gmail.Builder(httpTransport, jsonFactory, credential)
                                .setApplicationName(MFPAndroidLib.getContext().getResources().getString(R.string.app_name)).build();
                        Log.d(TAG, "EmailSignalService.refreshAccessToken : successfully refresh token, local address is " + localAddress + " current token is " + gmailToken + ".");
                        retVal = true;
                    } else {
                        Log.d(TAG, "EmailSignalService.refreshAccessToken : fail to refresh token, local address is " + localAddress + " current token is " + gmailToken + ".");
                    }
                    break;
                }
            }
        }

        return retVal;
    }

    public void start(Boolean enableDebugger) {
        // this is actually restart
        Log.d(TAG, "EmailSignaService.start : All stale messages will be cleared. An activate signal will be sent to local i.e. " + localAddress);
        enableDbg = enableDebugger;
        JSONObject message = new JSONObject();
        isActivated.set(false); // set activated to false so that previous asyncWork can exit.
        synchronized (mapAllMsgs) {
            mapAllMsgs.clear(); // clear all the outgoing messages.
        }
        // do not clear received message queue because received message queue
        // may include calling request just sent.
        isActivated.set(true);
        // send an email when the service starts.
        // This is required because, otherwise, the service will start to check email first.
        // For some email service providers, e.g. outlook, checking email first before sending
        // any email will make emails cannot be sent.
        send(-1, localAddress, "activate", message.toString());
        executor.execute(new Runnable() {
            @Override
            public void run() {
                asyncWork();
            }
        });
    }

    public void asyncWork() {    // to avoid multithreading send, sending email can only be done by one thread.
        while (isActivated.get()) { // if is not activated, exit.
            sendEmails();
            if (!isActivated.get()) {break;}    // exit the loop ASAP.
            try {
                // sleep a few seconds before fetching emails so that it can send an email
                // before fetching(seems that sending should always happen before fetching).
                Thread.sleep(20000); // need not to set it very small because the scheduler will not do it on time.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!isActivated.get()) {break;}    // exit the loop ASAP
            fetchEmails();
            if (!isActivated.get()) {break;}    // exit the loop ASAP
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        Log.d(TAG, "EmailSignalService.stop : all pending messages will be cleared.");
        isActivated.set(false);
        synchronized (mapAllMsgs) {
            mapAllMsgs.clear(); // clear all the outgoing messages
        }
    }

    public class BufferedMsg {
        public int agentId = -1;
        public String event = null;
        public Date bufferedTime = null;
        public String message = null;
        public BufferedMsg(Date bufTime, int aId, String evnt, String msg) {
            bufferedTime = bufTime;
            agentId = aId;
            event = evnt;
            message = msg;
        }
    }

    public class BufferedMsgList {
        public Queue<BufferedMsg> msgs2Send = new ConcurrentLinkedQueue<BufferedMsg>();
    }

    // has to use concurrent map otherwise may cause package loss although stack overflow claims that it avoids a ConcurrentModificationException
    public Map<String, BufferedMsgList> mapAllMsgs = new ConcurrentHashMap<String, BufferedMsgList>();

    public void send(int agentId, String destAddress, String event, String messageStr) {
        synchronized (mapAllMsgs) {
            if (null == mapAllMsgs.get(destAddress)) {
                mapAllMsgs.put(destAddress, new BufferedMsgList());
            }
            Date now = new Date();
            mapAllMsgs.get(destAddress).msgs2Send.add(new BufferedMsg(now, agentId, event, messageStr));
        }
    }
}
