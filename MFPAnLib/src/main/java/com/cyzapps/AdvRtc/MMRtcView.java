package com.cyzapps.AdvRtc;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.cyzapps.GI2DAdapter.ActivityGDIDaemon;
import com.cyzapps.GI2DAdapter.FlatGDIView;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class MMRtcView {

    public ViewGroup viewGroup;
    public MMRtcView(ViewGroup vg) {
        viewGroup = vg;
        gestureDetector = new GestureDetector(viewGroup.getContext(), new SingleTapConfirm());
    }
    /** webRTC video renders */
    private final EglBase rootEglBase = EglBase.create();
    public EglBase getEglBase(){ return rootEglBase; }

    private GestureDetector gestureDetector;
    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }

    public static class ProxyVideoSink implements VideoSink {
        private VideoSink mTarget;
        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (mTarget == null) {
                Log.d("RTCVideo", "Dropping frame in proxy because target is null.");
                return;
            }
            mTarget.onFrame(frame);
        }
        synchronized void setTarget(VideoSink target) {
            this.mTarget = target;
        }
    }

    public class VideoRendererPair {
        private ProxyVideoSink proxyRenderer;
        private SurfaceViewRenderer surfaceViewRenderer;
        public VideoRendererPair(ProxyVideoSink proxy, SurfaceViewRenderer renderer)    {
            // assume proxy is not null. renderer can be null.
            proxyRenderer = proxy;
            surfaceViewRenderer = renderer;
            proxy.setTarget(surfaceViewRenderer);
        }

        public void setSurfaceViewRenderer(SurfaceViewRenderer renderer) {
            surfaceViewRenderer = renderer;
            proxyRenderer.setTarget(surfaceViewRenderer);
        }

        public ProxyVideoSink getProxyRenderer() { return proxyRenderer; }
        public SurfaceViewRenderer getSurfaceViewRenderer() { return surfaceViewRenderer; }
    }

    public LinkedList<VideoRendererPair> videoRendererPairs = new LinkedList<VideoRendererPair>();

    /**
     * Add a web rtc renderer
     * @param left
     * @param top
     * @param width
     * @param height
     *      left, top width and height determine the initial position of the renderer
     * @param enableSlide tells the renderer if it can be slided.
     * @return the renderer id.
     */
    public int addVideoRenderer(int left, int top, int width, int height, boolean enableSlide) {
        FutureTask<Integer> futureResult = new FutureTask<Integer>(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                SurfaceViewRenderer rtcRenderer = new SurfaceViewRenderer(viewGroup.getContext());
                viewGroup.addView(rtcRenderer);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT); // cannot use ALIGN_PARENT_RIGHT because cannot move horizontally
                layoutParams.leftMargin = left;
                layoutParams.topMargin = top;
                // bootomMargin and rightMargin will be overridden
                layoutParams.bottomMargin = 0;
                layoutParams.rightMargin = 0;
                rtcRenderer.setLayoutParams(layoutParams);

                rtcRenderer.setOnTouchListener(new View.OnTouchListener() {
                    private int _xDelta;
                    private int _yDelta;

                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        final int X = (int) event.getRawX();
                        final int Y = (int) event.getRawY();
                        if (gestureDetector.onTouchEvent(event)) {
                            // single tap. Not do anything at this moment. But consider to do something
                        } else if (enableSlide) {
                            // drag and drop
                            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                                case MotionEvent.ACTION_DOWN:
                                    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                                    _xDelta = X - lParams.leftMargin;
                                    _yDelta = Y - lParams.topMargin;
                                    break;
                                case MotionEvent.ACTION_UP:
                                    break;
                                case MotionEvent.ACTION_POINTER_DOWN:
                                    break;
                                case MotionEvent.ACTION_POINTER_UP:
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                                    layoutParams.leftMargin = X - _xDelta;
                                    layoutParams.topMargin = Y - _yDelta;
                                    layoutParams.rightMargin = 0;
                                    layoutParams.bottomMargin = 0;
                                    view.setLayoutParams(layoutParams);
                                    break;
                            }
                            // todo Not sure if I should call FlatGDIView.this.invalidate();
                        }
                        return true;
                    }
                });

                rtcRenderer.init(rootEglBase.getEglBaseContext(), null);
                rtcRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
                rtcRenderer.setZOrderMediaOverlay(true);
                rtcRenderer.setEnableHardwareScaler(true /* enabled */);
                // add the rtc renderer into renderer list and return its position.
                ProxyVideoSink proxyRenderer = new ProxyVideoSink();
                VideoRendererPair videoRendererPair = new VideoRendererPair(proxyRenderer, rtcRenderer);
                videoRendererPairs.add(videoRendererPair);
                return videoRendererPairs.size() - 1;
            }
        });

        final Activity activity = (Activity)viewGroup.getContext();
        // we have to do it in the UI thread
        activity.runOnUiThread(futureResult);
        // this block until the result is calculated!
        try {
            int returnValue = futureResult.get();
            return returnValue;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return -1;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return -2;
        }
    }
}
