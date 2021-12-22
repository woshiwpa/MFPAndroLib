package com.cyzapps.AdvRtc;

import android.util.Log;

import com.cyzapps.OSAdapter.ParallelManager.ConnectObject;
import com.cyzapps.OSAdapter.ParallelManager.LocalObject;
import com.cyzapps.OSAdapter.ParallelManager.MFP4AndroidCommMan;
import com.cyzapps.OSAdapter.ParallelManager.WEBRTCConnMan;
import com.cyzapps.mfpanlib.MFPAndroidLib;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * Created by tony on 29/11/2017.
 */

public class Peer implements SdpObserver, PeerConnection.Observer {
    private final static String TAG = "New_AdvRtcapp_Debug";

    public RtcAgent mAgent = null;

    public PeerConnection pc;
    public ConnectObject connectObj;
    public String remoteAddress;
    public int currentSessionId;

    public DataChannel dataChannel;

    public String peerStatus = "";
    public Peer(RtcAgent agent, String id, int sessionId) {
        Log.d(TAG, "Peer (" + hashCode() + ") constructor : Agent " + agent.mAgentId + " new Peer: remoteAddr: " + id + " , sessionId " + sessionId);
        mAgent = agent;
        this.pc = mAgent.factory.createPeerConnection(mAgent.iceServers, mAgent.pcConstraints, this);   // this can return null but cannot be saved.
        this.remoteAddress = id;
        this.currentSessionId = (sessionId == -1) ? 0 : sessionId;

        RtcAgent.mListener.onStatusChanged(mAgent, this, "Peer CONNECTING");

        if (mAgent.mAgentId == 1) {
            // this is client. only client needs to create data channel.
            DataChannel.Init dcInit = new DataChannel.Init();
            dcInit.id = 1;
            dataChannel = pc.createDataChannel("1", dcInit);
            // the client create a data channel, so we also need to register observer.
            dataChannel.registerObserver(new DcObserver(Peer.this));
        }
    }

    @Override
    public void onCreateSuccess(final SessionDescription sdp) {
        // according to WebRTC doc:
        // https://webrtc.github.io/webrtc-org/native-code/native-apis/
        // all the API calls have been mapped to the  signal thread so
        // that no need to call RtcAgent.EXECUTOR.execute(rRtc);
        Log.d(TAG, "In Rtc thread : Peer.onCreateSuccess : Agent " + mAgent.mAgentId + " Peer onCreateSuccess : type : " + sdp.type.canonicalForm() + " , sdp : " + sdp.description);
        // TODO: modify sdp to use pcParams prefered codecs
        try {
            JSONObject payload = new JSONObject();
            payload.put("type", sdp.type.canonicalForm());
            payload.put("sdp", sdp.description);
            mAgent.sendControlMessage(remoteAddress, "signal", currentSessionId, sdp.type.canonicalForm(), payload);
            pc.setLocalDescription(Peer.this, sdp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSetSuccess() {
        Log.d(TAG, "Peer.onSetSuccess : Agent " + mAgent.mAgentId + " Peer onSetSuccess");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.d(TAG, "Peer.onCreateFailure : Agent " + mAgent.mAgentId + " Peer onCreateFailure : s is " + s);
    }

    @Override
    public void onSetFailure(String s) {
        Log.d(TAG, "Peer.onSetFailure : Agent " + mAgent.mAgentId + " Peer onSetFailure : s is " + s);
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.d(TAG, "Peer.onSignalingChange : Agent " + mAgent.mAgentId + " Peer onSignalingChange. State is " + signalingState.toString());
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.d(TAG, "Peer (" + hashCode() + ").onIceConnectionChange : Agent " + mAgent.mAgentId + " Peer onIceConnectionChange : iceConnectionState is " + iceConnectionState);
        if(iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
            peerStatus = "DISCONNECTED";
            Peer.this.close();
            RtcAgent.mListener.onStatusChanged(mAgent, Peer.this, "IceConnectionState DISCONNECTED");
        } else if (iceConnectionState == PeerConnection.IceConnectionState.CLOSED) {
            peerStatus = "CLOSED";
            Peer.this.close();
            RtcAgent.mListener.onStatusChanged(mAgent, Peer.this, "IceConnectionState CLOSED");
        } else if (iceConnectionState == PeerConnection.IceConnectionState.CONNECTED) {
            peerStatus = "CONNECTED";
            RtcAgent.mListener.onStatusChanged(mAgent, Peer.this, "IceConnectionState CONNECTED");
        } else if (iceConnectionState == PeerConnection.IceConnectionState.COMPLETED) {
            // note that "COMPLETED" may arrive before or after "CONNECTED".
            // also, one side (which is client side, very likely) may never recieve COMPLETED, just receive "CONNECTTED"
            peerStatus = "COMPLETED";
            RtcAgent.mListener.onStatusChanged(mAgent, Peer.this, "IceConnectionState COMPLETED");
        } else if (iceConnectionState == PeerConnection.IceConnectionState.NEW) {
            peerStatus = "NEW";
            RtcAgent.mListener.onStatusChanged(mAgent, Peer.this, "IceConnectionState NEW");
        } else if (iceConnectionState == PeerConnection.IceConnectionState.FAILED) {
            peerStatus = "FAILED";
            RtcAgent.mListener.onStatusChanged(mAgent, Peer.this, "IceConnectionState FAILED");
            /* Dont reconnect because send restart will cause the other side's client1 try to reconnect. And
            an additional channel between this side's client0 to the other side's client1 will be set up. Currently
            we have a channel between this side's client1 to the other side's client0. Two channels are difficult
            to manage especially if we are connecting multiple remotes.
            Also, restart may cause continously shutdown and restart data channel.
            */
        }
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        Log.d(TAG, "Peer.onIceConnectionReceivingChange : Agent " + mAgent.mAgentId + " changed is " + b);
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Log.d(TAG, "Peer.onIceGatheringChange : Agent " + mAgent.mAgentId + ", iceGatheringState is " + iceGatheringState);
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        // no need to work in RTC Thread.
        Log.d(TAG, "Peer.onIceCandidate : Agent " + mAgent.mAgentId + ", label : " + candidate.sdpMLineIndex + " , id : " + candidate.sdpMid);
        try {
            JSONObject payload = new JSONObject();
            payload.put("label", candidate.sdpMLineIndex);
            payload.put("id", candidate.sdpMid);
            payload.put("candidate", candidate.sdp);
            mAgent.sendControlMessage(remoteAddress, "signal", currentSessionId, "candidate", payload);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        Log.d(TAG, "Peer.onIceCandidatesRemoved : Agent " + mAgent.mAgentId + ", remove : " + iceCandidates.length + " ice candidates.");
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.d(TAG, "Peer.onAddStream : Agent " + mAgent.mAgentId + " add Stream " + mediaStream.label());
        // Peer is not for media stream so do nothing here.
        // For media streaming let's use MMPeer.
    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
        Log.d(TAG, "Peer.onAddTrack : Agent " + mAgent.mAgentId + " add " + mediaStreams.length + " Streams ");
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.d(TAG, "Peer.onRemoveStream : Agent " + mAgent.mAgentId + " remove Stream " + mediaStream.label());
        // Peer is not for media stream so do nothing here.
        // For media streaming let's use MMPeer.
    }

    @Override
    public void onDataChannel(final DataChannel dc) {
        // according to WebRTC doc:
        // https://webrtc.github.io/webrtc-org/native-code/native-apis/
        // all the API calls have been mapped to the  signal thread so
        // that no need to call RtcAgent.EXECUTOR.execute(rRtc);
        Log.d(TAG, "In Rtc thread : Peer.onDataChannel : Agent " + mAgent.mAgentId + ", dataChannel state is " + dc.state());
        dataChannel = dc;
        // OK, now register the observer. Note that very possible, dataChannel has been open
        // so that DcObserver.onStateChange will not be called at dataChannel == open.
        dataChannel.registerObserver(new DcObserver(Peer.this));
        // this is server peer, and when onDataChannel is called the data channel should be ready
        // so let's notify activate function called by listen function.
        MFP4AndroidCommMan mfp4AndroidCommMan = (MFP4AndroidCommMan)RtcAgent.mListener;
        String localAddress = RtcAppClient.getLocalAddress();
        LocalObject.LocalKey localKey = new LocalObject.LocalKey("WEBRTC", localAddress);
        LocalObject localObj = mfp4AndroidCommMan.findLocal(localKey);
        if (localObj == null) {
            Log.d(TAG, "Peer.onDataChannel : No in local found");
        } else {
            WEBRTCConnMan webrtcConnMan = new WEBRTCConnMan(localObj, true, Peer.this.remoteAddress, new ConnectObject.ConnectSettings(), new ConnectObject.ConnectAdditionalInfo(), Peer.this); // empty remote address means it is server side
            localObj.addConnect(Peer.this.remoteAddress, webrtcConnMan);
            // accept call in listen function also blocks at EmailSignalChannelAgent.wait() but in an async thread.
            EmailSignalChannelAgent emailSignalChannelAgent = MFPAndroidLib.getRtcAppClient().emailSignalChannelAgent;
            synchronized (emailSignalChannelAgent) {
                emailSignalChannelAgent.notify();
            }
        }
    }

    @Override
    public void onRenegotiationNeeded() {
        Log.d(TAG, "Peer.onRenegotiationNeeded : Agent " + mAgent.mAgentId);
    }

    public void close() {
        // according to WebRTC doc:
        // https://webrtc.github.io/webrtc-org/native-code/native-apis/
        // all the API calls have been mapped to the  signal thread so
        // that no need to call RtcAgent.EXECUTOR.execute(rRtc);
        if (null != pc && pc.iceConnectionState() != PeerConnection.IceConnectionState.CLOSED) {
            Log.d(TAG, "Peer.close : RTC Agent " + mAgent.mAgentId + " before close Peer(" + remoteAddress + ")'s pc");
            if (null != dataChannel) {
                dataChannel.unregisterObserver();
                dataChannel.dispose();
                // set dataChannel to null because if dataChannel is disposed, dataChannel.state() will throw exception
                // use dataChannel == null to identify if dataChannel has been disposed or not.
                dataChannel = null;
            }
            pc.close();
            // now peer shouldn't receive any onIceConnectionChange event because dataChannel and pc have been closed.
            // so no need to worry that an obsoleted peer call this function again
        }
        Log.d(TAG, "Peer.close : RTC Agent " + mAgent.mAgentId + " before remove Peer(" + remoteAddress + ", " + currentSessionId + ")");
        mAgent.peersManager.remove(Peer.this);
    }
}
