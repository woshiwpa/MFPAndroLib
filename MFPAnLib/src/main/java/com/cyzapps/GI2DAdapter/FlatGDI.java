package com.cyzapps.GI2DAdapter;


import com.cyzapps.AdvRtc.AdvRtcCameraEventHandler;
import com.cyzapps.AdvRtc.MMPeer;
import com.cyzapps.AdvRtc.MMRtcDisplay;
import com.cyzapps.AdvRtc.MMRtcView;
import com.cyzapps.AdvRtc.MmediaPeerConnectionParams;
import com.cyzapps.AdvRtc.RtcAgent;
import com.cyzapps.JGI2D.DisplayLib;
import com.cyzapps.JGI2D.DrawLib;
import com.cyzapps.JGI2D.GIEvent;
import com.cyzapps.JGI2D.Display2D;
import com.cyzapps.JGI2D.PaintingCallBacks;
import com.cyzapps.JGI2D.PaintingCallBacks.PaintingCallBack;
import com.cyzapps.JGI2D.PaintingCallBacks.UpdatePaintingCallBack;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassExtObjRef;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.MultimediaAdapter.ImageDisplay;
import com.cyzapps.MultimediaAdapter.ImageDisplay.Graphics;
import com.cyzapps.MultimediaAdapter.ImageMgrAndroid;
import com.cyzapps.OSAdapter.AndroidRtcMMediaMan;
import com.cyzapps.VisualMFP.LineStyle;
import com.cyzapps.VisualMFP.LineStyle.LINEPATTERN;
import com.cyzapps.VisualMFP.PointStyle;
import com.cyzapps.VisualMFP.TextStyle;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Capturer;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCodecInfo;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FlatGDI extends Display2D implements MMRtcDisplay {

    public static final String LOG_TAG = "GI2DAdapter.FlatGDI";

    private static AtomicInteger sid = new AtomicInteger();

    private int mnId = 0;
    public int getId() {
        return mnId;
    }

    public boolean mbHasBeenShutdown = false;

    protected ImageDisplay.SizeChoices msizeChoices = new ImageDisplay.SizeChoices();

    protected ConcurrentLinkedQueue<GIEvent> mqueueGIEvents0 = new ConcurrentLinkedQueue<>();
    protected ConcurrentLinkedQueue<GIEvent> mqueueGIEvents1 = new ConcurrentLinkedQueue<>();
    
    protected ConcurrentLinkedQueue<PaintingCallBack> mqueuePaintingCallBacks = new ConcurrentLinkedQueue<>();

    protected ImageMgrAndroid.ImageWrapper mimageBkGrnd = null;    // background image.
    protected int mBkgrndImgMode = 0;   // 0 means actually, 1 means scaled, 2 means tiled

    protected String mstrTitle = "";

    @Override
    public DisplayLib.GraphicDisplayType getDisplayType() {
        if (isDisplayOnLive()) {
            return DisplayLib.GraphicDisplayType.SCREEN_2D_DISPLAY;
        } else {
            return DisplayLib.GraphicDisplayType.INVALID_DISPLAY;
        }
    }

    protected final int MAX_EVENT_QUEUE1_LENGTH = 128;
    public void addGIEvent(GIEvent giEvent) {
        if (giEvent.menumEventType == GIEvent.EVENTTYPE.GDI_INITIALIZE
                || giEvent.menumEventType == GIEvent.EVENTTYPE.GDI_CLOSE) {
            mqueueGIEvents0.add(giEvent);
        } else {
            mqueueGIEvents1.add(giEvent);
            while (mqueueGIEvents1.size() > MAX_EVENT_QUEUE1_LENGTH) {
                mqueueGIEvents1.poll();
            }
        }
    }

    @Override
    public GIEvent pullGIEvent() {
        GIEvent event = mqueueGIEvents0.poll();
        if (event == null) {
            event = mqueueGIEvents1.poll();
        }
        return event;
    }
    
    @Override
    public void addPaintingCallBack(PaintingCallBack paintingCallBack) {
        if (paintingCallBack instanceof UpdatePaintingCallBack) {
            // remove obsolete call backs first.
            int idx = mqueuePaintingCallBacks.size() - 1;
            for (PaintingCallBack callBack : mqueuePaintingCallBacks) {
                if (callBack.isCallBackOutDated(paintingCallBack)) {
                    mqueuePaintingCallBacks.remove(callBack);
                }
            }
        }
        mqueuePaintingCallBacks.add(paintingCallBack);
        mbPCBQueueChangedAfterDraw.set(true);  //need to reset buffer because painting call back queue is changed.
     }

    @Override
    public void clearPaintingCallBacks() {
        mqueuePaintingCallBacks.clear();
    }

    // background color
    public com.cyzapps.VisualMFP.Color mcolorBkGrnd = new com.cyzapps.VisualMFP.Color();
    
    @Override
    public void setBackgroundColor(com.cyzapps.VisualMFP.Color newColor) {
        if (getGDIView() != null && !mcolorBkGrnd.isEqual(newColor)) {
            mcolorBkGrnd = newColor;
            SyncUITask syncTask = new SyncUITask(getGDIView().getGDIActivity(), new SyncUITask.SyncUITaskListener() {
                @Override
                public void onRunOnUIThread() {
                    getGDIView().setBackgroundColor(Color.argb(
                            mcolorBkGrnd.mnAlpha,
                            mcolorBkGrnd.mnR,
                            mcolorBkGrnd.mnG,
                            mcolorBkGrnd.mnB)
                    );
                }
            } );
            syncTask.startOnUiAndWait();
            // repaint(); // don't think we need to run repaint again ? Is it?
        }
    }

    @Override
    public com.cyzapps.VisualMFP.Color getBackgroundColor() {
        return mcolorBkGrnd;
    }

    @Override
    synchronized public void setBackgroundImage(DataClassExtObjRef imgHandler, int mode) {
        if (getGDIView() != null) {
            try {
                if (imgHandler == null) {
                    mimageBkGrnd = null;
                    mBkgrndImgMode = mode;
                    // do not force to repaint.
                    mbPCBQueueChangedAfterDraw.set(true);
                    //repaint();
                } else if (imgHandler.getExternalObject() instanceof ImageMgrAndroid.ImageWrapper) {
                    // only if it is a buffered image.
                    if (imgHandler.getExternalObject() != mimageBkGrnd
                            || mode != mBkgrndImgMode) {
                        mimageBkGrnd = (ImageMgrAndroid.ImageWrapper)imgHandler.getExternalObject();
                        mBkgrndImgMode = mode;
                        // not force to repaint
                        mbPCBQueueChangedAfterDraw.set(true);
                        // repaint();
                    }
                }
            } catch (ErrProcessor.JFCALCExpErrException ex) {
                // will not be here.
                ex.printStackTrace();
            }
        }
    }

    @Override
    synchronized public DataClass getBackgroundImage() {
        if (mimageBkGrnd == null) {
            return new DataClassNull();
        } else {
            try {
                return new DataClassExtObjRef(mimageBkGrnd);
            } catch (ErrProcessor.JFCALCExpErrException ex) {
                return new DataClassNull(); // will not be here.
            }
        }
    }

    @Override
    public int getBackgroundImageMode() {
        return mBkgrndImgMode;
    }

    // default foreground color
    public com.cyzapps.VisualMFP.Color mcolorForeGrnd = new com.cyzapps.VisualMFP.Color(255, 255, 255);

    public boolean mbConfirmClose = false;

    @Override
    synchronized public void setSnapshotAsBackground(boolean bUpdateScreen, boolean bClearPaintingCallbacks) {
        if (mflatGDIView != null) {
            // sync mode
            DataClass datumImg = getSnapshotImage(bUpdateScreen, 1.0, 1.0);
            if (!datumImg.isNull()) {
                try {
                    mimageBkGrnd = (ImageMgrAndroid.ImageWrapper)(DCHelper.lightCvtOrRetDCExtObjRef(datumImg).getExternalObject());
                } catch (ErrProcessor.JFCALCExpErrException ex) {
                    mimageBkGrnd = null;
                    ex.printStackTrace();  // will not be here.
                }
            } else {
                mimageBkGrnd = null;
            }
            if (bClearPaintingCallbacks) {
                clearPaintingCallBacks();
            }
            mbPCBQueueChangedAfterDraw.set(true); // even if we do not clear PCB, still set it to true coz final painting result is different.
            // repaint(); // do not force to repaint.
        }
    }

    @Override
    public DataClass getSnapshotImage(boolean bUpdateScreen, double wRatio, double hRatio) {
        if (mflatGDIView != null && wRatio > 0 && hRatio > 0) {
            try {
                int destW = (int) (mflatGDIView.getWidth() * wRatio);
                int destH = (int) (mflatGDIView.getHeight() * hRatio);
                if (destW < 1) {
                    destW = 1;
                }
                if (destH < 1) {
                    destH = 1;
                }
                Bitmap image = Bitmap.createBitmap(destW, destH, Bitmap.Config.ARGB_8888);
                Canvas canvas=new Canvas(image);
                if (bUpdateScreen && mbPCBQueueChangedAfterDraw.get()) {
                    // we need to update screen
                    update();
                    repaint();
                    int loopCnt = 0;
                    do {
                        try {
                            Thread.sleep(10);// wait for UI thread until it is updated.
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        loopCnt ++;
                    } while(mbPCBQueueChangedAfterDraw.get() && loopCnt < 8);
                }
                try {
                    synchronized (this) {
                        // the best approach is still draw underlying bitmap of currentGraphics's canvas.
                        // Because it is possible the canvas has been updated but the view hasn't, as such
                        // mflatGDIView.getDrawingCache() may not properly reflect the view's snapshot.
                        // unfortunately the underlying bitmap is not exposed to developer so we cannot do
                        // in this way.
                        mflatGDIView.setDrawingCacheEnabled(true);
                        Bitmap bitmapCache = mflatGDIView.getDrawingCache();
                        Bitmap bitmapDrawSrc = null;
                        if (bitmapCache != null && bitmapCache.isRecycled() == false) {
                            bitmapDrawSrc = bitmapCache.copy(bitmapCache.getConfig(), false);
                        }
                        mflatGDIView.setDrawingCacheEnabled(false);
                        if (bitmapDrawSrc != null && bitmapDrawSrc.isRecycled() == false) {
                            canvas.drawBitmap(bitmapDrawSrc, new Rect(0, 0, bitmapDrawSrc.getWidth(), bitmapDrawSrc.getHeight()),
                                    new Rect(0, 0, destW, destH), new Paint());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new DataClassExtObjRef(new ImageMgrAndroid.ImageWrapper(image));
            } catch (ErrProcessor.JFCALCExpErrException ex) {
                ex.printStackTrace();
                return new DataClassNull();
            }
        } else {
            return new DataClassNull();
        }
    }

    @Override
    public void setDisplayConfirmClose(boolean bConfirmClose) {
        mbConfirmClose = bConfirmClose;
    }
    
    @Override
    public boolean getDisplayConfirmClose() {
        return mbConfirmClose;
    }


    public AtomicBoolean mbPCBQueueChangedAfterDraw = new AtomicBoolean(false);  // after last drawing painting call back queue is changed.

    protected int mnDisplayOrientation = -1;

    public static int validateDisplayOrientation(int orientation) {
        // currently supported orientations are:
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED       // -1
                || orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE  // 0
                || orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT   // 1
                || orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE   // 6
                || orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT    // 7
                || orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE  // 8
                || orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) { //9
            return orientation;
        } else {
            return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        }
    }

    public static boolean isResizingChange(int oldOrientation, int newOrientation) {
        if (oldOrientation == newOrientation) {
            return false;   // orientation setting has no change, we assume during this call, no physical rotation happens
        } else if ((oldOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                || oldOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                || oldOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
            && (newOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            || newOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            || newOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)) {
            return false;
        } else if ((oldOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                || oldOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                || oldOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT)
            && (newOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            || newOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            || newOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT)) {
            return false;   // before and after are both portrait or both landscape
        }
        return true;   // before and after could be one portrait and one landscape
    }

    /**
     * check if rotation finished
     * @param orientation : the target orientation
     * @param width : the checked width
     * @param height : the checked height
     * @return : 1 means must have finished, -1 means must haven't finished, 0 means not sure.
     */
    public static int isRotationFinished(int orientation, int width, int height) {
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                || orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                || orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            if (width <= 0 || height <= 0 || width < height) {
                return -1;
            } else {
                return 1;
            }
        } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                || orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                || orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
            if (width <= 0 || height <= 0 || width > height) {
                return -1;
            } else {
                return 1;
            }
        }
        return 0;   // we are not sure if it is finished.
    }

    @Override
    public void setDisplayOrientation(int orientation) {
        final int newOrientation = validateDisplayOrientation(orientation);
        if (getGDIView() != null) {
            Log.d("ROTCRASH", "OChange starts : current O=" + mnDisplayOrientation + " target O=" + newOrientation + " w=" + (int)mdWidth + " h=" + (int)mdHeight);
            SyncUITask syncTask = new SyncUITask(getGDIView().getGDIActivity(), new SyncUITask.SyncUITaskListener() {
                @Override
                public void onRunOnUIThread() {
                    mnDisplayOrientation = (int)getGDIView().getGDIActivity().getRequestedOrientation();
                    // do not compare mnDisplayOrientation with the new orientation setting worrying
                    // about some external change of orientation.
                    Log.d("ROTCRASH", "OChange thread begins : current O=" + mnDisplayOrientation + " target O=" + newOrientation + " w=" + (int)mdWidth + " h=" + (int)mdHeight);
                    if (mnDisplayOrientation != newOrientation) {
                        // ok, we do need to change.
                        getGDIView().getGDIActivity().setRequestedOrientation(newOrientation);
                    }
                    Log.d("ROTCRASH", "OChange thread ends : w=" + (int)mdWidth + " h=" + (int)mdHeight);
                }
            });
            syncTask.startOnUiAndWait();
            if (isResizingChange(mnDisplayOrientation, newOrientation)) {
                try {
                    Thread.sleep(250);  // wait for 250 ms until rotation finishes.
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int nWaitCnt = 0;
                while(isRotationFinished(newOrientation, (int)mdWidth, (int)mdHeight) == -1) {
                    // for sure rotation hasn't finished.
                    try {
                        Thread.sleep(50);  // wait for 50 ms until rotation finishes.
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    nWaitCnt ++;
                    if (nWaitCnt >= 3) {
                        break;  // wait for at most 150 ms.
                    }
                }
            }
            Log.d("ROTCRASH", "OChange finishes : w=" + (int)mdWidth + " h=" + (int)mdHeight);
            mnDisplayOrientation = newOrientation;
        }
    }

    @Override
    public int getDisplayOrientation() {
        return mnDisplayOrientation;    // use mnDisplayOrientation to avoid call from ui thread.
    }

    protected MMRtcView mmRtcView = null;

    protected FlatGDIView mflatGDIView = null;
    public void setGDIView(FlatGDIView flatGDIView)
    {
        mflatGDIView = flatGDIView;
        mmRtcView = new MMRtcView(mflatGDIView);
    }
    public FlatGDIView getGDIView() {
        return mflatGDIView;
    }
    public MMRtcView getMmRtcView() { return mmRtcView; }

    protected Graphics mcurrentGraphics = null;
    public void setCurrentGraphics(Graphics graphics) {
        mcurrentGraphics = graphics;
    }
    public void setCurrentGraphics(Paint p, Canvas c) {
        mcurrentGraphics = new Graphics();
        mcurrentGraphics.paint = p;
        mcurrentGraphics.canvas = c;
    }

    /**
     * this function prevent a situation where several statements
     * continously run after open_display, where display hasn't been
     * initialized.
     * @return true or false
     */
    @Override
    public boolean isDisplayOnLive() {
        return mflatGDIView != null // do not test mcurrentGraphics != null to align with JAVA on Windows
                && mbHasBeenShutdown == false
                && (mdWidth != 0 || mdHeight != 0);   // on Android platform, the display cannot be 0*0
    }

    protected void deprecateFlatGDI() {
        mflatGDIView = null;
        mcurrentGraphics = null;
        clearPaintingCallBacks();
        FlatGDIManager.mslistFlatGDI.remove(this);  // remove from list to save memory
    }

    public FlatGDI() {
        mnId = sid.getAndIncrement() + 1;
    }

    /**
     * Calculates the best text to fit into the available space.
     * @param text
     * @param width
     * @param paint
     * @return 
     */
    public static String getFitText(String text, double width, Paint paint) {
        String newText = text;
        int length = text.length();
        int diff = 0;
        while (paint.measureText(newText) > width && diff < length) {
            diff++;
            newText = text.substring(0, length - diff) + "...";
        }
        if (diff == length) {
            newText = "...";
        }
        return newText;
    }

    /**
     * Calculates How many characters can be placed in the width
     * @param width
     * @param paint
     * @return 
     */
    public static int getNumOfCharsInWidth(double width, Paint paint) {
        if (width <= 0)	{
            return 0;	// width cannot be negative.
        }
        double dWWidth = paint.measureText("W");
        return (int)(width/dWWidth);
    }

    // width and height are updated each time view's size is changed.
    protected double mdWidth = 0;
    protected double mdHeight = 0;
    @Override
    public void setDisplaySize(int width, int height) {
        // display size is unchangable in Android.
    }

    @Override
    public int[] getDisplaySize() {
        return new int[]{(int)mdWidth, (int)mdHeight};
    }

    @Override
    public void setDisplayCaption(String strCaption) {
        mstrTitle = strCaption;
        if (getGDIView() != null) {
            SyncUITask syncTask = new SyncUITask(getGDIView().getGDIActivity(), new SyncUITask.SyncUITaskListener() {
                @Override
                public void onRunOnUIThread() {
                    getGDIView().getGDIActivity().setTitle(mstrTitle);
                }
            } );
            syncTask.startOnUiAndWait();
        }
    }

    @Override
    public String getDisplayCaption() {
        if (getGDIView() != null) {
            return mstrTitle; // do not call activity.getTitle(), worrying about ui thread requirement.
        } else {
            return "";
        }
    }

    /**
     * calculate rectangular boundary of text. The text shouldn't be rotated.
     * @param text : multi-line text
     * @param x : x of the starting point of text
     * @param y : y of the starting point of text
     * @param txtStyle : text font and size
     * @return boundary rectangle [x, y, w, h]
     */
    @Override
    public int[] calcTextBoundary(String text, int x, int y, TextStyle txtStyle) {
        // Although this is not a drawing function, still has to use mcurrentGraphics.
        // Otherwise, the result may not be accurate. However, a problem is
        // mcurrentGraphics may not be initialized (i.e. is null) when display starts,
        // so may use a newly created graphics here.
        if (mcurrentGraphics == null) {
            Graphics graphics = mcurrentGraphics;
            Bitmap image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            Graphics g = Graphics.createGraphics(image, new Paint());
            int[] rect = ImageDisplay.calcTextBoundary(g, text, x, y, txtStyle);
            g.dispose();
            image.recycle();
            return rect;
        } else {
            Graphics g = mcurrentGraphics;
            int[] rect = ImageDisplay.calcTextBoundary(g, text, x, y, txtStyle);
            return rect;
        }
    }

    /**
     * calculate origin of text. The text shouldn't be rotated.
     * @param text : multi-line text
     * @param x : left of boundary rectangle
     * @param y : top of boundary rectangle
     * @param w : width of boundary rectangle
     * @param h : height of boundary rectangle
     * @param horAlign : horizontal alignment, -1 means left, 0 means center, 1 means right aligned.
     * @param verAlign : vertical alignment, -1 means left, 0 means center, 1 means right aligned.
     * @param txtStyle : text font and size
     * @return boundary rectangle [x, y, w, h]
     */
    @Override
    public int[] calcTextOrigin(String text, int x, int y, int w, int h, int horAlign, int verAlign, TextStyle txtStyle) {
        // Although this is not a drawing function, still has to use mcurrentGraphics.
        // Otherwise, the result may not be accurate. However, a problem is
        // mcurrentGraphics may not be initialized (i.e. is null) when display starts,
        // so may use a newly created graphics here.
        if (mcurrentGraphics == null) {
            Bitmap image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            Graphics g = Graphics.createGraphics(image, new Paint());
            int[] origin = ImageDisplay.calcTextOrigin(g, text, x, y, w, h, horAlign, verAlign, txtStyle);
            g.dispose();
            image.recycle();
            return origin;
        } else {
            Graphics g = mcurrentGraphics;
            int[] origin = ImageDisplay.calcTextOrigin(g, text, x, y, w, h, horAlign, verAlign, txtStyle);
            return origin;
        }
    }

    /**
     * Draw a multiple lines text. x is the starting point of first character
     * and y is the pivot point
     * @param text
     * @param x
     * @param y
     * @param txtStyle
     * @param dRotateRadian 
     */
    @Override
    public void drawText(String text, double x, double y, TextStyle txtStyle, double dRotateRadian, DrawLib.PaintingExtraInfo pei) {
        if (mcurrentGraphics == null) {
            return;
        }
        Graphics graphics = mcurrentGraphics;
        ImageDisplay.drawText(graphics, text, x, y, txtStyle, dRotateRadian, pei);
    }

    @Override
    public void drawPoint(double x, double y, PointStyle pointStyle, DrawLib.PaintingExtraInfo pei) {
        if (mcurrentGraphics == null) {
            return;
        }
        Graphics graphics = mcurrentGraphics;
        ImageDisplay.drawPoint(graphics, msizeChoices, x, y, pointStyle, pei);
    }

    @Override
    public void drawLine(double x0, double y0, double x1, double y1, LineStyle lineStyle, DrawLib.PaintingExtraInfo pei) {
        if (mcurrentGraphics == null) {
            return;
        }
        Graphics graphics = mcurrentGraphics;
        ImageDisplay.drawLine(graphics, msizeChoices, x0, y0, x1, y1, lineStyle, pei);
    }

    /**
     * draw a polygon.
     * @param points : vetex of the points.
     * @param color : color to fill or frame.
     * @param drawMode : this is an integer parameter, if it is zero or negative, the polygon is filled,
     * otherwise, the polygon is framed and the drawMode value is the border's with ( the border is always
     * solid line).
     * @param pei : extra information of painting.
     */
    @Override
    public void drawPolygon(LinkedList<double[]> points,
                            com.cyzapps.VisualMFP.Color color, int drawMode,
                            DrawLib.PaintingExtraInfo pei) {
        if (mcurrentGraphics == null) {
            return;
        }
        Graphics graphics = mcurrentGraphics;
        ImageDisplay.drawPolygon(graphics, points, color, drawMode, pei);
    }

    /**
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @param color
     * @param drawMode : this is an integer parameter, if it is zero or negative, the rectangle is filled,
     * otherwise, the rectangle is framed and the drawMode value is the border's with ( the border is always
     * solid line).
     * @param pei : extra information of painting.
     */
    @Override
    public void drawRect(double x, double y, double width, double height,
                         com.cyzapps.VisualMFP.Color color, int drawMode,
                         DrawLib.PaintingExtraInfo pei) {
        if (mcurrentGraphics == null) {
            return;
        }
        Graphics graphics = mcurrentGraphics;
        ImageDisplay.drawRect(graphics, x, y, width, height, color, drawMode, pei);
    }

    /**
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    @Override
    public void clearRect(double x, double y, double width, double height) {
        if (mcurrentGraphics == null) {
            return;
        }
        Graphics graphics = mcurrentGraphics;
        ImageDisplay.clearRect(graphics, x, y, width, height);
    }

    /**
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @param color
     * @param drawMode : this is an integer parameter, if it is zero or negative, the oval is filled,
     * otherwise, the oval is framed and the drawMode value is the border's with ( the border is always
     * solid line).
     * @param pei : extra information of painting.
     */
    @Override
    public void drawOval(double x, double y, double width, double height,
                         com.cyzapps.VisualMFP.Color color, int drawMode,
                         DrawLib.PaintingExtraInfo pei) {
        if (mcurrentGraphics == null) {
            return;
        }
        Graphics graphics = mcurrentGraphics;
        ImageDisplay.drawOval(graphics, x, y, width, height, color, drawMode, pei);
    }

    /**
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    @Override
    public void clearOval(double x, double y, double width, double height) {
        if (mcurrentGraphics == null) {
            return;
        }
        Graphics graphics = mcurrentGraphics;
        ImageDisplay.clearOval(graphics, x, y, width, height);
    }

    /**
     * return false if loading image fails.
     * @param imgHandle
     * @param left
     * @param top
     * @param widthRatio
     * @param heightRatio
     * @param pei
     * @return 
     */
    @Override
    public boolean drawImage(DataClass imgHandle, double left, double top, double widthRatio, double heightRatio, DrawLib.PaintingExtraInfo pei) {
        if (mcurrentGraphics == null) {
            return false;
        }
        Graphics graphics = mcurrentGraphics;
        boolean bReturn = ImageDisplay.drawImage(graphics, imgHandle, left, top, widthRatio, heightRatio, pei);
        return bReturn;
    }

    /**
     * return false if loading image fails.
     * @param imgHandle
     * @param srcX1
     * @param srcY1
     * @param srcX2
     * @param srcY2
     * @param dstX1
     * @param dstY1
     * @param dstX2
     * @param dstY2
     * @param pei
     * @return 
     */
    @Override
    public boolean drawImage(DataClass imgHandle,
            double srcX1, double srcY1, double srcX2, double srcY2,
            double dstX1, double dstY1, double dstX2, double dstY2,
            DrawLib.PaintingExtraInfo pei) {
        if (mcurrentGraphics == null) {
            return false;
        }
        Graphics graphics = mcurrentGraphics;
        boolean bReturn = ImageDisplay.drawImage(graphics, imgHandle, srcX1, srcY1, srcX2, srcY2, dstX1, dstY1, dstX2, dstY2, pei);
        return bReturn;
    }

    @Override
    public void repaint() {
        if (getGDIView() != null && mbPCBQueueChangedAfterDraw.get()) {
            // need not to call getGDIView().invalidate() because repaint will call
            // invalidate in the UI thread.
            getGDIView().repaint();
        }
    }
    
    @Override
    public void setDisplayResizable(boolean bResizable) {
        // do nothing here.
    }
    
    @Override
    public boolean getDisplayResizable() {
        return false;
    }
    
    synchronized public void draw(double x, double y, double width, double height) {
        if (getGDIView() != null) {
            if (mimageBkGrnd != null) {
                // draw background image
                ImageDisplay.drawBackgroundImage(mcurrentGraphics,
                        getGDIView().getWidth(), getGDIView().getHeight(),
                        mimageBkGrnd, mBkgrndImgMode);
            }
            // no need to remove all obsolete painting call backs because these call backs
            // have been removed when updatepainting call back is added.
            double right = x + width;
            double bottom = y + height;
            boolean bWholeDisplayInRange = (x == 0) && (y == 0)
                    && (width == getGDIView().getWidth()) // no need to worry about getGDIView() is null.
                    && (height == getGDIView().getHeight());
            for (PaintingCallBack pcb : mqueuePaintingCallBacks) {
                if (this.equals(pcb.getDisplay2D())
                        && (bWholeDisplayInRange || pcb.isInPaintingRange(x, y, right, bottom))) {
                    pcb.call(); // draw all the painting call backs.
                }
            }
            mbPCBQueueChangedAfterDraw.set(false);  //no need to reset buffer until painting call back queue is changed.
        }
    }
    
    public void draw() {
        draw(0, 0, getGDIView().getWidth(), getGDIView().getHeight());
    }
    
    @Override
    public void update() {
        // TODO: this function updates some underlying logic (for example, if FLatGDI
        // is for a game, this function updates some game logic), it has nothing to do
        // with graphics (UI).
    }

    @Override
    public void initialize() {
        GIEvent gIEvent = new GIEvent();
        gIEvent.menumEventType = GIEvent.EVENTTYPE.GDI_INITIALIZE;
        addGIEvent(gIEvent);
    }
    
    @Override
    public void close() {
        deprecateFlatGDI();

        GIEvent gIEvent = new GIEvent();
        gIEvent.menumEventType = GIEvent.EVENTTYPE.GDI_CLOSE;
        addGIEvent(gIEvent);
    }

    public void resize(double dWidth, double dHeight, double dOldWidth, double dOldHeight) {
        mdWidth = dWidth;
        mdHeight = dHeight;
        if (dWidth != dOldWidth || dHeight != dOldHeight) {
            update();
        }
    }

    private final static int VIDEO_CALL_SENT = 666;
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";

    public static volatile MediaStream mediaStream = null;
    public volatile AudioSource audioSource = null;
    public volatile VideoSource videoSource = null;
    public volatile VideoCapturer videoCapturer = null;
    public volatile SurfaceTextureHelper surfaceTextureHelper = null;

    @Override
    public int addRtcVideoOutput(int left, int top, int width, int height, boolean enableSlide) {
        int id = mmRtcView.addVideoRenderer(left,top, width, height, enableSlide);
        return id;
    }

    @Override
    public boolean startLocalStream(int videoOutputId) {
        // step 0. check if we have got camera permission
        if (ContextCompat.checkSelfPermission(mflatGDIView.getGDIActivity(), android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(mflatGDIView.getGDIActivity(), android.Manifest.permission.MODIFY_AUDIO_SETTINGS)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(mflatGDIView.getGDIActivity(), android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                /*|| ContextCompat.checkSelfPermission(mflatGDIView.getGDIActivity(), android.Manifest.permission.FLASHLIGHT)    // flashlight is a normal permission so no need to request.
                    != PackageManager.PERMISSION_GRANTED*/
                || ContextCompat.checkSelfPermission(mflatGDIView.getGDIActivity(), android.Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(mflatGDIView.getGDIActivity(), Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            return false;
        }
        // step 1. initialize front camera
        Camera1Enumerator camera1Enumerator = new Camera1Enumerator();
        final String[] deviceNames = camera1Enumerator.getDeviceNames();
        String frontCameraDeviceName = null;
        for (String name : deviceNames) {
            if (camera1Enumerator.isFrontFacing(name)) {
                frontCameraDeviceName = name;
                break;
            }
        }
        if (frontCameraDeviceName == null) {
            return false;   // no front camera
        }
        if (videoOutputId < 0 || videoOutputId >= mmRtcView.videoRendererPairs.size()) {
            return false;
        }
        // step 2. initialize factory if needed
        // do not need if (RtcAgent.factoryMMedia == null)
        RtcAgent.factoryMMedia = RtcAgent.initWebRtcFactory(mmRtcView.getEglBase().getEglBaseContext());   // we do not need to do this in Peer as Peer is created in the initialize local.

        // step 3. create local media stream
        if (mediaStream == null) {
            mediaStream = RtcAgent.factoryMMedia.createLocalMediaStream("ARDAMS");
        }

        // step 4. create audio source
        // do not need if (audioSource == null)
        // Create audio constraints.
        MediaConstraints audioConstraints = new MediaConstraints();
        audioSource = RtcAgent.factoryMMedia.createAudioSource(audioConstraints);

        AudioTrack localAudioTrack = RtcAgent.factoryMMedia.createAudioTrack("ARDAMSa0", audioSource);
        localAudioTrack.setEnabled(true);
        mediaStream.addTrack(localAudioTrack);
        // step 5. initialize parameters
        View v = mmRtcView.videoRendererPairs.get(videoOutputId).getSurfaceViewRenderer();
        int surfaceViewRendererWidth = v.getWidth();
        int surfaceViewRendererHeight = v.getHeight();
        Point displaySize = new Point();
        mflatGDIView.getGDIActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
        MmediaPeerConnectionParams params = new MmediaPeerConnectionParams(
                true, false, displaySize.x, displaySize.y, /*surfaceViewRendererWidth, surfaceViewRendererHeight,*/ 30, 1, VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, true);
        // step 6. create video source and add it the renderer
        // videoConstraints is removed from latest code library.
        if (Camera2Enumerator.isSupported(mflatGDIView.getGDIActivity())) {
            Log.d("FlatGDI_WebRTC_MMedia", "Creating capturer using camera2 API.");
            videoCapturer = createCameraCapturer(new Camera2Enumerator(mflatGDIView.getGDIActivity()));
        } else {
            Log.d("FlatGDI_WebRTC_MMedia", "Creating capturer using camera1 API.");
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));  // should captureToTexture be false?
        }
        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", mmRtcView.getEglBase().getEglBaseContext());
        videoSource = RtcAgent.factoryMMedia.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, mflatGDIView.getGDIActivity(), videoSource.getCapturerObserver());
        videoCapturer.startCapture(params.videoWidth, params.videoHeight, params.videoFps);

        VideoTrack localVideoTrack = RtcAgent.factoryMMedia.createVideoTrack("ARDAMSv0", videoSource);
        localVideoTrack.setEnabled(true);
        mediaStream.addTrack(localVideoTrack);
        MMRtcView.ProxyVideoSink proxyRenderer = mmRtcView.videoRendererPairs.get(videoOutputId).getProxyRenderer();
        if (proxyRenderer != null) {
            Log.d("FlatGDI_WebRTC_MMedia", "FlatGDI.startLocalStream: local video stream has been successfully mapped to a sink!");
            localVideoTrack.addSink(proxyRenderer);
        }
        return true;
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Log.d("FlatGDI_WebRTC_MMedia", "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Log.d("FlatGDI_WebRTC_MMedia", "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, new AdvRtcCameraEventHandler());
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        // Front facing camera not found, try something else
        Log.d("FlatGDI_WebRTC_MMedia", "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Log.d("FlatGDI_WebRTC_MMedia", "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, new AdvRtcCameraEventHandler());
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }


    @Override
    public void stopLocalStream() {
        if (mediaStream != null) {
            while(!mediaStream.audioTracks.isEmpty()) {
                AudioTrack track = (AudioTrack)mediaStream.audioTracks.get(0);
                mediaStream.removeTrack(track);
                track.dispose();
            }

            while(!mediaStream.videoTracks.isEmpty()) {
                VideoTrack track = (VideoTrack)mediaStream.videoTracks.get(0);
                mediaStream.removeTrack(track);
                track.dispose();
            }
            // do not call mediaStream.dispose(); because free(mediaStream.nativeStream) causes crash.
            mediaStream = null;
        }
        Log.d("FlatGDI_WebRTC_MMedia", "Stopping capture.");
        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            videoCapturer.dispose();
            videoCapturer = null;
        }
        Log.d("FlatGDI_WebRTC_MMedia", "Closing video source.");
        if (videoSource != null) {
            videoSource.dispose();
            videoSource = null;
        }
        if (surfaceTextureHelper != null) {
            surfaceTextureHelper.dispose();
            surfaceTextureHelper = null;
        }
        /* I have to comment the following code. It seems that if you want to
        restart webRTC without restarting app, factory cannot be restarted.
         */
        /*if (RtcAgent.factoryMMedia != null) {
            RtcAgent.factoryMMedia.dispose();
            RtcAgent.factoryMMedia = null;
        }*/
        // release eglBase?
    }

    @Override
    public boolean startVideoCapturer() {
        if (videoCapturer != null) {
            Point displaySize = new Point();
            mflatGDIView.getGDIActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
            MmediaPeerConnectionParams params = new MmediaPeerConnectionParams(
                    true, false, displaySize.x, displaySize.y, /*surfaceViewRendererWidth, surfaceViewRendererHeight,*/ 30, 1, VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, true);
            videoCapturer.startCapture(params.videoWidth, params.videoHeight, params.videoFps);
            return true;
        }
        return false;
    }

    @Override
    public void stopVideoCapturer() {
        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean setVideoTrackEnable(int idx, boolean enable) {
        if (idx < mediaStream.videoTracks.size()) {
            return mediaStream.videoTracks.get(idx).setEnabled(enable);
        }
        return false;
    }

    @Override
    public boolean getVideoTrackEnable(int idx) {
        if (idx < mediaStream.videoTracks.size()) {
            return mediaStream.videoTracks.get(0).enabled();
        }
        return false;
    }

    @Override
    public boolean setAudioTrackEnable(int idx, boolean enable) {
        if (idx < mediaStream.audioTracks.size()) {
            return mediaStream.audioTracks.get(idx).setEnabled(enable);
        }
        return false;
    }

    @Override
    public boolean getAudioTrackEnable(int idx) {
        if (idx < mediaStream.audioTracks.size()) {
            return mediaStream.audioTracks.get(0).enabled();
        }
        return false;
    }

    @Override
    public int[] getRtcVideoOutputLeftTop(int id) {
        if (id < 0 || id >= mmRtcView.videoRendererPairs.size()) {
            return null;
        } else {
            View v = mmRtcView.videoRendererPairs.get(id).getSurfaceViewRenderer();
            int[] leftTop = new int[2];
            leftTop[0] = v.getLeft();
            leftTop[1] = v.getTop();
            return leftTop;
        }
    }

    @Override
    public int getRtcVideoOutputCount() {
        return mmRtcView.videoRendererPairs.size();
    }

    @Override
    public boolean linkVideoStream(String peerId, int trackId, int videoOutputId) {
        // if peerId is an empty string, it means local
        AndroidRtcMMediaMan.StreamTrackId streamTrackId = new AndroidRtcMMediaMan.StreamTrackId(peerId, trackId);
        AndroidRtcMMediaMan rtcMMediaMan = (AndroidRtcMMediaMan) FuncEvaluator.msRtcMMediaManager;
        if (mmRtcView.videoRendererPairs.get(videoOutputId) != null) {
            rtcMMediaMan.mapStream2ProxyRenderer.put(streamTrackId, mmRtcView.videoRendererPairs.get(videoOutputId).getProxyRenderer());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean unlinkVideoStream(String peerId, int trackId) {
        // if peerId is an empty string, it means local
        AndroidRtcMMediaMan.StreamTrackId streamTrackId = new AndroidRtcMMediaMan.StreamTrackId(peerId, trackId);
        AndroidRtcMMediaMan rtcMMediaMan = (AndroidRtcMMediaMan) FuncEvaluator.msRtcMMediaManager;
        MMRtcView.ProxyVideoSink proxyRenderer = rtcMMediaMan.mapStream2ProxyRenderer.remove(streamTrackId);
        if (proxyRenderer == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public int unlinkVideoStream(int videoOutputId) {
        if (videoOutputId >= mmRtcView.videoRendererPairs.size() || videoOutputId < 0) {
            return 0;
        }
        int removedCnt = 0;
        MMRtcView.ProxyVideoSink value = mmRtcView.videoRendererPairs.get(videoOutputId).getProxyRenderer();
        AndroidRtcMMediaMan rtcMMediaMan = (AndroidRtcMMediaMan) FuncEvaluator.msRtcMMediaManager;
        Set<AndroidRtcMMediaMan.StreamTrackId> keySet = rtcMMediaMan.mapStream2ProxyRenderer.keySet();
        for(AndroidRtcMMediaMan.StreamTrackId key: keySet) {
            if (rtcMMediaMan.mapStream2ProxyRenderer.get(key) == value) {
                rtcMMediaMan.mapStream2ProxyRenderer.remove(key);
                removedCnt ++;
            }
        }
        return removedCnt;
    }
}
