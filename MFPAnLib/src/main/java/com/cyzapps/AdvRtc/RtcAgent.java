package com.cyzapps.AdvRtc;

import android.content.Context;
import android.util.Log;

import com.cyzapps.mfpanlib.MFPAndroidLib;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.SoftwareVideoEncoderFactory;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tony on 11/02/2018.
 */

public abstract class RtcAgent {
    public final static String TAG = "New_AdvRtcapp_Debug";

    public int mAgentId;
    public final static int MAX_PEER = 4;
    public final static int MAX_PACKET_SIZE = 16000;
    public static PeerConnectionFactory factoryDC = null; // this is for data channel
    public static PeerConnectionFactory factoryMMedia = null;   // this is for multimedia

    // peers manager will be visited by two functions.
    // 1 is EmailSignalChannelAgent.IncomingHandler thread
    // 2 is Peer.onIceConnectionChange
    // have to be very careful to ensure no racing issue
    // The only way is to ensure all the tasks related to peer and PeersManager are run in EXECUTOR's thread
    public static class PeersManager {
        private HashMap<String, LinkedList<Peer>> peers = new HashMap<String, LinkedList<Peer>>();

        public synchronized int size() {
            return peers.size();
        }

        @Deprecated
        public synchronized Peer get(String peerId) {
            if (peers.get(peerId) != null) {
                return peers.get(peerId).getFirst();
            }
            return null;
        }

        public synchronized Peer get(String peerId, int sessionId) {
            if (peers.get(peerId) != null) {
                LinkedList<Peer> allPeersAtThisAddr = peers.get(peerId);
                for (Peer p: allPeersAtThisAddr) {
                    if (p.currentSessionId == sessionId) {
                        return p;
                    }
                }
            }
            return null;
        }

        public synchronized Peer remove(String peerId) {
            if (peers.get(peerId) != null) {
                Peer p = peers.get(peerId).removeFirst();
                if (peers.get(peerId).size() == 0) {
                    peers.remove(peerId);
                }
                return p;
            }
            return null;
        }

        public synchronized Peer remove(String peerId, int sessionId) {
            if (peers.get(peerId) != null) {
                LinkedList<Peer> allPeersAtThisAddr = peers.get(peerId);
                for (int idx = 0; idx < allPeersAtThisAddr.size(); idx ++) {
                    if (allPeersAtThisAddr.get(idx).currentSessionId == sessionId) {
                        Peer p = allPeersAtThisAddr.remove(idx);
                        if (allPeersAtThisAddr.size() == 0) {
                            peers.remove(peerId);
                        }
                        return p;
                    }
                }
            }
            return null;
        }

        public synchronized Peer remove(Peer peer) {
            if (peers.get(peer.remoteAddress) != null) {
                LinkedList<Peer> allPeersAtThisAddr = peers.get(peer.remoteAddress);
                for (int idx = 0; idx < allPeersAtThisAddr.size(); idx ++) {
                    if (allPeersAtThisAddr.get(idx) == peer) {
                        Peer p = allPeersAtThisAddr.remove(idx);
                        if (allPeersAtThisAddr.size() == 0) {
                            peers.remove(peer.remoteAddress);
                        }
                        return p;
                    }
                }
            }
            return null;
        }

        public synchronized void put(Peer peer) {
            if (peers.get(peer.remoteAddress) == null) {
                LinkedList<Peer> peers4ThisAddr = new LinkedList<Peer>();
                peers4ThisAddr.addFirst(peer);
                peers.put(peer.remoteAddress, peers4ThisAddr);
            } else {
                LinkedList<Peer> peers4ThisAddr = peers.get(peer.remoteAddress);
                int idx = 0;
                for (; idx < peers4ThisAddr.size(); idx ++) {
                    if (peer.currentSessionId > peers4ThisAddr.indexOf(idx)) {
                        peers4ThisAddr.add(idx, peer);
                        break;
                    } else if (peer.currentSessionId == peers4ThisAddr.indexOf(idx)) {
                        // this could happen, for example, client restarts
                        Log.d(TAG, "Peer.put : find duplicate session id for agent " + peer.mAgent.mAgentId + ", address " + peer.remoteAddress + " and session Id " + peer.currentSessionId + ".");
                        peers4ThisAddr.set(idx, peer);
                        break;
                    }
                }
                if (idx == peers4ThisAddr.size()) {
                    peers4ThisAddr.addLast(peer);
                }
            }
        }

        public synchronized void clear() {
            LinkedList<Peer> peers2Close = new LinkedList<Peer>();
            for (LinkedList<Peer> peers4ThisAddr : peers.values()) {
                while (peers4ThisAddr.size() > 0) {
                    Peer p = peers4ThisAddr.removeLast();
                    peers2Close.add(p);
                }
            }
            peers.clear();
            for (Peer p: peers2Close) {
                // Have to send signal to remote because webrtc cannot guarantee that remote can detect abrupt
                // closure from remote peer.
                // https://bugs.chromium.org/p/webrtc/issues/detail?id=1676
                p.mAgent.sendControlMessage(p.remoteAddress, "signal", p.currentSessionId, "leave", new JSONObject());
                p.close();
            }
        }

    }
    public PeersManager peersManager = new PeersManager();

    public MediaConstraints pcConstraints = new MediaConstraints();
    public LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
    public static RtcListener mListener = null;
    public static MmediaPeerConnectionParams mPCParams = null;

    public AtomicInteger packetId = new AtomicInteger();    // packetId is atomic as long as sendData is not always called by a single thread.

    // SignalChannelAgent will not change when we start email channel, so no need to be volatile.
    public /*volatile*/ EmailSignalChannelAgent memailSignalChannelAgent;

    public MessageHandler mmsgHandler = new MessageHandler(this);

    public ScheduledExecutorService scheduler = null;

    public RtcAgent(int agentId) {
        mAgentId = agentId;

        iceServers.add(new PeerConnection.IceServer("stun:stun.stunprotocol.org:3478"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("stun:numb.viagenie.ca:3478"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.xten.com:3478"));
        iceServers.add(new PeerConnection.IceServer("stun:stun.voipbuster.com:3478"));
        iceServers.add(new PeerConnection.IceServer("turn:numb.viagenie.ca", "tony.tcui@gmail.com", "rrfz4610"));
    }

    // ensure this function is called each time activity is changed.
    public static void setRtcListener(final RtcListener listener, final MmediaPeerConnectionParams params) {
        // assume rtcAgent.peers now is empty.
        mListener = listener;
        mPCParams = params;
    }

    // initialize webrtc factory
    public static PeerConnectionFactory initWebRtcFactory(EglBase.Context eglBaseContext) {
        // if call factory.dispose() here, whether called by Rtc thread or UI thread,
        // it crashes at AudioTrack thread. And no matter how to organize the RtcAppClient,
        // it always crashes here. So I have to comment it.
        Log.d(TAG, "RtcAgent.setRtcListener : before initializeAndroidGlobals");
        // this function no longer exists, replaced by
        // PeerConnectionFactory.initializeAndroidGlobals(MFPAndroidLib.getContext(), true);
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(MFPAndroidLib.getContext())
                .setFieldTrials("WebRTC-IntelVP8/Enabled/")
                .setEnableInternalTracer(true)
                .createInitializationOptions());
        Log.d(TAG, "RtcAgent.setRtcListener : before factory = new PeerConnectionFactory()");
        VideoEncoderFactory encoderFactory = new SoftwareVideoEncoderFactory();
        VideoDecoderFactory decoderFactory = new SoftwareVideoDecoderFactory();
        if (eglBaseContext != null) {
            encoderFactory = new DefaultVideoEncoderFactory(eglBaseContext, true, true);
            decoderFactory = new DefaultVideoDecoderFactory(eglBaseContext);
        }
        PeerConnectionFactory factory = PeerConnectionFactory.builder()
                                        .setOptions(new PeerConnectionFactory.Options())
                                        .setVideoEncoderFactory(encoderFactory)
                                        .setVideoDecoderFactory(decoderFactory)
                                        .createPeerConnectionFactory();
        return factory;
    }

    public boolean isDataRtcAgentServer() {
        return mAgentId == 0;
    }

    public boolean canHaveMorePeers() {
        return peersManager.size() < MAX_PEER;
    }

    public Peer addPeer(String remoteAddr, int sessionId)  {
        // because it will return a peer object, the caller of addPeer instead of the body of addPeer
        // should be placed in the EXECUTOR thread.
        Peer peer = new Peer(this, remoteAddr, sessionId);
        peersManager.put(peer);
        return peer;
    }

    /**
     * Send a message through the signaling server
     *
     * @param to address of recipient
     * @param sessionId session id of this signal channel
     * @param type type of message
     * @param payload payload of message
     * @throws JSONException
     */
    public abstract Boolean sendControlMessage(String to, String event, int sessionId, String type, JSONObject payload);

    public abstract String getRtcAgentType();
    /**
     * Call this method in Activity.onPause()
     */
    public void onPause() {
        Log.d(TAG, "RtcAgent.onPause : " + getRtcAgentType() + " " + mAgentId + " onPause");
    }

    /**
     * Call this method in Activity.onResume()
     */
    public void onResume() {
        Log.d(TAG, "RtcAgent.onResume : " + getRtcAgentType() + " " + mAgentId + " onResume");
    }

    /**
     * Call this method in Activity.onDestroy()
     */
    public void onDestroy(boolean destroyPC) {
        // according to WebRTC doc:
        // https://webrtc.github.io/webrtc-org/native-code/native-apis/
        // all the API calls have been mapped to the  signal thread so
        // that no need to call RtcAgent.EXECUTOR.execute(rRtc);
        Log.d(TAG, "RtcAgent.onDestroy : " + getRtcAgentType() + " " + mAgentId + " onDestroy");
        peersManager.clear();
    }

    public void sendData(final Peer peer, final String data) {
        // according to WebRTC doc:
        // https://webrtc.github.io/webrtc-org/native-code/native-apis/
        // all the API calls have been mapped to the  signal thread so
        // that no need to call RtcAgent.EXECUTOR.execute(rRtc);
        // However:
        // we have to use atomic int for packetId shared by different threads.
        int pktId = packetId.incrementAndGet();
        if (peer == null) {
            Log.d(TAG, "RtcAgent.sendData cannot find a peer.");
        } else if (peer.dataChannel == null || peer.dataChannel.state() != DataChannel.State.OPEN) {
            Log.d(TAG, "RtcAgent.sendData to " + peer.remoteAddress + ": peer is not ready. Its status is " + peer.peerStatus);
        } else  { // it is possible that dataChannel may not be ready or may be destroyed.
            byte[] allBytes = data.getBytes();
            int dataIdx = 0;
            int packetSectionId = 0;
            while (dataIdx < allBytes.length) {
                // peer's packet format:
                // rtcAgentId: 1 byte
                // packetId: 4 bytes
                // packet total size: 4 bytes
                // packet section Id: 4 bytes
                // packet data start Idx: 4 bytes
                // packet data end Idx: 4 bytes
                // reserved: 16 bytes
                int nextDataIdx = Math.min(dataIdx + MAX_PACKET_SIZE, allBytes.length);
                byte[] thisPartOfData = new byte[37 + nextDataIdx - dataIdx];
                thisPartOfData[0] = (byte)RtcAgent.this.mAgentId;
                System.arraycopy(allBytes, dataIdx, thisPartOfData, 37, nextDataIdx - dataIdx);
                ByteBuffer buffer = ByteBuffer.wrap(thisPartOfData);
                buffer.putInt(1, pktId);
                buffer.putInt(5, allBytes.length);
                buffer.putInt(9, packetSectionId);
                buffer.putInt(13, dataIdx);
                buffer.putInt(17, nextDataIdx);
                /*Log.d(TAG, "RtcAgent.sendData to " + remoteAddr + ": agentId = " + RtcAgent.this.mAgentId + " packetId = " + packetId
                        + " packet length = " + allBytes.length + " packetSectionId = " + packetSectionId
                        + " dataIdx = " + dataIdx + " nextDataIdx = " + nextDataIdx);*/
                peer.dataChannel.send(new DataChannel.Buffer(buffer, false));
                packetSectionId ++;
                dataIdx = nextDataIdx;
            }
        }
    }

}
