package com.cyzapps.AdvRtc;

import org.webrtc.MediaStream;
import org.webrtc.VideoSource;

/**
 * Created by tony on 23/01/2018.
 */

public interface RtcListener {

    void onLocalStream(MediaStream localStream);

    void onAddRemoteStream(RtcAgent agent, Peer peer, MediaStream remoteStream);

    void onRemoveRemoteStream(RtcAgent agent, Peer peer, MediaStream remoteStream);

    void onStatusChanged(RtcAgent agent, Peer peer, String newStatus);

    void onReceivedData(RtcAgent agent, Peer peer, String command);

    void onLogInfo(String info, int type);
}
