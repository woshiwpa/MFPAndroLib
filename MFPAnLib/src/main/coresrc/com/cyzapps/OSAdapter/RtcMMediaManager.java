package com.cyzapps.OSAdapter;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class RtcMMediaManager {

    public abstract boolean initRtcMMediaMan();

    public static class RtcMMediaEvent {
        public String peerId;
        public int sessionIndex;
        public String eventType;
        public String eventInfo;
        public RtcMMediaEvent(String peerIdVal, int sessionIdx, String evtType, String evtInfo) {
            peerId = peerIdVal;
            sessionIndex = sessionIdx;
            eventType = evtType;
            eventInfo = evtInfo;
        }
    }
    public final BlockingQueue<RtcMMediaEvent> rtcMMediaEventBlockingQueue = new LinkedBlockingQueue<RtcMMediaEvent>();

    public RtcMMediaEvent pullEvent(long lWaitingTime) throws InterruptedException {
        RtcMMediaEvent rtcMMEvent = null;
        if (lWaitingTime >= 0) {
            rtcMMEvent = rtcMMediaEventBlockingQueue.poll(lWaitingTime, TimeUnit.MILLISECONDS);
        } else {
            // wait indefinitely
            rtcMMEvent = rtcMMediaEventBlockingQueue.take();
        }
        return rtcMMEvent;
    }

    public abstract boolean createOffer(String peerId, Map<String, String> mandatoryConstraints, Map<String, String> optionalConstraints);

    public abstract boolean createAnswer(String peerId, String sdpType, String sdpContent, Map<String, String> mandatoryConstraints, Map<String, String> optionalConstraints);

    public abstract boolean setRemoteDescription(String peerId, String sdpType, String sdpContent);

    public abstract boolean addIceCandidate(String peerId, String payload);

    public abstract boolean addPeerStream(String peerId);

    public abstract void removePeerStream(String peerId);

    public abstract void closePeer(String peerId);
}
