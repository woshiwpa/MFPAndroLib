package com.cyzapps.OSAdapter.ParallelManager;

import android.util.Log;

import com.cyzapps.AdvRtc.Peer;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.mfpanlib.MFPAndroidLib;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WEBRTCConnMan extends ConnectObject {
    public static final String TAG = "New_AdvRtcapp_Debug";

    public final Peer peer; // the peer used in the connection object

    public WEBRTCConnMan(LocalObject po, Boolean isIn, String addr, ConnectSettings config, ConnectAdditionalInfo addInfo, final Peer myPeer) {
        super(po, isIn, false, addr, config, addInfo);
        // create mutual reference between peer and WEBRTCConnMan
        peer = myPeer;
        if (peer != null) {
            peer.connectObj = this;
        }
    }

    @Override
    public boolean sendCallRequest(CallObject callObj, String cmd, String cmdParam, String content) {
        ObjectOutputStream oOut = null;
        PackedCallRequestInfo packedCallRequestInfo = new PackedCallRequestInfo();
        // set srcLocalAddr and srcRemoteAddr to make WebRTCConnMan compatible with TCPIPConnMan.
        packedCallRequestInfo.srcLocalAddr = getSourceAddress();
        packedCallRequestInfo.srcRemoteAddr = address;
        packedCallRequestInfo.destCallPoint = callObj.remoteCallPoint;
        packedCallRequestInfo.cmd = cmd;
        packedCallRequestInfo.cmdParam = cmdParam;
        packedCallRequestInfo.content = content;
        if (isLoopbackConnect()) {
            processReceivedCallRequest(packedCallRequestInfo);
            return true;
        } else {
            boolean bRet = false;
            try {
                JSONObject json = new JSONObject();
                try {
                    json.put("event", "message");
                    json.put("from", MFPAndroidLib.getRtcAppClient().getLocalAddress());
                    json.put("to", address);
                    json.put("data", FuncEvaluator.msCommMgr.serialize(packedCallRequestInfo));
                    String str2Send = json.toString();
                    if (peer == null) {
                        Log.d(TAG, "WEBRTCConnMan.sendData : peer is unavailable for " + address);
                    } else {
                        // peer has been connected.
                        peer.mAgent.sendData(peer, str2Send);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                bRet = true;
            } catch (IOException ex) {
                Logger.getLogger(WEBRTCConnMan.class.getName()).log(Level.SEVERE, null, ex);
            }
            return bRet;
        }
    }

    @Override
    public PackedCallRequestInfo receiveCallRequest() {
        return null;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        // Have to send signal to remote because webrtc cannot guarantee that remote can detect abrupt
        // closure from remote peer.
        // https://bugs.chromium.org/p/webrtc/issues/detail?id=1676
        peer.mAgent.sendControlMessage(peer.remoteAddress, "signal", peer.currentSessionId, "leave", new JSONObject());
        // peer.close() will be run in RTC thread and will call peer.mAgent.peersManager.remove(peer);
        peer.close();
    }
}
