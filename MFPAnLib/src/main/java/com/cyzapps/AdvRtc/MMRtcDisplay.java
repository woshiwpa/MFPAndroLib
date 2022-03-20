package com.cyzapps.AdvRtc;

public interface MMRtcDisplay {
    public abstract int addRtcVideoOutput(int left, int top, int width, int height, boolean enableSlide);
    public abstract boolean startLocalStream(int videoOutputId);
    public abstract void stopLocalStream();
    public abstract boolean startVideoCapturer();
    public abstract void stopVideoCapturer();
    public abstract boolean setVideoTrackEnable(int idx, boolean enable);
    public abstract boolean getVideoTrackEnable(int idx);
    public abstract boolean setAudioTrackEnable(int idx, boolean enable);
    public abstract boolean getAudioTrackEnable(int idx);
    public abstract int[] getRtcVideoOutputLeftTop(int id);
    public abstract int getRtcVideoOutputCount();
    public abstract boolean linkVideoStream(String peerId, int trackId, int videoOutputId);
    public abstract boolean unlinkVideoStream(String peerId, int trackId);
    public abstract int unlinkVideoStream(int videoOutputId);

}