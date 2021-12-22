package com.cyzapps.GI2DAdapter;


import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.cyzapps.JGI2D.GIEvent;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClassExtObjRef;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.PlotAdapter.FlatChartView.LimitedSizeStack;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSink;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

public class FlatGDIView extends RelativeLayout {
    /** the chart to be drawn */
    public FlatGDI mflatGDI = null;

    /** The paint to be used when drawing the chart. */
    private Paint mPaint = new Paint();

    /** The view bounds. */
    private Rect mRect = new Rect();

    /** The old x coordinates. Use float here coz they definitely are Android's coordinate. The length should be no grate than 16.*/
    private LimitedSizeStack mlistfOldX = new LimitedSizeStack(16);
    /** The old y coordinates. The length should be no grate than 16.*/
    private LimitedSizeStack mlistfOldY = new LimitedSizeStack(16);
    /** The old x1 coordinates. Use float here coz they definitely are Android's coordinate*/

    private float mfOldX1 = -1;
    /** The old y1 coordinate. */
    private float mfOldY1 = -1;
    /** The old x2 coordinate. */
    private float mfOldX2 = -1;
    /** The old y2 coordinate. */
    private float mfOldY2 = -1;

    /* // disable default constructor.
    public FlatGDIView(Context context) {
        super(context);
    }*/

    public FlatGDIView(Context context, FlatGDI flatGDI) {
        super(context);
        mflatGDI = flatGDI;
        gestureDetector = new GestureDetector(context, new SingleTapConfirm());
    }

    public Activity getGDIActivity() {
        return (Activity)getContext();
    }


    @Override
    protected void onSizeChanged(int w, int h, int wOld, int hOld) {
        super.onSizeChanged(w, h, wOld, hOld);

        mflatGDI.resize(w, h, wOld, hOld);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d("ROTCRASH", "OnDraw starts : this=" + this.toString()
                + " canvas=" + ((canvas == null)?"null":canvas.toString())
                + " paint=" + ((mPaint == null)?"null":mPaint.toString()));
        super.onDraw(canvas);
        canvas.getClipBounds(mRect);
        double top = mRect.top;
        double left = mRect.left;
        double width = mRect.width();
        double height = mRect.height();

        mflatGDI.setCurrentGraphics(mPaint, canvas);
        mflatGDI.draw(left, top, width, height);
        Log.d("ROTCRASH", "OnDraw ends");
    }

    /**
     * Handles the touch event.
     *
     * @param event
     *            the touch event
     */
    public boolean handleTouch(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;	// same as getActionMask
        if (action == MotionEvent.ACTION_MOVE) {
            if (event.getPointerCount() == 1)	{	// slide
                float newX = event.getX(0);
                float newY = event.getY(0);
                if (mlistfOldX.size() > 0 && mlistfOldY.size() > 0)	{	// valid last position.
                    /* no longer move chart at each moving event in Android because continuous
                     * move in Android is very sluggish. Move once all all moving event finishes.
                     */
                    GIEvent gIEvent = new GIEvent();
                    gIEvent.menumEventType = GIEvent.EVENTTYPE.POINTER_DRAGGED;
                    try {
                        gIEvent.setInfo("button", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO)); // button id is not supported.
                        gIEvent.setInfo("x", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(newX, true)));
                        gIEvent.setInfo("y", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(newY, true)));
                        gIEvent.setInfo("last_x", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(mlistfOldX.getFirst(), true)));
                        gIEvent.setInfo("last_y", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(mlistfOldY.getFirst(), true)));
                        // e is required to run e.getComponent().repaint() in the event handler.
                        gIEvent.setInfo("event", new DataClassExtObjRef(event));
                    } catch (ErrProcessor.JFCALCExpErrException ex) {
                        // will not be here. If here, something must be very wrong.
                        ex.printStackTrace();
                    }
                    mflatGDI.addGIEvent(gIEvent);
                    Log.d("GIEventLog", "event qsize=" + mflatGDI.mqueueGIEvents1.size() + " DRAG : x=" + newX + " y=" + newY + " last_x=" + mlistfOldX.getFirst() + " last_y=" + mlistfOldY.getFirst());
                    Float fXLast = mlistfOldX.getLast(), fYLast = mlistfOldY.getLast();
                    mlistfOldX.clear();
                    mlistfOldY.clear();
                    mlistfOldX.addFirst(fXLast);
                    mlistfOldY.addFirst(fYLast);
                }
                mfOldX2 = -1;
                mfOldY2 = -1;
                mfOldX1 = newX;
                mfOldY1 = newY;
                mlistfOldX.addFirst(newX);
                mlistfOldY.addFirst(newY);
            } else if (event.getPointerCount() == 2)	{	// pinch
                mlistfOldX.clear();	// clear old xy lists. this prevent miss recognize pinch to move
                mlistfOldY.clear();
            }
        } else if (action == MotionEvent.ACTION_DOWN) {
            mfOldX1 = event.getX(0);
            mfOldY1 = event.getY(0);
            GIEvent gIEvent = new GIEvent();
            gIEvent.menumEventType = GIEvent.EVENTTYPE.POINTER_DOWN;
            try {
                gIEvent.setInfo("button", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO));
                gIEvent.setInfo("x", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(mfOldX1, true)));
                gIEvent.setInfo("y", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(mfOldY1, true)));
                // e is required to run e.getComponent().repaint() in the event handler.
                gIEvent.setInfo("event", new DataClassExtObjRef(event));
            } catch (ErrProcessor.JFCALCExpErrException ex) {
                // will not be here. If here, something must be very wrong.
                ex.printStackTrace();
            }
            mflatGDI.addGIEvent(gIEvent);
            Log.d("GIEventLog", "event qsize=" + mflatGDI.mqueueGIEvents1.size() + " DOWN : x=" + mfOldX1 + " y=" + mfOldY1);
            mlistfOldX.clear();
            mlistfOldX.addFirst(mfOldX1);
            mlistfOldY.clear();
            mlistfOldY.addFirst(mfOldY1);
        } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
            mlistfOldX.clear();
            mlistfOldY.clear();
            if (event.getPointerCount() == 2)	{
                mfOldX1 = event.getX(0);
                mfOldY1 = event.getY(0);
                mfOldX2 = event.getX(1);
                mfOldY2 = event.getY(1);
            } else if (event.getPointerCount() > 2)	{
                // if more than two pointers, exit pinch to zoom mode.
                mfOldX1 = -1;
                mfOldY1 = -1;
                mfOldX2 = -1;
                mfOldY2 = -1;
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            // a pointer down implies that movement track is cleared.
            if (action == MotionEvent.ACTION_UP && mlistfOldX.size() == mlistfOldY.size()) {
                float x = event.getX(0);
                float y = event.getY(0);
                GIEvent gIEvent = new GIEvent();
                gIEvent.menumEventType = GIEvent.EVENTTYPE.POINTER_UP;
                try {
                    gIEvent.setInfo("button", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO));
                    gIEvent.setInfo("x", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(x, true)));
                    gIEvent.setInfo("y", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(y, true)));
                    // e is required to run e.getComponent().repaint() in the event handler.
                    gIEvent.setInfo("event", new DataClassExtObjRef(event));
                } catch (ErrProcessor.JFCALCExpErrException ex) {
                    // will not be here. If here, something must be very wrong.
                    ex.printStackTrace();
                }
                mflatGDI.addGIEvent(gIEvent);
                Log.d("GIEventLog", "event qsize=" + mflatGDI.mqueueGIEvents1.size() + " UP : x=" + x + " y=" + y);
                if (mlistfOldX.size() == 1 && x == mlistfOldX.getFirst() && y == mlistfOldY.getFirst()) {
                    // this is tap event.
                    GIEvent gIEvent1 = new GIEvent();
                    gIEvent1.menumEventType = GIEvent.EVENTTYPE.POINTER_CLICKED;
                    try {
                        gIEvent1.setInfo("button", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO));
                        gIEvent1.setInfo("x", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(x, true)));
                        gIEvent1.setInfo("y", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(y, true)));
                        // e is required to run e.getComponent().repaint() in the event handler.
                        gIEvent1.setInfo("event", new DataClassExtObjRef(event));
                    } catch (ErrProcessor.JFCALCExpErrException ex) {
                        // will not be here. If here, something must be very wrong.
                        ex.printStackTrace();
                    }
                    mflatGDI.addGIEvent(gIEvent1);
                    Log.d("GIEventLog", "event qsize=" + mflatGDI.mqueueGIEvents1.size() + " CLICK : x=" + x + " y=" + y);
                } else if (mlistfOldX.size() > 1) {
                    // we just finished a series moving event.
                    GIEvent gIEvent1 = new GIEvent();
                    gIEvent1.menumEventType = GIEvent.EVENTTYPE.POINTER_SLIDED;
                    try {
                        gIEvent1.setInfo("button", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO));
                        gIEvent1.setInfo("x", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(x, true)));
                        gIEvent1.setInfo("y", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(y, true)));
                        gIEvent1.setInfo("last_x", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(mlistfOldX.getLast(), true)));
                        gIEvent1.setInfo("last_y", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(mlistfOldY.getLast(), true)));
                        // e is required to run e.getComponent().repaint() in the event handler.
                        gIEvent1.setInfo("event", new DataClassExtObjRef(event));
                    } catch (ErrProcessor.JFCALCExpErrException ex) {
                        // will not be here. If here, something must be very wrong.
                        ex.printStackTrace();
                    }
                    mflatGDI.addGIEvent(gIEvent1);
                    Log.d("GIEventLog", "event qsize=" + mflatGDI.mqueueGIEvents1.size() + " SLIDE : x=" + x + " y=" + y + " last_x=" + mlistfOldX.getLast() + " last_y=" + mlistfOldY.getLast());
                }
            } else if (event.getPointerCount() == 2) { // Action pointer up.
                float newX = event.getX(0);
                float newY = event.getY(0);
                float newX2 = event.getX(1);
                float newY2 = event.getY(1);
                if (mfOldX1 >= 0 && mfOldY1 >= 0 && mfOldX2 >= 0 && mfOldY2 >= 0) {    // valid last positions
                    float newDeltaX = Math.abs(newX - newX2);
                    float newDeltaY = Math.abs(newY - newY2);
                    float oldDeltaX = Math.abs(mfOldX1 - mfOldX2);
                    float oldDeltaY = Math.abs(mfOldY1 - mfOldY2);
                    float fXMove = Math.abs((newX2 - newX) - (mfOldX2 - mfOldX1));
                    float fYMove = Math.abs((newY2 - newY) - (mfOldY2 - mfOldY1));
                    // if both oldDeltaX and oldDeltaY are 0, we cannot calculate zoomRate,
                    // if both fXMove and fYMove = 0, there is actually no move.
                    boolean bPinched = (oldDeltaX != 0 || oldDeltaY != 0) && (fXMove != 0 || fYMove != 0);
                    if (bPinched) {
                        float tan = fYMove / fXMove;
                        float zoomRate = 1;
                        if (tan <= 0.577) {
                            // 0.577 is the approximate value of tan(Pi/6)
                            zoomRate = (float) Math.sqrt(
                                    (newDeltaX * newDeltaX + newDeltaY * newDeltaY)
                                            / (oldDeltaX * oldDeltaX + oldDeltaY * oldDeltaY));
                        } else if (tan >= 1.732) {
                            // 1.732 is the approximate value of tan(Pi/3)
                            zoomRate = (float) Math.sqrt(
                                    (newDeltaX * newDeltaX + newDeltaY * newDeltaY)
                                            / (oldDeltaX * oldDeltaX + oldDeltaY * oldDeltaY));
                        } else {
                            // pinch zoom diagonally
                            zoomRate = (float) Math.sqrt(
                                    (newDeltaX * newDeltaX + newDeltaY * newDeltaY)
                                            / (oldDeltaX * oldDeltaX + oldDeltaY * oldDeltaY));
                        }
                        GIEvent gIEvent = new GIEvent();
                        gIEvent.menumEventType = GIEvent.EVENTTYPE.POINTER_PINCHED;
                        try {
                            gIEvent.setInfo("button", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO));
                            gIEvent.setInfo("x", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(newX, true)));
                            gIEvent.setInfo("y", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(newY, true)));
                            gIEvent.setInfo("x2", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(newX2, true)));
                            gIEvent.setInfo("y2", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(newY2, true)));
                            gIEvent.setInfo("last_x", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(mfOldX1, true)));
                            gIEvent.setInfo("last_y", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(mfOldY1, true)));
                            gIEvent.setInfo("last_x2", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(mfOldX2, true)));
                            gIEvent.setInfo("last_y2", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(mfOldY2, true)));
                            gIEvent.setInfo("zoom_rate", new DataClassSingleNum(DCHelper.DATATYPES.DATUM_MFPDEC, new MFPNumeric(zoomRate, true)));
                            // e is required to run e.getComponent().repaint() in the event handler.
                            gIEvent.setInfo("event", new DataClassExtObjRef(event));
                        } catch (ErrProcessor.JFCALCExpErrException ex) {
                            // will not be here. If here, something must be very wrong.
                            ex.printStackTrace();
                        }
                        mflatGDI.addGIEvent(gIEvent);
                        Log.d("GIEventLog", "event qsize=" + mflatGDI.mqueueGIEvents1.size() + " PINCH : x=" + newX + " y=" + newY + " x2=" + newX2 + " y2=" + newY2
                                + " last_x=" + mfOldX1 + " last_y=" + mfOldY1 + " last_x2=" + mfOldX2 + " last_y2=" + mfOldY2 + " zoom=" + zoomRate);
                    }
                }
                mfOldX1 = newX;
                mfOldY1 = newY;
                mfOldX2 = newX2;
                mfOldY2 = newY2;
            }
            mlistfOldX.clear();
            mlistfOldY.clear();
            mfOldX1 = -1;
            mfOldY1 = -1;
            mfOldX2 = -1;
            mfOldY2 = -1;
        }
        //TODO: enable or disable click.
        return true;	//!mRenderer.isClickEnabled();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return handleTouch(event);
    }

    /**
     * Schedule a view content repaint.
     */
    public void repaint() {
        getGDIActivity().runOnUiThread(new Runnable() {
            public void run() {
                invalidate();
            }
        });
    }

    /**
     * Schedule a view content repaint, in the specified rectangle area.
     */
    public void repaint(final int left, final int top, final int right,
                        final int bottom) {
        getGDIActivity().runOnUiThread(new Runnable() {
            public void run() {
                invalidate(left, top, right, bottom);
            }
        });
    }

    /** webRTC video renders */
    private final EglBase rootEglBase = EglBase.create();

    private GestureDetector gestureDetector;
    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }

    public class ProxyRenderer<T extends VideoRenderer.Callbacks & VideoSink>
            implements VideoRenderer.Callbacks, VideoSink {
        private T target;
        public AtomicReference<VideoRenderer> videoRendererRef = new AtomicReference<VideoRenderer>();

        @Override
        synchronized public void renderFrame(VideoRenderer.I420Frame frame) {
            if (target == null) {
                Log.d("RTCVideo", "ProxyRenderer.renderFrame : Dropping frame in proxy because target is null.");
                VideoRenderer.renderFrameDone(frame);
                return;
            }

            target.renderFrame(frame);
        }

        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (target == null) {
                Log.d("RTCVideo", "ProxyRenderer.onFrame : Dropping frame in proxy because target is null.");
                return;
            }

            target.onFrame(frame);
        }

        synchronized public void setTarget(T target) {
            this.target = target;
        }

        synchronized public int getTargetHashCode() {
            if (this.target == null) {
                return 0;
            }
            return this.target.hashCode();
        }

        synchronized public T getTarget() {
            return target;
        }
    }

    public class VideoRendererPair {
        private ProxyRenderer proxyRenderer;
        private SurfaceViewRenderer surfaceViewRenderer;
        public VideoRendererPair(ProxyRenderer proxy, SurfaceViewRenderer renderer)    {
            // assume proxy is not null. renderer can be null.
            proxyRenderer = proxy;
            surfaceViewRenderer = renderer;
            proxy.setTarget(surfaceViewRenderer);
        }

        public void setSurfaceViewRenderer(SurfaceViewRenderer renderer) {
            surfaceViewRenderer = renderer;
            proxyRenderer.setTarget(surfaceViewRenderer);
        }

        public ProxyRenderer getProxyRenderer() { return proxyRenderer; }
        public SurfaceViewRenderer getSurfaceViewRenderer() { return surfaceViewRenderer; }
    }

    protected LinkedList<VideoRendererPair> videoRendererPairs = new LinkedList<VideoRendererPair>();

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
                SurfaceViewRenderer rtcRenderer = new SurfaceViewRenderer(FlatGDIView.this.getContext());
                addView(rtcRenderer);
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
                ProxyRenderer proxyRenderer = new ProxyRenderer();
                VideoRendererPair videoRendererPair = new VideoRendererPair(proxyRenderer, rtcRenderer);
                videoRendererPairs.add(videoRendererPair);
                return videoRendererPairs.size() - 1;
            }
        });

        final ActivityGDIDaemon activityGDIDaemon = (ActivityGDIDaemon)getContext();
        // we have to do it in the UI thread
        activityGDIDaemon.runOnUiThread(futureResult);
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
