package com.cyzapps.OSAdapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cyzapps.AdvRtc.MMPeer;
import com.cyzapps.AdvRtc.Peer;
import com.cyzapps.AdvRtc.RtcAgent;
import com.cyzapps.GI2DAdapter.FlatGDI;
import com.cyzapps.GI2DAdapter.FlatGDIView;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.OSAdapter.ParallelManager.LocalObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoSink;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AndroidRtcMMediaMan extends RtcMMediaManager {
    private final static String TAG = "New_AdvRtcapp_MMDebug";
    private static final int MY_PERMISSIONS_REQUEST = 0;

    public Map<String, MMPeer> allPeerConnections = new ConcurrentHashMap<String, MMPeer>();
    public static class StreamTrackId {
        private String peerId;
        public String getPeerId() {
            return peerId;
        }
        private Integer trackId;
        public Integer getTrackId() {
            return trackId;
        }

        public StreamTrackId(String peerIdValue, Integer trackIdValue) {
            peerId = peerIdValue;   // peerIdValue cannot be null. If peerIdValue is "", it means local
            trackId = trackIdValue;
        }
        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof StreamTrackId)) {
                return false;
            }
            StreamTrackId id = (StreamTrackId) o;
            return id.peerId.equals(peerId) && id.trackId == trackId;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + peerId.hashCode();
            result = 31 * result + trackId;
            return result;
        }
    }
    public Map<StreamTrackId, FlatGDIView.ProxyVideoSink> mapStream2ProxyRenderer = new ConcurrentHashMap<StreamTrackId, FlatGDIView.ProxyVideoSink>();

    public LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();

    public Context context;

    public AndroidRtcMMediaMan(Context activityContext) {
        // this function is called only once.
        // one off construction work is carried out inside.
        context = activityContext;
        iceServers.add(new PeerConnection.IceServer("stun:stun.stunprotocol.org:3478"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("stun:numb.viagenie.ca:3478"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.xten.com:3478"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.voipbuster.com:3478"));
        iceServers.add(new PeerConnection.IceServer("turn:numb.viagenie.ca", "tony.tcui@gmail.com", "rrfz4610"));
    }

    @Override
    public boolean initRtcMMediaMan() {
        // remember, this function is different from constructor, it might be called multiple time by user
        // although this is not recommended.
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, android.Manifest.permission.MODIFY_AUDIO_SETTINGS)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                /*|| ContextCompat.checkSelfPermission(context, android.Manifest.permission.FLASHLIGHT)    // flashlight is a normal permission so no need to request.
                    != PackageManager.PERMISSION_GRANTED*/
                || ContextCompat.checkSelfPermission(context, android.Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            // No explanation, we request the permissions we need.
            ActivityCompat.requestPermissions((Activity)context, new String[]{
                            android.Manifest.permission.RECORD_AUDIO,
                            android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
                            android.Manifest.permission.CAMERA,
                            //android.Manifest.permission.FLASHLIGHT,   // normal permission no need to request
                            android.Manifest.permission.INTERNET,
                            android.Manifest.permission.ACCESS_NETWORK_STATE
                    },
                    MY_PERMISSIONS_REQUEST);
        }
        // do not initial WebRTC factory here because for multimedia webRTC application, we need EglBaseContext as
        // a parameter to initialize factory. RtcAgent.initWebRtcFactory(eglBaseContext); will be called in the FlatGDI's
        // startLocalStream function. This means before createOffer and createAnswer, we need to call initialize_local_video
        // first
        //RtcAgent.initWebRtcFactory();   // dont forget to initialize factory (and it can be called multiple times)
        return true;
    }

    @Override
    public boolean createOffer(String peerId, Map<String, String> mandatoryConstraints, Map<String, String> optionalConstraints) {
        Log.d(TAG, "AndroidRtcMMediaMan.CreateOffer : peerId " + peerId);
        MMPeer peer = allPeerConnections.get(peerId);   // we createOffer on the same peer if we want to reconnect to same peer after a peer accidentally disconnect
        if (peer == null)  {
            // this is a new peer
            MediaConstraints pcConstraints = new MediaConstraints();
            for (Map.Entry<String, String> entry : mandatoryConstraints.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair(key, value));
            }
            for (Map.Entry<String, String> entry : optionalConstraints.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                pcConstraints.optional.add(new MediaConstraints.KeyValuePair(key, value));
            }
            peer = new MMPeer(peerId, 0, pcConstraints);
            allPeerConnections.put(peerId, peer);
        }
        peer.pc.createOffer(peer, peer.pcConstraints);
        // if createOffer successfully, message will be sent in MMPeer.onCreateSuccess function
        return true;
    }

    @Override
    public boolean createAnswer(String peerId, String sdpType, String sdpContent, Map<String, String> mandatoryConstraints, Map<String, String> optionalConstraints) {
        Log.d(TAG, "AndroidRtcMMediaMan.CreateAnswer : peerId " + peerId);
        MMPeer peer = allPeerConnections.get(peerId);   // we createAnswer on the same peer if we want to reconnect to same peer after a peer accidentally disconnect
        if (peer == null) {
            // this is a new peer
            MediaConstraints pcConstraints = new MediaConstraints();
            for (Map.Entry<String, String> entry : mandatoryConstraints.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair(key, value));
            }
            for (Map.Entry<String, String> entry : optionalConstraints.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                pcConstraints.optional.add(new MediaConstraints.KeyValuePair(key, value));
            }
            peer = new MMPeer(peerId, 0, pcConstraints);
            allPeerConnections.put(peerId, peer);
        }
        SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm(sdpType), sdpContent);
        peer.pc.setRemoteDescription(peer, sdp); // cannot separate setRemoteDescription from createAnswer coz we need create Peer->setRemoteDescription->createAnswer
        peer.pc.createAnswer(peer, peer.pcConstraints);
        // if createAnswer successfully, message will be sent in MMPeer.onCreateSuccess function
        return true;
    }

    @Override
    public boolean setRemoteDescription(String peerId, String sdpType, String sdpContent) {
        Log.d(TAG, "AndroidRtcMMediaMan.SetRemoteDescription : peerId " + peerId + " sdpType " + sdpType);
        MMPeer peer = allPeerConnections.get(peerId);
        if (peer != null) {
            SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm(sdpType), sdpContent);
            peer.pc.setRemoteDescription(peer, sdp);
            return true;
        }
        return false;
    }

    @Override
    public boolean addIceCandidate(String peerId, String payloadStr) {
        Log.d(TAG, "AndroidRtcMMediaMan.AddIceCandidate : peerId " + peerId + " payload is " + payloadStr);
        MMPeer peer = allPeerConnections.get(peerId);
        if (peer == null) {
            return false;
        }
        PeerConnection pc = peer.pc;
        JSONObject payload  = null;
        try {
            payload = new JSONObject(payloadStr);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        if (pc.getRemoteDescription() != null) {
            IceCandidate candidate = null;
            try {
                candidate = new IceCandidate(
                        payload.getString("id"),
                        payload.getInt("label"),
                        payload.getString("candidate")
                );
                pc.addIceCandidate(candidate);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addPeerStream(String peerId) {
        MMPeer peer = allPeerConnections.get(peerId);
        boolean ret = false;
        if (peer != null && peer.pc != null && FlatGDI.mediaStream != null) {
            ret = peer.pc.addStream(FlatGDI.mediaStream);  // without this, onAddStream event will not be called.
        }
        return ret;
    }

    @Override
    public void removePeerStream(String peerId) {
        MMPeer peer = allPeerConnections.get(peerId);
        if (peer != null && peer.pc != null && FlatGDI.mediaStream != null) {
            peer.pc.removeStream(FlatGDI.mediaStream);  // without this, onAddStream event will not be called.
        }
    }

    @Override
    public void closePeer(String peerId) {
        Log.d(TAG, "AndroidRtcMMediaMan.closePeer : peerId " + peerId);
        MMPeer peer = allPeerConnections.get(peerId);
        if (peer != null) {
            peer.close();
        }
    }
}
