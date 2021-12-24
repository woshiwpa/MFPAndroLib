/*
 * MFP project, PaintingCallBacks.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.JGI2D;

import com.cyzapps.JGI2D.DrawLib.PaintingExtraInfo;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.DataClassExtObjRef;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.DataClassString;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.VisualMFP.LineStyle;
import com.cyzapps.VisualMFP.PointStyle;
import com.cyzapps.VisualMFP.TextStyle;
import java.util.LinkedList;

/**
 *
 * @author tony
 */
public class PaintingCallBacks {

    public static class PaintingCallBackBGrnd {
        protected DataClass mdcOwner = new DataClassNull();  // the owner object which calls this draw. It cannot be JAVA null.
        DataClass getOwner() {
            // mdcOwner can only be DataClassNull, DataClassSingleNum(Integer),
            // DataClassString and DataClassExtObjRef, not derived type.
            return mdcOwner;
        }
        protected double mdTS = Double.NaN; // the time stamp of the object which calls this draw.
        double getTS() {
            return mdTS;
        }
        
        /**
         * convert a datumOwnerInfo into painting callback background information. note
         * that datumOwnerInfo cannot be null
         * consider the following situations:
         * 1.
         * datumOwnerInfo is JAVA null or DataClassNull (or any MFP data class with Null type),
         * this means PaintingCallBackBGrnd doesn't have an owner, and mdTS is current
         * timestamp;
         * 2.
         * datumOwnerInfo is a DataClassSingleNum (Integer), this means the mObjOwner member of
         * PaintingCallBackBGRnd is an id (integer based), and mdTS is current timestamp;
         * 3.
         * datumOwnerInfo is a DataClassString (String), this means the mObjOwner member of
         * PaintingCallBackBGRnd is a name (String based), and mdTS is current timestamp;
         * 4.
         * datumOwnerInfo is a DataClassExtObjRef, this means the mObjOwner member of
         * PaintingCallBackBGRnd is an external object, and mdTS is current timestamp;
         * 5.
         * datumOwnerInfo is a 2-element DataClassArray, the first element is either
         * JAVA null or DataClassNull (no owner or say screen owned, mobjOwner is mfp null), or
         * DataClassSingleNum (Integer) (integer based owner's id, mobjOwner is an integer), or
         * DataClassString (string based owner's name, mobjOwner is a string), or
         * DataClassExtObjRef (owner object, mobjOwner is an external object). The second
         * element can be converted to a DataClassSingleNum (Double), which is timestamp.
         * 6.
         * None of the above cases, which is invalid.
         * 
         * @param datumOwnerInfo
         * @return a PaintingCallBackBGrnd object. NOTE THAT IT CANNOT BE null. An invalid
         * PaintingCallBackBGrnd should be an uninitialized PaintingCallBackBGrnd but not null.
         * @throws com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException 
         */
        public static PaintingCallBackBGrnd getPaintingCallBackBGrnd(DataClass datumOwnerInfo) throws ErrProcessor.JFCALCExpErrException {
            PaintingCallBackBGrnd pcbBGrnd = new PaintingCallBackBGrnd();
            DataClass datumConverted = null;
            if (datumOwnerInfo == null || datumOwnerInfo.isNull()) {
                pcbBGrnd.mdTS = System.currentTimeMillis();
            } else if (null != (datumConverted = DCHelper.try2LightCvtOrRetDCMFPInt(datumOwnerInfo))) {
                // shouldn't be mfp data type null here.
                // note that we need to recreate DataClassSingleNum to avoid overrided type of
                // datumOwnerInfo.
                pcbBGrnd.mdcOwner = datumConverted;
                pcbBGrnd.mdTS = System.currentTimeMillis();
            } else if (null != (datumConverted = DCHelper.try2LightCvtOrRetDCString(datumOwnerInfo))) {
                // shouldn't be mfp data type null here.
                // dataclassstring type does not allow String is null.
                pcbBGrnd.mdcOwner = datumConverted;
                pcbBGrnd.mdTS = System.currentTimeMillis();
            } else if (null != (datumConverted = DCHelper.try2LightCvtOrRetDCExtObjRef(datumOwnerInfo))) {
                // shouldn't be mfp data type null here.
                pcbBGrnd.mdcOwner = DCHelper.lightCvtOrRetDCExtObjRef(datumConverted).getExternalObject() == null?
                        new DataClassNull():datumConverted;
                pcbBGrnd.mdTS = System.currentTimeMillis();
            } else {
                // now datumOwnerInfo must be an array, otherwise will throw exception
                DataClassArray datumArray = DCHelper.lightCvtOrRetDCArray(datumOwnerInfo);
                if (datumArray.getDataListSize() != 2) {
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
                DataClass datumOwner = datumArray.getDataAtIndexByRef(new int[] {0});  // OK, it is nowner
                if (datumOwner == null || datumOwner.isNull()) {
                    // need do nothing here
                } else if (null != (datumConverted = DCHelper.try2LightCvtOrRetDCMFPInt(datumOwner))) {
                    // shouldn't be mfp data type null here.
                    pcbBGrnd.mdcOwner = datumConverted;
                } else if (null != (datumConverted = DCHelper.try2LightCvtOrRetDCString(datumOwner))) {
                    // shouldn't be mfp data type null here.
                    // dataclassstring type does not allow String is null.
                    pcbBGrnd.mdcOwner = datumConverted;
                } else if (null != (datumConverted = DCHelper.try2LightCvtOrRetDCExtObjRef(datumOwner))) {
                    // shouldn't be mfp data type null here.
                    pcbBGrnd.mdcOwner = DCHelper.lightCvtOrRetDCExtObjRef(datumConverted).getExternalObject() == null?
                            new DataClassNull():datumConverted;
                } else {
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
                DataClass datumTS = datumArray.getDataAtIndexByRef(new int[] {1});  // OK, it is timestamp
                pcbBGrnd.mdTS = DCHelper.lightCvtOrRetDCMFPDec(datumTS).getDataValue().doubleValue();
            }
            return pcbBGrnd;
        }
    }

    public static abstract class PaintingCallBack {
        protected PaintingCallBackBGrnd mpcbBGrnd = new PaintingCallBackBGrnd(); // the owner object which calls this draw. It cannot be null.
        protected Display2D mdestDisplay = null;  // Destination display. Could be a flat GDI, a image handler or string path.
        
        public PaintingCallBackBGrnd getBackgroundInfo() {
            return mpcbBGrnd;
        }
        
        /**
         * NOTE that mdTS is the timestamp to compare with call back background's timestamp of
         * next UpdatePaintingCallBack with the same mobjOwner. If it is smaller, this callback
         * is obselete and should be removed from call back list.
         * Also NOTE that to identify if two callbacks are with the same mobjOwner, if the two
         * callbacks has different owner if (i.e. one is owner id, the other is owner name, or
         * one is a null owner while the other is owner object), the two owners must be
         * different. If both of them are owner ids, compare ids' integer values. If both of
         * them are owner names, compare the two name strings. If both of them are owner
         * external object, use object equal function to compare. If both of them are null,
         * they always equal.
         * @param pcbBGrndAnother another callback's background info.
         * @return  true or false.
         */
        public boolean isCallBackOutDated(PaintingCallBackBGrnd pcbBGrndAnother) {
            boolean bIsSameOwner = false;
            if (mpcbBGrnd.mdcOwner.getDataClassType() == pcbBGrndAnother.mdcOwner.getDataClassType()) {
                if (mpcbBGrnd.mdcOwner.isNull()) {
                    bIsSameOwner = true; // pcbBGrndAnother.mdcOwner must be null as well.
                } else if (mpcbBGrnd.mdcOwner instanceof DataClassSingleNum) {
                    try {
                        int id = DCHelper.lightCvtOrRetDCMFPInt(mpcbBGrnd.mdcOwner).getDataValue().intValue();
                        int idAnother = DCHelper.lightCvtOrRetDCMFPInt(pcbBGrndAnother.mdcOwner).getDataValue().intValue();
                        bIsSameOwner = id == idAnother;
                    } catch (ErrProcessor.JFCALCExpErrException ex) {
                        // will not be here;
                    }
                } else if (mpcbBGrnd.mdcOwner instanceof DataClassString) {
                    try {
                        String name = DCHelper.lightCvtOrRetDCString(mpcbBGrnd.mdcOwner).getStringValue();
                        String nameAnother = DCHelper.lightCvtOrRetDCString(pcbBGrndAnother.mdcOwner).getStringValue();
                        bIsSameOwner = name.equals(nameAnother);
                    } catch (ErrProcessor.JFCALCExpErrException ex) {
                        // will not be here;
                    }
                } else if (mpcbBGrnd.mdcOwner instanceof DataClassExtObjRef) {
                    try {
                        // obj and objAnother shouldn't be null.
                        Object obj = DCHelper.lightCvtOrRetDCExtObjRef(mpcbBGrnd.mdcOwner).getExternalObject();
                        Object objAnother = DCHelper.lightCvtOrRetDCExtObjRef(pcbBGrndAnother.mdcOwner).getExternalObject();
                        bIsSameOwner = obj.equals(objAnother);
                    } catch (ErrProcessor.JFCALCExpErrException ex) {
                        // will not be here;
                    }
                }
            }
            if (bIsSameOwner) {
                // we are comparing the same object.
                return mpcbBGrnd.mdTS < pcbBGrndAnother.mdTS;
            } else {
                return false;
            }
        }
        
        public boolean isCallBackOutDated(PaintingCallBack pcbAnother) {
            return null != mdestDisplay
                    && mdestDisplay.equals(pcbAnother.mdestDisplay)
                    && isCallBackOutDated(pcbAnother.mpcbBGrnd);
        }
        
        public Display2D getDisplay2D() {
            // which display to show this 
            return mdestDisplay;
        }
        
        public abstract boolean call();  // true if successful, false if failed.
        
        public abstract boolean isInPaintingRange(double left, double top, double right, double bottom); // is this call back in the painting range?
    }
    
    public static class UpdatePaintingCallBack extends PaintingCallBack {
        public UpdatePaintingCallBack(PaintingCallBackBGrnd pcbBGrnd, Display2D destDisplay) {
            this.mpcbBGrnd = pcbBGrnd;
            this.mdestDisplay = destDisplay;
        }

        @Override
        public boolean call() {
            return null != mdestDisplay; // has been initialized or not.
        }
        
        @Override
        public boolean isInPaintingRange(double left, double top, double right, double bottom) {
            return false;
        }
    }
    
    public static class DrawTextCallBack extends PaintingCallBack {

        public String text = "";
        public double x = 0;
        public double y = 0;
        public TextStyle txtStyle;
        public double dRotateRadian = 0;
        public PaintingExtraInfo pei = null;
        
        public DrawTextCallBack(PaintingCallBackBGrnd pcbBGrnd, Display2D destDisplay, String text, 
        		double x, double y, TextStyle txtStyle, double dRotateRadian, PaintingExtraInfo pei) {
            this.mpcbBGrnd = pcbBGrnd;
            this.mdestDisplay = destDisplay;
            this.text = text;
            this.x = x;
            this.y = y;
            this.txtStyle = txtStyle;
            this.dRotateRadian = dRotateRadian;
            this.pei = pei;
        }
        @Override
        public boolean call() {
            if (null == mdestDisplay) {
                return false;   // hasnt been initialized
            } else {
            	mdestDisplay.drawText(text, x, y, txtStyle, dRotateRadian, pei);
                return true;
            }
        }
        
        @Override
        public boolean isInPaintingRange(double left, double top, double right, double bottom) {
            return true;    // this needs complicated calculation, so simply set it true.
        }
    }
    
    public static class DrawPointCallBack extends PaintingCallBack {

        public double x = 0;
        public double y = 0;
        public PointStyle pointStyle = new PointStyle();
        public PaintingExtraInfo pei = null;

        public DrawPointCallBack(PaintingCallBackBGrnd pcbBGrnd, Display2D destDisplay, double x, double y,
        		PointStyle pointStyle, PaintingExtraInfo pei) {
            this.mpcbBGrnd = pcbBGrnd;
            this.mdestDisplay = destDisplay;
            this.x = x;
            this.y = y;
            this.pointStyle = pointStyle;
            this.pei = pei;
        }
        
        @Override
        public boolean call() {
            if (null == mdestDisplay) {
                return false;   // hasnt been initialized
            } else {
            	mdestDisplay.drawPoint(x, y, pointStyle, pei);
                return true;
            }
        }
        
        @Override
        public boolean isInPaintingRange(double left, double top, double right, double bottom) {
            return x + pointStyle.mdSize >= left
                    && x - pointStyle.mdSize <= right
                    && y + pointStyle.mdSize >= top
                    && y - pointStyle.mdSize <= bottom;
        }
    }
    
    public static class DrawLineCallBack extends PaintingCallBack {

        public double x0 = 0;
        public double y0 = 0;
        public double x1 = 0;
        public double y1 = 0;
        public LineStyle lineStyle = new LineStyle();
        public PaintingExtraInfo pei = null;

        public DrawLineCallBack(PaintingCallBackBGrnd pcbBGrnd, Display2D destDisplay, double x0, double y0, double x1, double y1,
        		LineStyle lineStyle, PaintingExtraInfo pei) {
            this.mpcbBGrnd = pcbBGrnd;
            this.mdestDisplay = destDisplay;
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
            this.lineStyle = lineStyle;
            this.pei = pei;
        }
        @Override
        public boolean call() {
            if (null == mdestDisplay) {
                return false;   // hasnt been initialized
            } else {
                mdestDisplay.drawLine(x0, y0, x1, y1, lineStyle, pei);
                return true;
            }
        }
        
        @Override
        public boolean isInPaintingRange(double left, double top, double right, double bottom) {
            double lineLeft = Math.min(x0, x1) - lineStyle.mdLineWidth;
            double lineRight = Math.max(x0, x1) + lineStyle.mdLineWidth;
            double lineTop = Math.min(y0, y1) - lineStyle.mdLineWidth;
            double lineBottom = Math.max(y0, y1) + lineStyle.mdLineWidth;
            return lineRight >= left
                    && lineLeft <= right
                    && lineBottom >= top
                    && lineTop <= bottom;
        }
    }
    
    public static class DrawPolygonCallBack extends PaintingCallBack {

        public LinkedList<double[]> points = new LinkedList<>();
        public com.cyzapps.VisualMFP.Color color = new com.cyzapps.VisualMFP.Color();
        public int drawMode = 0;
        public PaintingExtraInfo pei = null;

        public DrawPolygonCallBack(PaintingCallBackBGrnd pcbBGrnd, Display2D destDisplay, LinkedList<double[]> points,
        		com.cyzapps.VisualMFP.Color color, int drawMode, PaintingExtraInfo pei) {
            this.mpcbBGrnd = pcbBGrnd;
            this.mdestDisplay = destDisplay;
            this.points = points;
            this.color = color;
            this.drawMode = drawMode;
            this.pei = pei;
        }
        @Override
        public boolean call() {
            if (null == mdestDisplay) {
                return false;   // hasnt been initialized
            } else {
                mdestDisplay.drawPolygon(points, color, drawMode, pei);
                return true;
            }
        }
        
        @Override
        public boolean isInPaintingRange(double left, double top, double right, double bottom) {
            double paintLeft = Double.MAX_VALUE;
            double paintRight = Double.MIN_VALUE;
            double paintTop = Double.MAX_VALUE;
            double paintBottom = Double.MIN_VALUE;
            for (double[] point : points) {
                if (point.length == 2) {
                    // this is a valid point
                    if (point[0] > paintRight) {
                        paintRight = point[0];
                    }
                    if (point[0] < paintLeft) {
                        paintLeft = point[0];
                    }
                    if (point[1] > paintBottom) {
                        paintBottom = point[1];
                    }
                    if (point[1] < paintTop) {
                        paintTop = point[1];
                    }
                }
            }
            if (drawMode > 0) {
                paintRight += drawMode;
                paintLeft -= drawMode;
                paintBottom += drawMode;
                paintTop -= drawMode;
            }
            return paintRight >= left
                    && paintLeft <= right
                    && paintBottom >= top
                    && paintTop <= bottom;
        }
    }
    
    public static class DrawRectCallBack extends PaintingCallBack {

        public double x = 0;
        public double y = 0;
        public double width = 0;
        public double height = 0;
        public com.cyzapps.VisualMFP.Color color = new com.cyzapps.VisualMFP.Color();
        public int drawMode = 0;
        public PaintingExtraInfo pei = null;

        public DrawRectCallBack(PaintingCallBackBGrnd pcbBGrnd, Display2D destDisplay, double x, double y, double width, double height,
        		com.cyzapps.VisualMFP.Color color, int drawMode, PaintingExtraInfo pei) {
            this.mpcbBGrnd = pcbBGrnd;
            this.mdestDisplay = destDisplay;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
            this.drawMode = drawMode;
            this.pei = pei;
        }
        @Override
        public boolean call() {
            if (null == mdestDisplay) {
                return false;   // hasnt been initialized
            } else {
                mdestDisplay.drawRect(x, y, width, height, color, drawMode, pei);
                return true;
            }
        }
        
        @Override
        public boolean isInPaintingRange(double left, double top, double right, double bottom) {
            double paintLeft = x;
            double paintRight = x + width;
            double paintTop = y;
            double paintBottom = y + height;
            if (drawMode > 0) {
                paintRight += drawMode;
                paintLeft -= drawMode;
                paintBottom += drawMode;
                paintTop -= drawMode;
            }
            return paintRight > left
                    && paintLeft < right
                    && paintBottom > top
                    && paintTop < bottom;
        }
    }
    
    public static class ClearRectCallBack extends PaintingCallBack {

        public double x = 0;
        public double y = 0;
        public double width = 0;
        public double height = 0;

        public ClearRectCallBack(PaintingCallBackBGrnd pcbBGrnd, Display2D destDisplay, double x, double y, double width, double height) {
            this.mpcbBGrnd = pcbBGrnd;
            this.mdestDisplay = destDisplay;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        @Override
        public boolean call() {
            if (null == mdestDisplay) {
                return false;   // hasnt been initialized
            } else {
                mdestDisplay.clearRect(x, y, width, height);
                return true;
            }
        }
        
        @Override
        public boolean isInPaintingRange(double left, double top, double right, double bottom) {
            double paintLeft = x;
            double paintRight = x + width;
            double paintTop = y;
            double paintBottom = y + height;
            return paintRight > left
                    && paintLeft < right
                    && paintBottom > top
                    && paintTop < bottom;
        }
    }
    
    public static class DrawOvalCallBack extends PaintingCallBack {

        public double x = 0;
        public double y = 0;
        public double width = 0;
        public double height = 0;
        public com.cyzapps.VisualMFP.Color color = new com.cyzapps.VisualMFP.Color();
        public int drawMode = 0;
        public PaintingExtraInfo pei = null;

        public DrawOvalCallBack(PaintingCallBackBGrnd pcbBGrnd, Display2D destDisplay, double x, double y, double width, double height,
        		com.cyzapps.VisualMFP.Color color, int drawMode, PaintingExtraInfo pei) {
            this.mpcbBGrnd = pcbBGrnd;
            this.mdestDisplay = destDisplay;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
            this.drawMode = drawMode;
            this.pei = pei;
        }
        @Override
        public boolean call() {
            if (null == mdestDisplay) {
                return false;   // hasnt been initialized
            } else {
                mdestDisplay.drawOval(x, y, width, height, color, drawMode, pei);
                return true;
            }
        }
        
        @Override
        public boolean isInPaintingRange(double left, double top, double right, double bottom) {
            double paintLeft = x;
            double paintRight = x + width;
            double paintTop = y;
            double paintBottom = y + height;
            if (drawMode > 0) {
                paintRight += drawMode;
                paintLeft -= drawMode;
                paintBottom += drawMode;
                paintTop -= drawMode;
            }
            return paintRight > left
                    && paintLeft < right
                    && paintBottom > top
                    && paintTop < bottom;
        }
    }
    
    public static class ClearOvalCallBack extends PaintingCallBack {

        public double x = 0;
        public double y = 0;
        public double width = 0;
        public double height = 0;

        public ClearOvalCallBack(PaintingCallBackBGrnd pcbBGrnd, Display2D destDisplay, double x, double y, double width, double height) {
            this.mpcbBGrnd = pcbBGrnd;
            this.mdestDisplay = destDisplay;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        @Override
        public boolean call() {
            if (null == mdestDisplay) {
                return false;   // hasnt been initialized
            } else {
                mdestDisplay.clearOval(x, y, width, height);
                return true;
            }
        }
        
        @Override
        public boolean isInPaintingRange(double left, double top, double right, double bottom) {
            double paintLeft = x;
            double paintRight = x + width;
            double paintTop = y;
            double paintBottom = y + height;
            return paintRight > left
                    && paintLeft < right
                    && paintBottom > top
                    && paintTop < bottom;
        }
    }
    
    public static class SimpleDrawImageCallBack extends PaintingCallBack {

        public DataClass imgHandle = new DataClassNull();
        public double left = 0;
        public double top = 0;
        public double wRatio = 1.0;
        public double hRatio = 1.0;
        public PaintingExtraInfo pei = null;
        
        public SimpleDrawImageCallBack(PaintingCallBackBGrnd pcbBGrnd, Display2D destDisplay, DataClass imgHandle, double left, double top,
        		double wRatio, double hRatio, PaintingExtraInfo pei) {
            this.mpcbBGrnd = pcbBGrnd;
            this.mdestDisplay = destDisplay;
            this.imgHandle = imgHandle;
            this.left = left;
            this.top = top;
            this.wRatio = wRatio;
            this.hRatio = hRatio;
            this.pei = pei;
        }
        @Override
        public boolean call() {
            if (null == mdestDisplay) {
                return false;   // hasnt been initialized
            } else {
                return mdestDisplay.drawImage(imgHandle, left, top, wRatio, hRatio, pei);
            }
        }
        
        @Override
        public boolean isInPaintingRange(double left, double top, double right, double bottom) {
            // to get the actual size of the image, we need to load it. However, isInPaintingRange
            // should be a light-weighted function so just do a quick judgement.
            return this.left < right && this.top < bottom;
        }
    }

    public static class FullDrawImageCallBack extends PaintingCallBack {

        public DataClass imgHandle = new DataClassNull();
        public double srcX1 = 0;
        public double srcY1 = 0;
        public double srcX2 = 0;
        public double srcY2 = 0;
        public double dstX1 = 0;
        public double dstY1 = 0;
        public double dstX2 = 0;
        public double dstY2 = 0;
        public PaintingExtraInfo pei = null;
        
        public FullDrawImageCallBack(PaintingCallBackBGrnd pcbBGrnd, Display2D destDisplay, DataClass imgHandle, double srcX1, double srcY1, double srcX2, double srcY2,
                double dstX1, double dstY1, double dstX2, double dstY2, PaintingExtraInfo pei) {
            this.mpcbBGrnd = pcbBGrnd;
            this.mdestDisplay = destDisplay;
            this.imgHandle = imgHandle;
            this.srcX1 = srcX1;
            this.srcY1 = srcY1;
            this.srcX2 = srcX2;
            this.srcY2 = srcY2;
            this.dstX1 = dstX1;
            this.dstY1 = dstY1;
            this.dstX2 = dstX2;
            this.dstY2 = dstY2;
            this.pei = pei;
        }
        @Override
        public boolean call() {
            if (null == mdestDisplay) {
                return false;   // hasnt been initialized
            } else {
                return mdestDisplay.drawImage(imgHandle, srcX1, srcY1, srcX2, srcY2, dstX1, dstY1, dstX2, dstY2, pei);
            }
        }
        
        @Override
        public boolean isInPaintingRange(double left, double top, double right, double bottom) {
            return Math.max(dstX1, dstX2) > left
                    && Math.min(dstX1, dstX2) < right
                    && Math.max(dstY1, dstY2) > top
                    && Math.min(dstY1, dstY2) < bottom;
        }
    }
    
}
