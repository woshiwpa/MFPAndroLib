/*
 * MFP project, Display2D.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.JGI2D;

import com.cyzapps.JGI2D.DrawLib.PaintingExtraInfo;
import com.cyzapps.JGI2D.PaintingCallBacks.PaintingCallBack;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassExtObjRef;
import com.cyzapps.VisualMFP.LineStyle;
import com.cyzapps.VisualMFP.PointStyle;
import com.cyzapps.VisualMFP.TextStyle;
import java.util.LinkedList;

/**
 *
 * @author tony
 */
public abstract class Display2D implements DisplayLib.IGraphicDisplay {
    public abstract GIEvent pullGIEvent();
    
    public abstract void addPaintingCallBack(PaintingCallBack paintingCallBack);
    public abstract void clearPaintingCallBacks();
    
    public abstract boolean isDisplayOnLive();
    public abstract void setDisplayOrientation(int orientation);
    public abstract int getDisplayOrientation();
    public abstract void setDisplaySize(int width, int height);
    public abstract int[] getDisplaySize();
    public abstract void setDisplayResizable(boolean resizable);
    public abstract boolean getDisplayResizable();
    public abstract void setDisplayCaption(String strCaption);
    public abstract String getDisplayCaption();
    public abstract void setBackgroundColor(com.cyzapps.VisualMFP.Color clr);
    public abstract com.cyzapps.VisualMFP.Color getBackgroundColor();
    public abstract void setBackgroundImage(DataClassExtObjRef imgHandler, int mode); // cannot be path. Only support image handler.
    public abstract DataClass getBackgroundImage();	// return a reference of image handler or Null if invalid.
    public abstract int getBackgroundImageMode();	// return image mode (tiled(2), scaled(1) or actual(0)).
    public abstract void setSnapshotAsBackground(boolean bUpdateScreen, boolean bClearPaintingCallbacks);
    public abstract DataClass getSnapshotImage(boolean bUpdateScreen, double dWRatio, double dHRatio);   // get snapshot as an image
    public abstract void setDisplayConfirmClose(boolean bConfirmClose);
    public abstract boolean getDisplayConfirmClose();
    public abstract int[] calcTextOrigin(String text, int x, int y, int w, int h, int horAlign,int verAlign, TextStyle txtStyle);
    public abstract int[] calcTextBoundary(String text, int x, int y, TextStyle txtStyle);
    public abstract void drawText(String text, double x, double y,
        TextStyle txtStyle, double dRotateRadian, PaintingExtraInfo paintingExtras);
    public abstract void drawPoint(double x, double y, PointStyle pointStyle, PaintingExtraInfo paintingExtras);
    public abstract void drawLine(double x0, double y0, double x1, double y1,
    	LineStyle lineStyle, PaintingExtraInfo paintingExtras);
    public abstract void drawPolygon(LinkedList<double[]> points,
        com.cyzapps.VisualMFP.Color color, int drawMode, PaintingExtraInfo paintingExtras);
    public abstract void drawRect(double x, double y, double width, double height,
        com.cyzapps.VisualMFP.Color color, int drawMode, PaintingExtraInfo paintingExtras);
    public abstract void clearRect(double x, double y, double width, double height);
    public abstract void drawOval(double x, double y, double width, double height,
        com.cyzapps.VisualMFP.Color color, int drawMode, PaintingExtraInfo paintingExtras);
    public abstract void clearOval(double x, double y, double width, double height);
    public abstract boolean drawImage(DataClass imgHandleOrPath, double left, double top, double wRatio, double hRatio, PaintingExtraInfo paintingExtras);
    public abstract boolean drawImage(DataClass imgHandleOrPath,
        double srcX1, double srcY1, double srcX2, double srcY2,
        double dstX1, double dstY1, double dstX2, double dstY2,
        PaintingExtraInfo paintingExtras);
    public abstract void repaint();

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
