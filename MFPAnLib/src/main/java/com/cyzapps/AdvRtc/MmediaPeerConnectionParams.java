package com.cyzapps.AdvRtc;

/**
 * Created by tony on 11/02/2018.
 */

public class MmediaPeerConnectionParams {
    public final boolean videoCallEnabled;
    public final boolean loopback;
    public final int videoWidth;
    public final int videoHeight;
    public final int videoFps;
    public final int videoStartBitrate;
    public final String videoCodec;
    public final boolean videoCodecHwAcceleration;
    public final int audioStartBitrate;
    public final String audioCodec;
    public final boolean cpuOveruseDetection;
    public MmediaPeerConnectionParams(
            boolean videoCallEnabled, boolean loopback,
            int videoWidth, int videoHeight, int videoFps, int videoStartBitrate,
            String videoCodec, boolean videoCodecHwAcceleration,
            int audioStartBitrate, String audioCodec,
            boolean cpuOveruseDetection) {
        this.videoCallEnabled = videoCallEnabled;
        this.loopback = loopback;
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.videoFps = videoFps;
        this.videoStartBitrate = videoStartBitrate;
        this.videoCodec = videoCodec;
        this.videoCodecHwAcceleration = videoCodecHwAcceleration;
        this.audioStartBitrate = audioStartBitrate;
        this.audioCodec = audioCodec;
        this.cpuOveruseDetection = cpuOveruseDetection;
    }
}
