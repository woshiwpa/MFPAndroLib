package com.cyzapps.AdvRtc;

import android.util.Log;

import com.cyzapps.OSAdapter.ParallelManager.ConnectObject;
import com.cyzapps.OSAdapter.ParallelManager.LocalObject;
import com.cyzapps.OSAdapter.ParallelManager.MFP4AndroidCommMan;
import com.cyzapps.OSAdapter.ParallelManager.TCPIPConnMan;
import com.cyzapps.OSAdapter.ParallelManager.WEBRTCConnMan;
import com.cyzapps.mfpanlib.MFPAndroidLib;

import org.webrtc.DataChannel;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by tony on 23/01/2018.
 */

public class DcObserver implements DataChannel.Observer {
    private final static String TAG = "New_AdvRtcapp_Debug";
    public Peer mPeer = null;

    public DcObserver(Peer peer) {
        mPeer = peer;
    }

    @Override
    public void onBufferedAmountChange(long l) {
        Log.d(TAG, "DcObserver.onBufferedAmountChange: Agent " + mPeer.mAgent.mAgentId + " Peer " + mPeer.remoteAddress + " sessionId " + mPeer.currentSessionId
                + " buffer amount changed to " + l);
    }

    @Override
    public void onStateChange() {
        // it seems that, this function is called within the RTC thread.
        if (mPeer.dataChannel == null) {
            Log.d(TAG, "DcObserver.onStateChange: Agent " + mPeer.mAgent.mAgentId + " Peer " + mPeer.remoteAddress + " sessionId " + mPeer.currentSessionId
                    + " DataChannel is NULL.");
        } else {
            // dataChannel will not be null
            Log.d(TAG, "DcObserver.onStateChange: Agent " + mPeer.mAgent.mAgentId + " Peer " + mPeer.remoteAddress + " sessionId " + mPeer.currentSessionId
                    + " DataChannel state is " + mPeer.dataChannel.state());
            MFP4AndroidCommMan mfp4AndroidCommMan = (MFP4AndroidCommMan) RtcAgent.mListener;
            String localAddress = RtcAppClient.getLocalAddress();
            LocalObject.LocalKey localKey = new LocalObject.LocalKey("WEBRTC", localAddress);
            LocalObject localObj = mfp4AndroidCommMan.findLocal(localKey);
            if (mPeer.dataChannel.state() == DataChannel.State.OPEN) {
                // Now the data channel is open
                if (mPeer.mAgent == MFPAndroidLib.getRtcAppClient().dataClient1) {
                    // this is client.
                    Log.d(TAG, "DcObserver.onStateChange : This is client, connect successfully");
                    if (localObj == null) {
                        Log.d(TAG, "DcObserver.onStateChange : No out local found");
                    } else {
                        WEBRTCConnMan webrtcConnMan = new WEBRTCConnMan(localObj, false, mPeer.remoteAddress, new ConnectObject.ConnectSettings(), new ConnectObject.ConnectAdditionalInfo(), mPeer); // empty remote address means it is server side
                        localObj.addConnect(mPeer.remoteAddress, webrtcConnMan);
                        // connect function does not block at EmailSignalChannelAgent.wait() because if no connect can be setup
                        // connect function needs to return a failure.
                    }
                } // do not worry about server because when Peer.onDataChannel is called, dataChannel of remote peer has been open
                // so in general DcObserver.OnStateChange will not be called when remote peer's dataChannel turns to open because
                // it has been open before registering its DcObserver.
            } else if (mPeer.dataChannel.state() == DataChannel.State.CLOSING || mPeer.dataChannel.state() == DataChannel.State.CLOSED) {
                if (mPeer.connectObj != null) {
                    mPeer.connectObj.setIsShutdown(true);
                }
            }
        }
    }

    public static class MsgPktBuffer {
        public LinkedList<int[]> pktStartEndPairs;
        public byte[] wholePkt;
        public int currentAccumBytes;

        public MsgPktBuffer(int pktTotalSize, int pktSectionId, int pktDataStartIdx, int pktDataEndIdx, byte[] bytes) {
            pktStartEndPairs = new LinkedList<int[]>();
            wholePkt = new byte[pktTotalSize];
            currentAccumBytes = 0;
            bufferPktData(pktTotalSize, pktSectionId, pktDataStartIdx, pktDataEndIdx, bytes);
        }

        public boolean bufferPktData(int pktTotalSize, int pktSectionId, int pktDataStartIdx, int pktDataEndIdx, byte[] bytes) {
            if (wholePkt.length != pktTotalSize) {
                return false;   // size doesn't match.
            }
            int startEndPairsInitSize = pktStartEndPairs.size();
            for (int idx = startEndPairsInitSize; idx <= pktSectionId; idx ++) {
                pktStartEndPairs.add(null);
            }
            if (null == pktStartEndPairs.get(pktSectionId)) {
                // first we need to ensure that the indices match.
                if (pktSectionId > 0
                        && null != pktStartEndPairs.get(pktSectionId - 1)
                        && pktStartEndPairs.get(pktSectionId - 1)[1] != pktDataStartIdx) {
                    return false;
                }
                if (pktSectionId < pktStartEndPairs.size() - 1
                        && null != pktStartEndPairs.get(pktSectionId + 1)
                        && pktStartEndPairs.get(pktSectionId + 1)[0] != pktDataEndIdx) {
                    return false;
                }
                // then we fill the buffer.
                int[] startEndPair = new int[2];
                startEndPair[0] = pktDataStartIdx;
                startEndPair[1] = pktDataEndIdx;
                pktStartEndPairs.set(pktSectionId, startEndPair);
                int thisPartLen = pktDataEndIdx - pktDataStartIdx;
                currentAccumBytes += thisPartLen;
                System.arraycopy(bytes, 0, wholePkt, pktDataStartIdx, thisPartLen);
                return true;
            } else {
                // this piece of data exists.
                return false;
            }
        }

        public boolean ready() {
            return currentAccumBytes == wholePkt.length;
            /* // we use a quick way to check if it is ready. This saves time.
            for (int idx = pktStartEndPairs.size() - 1; idx >= 0; idx --) {
                if (pktStartEndPairs.get(idx) == null) {
                    return false;
                }
                if (idx == 0 && pktStartEndPairs.get(idx)[0] != 0) {
                    return false;
                }
                if (idx < pktStartEndPairs.size() - 1) {
                    if (pktStartEndPairs.get(idx)[1] != pktStartEndPairs.get(idx + 1)[0]) {
                        return false;
                    }
                } else {
                    if (pktStartEndPairs.get(idx)[1] != wholePkt.length) {
                        return false;
                    }
                }
            }
            return true;*/
        }
    };
    public Map<Integer, MsgPktBuffer> messagesBuffer = new HashMap<Integer, MsgPktBuffer>();    // buffer for messages haven't finished transmission.

    @Override
    public void onMessage(DataChannel.Buffer buffer) {
        ByteBuffer data = buffer.data;
        int bufferSize = data.remaining();
        if (bufferSize < 37 + 1) {
            // this packet is incomplete.
            Log.d(TAG, "DcObserver.onMessage from " + mPeer.remoteAddress + ": Receive incomplete packet.");
        } else {
            int remoteRtcAgentId = data.get();
            int packetId = data.getInt();
            int pktTotalSize = data.getInt();
            int pktSectionId = data.getInt();
            int pktDataStartIdx = data.getInt();
            int pktDataEndIdx = data.getInt();
            data.getInt();data.getInt();data.getInt();data.getInt();    // jump over reserved bytes.
            if (remoteRtcAgentId < 0 || packetId < 0 || pktTotalSize <= 0 || pktSectionId < 0 || pktDataStartIdx < 0 || pktDataEndIdx < 0
                || (pktDataEndIdx - pktDataStartIdx) != (bufferSize - 37) || pktDataEndIdx > pktTotalSize) {
                Log.d(TAG, "DcObserver.onMessage " + mPeer.remoteAddress + ": Receive invalid packet. remoteRtcAgentId is " + remoteRtcAgentId
                                + " packet Id is " + packetId + " packet total size is " + pktTotalSize
                                + " packet Section Id is " + pktSectionId + " packet data start idx is " + pktDataStartIdx
                                + " packet data end idx is " + pktDataEndIdx);
            } else {
                /*Log.d(TAG, "DcObserver.onMessage " + mPeer.remoteAddress + ": Receive packet. remoteRtcAgentId is " + remoteRtcAgentId
                        + " packet Id is " + packetId + " packet total size is " + pktTotalSize
                        + " packet Section Id is " + pktSectionId + " packet data start idx is " + pktDataStartIdx
                        + " packet data end idx is " + pktDataEndIdx);*/
                byte[] bytes = new byte[bufferSize - 37];
                data.get(bytes, 0, bufferSize - 37);
                if (messagesBuffer.containsKey(packetId)) {
                    messagesBuffer.get(packetId).bufferPktData(pktTotalSize, pktSectionId, pktDataStartIdx, pktDataEndIdx, bytes);
                } else {
                    messagesBuffer.put(packetId, new MsgPktBuffer(pktTotalSize, pktSectionId, pktDataStartIdx, pktDataEndIdx, bytes));
                }
                if (messagesBuffer.get(packetId).ready()) {
                    MsgPktBuffer msgBytes = messagesBuffer.remove(packetId);    // free the memory.
                    final String command = new String(msgBytes.wholePkt);
                    // peerconnection API is not used inside onReceivedData function
                    // only mPeer.connectObj is used.
                    // so that we need not to worry about thread here anyway.
                    RtcAgent.mListener.onReceivedData(mPeer.mAgent, mPeer, command);
                }
            }
        }
    }
}
