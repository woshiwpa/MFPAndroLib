/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.MultimediaAdapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Xfermode;
import android.util.Log;

import com.cyzapps.GI2DAdapter.FlatGDI;
import com.cyzapps.JGI2D.Display2D;
import com.cyzapps.JGI2D.DisplayLib;
import com.cyzapps.JGI2D.DisplayLib.GraphicDisplayType;
import com.cyzapps.JGI2D.DrawLib;
import com.cyzapps.JGI2D.GIEvent;
import com.cyzapps.JGI2D.PaintingCallBacks;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassExtObjRef;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.VisualMFP.Color;
import com.cyzapps.VisualMFP.LineStyle;
import com.cyzapps.VisualMFP.PointStyle;
import com.cyzapps.VisualMFP.TextStyle;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A feature of this class is, there is no real draw happens until we try to get image or copy of image.
 * Also image display is different from screen display, it is not supposed to be multithreading, so no sync
 * functions.
 * @author tony
 */
public class ImageDisplay extends Display2D {

    public static class SizeChoices {
        public double mdVeryTinySize = 2;
        public double mdTinySize = 4;
        public double mdVerySmallSize = 8;
        public double mdSmallSize = 16;
        public double mdMediumSize = 32;
        public double mdLargeSize = 64;
        public double mdVeryLargeSize = 128;
        public double mdHugeSize = 256;
        public double mdVeryHugeSize = 512;

        public void resetAllSizesBasedOnMedium(double dNewMediumSize) {
            mdMediumSize = dNewMediumSize;
            mdVeryTinySize = mdMediumSize / 16;
            mdTinySize = mdMediumSize / 8;
            mdVerySmallSize = mdMediumSize / 4;
            mdSmallSize = mdMediumSize / 2;
            mdLargeSize = mdMediumSize * 2;
            mdVeryLargeSize = mdMediumSize * 4;
            mdHugeSize = mdMediumSize * 8;
            mdVeryHugeSize = mdMediumSize * 16;
        }

        public void resetAllSizesBasedOnMedium() {
            resetAllSizesBasedOnMedium(mdMediumSize);
        }
    }

    public static class Graphics {
        public Paint paint;
        public Canvas canvas;
        public static Graphics createGraphics(Bitmap bitmap, Paint paint) {
            Graphics g = new Graphics();
            g.canvas = new Canvas(bitmap);
            g.paint = paint;
            return g;
        }

        public void dispose() {
            // do nothing here because I cannot find canvas's dispose function.
        }
    }

    public static final String LOG_TAG = "ImageDisplay";

    protected SizeChoices msizeChoices = new SizeChoices();

    protected ConcurrentLinkedQueue<PaintingCallBacks.PaintingCallBack> mqueuePaintingCallBacks = new ConcurrentLinkedQueue<>();

    protected Paint mPaint = new Paint();

    protected Color mcolorBkGrnd = new Color(0, 0, 0, 0);
    protected int mBkgrndImgMode = 0;   // 0 means actually, 1 means scaled, 2 means tiled
    protected ImageMgrAndroid.ImageWrapper mimageBkGrnd = null;    // background image which is the loaded image.
    protected int mnTargetWidth = 0;
    protected int mnTargetHeight = 0;
    protected String mstrFilePath = null;
    protected Bitmap mcurrentImage = null; // no need to use ImageWrapper here because this is an internal variable.
    protected boolean mbImageUptoDate = false;
    
    protected Boolean mbDisplayOnLive = true;   // it is true until display shutdown.

    /** this function is unsafe. Comment it. Use cloneImage instead
    public Bitmap getImage(boolean bUpdateScreen) {
        if (mbDisplayOnLive == false) {
            return null;
        } else {
            if (bUpdateScreen) {
                startPainting();
            }
            if (mcurrentImage == null && mnTargetWidth > 0 && mnTargetHeight > 0) {
                // if no need to update screen, at least we paint the background.
                Bitmap img2Clone = Bitmap.createBitmap(mnTargetWidth, mnTargetHeight, Bitmap.Config.ARGB_8888);
                drawBackground(img2Clone, mcolorBkGrnd, mimageBkGrnd, mBkgrndImgMode);
                return img2Clone;
            } else {
                return mcurrentImage;
            }
        }
    }*/
    
    public Bitmap cloneImage(boolean bUpdateScreen, double wRatio, double hRatio) {
        if (mbDisplayOnLive == false || wRatio <= 0 || hRatio <= 0) {
            return null;
        } else {
            if (bUpdateScreen) {
                startPainting();
            }
            if (mcurrentImage == null && mnTargetWidth > 0 && mnTargetHeight > 0) {
                // if no need to update screen, at least we paint the background.
                Bitmap imgBkGrnd = Bitmap.createBitmap(mnTargetWidth, mnTargetHeight, Bitmap.Config.ARGB_8888);
                drawBackground(imgBkGrnd, mcolorBkGrnd, mimageBkGrnd, mBkgrndImgMode);
                int destW = (int) (mnTargetWidth * wRatio);
                int destH = (int) (mnTargetHeight * hRatio);
                if (destW < 1) {
                    destW = 1;
                }
                if (destH < 1) {
                    destH = 1;
                }
                Bitmap imgClone = Bitmap.createScaledBitmap(imgBkGrnd, destW, destH, true);
                imgBkGrnd.recycle();
                return imgClone;
            } else if (mcurrentImage != null) {
                if (wRatio == 1 && hRatio == 1) {
                    // needs a clone, not shallow copy.
                    Bitmap imgClone = mcurrentImage.copy(mcurrentImage.getConfig(), true);
                    return imgClone;
                } else {
                    int destW = (int) (mcurrentImage.getWidth() * wRatio);
                    int destH = (int) (mcurrentImage.getHeight() * hRatio);
                    if (destW < 1) {
                        destW = 1;
                    }
                    if (destH < 1) {
                        destH = 1;
                    }
                    Bitmap imgClone = Bitmap.createScaledBitmap(mcurrentImage, destW, destH, true);
                    return imgClone;
                }
            } else {
                return null;
            }
        }
    }
    
    @Override
    public GIEvent pullGIEvent() {
        return null;    // no GI event is supported.
    }

    @Override
    public void addPaintingCallBack(PaintingCallBacks.PaintingCallBack paintingCallBack) {
        if (mbDisplayOnLive == false) {
            return;
        }
        if (paintingCallBack instanceof PaintingCallBacks.UpdatePaintingCallBack) {
            // remove obsolete call backs first.
            int idx = mqueuePaintingCallBacks.size() - 1;
            for (PaintingCallBacks.PaintingCallBack callBack : mqueuePaintingCallBacks) {
                if (callBack.isCallBackOutDated(paintingCallBack)) {
                    mqueuePaintingCallBacks.remove(callBack);
                }
            }
            // seems it is much slower.
            //mqueuePaintingCallBacks.removeIf(elem -> elem.isCallBackOutDated(paintingCallBack));
        }
        mqueuePaintingCallBacks.add(paintingCallBack);
        mbImageUptoDate = false;
    }

    @Override
    public void clearPaintingCallBacks() {
        mqueuePaintingCallBacks.clear();
        mbImageUptoDate = false;
    }
    
    @Override
    public boolean isDisplayOnLive() {
        return mbDisplayOnLive;
    }

    @Override
    public void setDisplayOrientation(int orientation) {
        // not supported.
    }

    @Override
    public int getDisplayOrientation() {
        return -1000;  // always return -1000 for an image.
    }

    @Override
    public void setDisplaySize(int width, int height) {
        if (mbDisplayOnLive) {
            mnTargetWidth = width;
            mnTargetHeight = height;
        }
    }

    @Override
    public int[] getDisplaySize() {
        if (mbDisplayOnLive) {
            return new int[] {mnTargetWidth, mnTargetHeight};
        } else {
            return new int[] {0,0};
        }
    }

    @Override
    public void setDisplayResizable(boolean resizable) {
        // do nothing.
    }

    @Override
    public boolean getDisplayResizable() {
        return false;
    }

    @Override
    public void setDisplayCaption(String strCaption) {
        // do nothing.
    }

    @Override
    public String getDisplayCaption() {
        return "";
    }

    @Override
    public void setBackgroundColor(Color clr) {
        if (mbDisplayOnLive) {
            mcolorBkGrnd = clr;
            mbImageUptoDate = false;
        }
    }

    @Override
    public Color getBackgroundColor() {
        if (mbDisplayOnLive) {
            return mcolorBkGrnd;
        } else {
            return new Color(); // invalid color.
        }
    }

    @Override
    public void setBackgroundImage(DataClassExtObjRef imgHandler, int mode) {
        if (mbDisplayOnLive) {
            try {
                if (imgHandler == null) {
                    mimageBkGrnd = null;
                    mBkgrndImgMode = mode;
                } else if (imgHandler.getExternalObject() instanceof ImageMgrAndroid.ImageWrapper) {
                    // only if it is a buffered image. do not check recycled or not here.
                    // only check recycled when we need to use the bitmap.
                    mimageBkGrnd = (ImageMgrAndroid.ImageWrapper)imgHandler.getExternalObject();
                    mBkgrndImgMode = mode;
                }
            } catch (JFCALCExpErrException ex) {
                // will not be here.
                Logger.getLogger(ImageDisplay.class.getName()).log(Level.SEVERE, null, ex);
            }
            mbImageUptoDate = false;
        }
    }

    @Override
    public DataClass getBackgroundImage() {
        if (!mbDisplayOnLive || mimageBkGrnd == null) {
            return new DataClassNull();
        } else {
            try {
                return new DataClassExtObjRef(mimageBkGrnd);
            } catch (JFCALCExpErrException ex) {
                return new DataClassNull(); // will not be here.
            }
        }
    }

    @Override
    public int getBackgroundImageMode() {
        if (mbDisplayOnLive) {
            return mBkgrndImgMode;
        } else {
            return 0;
        }
    }
    
    @Override
    public void setSnapshotAsBackground(boolean bUpdateScreen, boolean bClearPaintingCallbacks) {
        // image display only has two modes, clearing or not clearing drawing events.
        // there is no sync mode or async mode.
        if (mbDisplayOnLive) {
            mimageBkGrnd = new ImageMgrAndroid.ImageWrapper(cloneImage(bUpdateScreen, 1.0, 1.0));
            // no need to worry about background image mode because size of
            // mimageBkGrnd should exactly fit display.
            if (bClearPaintingCallbacks) {
                clearPaintingCallBacks();
            }
            mbImageUptoDate = false;
        }
    }
    
    @Override
    public DataClass getSnapshotImage(boolean bUpdateDisplay, double wRatio, double hRatio) {
        if (mbDisplayOnLive) {
            try {
                Bitmap image = cloneImage(bUpdateDisplay, wRatio, hRatio);
                if (image == null) {
                    return new DataClassNull();
                } else {
                    return new DataClassExtObjRef(new ImageMgrAndroid.ImageWrapper(image));
                }
            } catch (ErrProcessor.JFCALCExpErrException ex) {
                Logger.getLogger(FlatGDI.class.getName()).log(Level.SEVERE, null, ex);
                return new DataClassNull();
            }
        } else {
            return new DataClassNull();
        }
    }

    @Override
    public void setDisplayConfirmClose(boolean bConfirmClose) {
        // do nothing.
    }

    @Override
    public boolean getDisplayConfirmClose() {
        return false;
    }

    public static Xfermode convert2Xfermode(DrawLib.PorterDuffMode pdm) {
        switch(pdm) {
            case CLEAR:
                return new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
            case SRC:
                return new PorterDuffXfermode(PorterDuff.Mode.SRC);
            case DST:
                return new PorterDuffXfermode(PorterDuff.Mode.DST);
            case SRC_OVER:
                return new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
            case DST_OVER:
                return new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);
            case SRC_IN:
                return new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
            case DST_IN:
                return new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
            case SRC_OUT:
                return new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT);
            case DST_OUT:
                return new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
            case SRC_ATOP:
                return new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
            case DST_ATOP:
                return new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP);
            case XOR:
                return new PorterDuffXfermode(PorterDuff.Mode.XOR);
            default:
                return new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
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
        Bitmap image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        Graphics g = Graphics.createGraphics(image, mPaint);
        int[] rect = calcTextBoundary(g, text, x, y, txtStyle);
        g.dispose();
        image.recycle();
        return rect;
    }
    public static int[] calcTextBoundary(Graphics graphics, String text, int x, int y, TextStyle txtStyle) {
        float fPaintOriginalTxtSize = graphics.paint.getTextSize();
        Typeface typefaceOriginal = graphics.paint.getTypeface();
        double dTextSize = txtStyle.mdSize;
        graphics.paint.setTextSize((float)dTextSize);
        Boolean bBoldStyle = (txtStyle.mnStyle & 1) == 1;
        Boolean bItalicStyle = (txtStyle.mnStyle & 2) == 2;
        int nStyle;
        if (bBoldStyle && bItalicStyle) {
            nStyle = Typeface.BOLD_ITALIC;
        } else if (bBoldStyle) {
            nStyle = Typeface.BOLD;
        } else if (bItalicStyle) {
            nStyle = Typeface.ITALIC;
        } else {
            nStyle = Typeface.NORMAL;
        }
        graphics.paint.setTypeface(Typeface.create(txtStyle.mstrFont, nStyle));

        int nLineGap = (int)Math.max((graphics.paint.getFontMetrics().top - graphics.paint.getFontMetrics().bottom) / 3.0, 4);
        String[] lines = text.split("\n");
        Rect rect = new Rect();
        int yOff = 0;
        int textLeft = Integer.MAX_VALUE, textRight = Integer.MIN_VALUE, textTop = Integer.MAX_VALUE, textBottom = Integer.MIN_VALUE;
        for (int i = 0; i < lines.length; ++i) {
            graphics.paint.getTextBounds(lines[i], 0, lines[i].length(), rect);
            textLeft = Math.min(textLeft, rect.left);
            textRight = Math.max(textRight, rect.right);
            textTop = Math.min(textTop, rect.top + yOff);
            textBottom = Math.max(textBottom, rect.bottom + yOff);
            yOff = (int) (yOff + rect.height() + nLineGap); // space between lines
        }
        graphics.paint.setTypeface(typefaceOriginal);
        graphics.paint.setTextSize(fPaintOriginalTxtSize);
        return new int[] {textLeft + x, textTop + y, textRight - textLeft, textBottom - textTop};
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
        // this is not a drawing function, so not use mcurrentGraphics.
        Bitmap image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        Graphics g = Graphics.createGraphics(image, new Paint());
        int[] origin = calcTextOrigin(g, text, x, y, w, h, horAlign, verAlign, txtStyle);
        g.dispose();
        image.recycle();
        return origin;
    }
    public static int[] calcTextOrigin(Graphics graphics, String text, int x, int y, int w, int h, int horAlign, int verAlign, TextStyle txtStyle) {
        float fPaintOriginalTxtSize = graphics.paint.getTextSize();
        Typeface typefaceOriginal = graphics.paint.getTypeface();
        double dTextSize = txtStyle.mdSize;
        graphics.paint.setTextSize((float)dTextSize);
        Boolean bBoldStyle = (txtStyle.mnStyle & 1) == 1;
        Boolean bItalicStyle = (txtStyle.mnStyle & 2) == 2;
        int nStyle;
        if (bBoldStyle && bItalicStyle) {
            nStyle = Typeface.BOLD_ITALIC;
        } else if (bBoldStyle) {
            nStyle = Typeface.BOLD;
        } else if (bItalicStyle) {
            nStyle = Typeface.ITALIC;
        } else {
            nStyle = Typeface.NORMAL;
        }
        graphics.paint.setTypeface(Typeface.create(txtStyle.mstrFont, nStyle));

        int nLineGap = (int)Math.max((graphics.paint.getFontMetrics().top - graphics.paint.getFontMetrics().bottom) / 3.0, 4);
        String[] lines = text.split("\n");
        Rect rect = new Rect();
        int textLeft = Integer.MAX_VALUE, textRight = Integer.MIN_VALUE;
        int totalHeight = 0;
        int initialXOffset = 0, initialYOffset = 0;
        for (int i = 0; i < lines.length; ++i) {
            graphics.paint.getTextBounds(lines[i], 0, lines[i].length(), rect);
            if (i == 0) {
                initialXOffset = -rect.left;
                initialYOffset = -rect.top;
            }
            textLeft = Math.min(textLeft, rect.left);
            textRight = Math.max(textRight, rect.right);
            totalHeight += rect.height() + nLineGap; // nLineGap space between lines
        }
        graphics.paint.setTypeface(typefaceOriginal);
        graphics.paint.setTextSize(fPaintOriginalTxtSize);
        int xOrigin = 0, yOrigin = 0;
        int left = xOrigin, top = yOrigin;
        int right = left + textRight - textLeft, bottom = top + totalHeight - nLineGap;
        double centerX = (left + right)/2.0, centerY = (top + bottom)/2.0;
        int xAligned = 0, yAligned = 0;
        if (horAlign < 0) {
            xAligned = x + (xOrigin - left);
        } else if (horAlign > 0) {
            xAligned = (x + w) + (xOrigin - right);
        } else {
            xAligned = (int)((x + w/2.0) + (xOrigin - centerX));
        }
        if (verAlign < 0) {
            yAligned = y + (yOrigin - top);
        } else if (verAlign > 0) {
            yAligned = (y + h) + (yOrigin - bottom);
        } else {
            yAligned = (int)((y + h/2.0) + (yOrigin - centerY));
        }
        return new int[] {xAligned + initialXOffset, yAligned + initialYOffset};
    }

    /**
     * Draw a multiple lines text. x is the starting point of first character
     * and y is the pivot point
     * @param text
     * @param x
     * @param y
     * @param txtStyle
     * @param dRotateRadian
     * @param pei
     */
    @Override
    public void drawText(String text, double x, double y, TextStyle txtStyle, double dRotateRadian, DrawLib.PaintingExtraInfo pei) {
        if (mcurrentImage == null) {
            return; // if current image hasn't been fully initialized, return.
        }
        Graphics g = Graphics.createGraphics(mcurrentImage, mPaint);
        drawText(g, text, x, y, txtStyle, dRotateRadian, pei);
        g.dispose();
    }
    public static void drawText(Graphics graphics, String text, double x, double y, TextStyle txtStyle, double dRotateRadian, DrawLib.PaintingExtraInfo pei) {
        float fRotateRadian = (float) dRotateRadian, fX = (float) x, fY = (float) y;
        if (dRotateRadian != 0) {
            graphics.canvas.rotate(fRotateRadian, fX, fY);
        }
        Xfermode xfermodeOriginal = graphics.paint.getXfermode();
        int nPaintOriginalColor = graphics.paint.getColor();
        float fPaintOriginalTxtSize = graphics.paint.getTextSize();
        Typeface typefaceOriginal = graphics.paint.getTypeface();
        if (pei != null) {
            graphics.paint.setXfermode(convert2Xfermode(pei.mpdm));
        }
        com.cyzapps.VisualMFP.Color color = txtStyle.mclr;
        if (color != null) {
            graphics.paint.setColor(color.getARGB());
        }    // otherwise, use paint's color.
        double dTextSize = txtStyle.mdSize;
        graphics.paint.setTextSize((float)dTextSize);
        Boolean bBoldStyle = (txtStyle.mnStyle & 1) == 1;
        Boolean bItalicStyle = (txtStyle.mnStyle & 2) == 2;
        int nStyle;
        if (bBoldStyle && bItalicStyle) {
            nStyle = Typeface.BOLD_ITALIC;
        } else if (bBoldStyle) {
            nStyle = Typeface.BOLD;
        } else if (bItalicStyle) {
            nStyle = Typeface.ITALIC;
        } else {
            nStyle = Typeface.NORMAL;
        }
        graphics.paint.setTypeface(Typeface.create(txtStyle.mstrFont, nStyle));

        int nLineGap = (int)Math.max((graphics.paint.getFontMetrics().top - graphics.paint.getFontMetrics().bottom) / 3.0, 4);
        String[] lines = text.split("\n");
        Rect rect = new Rect();
        int yOff = 0;
        for (int i = 0; i < lines.length; ++i) {
            graphics.canvas.drawText(lines[i], fX, fY/* + graphics.paint.getFontMetrics().leading + graphics.paint.ascent()*/ + yOff, graphics.paint);
            graphics.paint.getTextBounds(lines[i], 0, lines[i].length(), rect);
            yOff = (int) (yOff + rect.height() + nLineGap); // space between lines
        }
        graphics.paint.setTypeface(typefaceOriginal);
        graphics.paint.setTextSize(fPaintOriginalTxtSize);
        graphics.paint.setColor(nPaintOriginalColor);
        graphics.paint.setXfermode(xfermodeOriginal);
        if (dRotateRadian != 0) {
            graphics.canvas.rotate(-fRotateRadian, fX, fY);
        }
    }

    @Override
    public void drawPoint(double x, double y, PointStyle pointStyle, DrawLib.PaintingExtraInfo pei) {
        if (mcurrentImage == null) {
            return; // if current image hasn't been fully initialized, return.
        }
        Graphics g = Graphics.createGraphics(mcurrentImage, mPaint);
        drawPoint(g, msizeChoices, x, y, pointStyle, pei);
        g.dispose();
    }

    public static void drawPoint(Graphics graphics, SizeChoices sizeChoices, double x, double y, PointStyle pointStyle, DrawLib.PaintingExtraInfo pei) {
        Xfermode xfermodeOriginal = graphics.paint.getXfermode();
        int nOriginalColor = graphics.paint.getColor();
        Paint.Style styleOriginal = graphics.paint.getStyle();
        float fOriginalStrokeWidth = graphics.paint.getStrokeWidth();
        PathEffect pathEffectOriginal = graphics.paint.getPathEffect();
        boolean bAntiAlias = graphics.paint.isAntiAlias();

        if (pei != null) {
            graphics.paint.setXfermode(convert2Xfermode(pei.mpdm));
        }
        if (pointStyle.mclr != null)	{
            graphics.paint.setColor(pointStyle.mclr.getARGB());
        }
        graphics.paint.setAntiAlias(true);
        graphics.paint.setStyle(Paint.Style.STROKE);
        graphics.paint.setStrokeWidth(1);
        graphics.paint.setPathEffect(new DashPathEffect(new float[]{(float)sizeChoices.mdMediumSize, 0}, 0));
        double dSize = pointStyle.mdSize;
        switch (pointStyle.menumPointShape)	{
            case POINTSHAPE_CIRCLE:
            {
                float fX = (float)x;
                float fY = (float)y;
                float fRadius = (float)(dSize/2);
                graphics.canvas.drawCircle(fX, fY, fRadius, graphics.paint);
                break;
            }
            case POINTSHAPE_DIAMOND:
            {
                float fX = (float) x;
                float fY = (float) y;
                float fX1 = (float) (x - dSize/2), fX2 = (float) (x + dSize/2);
                float fY1 = (float) (y - dSize/2), fY2 = (float) (y + dSize/2);
                graphics.canvas.drawLine(fX1, fY, fX, fY1, graphics.paint);
                graphics.canvas.drawLine(fX1, fY, fX, fY2, graphics.paint);
                graphics.canvas.drawLine(fX2, fY, fX, fY1, graphics.paint);
                graphics.canvas.drawLine(fX2, fY, fX, fY2, graphics.paint);
                break;
            }
            case POINTSHAPE_CROSS:
            {
                float fX = (float) x;
                float fY = (float) y;
                float fX1 = (float) (x - dSize/2), fX2 = (float) (x + dSize/2);
                float fY1 = (float) (y - dSize/2), fY2 = (float) (y + dSize/2);
                graphics.canvas.drawLine(fX1, fY, fX2, fY, graphics.paint);
                graphics.canvas.drawLine(fX, fY1, fX, fY2, graphics.paint);
                break;
            }
            case POINTSHAPE_X:
            {
                float fX1 = (float) (x - dSize/2), fX2 = (float) (x + dSize/2);
                float fY1 = (float) (y - dSize/2), fY2 = (float) (y + dSize/2);
                graphics.canvas.drawLine(fX1, fY1, fX2, fY2, graphics.paint);
                graphics.canvas.drawLine(fX1, fY2, fX2, fY1, graphics.paint);
                break;
            }
            case POINTSHAPE_DOWNTRIANGLE:
            {
                float fX = (float) x;
                float fX1 = (float) (x - dSize*0.433), fX2 = (float) (x + dSize*0.433);
                float fY1 = (float) (y + dSize/2), fY2 = (float) (y - dSize/4);
                graphics.canvas.drawLine(fX, fY1, fX1, fY2, graphics.paint);
                graphics.canvas.drawLine(fX, fY1, fX2, fY2, graphics.paint);
                graphics.canvas.drawLine(fX1, fY2, fX2, fY2, graphics.paint);
                break;
            }
            case POINTSHAPE_UPTRIANGLE:
            {
                float fX = (float) x;
                float fX1 = (float) (x - dSize*0.433), fX2 = (float) (x + dSize*0.433);
                float fY1 = (float) (y - dSize/2), fY2 = (float) (y + dSize/4);
                graphics.canvas.drawLine(fX, fY1, fX1, fY2, graphics.paint);
                graphics.canvas.drawLine(fX, fY1, fX2, fY2, graphics.paint);
                graphics.canvas.drawLine(fX1, fY2, fX2, fY2, graphics.paint);
                break;
            }
            case POINTSHAPE_SQUARE:
            {
                float fX1 = (float)(x - dSize/2);
                float fX2 = (float)(x + dSize/2);
                float fY1 = (float)(y - dSize/2);
                float fY2 = (float)(y + dSize/2);
                graphics.canvas.drawRect(fX1, fY1, fX2, fY2, graphics.paint);
                break;
            }
            default:	// dot.
                graphics.canvas.drawPoint((float)x, (float)y, graphics.paint);
        }

        graphics.paint.setAntiAlias(bAntiAlias);
        graphics.paint.setColor(nOriginalColor);
        graphics.paint.setStyle(styleOriginal);
        graphics.paint.setStrokeWidth(fOriginalStrokeWidth);
        graphics.paint.setPathEffect(pathEffectOriginal);
        graphics.paint.setXfermode(xfermodeOriginal);
    }

    @Override
    public void drawLine(double x0, double y0, double x1, double y1, LineStyle lineStyle, DrawLib.PaintingExtraInfo pei) {
        if (mcurrentImage == null) {
            return; // if current image hasn't been fully initialized, return.
        }
        Graphics g = Graphics.createGraphics(mcurrentImage, mPaint);
        drawLine(g, msizeChoices, x0, y0, x1, y1, lineStyle, pei);
        g.dispose();
    }
    public static void drawLine(Graphics graphics, SizeChoices sizeChoices, double x0, double y0, double x1, double y1,
                                LineStyle lineStyle, DrawLib.PaintingExtraInfo pei) {
        if (lineStyle.menumLinePattern == LineStyle.LINEPATTERN.LINEPATTERN_NON) {
            return;
        }
        Xfermode xfermodeOriginal = graphics.paint.getXfermode();
        int nOriginalColor = graphics.paint.getColor();
        Paint.Style styleOriginal = graphics.paint.getStyle();
        float fOriginalStrokeWidth = graphics.paint.getStrokeWidth();
        PathEffect pathEffectOriginal = graphics.paint.getPathEffect();

        if (pei != null) {
            graphics.paint.setXfermode(convert2Xfermode(pei.mpdm));
        }
        if (lineStyle.mclr != null)	{
            graphics.paint.setColor(lineStyle.mclr.getARGB());
        }
        graphics.paint.setStyle(Paint.Style.STROKE);
        graphics.paint.setStrokeWidth((float)lineStyle.mdLineWidth);
        switch (lineStyle.menumLinePattern)	{
            case LINEPATTERN_DASH:
            {
                graphics.paint.setPathEffect(new DashPathEffect(new float[]{(float)sizeChoices.mdVerySmallSize, (float)sizeChoices.mdTinySize}, 0));
                break;
            }
            case LINEPATTERN_DOT:
            {
                graphics.paint.setPathEffect(new DashPathEffect(new float[]{(float)sizeChoices.mdVeryTinySize, (float)sizeChoices.mdTinySize}, 0));
                break;
            }
            case LINEPATTERN_DASH_DOT:
            {
                graphics.paint.setPathEffect(new DashPathEffect(new float[]{(float)sizeChoices.mdVerySmallSize, (float)sizeChoices.mdTinySize, (float)sizeChoices.mdVeryTinySize, (float)sizeChoices.mdTinySize}, 0));
                break;
            }
            /* case LINEPATTERN_NON:	// need not to consider LINEPATTERN_NON
            {
                paint.setPathEffect(new DashPathEffect(new float[]{0, (float)mdMediumSize}, 0));
                break;
            } */
            default:	// solid
                graphics.paint.setPathEffect(new PathEffect());
        }

        float fX0 = (float)x0, fY0 = (float)y0,
                fX1 = (float)x1, fY1 = (float)y1;
        graphics.canvas.drawLine(fX0, fY0, fX1, fY1, graphics.paint);

        graphics.paint.setXfermode(xfermodeOriginal);
        graphics.paint.setColor(nOriginalColor);
        graphics.paint.setStyle(styleOriginal);
        graphics.paint.setStrokeWidth(fOriginalStrokeWidth);
        graphics.paint.setPathEffect(pathEffectOriginal);
    }

    /**
     * draw a polygon.
     * @param points : vetex of the points.
     * @param color : color to fill or frame.
     * @param drawMode : this is an integer parameter, if it is zero or negative, the polygon is filled,
     * otherwise, the polygon is framed and the drawMode value is the border's with ( the border is always
     * solid line).
     * @param pei
     */
    @Override
    public void drawPolygon(LinkedList<double[]> points,
            com.cyzapps.VisualMFP.Color color, int drawMode, DrawLib.PaintingExtraInfo pei) {
        if (mcurrentImage == null) {
            return; // if current image hasn't been fully initialized, return.
        }
        Graphics g = Graphics.createGraphics(mcurrentImage, mPaint);
        drawPolygon(g, points, color, drawMode, pei);
        g.dispose();
    }
    public static void drawPolygon(Graphics graphics, LinkedList<double[]> points,
                                   com.cyzapps.VisualMFP.Color color, int drawMode,
                                   DrawLib.PaintingExtraInfo pei) {
        if (points.size() < 3) {
            return; // The number of points is invalid.
        }
        int[] xValues = new int[points.size()];
        int[] yValues = new int[points.size()];
        for (int idx = 0; idx < points.size(); idx ++) {
            xValues[idx] = (int)points.get(idx)[0];
            yValues[idx] = (int)points.get(idx)[1];
        }
        Xfermode xfermodeOriginal = graphics.paint.getXfermode();
        if (pei != null) {
            graphics.paint.setXfermode(convert2Xfermode(pei.mpdm));
        }
        int nPaintOriginalColor = graphics.paint.getColor();
        if (color != null) {
            graphics.paint.setColor(color.getARGB());
        }
        Paint.Style styleOriginal = graphics.paint.getStyle();
        float strokeWidthOriginal = graphics.paint.getStrokeWidth();
        if (drawMode <= 0) {
            graphics.paint.setStyle(Paint.Style.FILL);
            graphics.paint.setStrokeWidth(1.0f);
        } else {
            graphics.paint.setStyle(Paint.Style.STROKE);
            graphics.paint.setStrokeWidth(drawMode);
        }

        // path
        Path polyPath = new Path();
        polyPath.moveTo((float)points.get(0)[0], (float)points.get(0)[1]);
        for (int idx = 1; idx < points.size(); idx ++) {
            polyPath.lineTo((float)points.get(idx)[0], (float)points.get(idx)[1]);
        }
        polyPath.lineTo((float)points.get(0)[0], (float)points.get(0)[1]);

        // draw
        graphics.canvas.drawPath(polyPath, graphics.paint);

        graphics.paint.setStrokeWidth(strokeWidthOriginal); // restore original stroke width.
        graphics.paint.setStyle(styleOriginal); // restore original style
        graphics.paint.setColor(nPaintOriginalColor); // restore original color.
        graphics.paint.setXfermode(xfermodeOriginal); // restore xfer mode
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
     * @param pei
     */
    @Override
    public void drawRect(double x, double y, double width, double height,
                         com.cyzapps.VisualMFP.Color color, int drawMode,
                         DrawLib.PaintingExtraInfo pei) {
        if (mcurrentImage == null) {
            return; // if current image hasn't been fully initialized, return.
        }
        Graphics g = Graphics.createGraphics(mcurrentImage, mPaint);
        drawRect(g, x, y, width, height, color, drawMode, pei);
        g.dispose();
    }
    public static void drawRect(Graphics graphics, double x, double y, double width, double height,
                                com.cyzapps.VisualMFP.Color color, int drawMode, DrawLib.PaintingExtraInfo pei) {
        Xfermode xfermodeOriginal = graphics.paint.getXfermode();
        if (pei != null) {
            graphics.paint.setXfermode(convert2Xfermode(pei.mpdm));
        }
        int nPaintOriginalColor = graphics.paint.getColor();
        if (color != null) {
            graphics.paint.setColor(color.getARGB());
        }
        Paint.Style styleOriginal = graphics.paint.getStyle();
        float strokeWidthOriginal = graphics.paint.getStrokeWidth();
        if (drawMode <= 0) {
            graphics.paint.setStyle(Paint.Style.FILL);
            graphics.paint.setStrokeWidth(1.0f);
        } else {
            graphics.paint.setStyle(Paint.Style.STROKE);
            graphics.paint.setStrokeWidth(drawMode);
        }

        // draw
        graphics.canvas.drawRect((float)x, (float)y, (float)(x + width), (float)(y + height), graphics.paint);

        graphics.paint.setStrokeWidth(strokeWidthOriginal); // restore original stroke width.
        graphics.paint.setStyle(styleOriginal); // restore original style
        graphics.paint.setColor(nPaintOriginalColor); // restore original color.
        graphics.paint.setXfermode(xfermodeOriginal);   // restore xfer mode
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
        if (mcurrentImage == null) {
            return; // if current image hasn't been fully initialized, return.
        }
        Graphics g = Graphics.createGraphics(mcurrentImage, mPaint);
        clearRect(g, x, y, width, height);
        g.dispose();
    }
    public static void clearRect(Graphics graphics, double x, double y, double width, double height) {
        Paint clearPaint = new Paint();
        clearPaint.setColor(android.graphics.Color.TRANSPARENT);
        clearPaint.setStyle(Paint.Style.FILL);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        graphics.canvas.drawRect((float)x, (float)y, (float)(x + width), (float)(y + height), clearPaint);
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
     * @param pei
     */
    @Override
    public void drawOval(double x, double y, double width, double height,
            com.cyzapps.VisualMFP.Color color, int drawMode, DrawLib.PaintingExtraInfo pei) {
        if (mcurrentImage == null) {
            return; // if current image hasn't been fully initialized, return.
        }
        Graphics g = Graphics.createGraphics(mcurrentImage, mPaint);
        drawOval(g, x, y, width, height, color, drawMode, pei);
        g.dispose();
    }
    public static void drawOval(Graphics graphics, double x, double y, double width, double height,
                                com.cyzapps.VisualMFP.Color color, int drawMode, DrawLib.PaintingExtraInfo pei) {
        Xfermode xfermodeOriginal = graphics.paint.getXfermode();
        if (pei != null) {
            graphics.paint.setXfermode(convert2Xfermode(pei.mpdm));
        }
        int nPaintOriginalColor = graphics.paint.getColor();
        if (color != null) {
            graphics.paint.setColor(color.getARGB());
        }
        Paint.Style styleOriginal = graphics.paint.getStyle();
        float strokeWidthOriginal = graphics.paint.getStrokeWidth();
        if (drawMode <= 0) {
            graphics.paint.setStyle(Paint.Style.FILL);
            graphics.paint.setStrokeWidth(1.0f);
        } else {
            graphics.paint.setStyle(Paint.Style.STROKE);
            graphics.paint.setStrokeWidth(drawMode);
        }

        // draw
        graphics.canvas.drawOval(new RectF((float)x, (float)y, (float)(x + width), (float)(y + height)), graphics.paint);

        graphics.paint.setStrokeWidth(strokeWidthOriginal); // restore original stroke width.
        graphics.paint.setStyle(styleOriginal); // restore original style
        graphics.paint.setColor(nPaintOriginalColor); // restore original color.
        graphics.paint.setXfermode(xfermodeOriginal); // restore xfer mode
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
        if (mcurrentImage == null) {
            return; // if current image hasn't been fully initialized, return.
        }
        Graphics g = Graphics.createGraphics(mcurrentImage, mPaint);
        clearOval(g, x, y, width, height);
        g.dispose();
    }
    public static void clearOval(Graphics graphics, double x, double y, double width, double height) {
        Paint clearPaint = new Paint();
        clearPaint.setColor(android.graphics.Color.TRANSPARENT);
        clearPaint.setStyle(Paint.Style.FILL);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        graphics.canvas.drawOval(new RectF((float)x, (float)y, (float)(x + width), (float)(y + height)), clearPaint);
    }

    /**
     * return false if loading image fails.
     * @param imgHandle
     * @param left
     * @param top
     * @param wRatio
     * @param hRatio
     * @param pei
     * @return 
     */
    @Override
    public boolean drawImage(DataClass imgHandle, double left, double top, double wRatio, double hRatio, DrawLib.PaintingExtraInfo pei) {
        if (mcurrentImage == null) {
            return false; // if current image hasn't been fully initialized, return.
        }
        Graphics g = Graphics.createGraphics(mcurrentImage, mPaint);
        boolean b = drawImage(g, imgHandle, left, top, wRatio, hRatio, pei);
        g.dispose();
        return b;
    }
    public static boolean drawImage(Graphics graphics, DataClass imgHandle, double left, double top, double wRatio, double hRatio, DrawLib.PaintingExtraInfo pei) {
        Bitmap img = null;
        DataClass datumConverted = null;
        Boolean bRecycleImg = false;
        if (wRatio <= 0 || hRatio <= 0) {
            return false; // ratio is invalid.
        }
        if (null != (datumConverted = DCHelper.try2LightCvtOrRetDCString(imgHandle))) {
            // string or derivative of string
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                img = BitmapFactory.decodeFile(DCHelper.lightCvtOrRetDCString(datumConverted).getStringValue());
                bRecycleImg = true;
            } catch (ErrProcessor.JFCALCExpErrException ex) {
                Log.e(LOG_TAG, "drawImageSimple : " + ex.toString());;  // unexpected exception.
            }
        } else if (null != (datumConverted = DCHelper.try2LightCvtOrRetDCExtObjRef(imgHandle))) {
            try {
                // should be a buffered image
                if (DCHelper.lightCvtOrRetDCExtObjRef(datumConverted).getExternalObject() instanceof ImageMgrAndroid.ImageWrapper) {
                    img = ((ImageMgrAndroid.ImageWrapper)DCHelper.lightCvtOrRetDCExtObjRef(datumConverted).getExternalObject()).getImageFromWrapper();
                }
            } catch (ErrProcessor.JFCALCExpErrException ex) {
                Log.e(LOG_TAG, "drawImageSimple : " + ex.toString());;  // unexpected exception.
            }
        }
        if (img != null && img.isRecycled() == false) { // check if recycled.
            Xfermode xfermodeOriginal = graphics.paint.getXfermode();
            if (pei != null) {
                graphics.paint.setXfermode(convert2Xfermode(pei.mpdm));
            }
            if (wRatio == 1.0 && hRatio == 1.0) {
                graphics.canvas.drawBitmap(img, (int) left, (int) top, graphics.paint);
            } else {
                int destW = (int) (img.getWidth() * wRatio);
                int destH = (int) (img.getHeight() * hRatio);
                if (destW < 1) {
                    destW = 1;
                }
                if (destH < 1) {
                    destH = 1;
                }
                graphics.canvas.drawBitmap(img,
                        new Rect(0, 0, img.getWidth(), img.getHeight()),
                        new Rect((int) left, (int) top, (int) (destW + left), (int) (destH + top)),
                        graphics.paint);
            }
            graphics.paint.setXfermode(xfermodeOriginal);
            if (bRecycleImg) {
                img.recycle();
            }
            return true;
        }
        return false;
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
            double dstX1, double dstY1, double dstX2, double dstY2, DrawLib.PaintingExtraInfo pei) {
        if (mcurrentImage == null) {
            return false; // if current image hasn't been fully initialized, return.
        }
        Graphics g = Graphics.createGraphics(mcurrentImage, mPaint);
        boolean b = drawImage(g, imgHandle, srcX1, srcY1, srcX2, srcY2, dstX1, dstY1, dstX2, dstY2, pei);
        g.dispose();
        return b;
    }
    public static boolean drawImage(Graphics graphics, DataClass imgHandle,
                             double srcX1, double srcY1, double srcX2, double srcY2,
                             double dstX1, double dstY1, double dstX2, double dstY2, DrawLib.PaintingExtraInfo pei) {
        Bitmap img = null;
        DataClass datumConverted = null;
        Boolean bRecycleImg = false;
        if (null != (datumConverted = DCHelper.try2LightCvtOrRetDCString(imgHandle))) {
            // string or derivative of string
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                img = BitmapFactory.decodeFile(DCHelper.lightCvtOrRetDCString(datumConverted).getStringValue());
                bRecycleImg = true;
            } catch (ErrProcessor.JFCALCExpErrException ex) {
                Log.e(LOG_TAG, "drawImageFull : " + ex.toString());;  // unexpected exception.
            }
        } else if (null != (datumConverted = DCHelper.try2LightCvtOrRetDCExtObjRef(imgHandle))) {
            try {
                // should be a buffered image
                if (DCHelper.lightCvtOrRetDCExtObjRef(datumConverted).getExternalObject() instanceof ImageMgrAndroid.ImageWrapper) {
                    img = ((ImageMgrAndroid.ImageWrapper) DCHelper.lightCvtOrRetDCExtObjRef(datumConverted).getExternalObject()).getImageFromWrapper();
                }
            } catch (ErrProcessor.JFCALCExpErrException ex) {
                Log.e(LOG_TAG, "drawImageFull : " + ex.toString());;  // unexpected exception.
            }
        }
        if (img != null && img.isRecycled() == false) { // check if recycled.
            Xfermode xfermodeOriginal = graphics.paint.getXfermode();
            if (pei != null) {
                graphics.paint.setXfermode(convert2Xfermode(pei.mpdm));
            }
            graphics.canvas.drawBitmap(img,
                    new Rect((int) srcX1, (int) srcY1, (int) srcX2, (int) srcY2),
                    new Rect((int) dstX1, (int) dstY1, (int) dstX2, (int) dstY2),
                    graphics.paint);
            graphics.paint.setXfermode(xfermodeOriginal);
            if (bRecycleImg) {
                img.recycle();
            }
            return true;
        }
        return false;
    }

    public static void drawBackgroundImage(Graphics g, int width, int height, ImageMgrAndroid.ImageWrapper imageBkGrnd, int bkGrndImgMode) {
        // draw background image
        if (imageBkGrnd != null && imageBkGrnd.getImageFromWrapper().isRecycled() == false && width > 0 && height > 0) {
            // no need to worry about invalid parameter because width and height now are valid.
            int w = imageBkGrnd.getImageFromWrapper().getWidth();
            int h = imageBkGrnd.getImageFromWrapper().getHeight();
            switch(bkGrndImgMode) {
                case 3: // original image in the middle
                    int overlappedWidth = Math.min(width, w);
                    int overlappedHeight = Math.min(height, h);
                    int srcX1 = 0, srcX2 = w, dstX1 = 0, dstX2 = width;
                    if (overlappedWidth < w) {
                        srcX1 = (w - overlappedWidth) / 2;
                        srcX2 = srcX1 + overlappedWidth;
                    }
                    if (overlappedWidth < width) {
                        dstX1 = (width - overlappedWidth) / 2;
                        dstX2 = dstX1 + overlappedWidth;
                    }
                    int srcY1 = 0, srcY2 = h, dstY1 = 0, dstY2 = height;
                    if (overlappedHeight < h) {
                        srcY1 = (h - overlappedHeight) / 2;
                        srcY2 = srcY1 + overlappedHeight;
                    }
                    if (overlappedHeight < height) {
                        dstY1 = (height - overlappedHeight) / 2;
                        dstY2 = dstY1 + overlappedHeight;
                    }
                    g.canvas.drawBitmap(imageBkGrnd.getImageFromWrapper(),
                            new Rect(srcX1, srcY1, srcX2, srcY2),
                            new Rect(dstX1, dstY1, dstX2, dstY2),
                            g.paint);
                    break;
                case 2: // tiled

                    for (int x = 0; x < width; x += w) {
                        for (int y = 0; y < height; y += h) {
                            g.canvas.drawBitmap(imageBkGrnd.getImageFromWrapper(), x, y, g.paint);
                        }
                    }
                    break;
                case 1: // scaled
                    g.canvas.drawBitmap(imageBkGrnd.getImageFromWrapper(),
                            new Rect(0, 0, w, h), new Rect(0, 0, width, height),
                            g.paint);
                    break;
                default: // original image.
                    g.canvas.drawBitmap(imageBkGrnd.getImageFromWrapper(), 0, 0, g.paint);
            }
        }
        
    }
    
    public static void drawBackground(Bitmap currentImg, Color colorBkGrnd, ImageMgrAndroid.ImageWrapper imageBkGrnd, int bkGrndImgMode) {
        if (currentImg == null) {
            return;
        }
        Graphics g = Graphics.createGraphics(currentImg, new Paint());
        // draw background color.
        g.canvas.drawARGB(colorBkGrnd.mnAlpha,
                          colorBkGrnd.mnR,
                          colorBkGrnd.mnG,
                          colorBkGrnd.mnB);

        drawBackgroundImage(g, currentImg.getWidth(), currentImg.getHeight(), imageBkGrnd, bkGrndImgMode);
        g.dispose();        
    }
    
    protected void startPainting() {
        if (mnTargetWidth <= 0 || mnTargetHeight <= 0 || !mbDisplayOnLive) {
            if (mcurrentImage != null && !mcurrentImage.isRecycled()) {
                mcurrentImage.recycle();
            }
            mcurrentImage = null;
            return;    // no need to paint.
        } 
        if (!mbImageUptoDate) { // current image cannot be reused.
            if (mcurrentImage != null && !mcurrentImage.isRecycled()) {
                mcurrentImage.recycle();
            }
            mcurrentImage = Bitmap.createBitmap(mnTargetWidth, mnTargetHeight, Bitmap.Config.ARGB_8888);
            draw(0, 0, mnTargetWidth, mnTargetHeight);
            mbImageUptoDate = true;
        }
    }
    
    public void draw(double x, double y, double width, double height) {
        if (mbDisplayOnLive) {
            drawBackground(mcurrentImage, mcolorBkGrnd, mimageBkGrnd, mBkgrndImgMode);
            // no need to remove all obsolete painting call backs because these call backs
            // have been removed when updatepainting call back is added.
            double right = x + width;
            double bottom = y + height;
            boolean bWholeDisplayInRange = (x == 0) && (y == 0)
                    && (width == mnTargetWidth) // no need to worry about getGDIView() is null.
                    && (height == mnTargetHeight);
            for (PaintingCallBacks.PaintingCallBack pcb : mqueuePaintingCallBacks) {
                if (this.equals(pcb.getDisplay2D())
                        && (bWholeDisplayInRange || pcb.isInPaintingRange(x, y, right, bottom))) {
                    pcb.call(); // draw all the painting call backs.
                }
            }
        }
    }

    public void draw() {
        draw(0, 0, mnTargetWidth, mnTargetHeight);  // check onlive inside this function.
    }
    
    @Override
    public void repaint() {
        startPainting();
    }

    @Override
    public int addRtcVideoOutput(int left, int top, int width, int height, boolean enableSlide) {
        return -1;   // RTC Video is not supported by ImageDisplay
    }

    @Override
    public boolean startLocalStream(int videoOutputId) {
        return false;   // RTC Video is not supported by ImageDisplay
    }

    @Override
    public void stopLocalStream() {    }

    @Override
    public boolean startVideoCapturer() {
        return false;
    }

    @Override
    public void stopVideoCapturer() {    }

    @Override
    public boolean setVideoTrackEnable(int idx, boolean enable) {
        return false;
    }

    @Override
    public boolean getVideoTrackEnable(int idx) {
        return false;
    }

    @Override
    public boolean setAudioTrackEnable(int idx, boolean enable) {
        return false;
    }

    @Override
    public boolean getAudioTrackEnable(int idx) {
        return false;
    }

    @Override
    public int[] getRtcVideoOutputLeftTop(int id) {
        return new int[0];   // RTC Video is not supported by ImageDisplay
    }

    @Override
    public int getRtcVideoOutputCount() {
        return 0;   // RTC Video is not supported by ImageDisplay
    }

    @Override
    public boolean linkVideoStream(String peerId, int trackId, int videoOutputId) {
        return false;   // RTC Video is not supported by ImageDisplay
    }

    @Override
    public boolean unlinkVideoStream(String peerId, int trackId) {
        return false;   // RTC Video is not supported by ImageDisplay
    }

    @Override
    public int unlinkVideoStream(int videoOutputId) {
        return 0;   // RTC Video is not supported by ImageDisplay
    }

    @Override
    public DisplayLib.GraphicDisplayType getDisplayType() {
        if (!mbDisplayOnLive) {
            return GraphicDisplayType.INVALID_DISPLAY;
        } else if (mstrFilePath == null) {
            return GraphicDisplayType.IMAGE_DISPLAY;
        } else {
            return GraphicDisplayType.IMAGE_PATH_DISPLAY;
        }
    }

    @Override
    public void initialize() {
        // do nothing.
    }

    @Override
    public void update() {
        // at this moment, do nothing here.
    }

    @Override
    public void close() {
        if (!mbDisplayOnLive) {
            return;
        } else {
            // do not draw image or save image, just close.
            mcolorBkGrnd = new Color();
            mBkgrndImgMode = 0;
            mimageBkGrnd = null;
            mnTargetWidth = 0;
            mnTargetHeight = 0;
            mstrFilePath = null;
            mcurrentImage = null;
            mbImageUptoDate = false;
            clearPaintingCallBacks();
            mbDisplayOnLive = false;
        }
    }
}
