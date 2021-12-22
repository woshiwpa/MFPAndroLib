package com.cyzapps.AdvRtc;

import android.opengl.EGLContext;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;

import java.nio.ByteBuffer;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by tony on 27/11/2017.
 */

public class DataRtcAgent extends RtcAgent {

    public DataRtcAgent(int agentId, boolean startMMediaScheduler) {
        super(agentId);

        Log.d(TAG, "DataRtcAgent constructor: client will be connecting");
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"));
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        //pcConstraints.optional.add(new MediaConstraints.KeyValuePair("iceRestart", "true"));      // not needed for reconnection?

        if (startMMediaScheduler) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
        } else {
            scheduler = null;
        }
    }

    @Override
    public String getRtcAgentType() { return "DataRtcAgent";}

    /**
     * Send a message through the signaling server
     *
     * @param to address of recipient
     * @param sessionId session id of this signal channel
     * @param type type of message
     * @param payload payload of message
     * @throws JSONException
     */
    @Override
    public Boolean sendControlMessage(String to, String event, int sessionId, String type, JSONObject payload) {
        Log.d(TAG, getRtcAgentType() + ".sendControlMessage: RtcAgent " + mAgentId + " sendMessage : to : " + to + " , session id : " + sessionId + " , type : " + type + " , payload : " + payload);

        Boolean ret = false;
        // DataRtcAgent always uses memailSignalChannelAgent to send message
        if (null == memailSignalChannelAgent) {
            Log.d(TAG, getRtcAgentType() + ".sendControlMessage: email signal channel is null.");
        } else if (!(ret = memailSignalChannelAgent.send(mAgentId, to, event, sessionId, type, payload))) {
            Log.d(TAG, getRtcAgentType() + ".sendControlMessage by email signal channel is unsuccessful.");
        }
        /*
        if (!(this instanceof DataRtcAgent)) {
            // MmediaRtcAgent always uses memailSignalChannelAgent to send message
            if (null == mdcSignalChannelAgent) {
                Log.d(TAG, getRtcAgentType() + ".sendControlMessage: DC signal channel is null.");
            } else if (!(ret = mdcSignalChannelAgent.send(mAgentId, to, event, sessionId, type, payload))) {
                Log.d(TAG, getRtcAgentType() + ".sendControlMessage by DC signal channel is unsuccessful.");
            }
        }*/
        return ret;
    }
}
