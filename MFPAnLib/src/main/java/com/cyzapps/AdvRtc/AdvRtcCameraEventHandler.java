package com.cyzapps.AdvRtc;

import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.OSAdapter.AndroidRtcMMediaMan;
import com.cyzapps.OSAdapter.RtcMMediaManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.CameraVideoCapturer;

/**
 * Created by tony on 26/02/2018.
 */

public class AdvRtcCameraEventHandler implements CameraVideoCapturer.CameraEventsHandler {

    private void generateCameraEvent(String type, String content) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("type", type);
            payload.put("content", content);
            RtcMMediaManager.RtcMMediaEvent rtcMMediaEvent = new RtcMMediaManager.RtcMMediaEvent("", -1, "camera", payload.toString());
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCameraError(String s) {
        generateCameraEvent("error", s);
    }

    @Override
    public void onCameraDisconnected() {
        generateCameraEvent("disconnected", "");
    }

    @Override
    public void onCameraFreezed(String s) {
        generateCameraEvent("freezed", s);
    }

    @Override
    public void onCameraOpening(String s) {
        generateCameraEvent("opening", s);
    }

    @Override
    public void onFirstFrameAvailable() {
        generateCameraEvent("first_frame_available", "");
    }

    @Override
    public void onCameraClosed() {
        generateCameraEvent("closed", "");
    }
}
