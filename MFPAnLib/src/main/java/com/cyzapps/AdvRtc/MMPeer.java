package com.cyzapps.AdvRtc;

import android.util.Log;

import com.cyzapps.GI2DAdapter.FlatGDI;
import com.cyzapps.GI2DAdapter.FlatGDIView;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.OSAdapter.AndroidRtcMMediaMan;
import com.cyzapps.OSAdapter.RtcMMediaManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoSink;
import org.webrtc.VideoTrack;

import java.util.Arrays;

public class MMPeer implements SdpObserver, PeerConnection.Observer {
    private final static String TAG = "New_AdvRtcapp_MMDebug";

    public AndroidRtcMMediaMan mRtcMMediaMan;
    public MediaConstraints pcConstraints;
    public String remoteAddress;
    public int currentSessionId;
    public PeerConnection pc;

    public String peerStatus = "";

    public MMPeer(String id, int sessionId, MediaConstraints peerConnectionConstraints) {
        Log.d(TAG, "MMPeer (" + hashCode() + ") constructor : new Peer: remoteAddr: " + id + " , sessionId " + sessionId);
        mRtcMMediaMan = (AndroidRtcMMediaMan) FuncEvaluator.msRtcMMediaManager;
        remoteAddress = id;
        currentSessionId = (sessionId == -1) ? 0 : sessionId;
        pcConstraints = peerConnectionConstraints;
        if (RtcAgent.factoryMMedia == null) {
            Log.d(TAG, "MMPeer (" + hashCode() + ") constructor : multimedia RTC factory hasn't been initialized.");
            return;
        } else {
            pc = RtcAgent.factoryMMedia.createPeerConnection(mRtcMMediaMan.iceServers, pcConstraints, this);   // this can return null but cannot be saved.
            if (FlatGDI.mediaStream != null) {
                pc.addStream(FlatGDI.mediaStream);  // without this, onAddStream event will not be called.
            }
        }
    }

    @Override
    public void onCreateSuccess(SessionDescription sdp) {
        // TODO: should I put it in a dedicated thread for WebRTC?
        Log.d(TAG, "In Rtc thread : MMPeer.onCreateSuccess : peerId : " + remoteAddress + " sessionId : " + currentSessionId + " type : " + sdp.type.canonicalForm() + " , sdp : " + sdp.description);
        // TODO: modify sdp to use pcParams prefered codecs
        // put the event in the queue.
        try {
            JSONObject payload = new JSONObject();
            payload.put("type", sdp.type.canonicalForm());
            payload.put("sdp", sdp.description);
            enqueRTCMMediaEvent("signal", payload.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pc.setLocalDescription(MMPeer.this, sdp);
    }

    @Override
    public void onSetSuccess() {
        Log.d(TAG, "MMPeer.onSetSuccess : peerId : " + remoteAddress + " sessionId : " + currentSessionId);
        generateMMPeerEvent("sdp", "set_success", "");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.d(TAG, "MMPeer.onCreateFailure : peerId : " + remoteAddress + " sessionId : " + currentSessionId + " error message : " + s);
        generateMMPeerEvent("sdp", "create_failure", s);
    }

    @Override
    public void onSetFailure(String s) {
        Log.d(TAG, "MMPeer.onSetFailure : peerId : " + remoteAddress + " sessionId : " + currentSessionId + " error message : " + s);
        generateMMPeerEvent("sdp", "set_failure", s);
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.d(TAG, "MMPeer.onSignalingChange : peerId : " + remoteAddress + " sessionId : " + currentSessionId + " onSignalingChange.State is " + signalingState.toString());
        generateMMPeerEvent("pc", "signal_change", signalingState.toString());
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.d(TAG, "MMPeer (" + hashCode() + ").onIceConnectionChange : peerId : " + remoteAddress + " sessionId : " + currentSessionId + " onIceConnectionChange : iceConnectionState is " + iceConnectionState);
        generateMMPeerEvent("pc", "ice_connection_change", iceConnectionState.toString());
        if(iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
            peerStatus = "DISCONNECTED";
            MMPeer.this.close();
        } else if (iceConnectionState == PeerConnection.IceConnectionState.CLOSED) {
            peerStatus = "CLOSED";
            MMPeer.this.close();
        } else if (iceConnectionState == PeerConnection.IceConnectionState.CONNECTED) {
            peerStatus = "CONNECTED";
        } else if (iceConnectionState == PeerConnection.IceConnectionState.COMPLETED) {
            // note that "COMPLETED" may arrive before or after "CONNECTED".
            // also, one side (which is client side, very likely) may never recieve COMPLETED, just receive "CONNECTTED"
            peerStatus = "COMPLETED";
        } else if (iceConnectionState == PeerConnection.IceConnectionState.NEW) {
            peerStatus = "NEW";
        } else if (iceConnectionState == PeerConnection.IceConnectionState.FAILED) {
            peerStatus = "FAILED";
        }
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        Log.d(TAG, "MMPeer.onIceConnectionReceivingChange : peerId : " + remoteAddress + " sessionId : " + currentSessionId + " changed is " + b);
        generateMMPeerEvent("pc", "ice_connection_receiving_change", b?"true":"false");
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Log.d(TAG, "MMPeer.onIceGatheringChange : peerId : " + remoteAddress + " sessionId : " + currentSessionId + ", iceGatheringState is " + iceGatheringState);
        generateMMPeerEvent("pc", "ice_gathering_change", iceGatheringState.toString());
    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {
        // no need to work in RTC Thread.
        Log.d(TAG, "MMPeer.onIceCandidate : peerId : " + remoteAddress + " sessionId : " + currentSessionId + ", label : " + candidate.sdpMLineIndex + " , id : " + candidate.sdpMid);
        // put the event in the queue.
        try {
            JSONObject payload = new JSONObject();
            payload.put("type", "candidate");
            payload.put("label", candidate.sdpMLineIndex);
            payload.put("id", candidate.sdpMid);
            payload.put("candidate", candidate.sdp);
            enqueRTCMMediaEvent("signal", payload.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        Log.d(TAG, "MMPeer.onIceCandidatesRemoved : peerId : " + remoteAddress + " sessionId : " + currentSessionId + ", remove : " + iceCandidates.length + " ice candidates.");
        String output = "";
        for (int idx = 0; idx < iceCandidates.length; idx ++) {
            if (idx > 0) {
                output += "\n";
            }
            output += iceCandidates[idx].toString();
        }
        generateMMPeerEvent("pc", "ice_candidates_removed", output);
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.d(TAG, "MMPeer.onAddStream : peerId : " + remoteAddress + " sessionId : " + currentSessionId + " add Stream " + mediaStream.getId()
                    + " stream's audio track size is " + mediaStream.audioTracks.size() + " stream's video track size is " + mediaStream.videoTracks.size());
        generateMMPeerEvent("pc", "add_stream", "");
        if (mediaStream.audioTracks.size() > 1 || mediaStream.videoTracks.size() > 1) {
            Log.d(TAG, "MMPeer.onAddStream: Weird-looking stream");
            return;
        }
        if (mediaStream.audioTracks.size() > 0) {
            AudioTrack audioTrack = mediaStream.audioTracks.get(0);
            audioTrack.setEnabled(true);
        }
        if (mediaStream.videoTracks.size() > 0) {
            VideoTrack videoTrack = mediaStream.videoTracks.get(0);
            videoTrack.setEnabled(true);
            VideoSink videoSink = mRtcMMediaMan.mapStream2ProxyRenderer.get(new AndroidRtcMMediaMan.StreamTrackId(remoteAddress, 0));
            if (videoSink == null) {
                Log.d(TAG, "MMPeer.onAddStream: The video stream has nowhere to go!");
            } else {
                Log.d(TAG, "MMPeer.onAddStream: The video stream has been successfully mapped to a sink!");
                videoTrack.addSink(videoSink);
            }
        }
    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
        Log.d(TAG, "MMPeer.onAddTrack : peerId : " + remoteAddress + " sessionId : " + currentSessionId + " add " + mediaStreams.length + " Streams ");
        generateMMPeerEvent("pc", "add_track", "");
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.d(TAG, "MMPeer.onRemoveStream : peerId : " + remoteAddress + " sessionId : " + currentSessionId + " add Stream " + mediaStream.getId()
                + " stream's audio track size is " + mediaStream.audioTracks.size() + " stream's video track size is " + mediaStream.videoTracks.size());
        generateMMPeerEvent("pc", "remove_stream", "");
        if (mediaStream.audioTracks.size() >1) {
            Log.d(TAG, "MMPeer.onRemoveStream : Weird-looking audio stream");
        } else if (mediaStream.audioTracks.size() == 1) {
            Log.d(TAG, "MMPeer.onRemoveStream : disable audio track by MMPeer " + remoteAddress + " session id " + currentSessionId);
            AudioTrack audioTrack = mediaStream.audioTracks.get(0);
            audioTrack.setEnabled(false);
        }
        if (mediaStream.videoTracks.size() > 1) {
            Log.d(TAG, "MMPeer.onRemoveStream : Weird-looking video stream");
        } else if (mediaStream.videoTracks.size() == 1) {
            Log.d(TAG, "MMPeer.onRemoveStream : remove video render by MMPeer " + remoteAddress + " session id " + currentSessionId);
            VideoTrack videoTrack = mediaStream.videoTracks.get(0);
            videoTrack.setEnabled(false);
            VideoSink videoSink = mRtcMMediaMan.mapStream2ProxyRenderer.get(new AndroidRtcMMediaMan.StreamTrackId(remoteAddress, 0));
            if (videoSink == null) {
                Log.d(TAG, "MMPeer.onRemoveStream: The video stream hasn't been mapped to a sink!");
            } else {
                Log.d(TAG, "MMPeer.onRemoveStream: The video stream has been successfully removed from a sink!");
                videoTrack.removeSink(videoSink);
            }
        }
    }

    @Override
    public void onDataChannel(final DataChannel dc) {
        /** todo */
        Log.d(TAG, "MMPeer.onDataChannel : peerId : " + remoteAddress + " sessionId : " + currentSessionId);
        generateMMPeerEvent("pc", "data_channel", "");
    }

    @Override
    public void onRenegotiationNeeded() {
        Log.d(TAG, "MMPeer.onRenegotiationNeeded : peerId : " + remoteAddress + " sessionId : " + currentSessionId);
        generateMMPeerEvent("pc", "renegotiation_needed", "");
    }

    public void close() {
        // according to WebRTC doc:
        // https://webrtc.github.io/webrtc-org/native-code/native-apis/
        // all the API calls have been mapped to the  signal thread so
        // that no need to call RtcAgent.EXECUTOR.execute(rRtc);
        if (null != pc && pc.iceConnectionState() != PeerConnection.IceConnectionState.CLOSED) {
            pc.close();
            // now peer shouldn't receive any onIceConnectionChange event.
            // so no need to worry that an obsoleted peer call this function again
        }
        mRtcMMediaMan.allPeerConnections.remove(remoteAddress);
        generateMMPeerEvent("pc", "close", "");
    }

    private void generateMMPeerEvent(String evtType, String type, String content) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("type", type);
            payload.put("content", content);
            enqueRTCMMediaEvent(evtType, payload.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void enqueRTCMMediaEvent(String evtType, String evtInfo) {
        RtcMMediaManager.RtcMMediaEvent rtcMMediaEvent = new RtcMMediaManager.RtcMMediaEvent(remoteAddress, currentSessionId, evtType, evtInfo);
        while(rtcMMediaEvent != null) {
            try {
                // msRtcMMediaManager should have been initialized before camera starts to work.
                // here assume it is always non null.
                FuncEvaluator.msRtcMMediaManager.rtcMMediaEventBlockingQueue.put(rtcMMediaEvent);
                rtcMMediaEvent = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
