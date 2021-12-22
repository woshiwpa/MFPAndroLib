package com.cyzapps.OSAdapter.ParallelManager;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import com.cyzapps.AdvRtc.EmailSignalChannelAgent;
import com.cyzapps.AdvRtc.MmediaPeerConnectionParams;
import com.cyzapps.AdvRtc.Peer;
import com.cyzapps.AdvRtc.RtcAgent;
import com.cyzapps.AdvRtc.RtcListener;
import com.cyzapps.mfpanlib.MFPAndroidLib;
import com.cyzapps.mfpanlib.R;
import com.cyzapps.Jfcalc.ErrProcessor;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.MediaStream;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.webrtc.ContextUtils.getApplicationContext;

public class WEBRTCLocalMan extends LocalObject {
    public static final String TAG = "New_AdvRtcapp_Debug";
    static AtomicInteger localSessionId = new AtomicInteger();

    public WEBRTCLocalMan(String addr) {
        super("WEBRTC", addr);
    }

    public boolean activate() {
        return true;
    }

    @Override
    public Boolean matchKey(LocalKey key) {
        if (this.protocolName.equals(key.getProtocolName()) && this.address.equals(key.getLocalAddress())) {
            return true;
        }
        return false;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    public String[] connect(String remote, boolean reuseExisting) throws ErrProcessor.JFCALCExpErrException, IOException {
        if (!MFPAndroidLib.getRtcAppClient().emailSignalChannelAgent.isStarted()) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INTERFACE_UNAVAILABLE);
        }

        if (reuseExisting && containConnectAddr(remote)) {
            // if reuse, we should also reuse opposite direction connection.
            return new String[] {this.address, remote};
        }
        if (containConnectAddr(remote)) {
            ConnectObject cntObj;
            while ((cntObj = removeConnect(remote)) !=null){
                // remove the connect from allConnects dictionary. Note that for webRTC, one address always corresponds a single connect
                if (!cntObj.getIsShutdown()) {
                    cntObj.shutdown();
                    try {
                        // wait a while until the exist out connect is disconnected.
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (remote.equals(this.address)) {
            // this is a loopback connection
            WEBRTCConnMan webrtcConnMan = new WEBRTCConnMan(this, null, remote, new ConnectObject.ConnectSettings(), new ConnectObject.ConnectAdditionalInfo(), null);
            this.addConnect(remote, webrtcConnMan);
        } else {
            // send init request to destination address
            Log.d(TAG, "WEBRTCLocalMan.connect starts to send init signal");
            JSONObject msg = new JSONObject();
            try {
                int sessionId = localSessionId.incrementAndGet();
                msg.put("sessionId", sessionId);  // always use different session ids.
                msg.put("type", "init");
            } catch (JSONException e) {
                e.printStackTrace();    // will never be here.
            }
            MFPAndroidLib.getRtcAppClient().dataClient1.mmsgHandler.onMessageEvent(remote, msg);
            // do not send init message so that less emails are required.
            //AppAnMath.getRtcAppClient().dataClient1.sendControlMessage(remote, "signal", -1, "init", msg);    // send init message to remote.

            // do not set restart event. This has the following benefits:
            // 1. we ensure that connection only starts from dataClient1 and connect to remote dataClient0.
            // 2. we avoid any situation that restart event is triggered furiously. This happens quite frequently
            // and we see video connection is setup but interrupted soon and setup again and interrupted again...

            // now we need to wait until the connection is really set up. We use a timer instead of blocking at
            // wait function because if connection cannot be setup, we can return error.
            int sleepTime = 0;
            while (!containConnectAddr(remote) && sleepTime < 300000) {
                try {
                    // wait a while until the connect is established.
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sleepTime += 50;
            }
        }
        if (!containConnectAddr(remote)) {
            Log.d(TAG, "WEBRTCLocalMan.connect : connection to remote address " + remote + " cannot be setup.");
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_CONNECT_UNAVAILABLE);
        } else {
            Log.d(TAG, "WEBRTCLocalMan.connect : connection to remote address " + remote + " has been setup.");
            return new String[] {this.address, remote};
        }
    }

    @Override
    public void listen() throws ErrProcessor.JFCALCExpErrException, IOException {
        if (!MFPAndroidLib.getRtcAppClient().emailSignalChannelAgent.isStarted()) {    //emailSignalChannelAgent is initialized in rtcAppClient's constructor so that it cannot be null.
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INTERFACE_UNAVAILABLE);
        }
        // we do not need to do anything more than above because MFP4AndroidCommMan has initialized service and webRTC.
    }

    @Override
    public ConnectObject accept() throws ErrProcessor.JFCALCExpErrException, IOException {
        EmailSignalChannelAgent emailSignalChannelAgent = MFPAndroidLib.getRtcAppClient().emailSignalChannelAgent;
        synchronized (emailSignalChannelAgent) {
            try {
                Log.d(TAG, "WEBRTCLocalMan.accept : Before wait");
                emailSignalChannelAgent.wait();
                Log.d(TAG, "WEBRTCLocalMan.accept : accept remote address " + emailSignalChannelAgent.acceptedRemoteAddress);
            } catch (InterruptedException e)  {
                Thread.currentThread().interrupt();
                Log.e(TAG, "WEBRTCLocalMan.accept, Thread interrupted", e);
            }
        }
        ConnectObject acceptedConnObj = getConnect(emailSignalChannelAgent.acceptedRemoteAddress);
        return acceptedConnObj;
    }
}
