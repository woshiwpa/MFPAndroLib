package com.cyzapps.AdvRtc;

import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.util.HashMap;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * Created by tony on 29/11/2017.
 */

public class MessageHandler {
    private final static String TAG = "New_AdvRtcapp_Debug";

    public RtcAgent mRtcAgent = null;

    private interface Command{
        void execute(String peerId, int sessionId, JSONObject payload) throws JSONException;
    }

    private class CreateOfferCommand implements Command{    // init message handler
        public void execute(String peerId, int sessionId, JSONObject payload) throws JSONException {
            Log.d(TAG, "MessageHandler.CreateOfferCommand : RtcAgent " + mRtcAgent.mAgentId + " init message handler : CreateOfferCommand");
            Peer peer = mRtcAgent.peersManager.get(peerId, sessionId);
            if (peer == null) {
                return;
            }
            peer.pc.createOffer(peer, mRtcAgent.pcConstraints);
            // if createOffer successfully, message will be sent in Peer.onCreateSuccess function
        }
    }

    private class CreateAnswerCommand implements Command{   // offer message handler
        public void execute(String peerId, int sessionId, JSONObject payload) throws JSONException {
            Log.d(TAG, "MessageHandler.CreateAnswerCommand : RtcAgent " + mRtcAgent.mAgentId + " offer message handler : CreateAnswerCommand");
            Peer peer = mRtcAgent.peersManager.get(peerId, sessionId);
            if (peer == null) {
                return;
            }
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    payload.getString("sdp")
            );
            peer.pc.setRemoteDescription(peer, sdp);
            peer.pc.createAnswer(peer, mRtcAgent.pcConstraints);
            // if createAnswer successfully, message will be sent in Peer.onCreateSuccess function
        }
    }

    private class SetRemoteSDPCommand implements Command{   // answer message handler
        public void execute(String peerId, int sessionId, JSONObject payload) throws JSONException {
            Log.d(TAG, "MessageHandler.SetRemoteSDPCommand : RtcAgent " + mRtcAgent.mAgentId + " answer message handler : SetRemoteSDPCommand");
            Peer peer = mRtcAgent.peersManager.get(peerId, sessionId);
            if (peer == null) {
                return;
            }
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    payload.getString("sdp")
            );
            peer.pc.setRemoteDescription(peer, sdp);
        }
    }

    private class AddIceCandidateCommand implements Command{    // candidate message handler
        public void execute(String peerId, int sessionId, JSONObject payload) throws JSONException {
            Log.d(TAG, "MessageHandler.AddIceCandidateCommand : RtcAgent " + mRtcAgent.mAgentId + " candidate message handler : AddIceCandidateCommand");
            Peer peer = mRtcAgent.peersManager.get(peerId, sessionId);
            if (peer == null) {
                return;
            }
            PeerConnection pc = peer.pc;
            if (pc.getRemoteDescription() != null) {
                IceCandidate candidate = new IceCandidate(
                        payload.getString("id"),
                        payload.getInt("label"),
                        payload.getString("candidate")
                );
                pc.addIceCandidate(candidate);
            }
        }
    }

    private HashMap<String, Command> commandMap;

    public MessageHandler(RtcAgent agent) {
        mRtcAgent = agent;
        commandMap = new HashMap<>();
        commandMap.put("init", new CreateOfferCommand());
        commandMap.put("offer", new CreateAnswerCommand());
        commandMap.put("answer", new SetRemoteSDPCommand());
        commandMap.put("candidate", new AddIceCandidateCommand());
    }

    public void onMessageEvent(String from, JSONObject data) {
        // this function is at the same layer as DataRtcAgent's sendSignalMessage function, so it can
        // only see what DataRtcAgent's sendSignalMessage has written.
        try {
            int sessionId = -1;
            if (data.has("sessionId")) {
                sessionId = data.getInt("sessionId");
            }
            String type = "";
            if (data.has("type")) {
                type = data.getString("type");
            }
            Log.d(TAG, "MessageHandler.onMessageEvent : RtcAgent " + mRtcAgent.mAgentId + " message handler onMessage : from : " + from + ", type : " + type + " , sessionId : " + sessionId);
            JSONObject payload = null;
            if(!type.equals("init")) {
                payload = data.getJSONObject("payload");
            }
            // always add a peer when it is init or offer so that it will never use obsolete peer from last session
            // if(null == mRtcAgent.peersManager.get(from, sessionId)) {
            if(type.equals("init") || type.equals("offer")) {
                // if MAX_PEER is reach, ignore the call
                if (mRtcAgent.canHaveMorePeers()) {
                    Log.d(TAG, "MessageHandler.onMessageEvent : RtcAgent " + mRtcAgent.mAgentId + " addPeer, sessionId = " + sessionId);
                    Peer peer = mRtcAgent.addPeer(from, sessionId);    // initialize peer with the latest session id. if sessionid == -1 peer's session id. will be set to 0
                    commandMap.get(type).execute(from, sessionId, payload);
                } else {
                    Log.d(TAG, "MessageHandler.onMessageEvent : cannot add more peer");
                }
            } else {
                commandMap.get(type).execute(from, sessionId, payload);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onLeaveEvent(String from, JSONObject data) {
        try {
            int sessionId = data.getInt("sessionId");
            Peer peer = mRtcAgent.peersManager.get(from, sessionId);
            if (null != peer) {
                Log.d(TAG, "MessageHandler.onLeaveEvent : RtcAgent " + mRtcAgent.mAgentId + " MessageHandler onLeave : remoteAddress : " + from + ", type : leave , sessionId : " + sessionId);
                peer.close();   // peer is no longer needed.
            } else {
                Log.d(TAG, "MessageHandler.onLeaveEvent : RtcAgent " + mRtcAgent.mAgentId + " ignores no peer message : from : " + from + ", type : leave , sessionId : " + sessionId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onRestartEvent(String from, JSONObject data) {
        try {
            int sessionId = data.getInt("sessionId");
            // ToDo
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
