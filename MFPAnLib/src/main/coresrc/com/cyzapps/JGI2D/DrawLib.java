// MFP project, DrawLib.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.JGI2D;

import com.cyzapps.JGI2D.PaintingCallBacks.ClearOvalCallBack;
import com.cyzapps.JGI2D.PaintingCallBacks.ClearRectCallBack;
import com.cyzapps.JGI2D.PaintingCallBacks.DrawLineCallBack;
import com.cyzapps.JGI2D.PaintingCallBacks.DrawOvalCallBack;
import com.cyzapps.JGI2D.PaintingCallBacks.DrawPointCallBack;
import com.cyzapps.JGI2D.PaintingCallBacks.DrawPolygonCallBack;
import com.cyzapps.JGI2D.PaintingCallBacks.DrawRectCallBack;
import com.cyzapps.JGI2D.PaintingCallBacks.DrawTextCallBack;
import com.cyzapps.JGI2D.PaintingCallBacks.FullDrawImageCallBack;
import com.cyzapps.JGI2D.PaintingCallBacks.PaintingCallBackBGrnd;
import com.cyzapps.JGI2D.PaintingCallBacks.SimpleDrawImageCallBack;
import com.cyzapps.JGI2D.PaintingCallBacks.UpdatePaintingCallBack;
import java.util.LinkedList;
import java.util.Locale;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.BuiltInFunctionLib;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.DataClassString;
import com.cyzapps.Jfcalc.BuiltInFunctionLib.BaseBuiltInFunction;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jfdatastruct.ArrayBasedDictionary;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import com.cyzapps.VisualMFP.LineStyle;
import com.cyzapps.VisualMFP.LineStyle.LINEPATTERN;
import com.cyzapps.VisualMFP.PointStyle;
import com.cyzapps.VisualMFP.PointStyle.POINTSHAPE;
import com.cyzapps.VisualMFP.TextStyle;

public class DrawLib {	// this is 2D lib.
    
    public static void call2Load(boolean bOutput) {
        if (bOutput) {
            System.out.println("Loading " + DrawLib.class.getName());
        }
    }
    
    public static enum PorterDuffMode {
        CLEAR,
        SRC,
        DST,
        SRC_OVER,
        DST_OVER,
        SRC_IN,
        DST_IN,
        SRC_OUT,
        DST_OUT,
        SRC_ATOP,
        DST_ATOP,
        XOR
    }
    
    public static class PaintingExtraInfo {
    	public static final String PORTERDUFFMODE_KEY = "porterduff_mode";
    	public PorterDuffMode mpdm = PorterDuffMode.SRC_OVER;
    }
    
    public static boolean canBePaintingExtraInfo(DataClass datum) {
    	if (datum.isNull()) {
    		return true;
    	} else {
    		try {
	    		if (null != ArrayBasedDictionary.getArrayBasedDictValue(datum, PaintingExtraInfo.PORTERDUFFMODE_KEY)) {
	    			return true;
		    	}
    		} catch(Exception e) {
    			
    		}
	    	return false;
    	}
    }
    
    // note that this function returns true doesn't mean datum is definitely a valid PaintingExtraInfo
    public static boolean looksLikeValidPaintingExtraInfo(DataClass datum) {
    	if (datum instanceof DataClassArray) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public static PaintingExtraInfo getPaintingExtraInfo(DataClass datum) throws JFCALCExpErrException {
    	if (datum.isNull()) {
    		return null;
    	}
    	PaintingExtraInfo pei = new PaintingExtraInfo();
    	DataClass datumPDM = ArrayBasedDictionary.getArrayBasedDictValue(datum, PaintingExtraInfo.PORTERDUFFMODE_KEY);
    	if (datumPDM != null) {
    		pei.mpdm = getPorterDuffMode(datumPDM);
    	}
    	return pei;
    }
    
    public static double[] getCoordinate(DataClass datumXY) {
        double[] darrayXY = null;
        try {
            DataClassArray datumXYCord = DCHelper.lightCvtOrRetDCArray(datumXY.cloneSelf());
            if (datumXYCord.getDataListSize() != 2) {
                datumXYCord = null;
            } else {
                DataClassSingleNum datumX = DCHelper.lightCvtOrRetDCSingleNum(datumXYCord.getDataList()[0]);
                DataClassSingleNum datumY = DCHelper.lightCvtOrRetDCSingleNum(datumXYCord.getDataList()[1]);
                darrayXY = new double[]{
                    datumX.getDataValue().doubleValue(),
                    datumY.getDataValue().doubleValue()
                };
            }
        } catch (JFCALCExpErrException e) {
            // TODO Auto-generated catch block
            darrayXY = null;
        }
        return darrayXY;
    }

    /**
     * This function assume the list of params are all 2D coordinates and read them from last
     * to the first. If any param in the list doesn't include a valid 2D coordinates (x,y), it
     * returns null.
     * @param listParams : parameter list of MFP function.
     * @return : A list of 2D coordinates (x,y)
     */
    public static LinkedList<double[]> getCoordListDbls(LinkedList<DataClass> listParams) {
        LinkedList<double[]> listReturn = new LinkedList<double[]>();
        while (listParams.size() > 0) {
            DataClass datumCoord = listParams.poll();
            double[] darray = getCoordinate(datumCoord);
            if (darray == null) {
                return null;	// invalid format
            } else {
                listReturn.addFirst(darray);
            }
        }
        return listReturn;
    }

    public static com.cyzapps.VisualMFP.Color getColorOrNull(DataClass datumColor) {
        com.cyzapps.VisualMFP.Color clr = null;	// by default it is null.
        try {
            DataClassArray datumClr = DCHelper.lightCvtOrRetDCArray(datumColor);	// no need to deep copy because we will not change.
            if (datumClr.getDataListSize() == 3 || datumClr.getDataListSize() == 4) {
                // R, G, B, Alpha
                DataClass datumR = datumClr.getDataList()[datumClr.getDataListSize() - 3];
                int r = DCHelper.lightCvtOrRetDCMFPInt(datumR).getDataValue().intValue();
                DataClass datumG = datumClr.getDataList()[datumClr.getDataListSize() - 2];
                int g = DCHelper.lightCvtOrRetDCMFPInt(datumG).getDataValue().intValue();
                DataClass datumB = datumClr.getDataList()[datumClr.getDataListSize() - 1];
                int b = DCHelper.lightCvtOrRetDCMFPInt(datumB).getDataValue().intValue();
                int alpha = 255;
                if (datumClr.getDataListSize() == 4) {
                    DataClass datumAlpha = datumClr.getDataList()[0];
                    alpha = DCHelper.lightCvtOrRetDCMFPInt(datumAlpha).getDataValue().intValue();
                }
                clr = new com.cyzapps.VisualMFP.Color(alpha, r, g, b);
            }
        } catch (JFCALCExpErrException e) {
        }
        return clr;
    }

    
    public static com.cyzapps.VisualMFP.Color getColor(DataClass datumColor) {
        com.cyzapps.VisualMFP.Color clr = getColorOrNull(datumColor);
        if (clr == null) {
            clr = new com.cyzapps.VisualMFP.Color();    // opaque black.
        }
        return clr;
    }

    public static PointStyle getPointStyle(DataClass datumColor, DataClass datumPntSize) {
        PointStyle pointStyle = new PointStyle();
        try {
            com.cyzapps.VisualMFP.Color clr = getColorOrNull(datumColor);
            if (clr == null) {
                pointStyle = null;
            } else {
                pointStyle.mclr = clr;
                if (DCHelper.isSingleDouble(datumPntSize)) {
                    pointStyle.mdSize = DCHelper.lightCvtOrRetDCMFPDec(datumPntSize).getDataValue().doubleValue();
                    if (pointStyle.mdSize < 1.0) {
                        pointStyle.mdSize = 1;
                    }
                } else {
                    DataClassArray datumPnt = DCHelper.lightCvtOrRetDCArray(datumPntSize);	// do not change its content.
                    if (datumPnt.getDataListSize() != 1 && datumPnt.getDataListSize() != 2) {
                        // invalid point info.
                        pointStyle = null;
                    } else {
                        DataClassSingleNum datumSize = DCHelper.lightCvtOrRetDCMFPDec(datumPnt.getDataList()[0]);
                        pointStyle.mdSize = datumSize.getDataValue().doubleValue();
                        if (pointStyle.mdSize < 1.0) {
                            pointStyle.mdSize = 1;
                        }
                        if (datumPnt.getDataListSize() == 2) {
                            DataClassString datumPntPattern = DCHelper.lightCvtOrRetDCString(datumPnt.getDataList()[1]);
                            String strLnPattern = datumPntPattern.getStringValue();
                            if (strLnPattern.equalsIgnoreCase("Dot")) {
                                pointStyle.menumPointShape = POINTSHAPE.POINTSHAPE_DOT;
                            } else if (strLnPattern.equalsIgnoreCase("Circle")) {
                                pointStyle.menumPointShape = POINTSHAPE.POINTSHAPE_CIRCLE;
                            } else if (strLnPattern.equalsIgnoreCase("Square")) {
                                pointStyle.menumPointShape = POINTSHAPE.POINTSHAPE_SQUARE;
                            } else if (strLnPattern.equalsIgnoreCase("Diamond")) {
                                pointStyle.menumPointShape = POINTSHAPE.POINTSHAPE_DIAMOND;
                            } else if (strLnPattern.equalsIgnoreCase("Up_triangle")) {
                                pointStyle.menumPointShape = POINTSHAPE.POINTSHAPE_UPTRIANGLE;
                            } else if (strLnPattern.equalsIgnoreCase("Down_triangle")) {
                                pointStyle.menumPointShape = POINTSHAPE.POINTSHAPE_DOWNTRIANGLE;
                            } else if (strLnPattern.equalsIgnoreCase("Cross")) {
                                pointStyle.menumPointShape = POINTSHAPE.POINTSHAPE_CROSS;
                            } else if (strLnPattern.equalsIgnoreCase("X")) {
                                pointStyle.menumPointShape = POINTSHAPE.POINTSHAPE_X;
                            } else {
                                pointStyle.menumPointShape = POINTSHAPE.POINTSHAPE_OTHERS;
                            }
                        }
                    }
                }
            }
        } catch (JFCALCExpErrException e) {
            pointStyle = null;
        }
        return pointStyle;
    }

    /**
     * This function reads from the tail of parameter list of an MFP function (i.e. reading
     * listParams from the beginning), and returns a point style. It may consume 2, or 1 or 0
     * params from listParams. If it consume 2 params, the last param (i.e. the first element
     * in listParams) is extra information of point style, the second last param is color; if
     * consuming 1 parameter, the parameter is color, if no valid point information is found,
     * it returns null and doesn't consume any param from listParam.
     * @param listParams : a parameter list for an MFP function
     * @return : a valid point style or null.
     */
    public static PointStyle getPointStyle(LinkedList<DataClass> listParams) {
        int idx = 0;
        for (; idx < listParams.size(); idx ++) {
            if (!DCHelper.isDataClassType(listParams.get(idx), DATATYPES.DATUM_REF_DATA)) {
                continue;
            }
            try {
                // call lightCvtOrRetDCArray will not change its content, so no deep copy.
                DataClassArray datumArray = DCHelper.lightCvtOrRetDCArray(listParams.get(idx));
                if (datumArray.getDataListSize() == 3 || datumArray.getDataListSize() == 4) {
                    if (null != getColorOrNull(listParams.get(idx))) {
                        break;	// this is a valid color.
                    }
                }
            } catch (JFCALCExpErrException e) {
                // will not be here.
            }
        }
        PointStyle style = new PointStyle();
        switch (idx) {
            case 0:
                com.cyzapps.VisualMFP.Color clr = getColorOrNull(listParams.poll());
                if (clr == null) {
                    style = null;
                } else {
                    style.mclr = clr;
                }   break;
            case 1:
                DataClass datumExtra = listParams.poll();
                DataClass datumColor = listParams.poll();
                style = getPointStyle(datumColor, datumExtra);
                break;
            default:
                style = null;	// always assume lineStyle is the last parameter(s) in listParams
                break;
        }
        return style;
    }

    public static LineStyle getLineStyle(DataClass datumColor, DataClass datumStroke) {
        LineStyle lineStyle = new LineStyle();
        try {
            com.cyzapps.VisualMFP.Color clr = getColorOrNull(datumColor);
            if (clr == null) {
                lineStyle = null;
            } else {
                lineStyle.mclr = clr;

                if (DCHelper.isSingleDouble(datumStroke)) {
                    lineStyle.mdLineWidth = DCHelper.lightCvtOrRetDCMFPDec(datumStroke).getDataValue().doubleValue();
                    if (lineStyle.mdLineWidth <= 1.0) {
                        lineStyle.mdLineWidth = 1;  // if line width < 0, it will fail following painting events in JAVA.
                    }
                } else {
                    // no need to deep copy because no content will change.
                    DataClassArray datumStrk = DCHelper.lightCvtOrRetDCArray(datumStroke);
                    if (datumStrk.getDataListSize() != 1 && datumStrk.getDataListSize() != 2) {
                        // invalid stroke info.
                        lineStyle = null;
                    } else {
                        DataClassSingleNum datumLnWidth = DCHelper.lightCvtOrRetDCMFPDec(datumStrk.getDataList()[0]);
                        lineStyle.mdLineWidth = datumLnWidth.getDataValue().doubleValue();
                        if (lineStyle.mdLineWidth <= 1.0) {
                            lineStyle.mdLineWidth = 1;  // if line width < 0, it will fail following painting events in JAVA.
                        }
                        if (datumStrk.getDataListSize() == 2) {
                            DataClassString datumLnPattern = DCHelper.lightCvtOrRetDCString(datumStrk.getDataList()[1]);
                            String strLnPattern = datumLnPattern.getStringValue();
                            if (strLnPattern.equalsIgnoreCase("Solid")) {
                                lineStyle.menumLinePattern = LINEPATTERN.LINEPATTERN_SOLID;
                            } else if (strLnPattern.equalsIgnoreCase("Dash")) {
                                lineStyle.menumLinePattern = LINEPATTERN.LINEPATTERN_DASH;
                            } else if (strLnPattern.equalsIgnoreCase("Dot")) {
                                lineStyle.menumLinePattern = LINEPATTERN.LINEPATTERN_DOT;
                            } else if (strLnPattern.equalsIgnoreCase("Dash_dot")) {
                                lineStyle.menumLinePattern = LINEPATTERN.LINEPATTERN_DASH_DOT;
                            } else {
                                lineStyle.menumLinePattern = LINEPATTERN.LINEPATTERN_OTHERS;
                            }
                        }
                    }
                }
            }
        } catch (JFCALCExpErrException e) {
            // TODO Auto-generated catch block
            lineStyle = null;
        }
        return lineStyle;
    }

    /**
     * This function reads from the tail of parameter list of an MFP function (i.e. reading
     * listParams from the beginning), and returns a line style. It may consume 2, or 1 or 0
     * params from listParams. If it consume 2 params, the last param (i.e. the first element
     * in listParams) is extra information of line style, the second last param is color; if
     * consuming 1 parameter, the parameter is color, if no valid line information is found,
     * it returns null and doesn't consume any param from listParam.
     * @param listParams : a parameter list for an MFP function
     * @return : a valid line style or null.
     */
    public static LineStyle getLineStyle(LinkedList<DataClass> listParams) {
        int idx = 0;
        for (; idx < listParams.size(); idx ++) {
            if (!DCHelper.isDataClassType(listParams.get(idx), DATATYPES.DATUM_REF_DATA)) {
                continue;
            }
            try {
                // call lightCvtOrRetDCArray will not change its content, so no deep copy.
                DataClassArray datumArray = DCHelper.lightCvtOrRetDCArray(listParams.get(idx));
                if (datumArray.getDataListSize() == 3 || datumArray.getDataListSize() == 4) {
                    if (null != getColorOrNull(listParams.get(idx))) {
                        break;	// this is a valid color.
                    }
                }
            } catch (JFCALCExpErrException e) {
                // will not be here.
            }
        }
        LineStyle style = new LineStyle();
        switch (idx) {
            case 0:
                com.cyzapps.VisualMFP.Color clr = getColorOrNull(listParams.poll());
                if (clr == null) {
                    style = null;
                } else {
                    style.mclr = clr;
                }   break;
            case 1:
                DataClass datumExtra = listParams.poll();
                DataClass datumColor = listParams.poll();
                style = getLineStyle(datumColor, datumExtra);
                break;
            default:
                style = null;	// always assume lineStyle is the last parameter(s) in listParams
                break;
        }
        return style;
    }

    public static TextStyle getTextStyle(DataClass datumColor, DataClass datumTextInfo) {
        TextStyle textStyle = new TextStyle();
        try {
            com.cyzapps.VisualMFP.Color clr = getColorOrNull(datumColor);
            if (clr == null) {
                textStyle = null;
            } else {
                textStyle.mclr = clr;

                if (DCHelper.isSingleDouble(datumTextInfo)) {
                    textStyle.mdSize = DCHelper.lightCvtOrRetDCMFPDec(datumTextInfo).getDataValue().doubleValue();
                    if (textStyle.mdSize < 1.0) {
                        textStyle.mdSize = 1;
                    }
                } else {
                    DataClassArray datumTxtInfo = DCHelper.lightCvtOrRetDCArray(datumTextInfo);
                    if (datumTxtInfo.getDataListSize() != 1 && datumTxtInfo.getDataListSize() != 2 && datumTxtInfo.getDataListSize() != 3) {
                        // invalid stroke info.
                        textStyle = null;
                    } else {
                        DataClassSingleNum datumSize = DCHelper.lightCvtOrRetDCMFPDec(datumTxtInfo.getDataList()[0]);
                        textStyle.mdSize = datumSize.getDataValue().doubleValue();
                        if (textStyle.mdSize < 1.0) {
                            textStyle.mdSize = 1;
                        }
                        if (datumTxtInfo.getDataListSize() >= 2) {
                            DataClassString datumFont = DCHelper.lightCvtOrRetDCString(datumTxtInfo.getDataList()[1]);
                            textStyle.mstrFont = datumFont.getStringValue();
                        }
                        if (datumTxtInfo.getDataListSize() >= 3) {
                            DataClassSingleNum datumStyle = DCHelper.lightCvtOrRetDCMFPInt(datumTxtInfo.getDataList()[2]);
                            textStyle.mnStyle = datumStyle.getDataValue().intValue();
                        }
                    }
                }
            }
        } catch (JFCALCExpErrException e) {
            // TODO Auto-generated catch block
            textStyle = null;
        }
        return textStyle;
    }


    /**
     * This function reads from the tail of parameter list of an MFP function (i.e. reading
     * listParams from the beginning), and returns a text style. It may consume 2, or 1 or 0
     * params from listParams. If it consume 2 params, the last param (i.e. the first element
     * in listParams) is extra information of text style, the second last param is color; if
     * consuming 1 parameter, the parameter is color, if no valid text information is found,
     * it returns null and doesn't consume any param from listParam.
     * @param listParams : a parameter list for an MFP function
     * @return : a valid text style or null.
     */
    public static TextStyle getTextStyle(LinkedList<DataClass> listParams) {
        int idx = 0;
        for (; idx < listParams.size(); idx ++) {
            if (!DCHelper.isDataClassType(listParams.get(idx), DATATYPES.DATUM_REF_DATA)) {
                continue;
            }
            try {
                // call lightCvtOrRetDCArray will not change its content, so no deep copy.
                DataClassArray datumArray = DCHelper.lightCvtOrRetDCArray(listParams.get(idx));
                if (datumArray.getDataListSize() == 3 || datumArray.getDataListSize() == 4) {
                    if (null != getColorOrNull(listParams.get(idx))) {
                        break;	// this is a valid color.
                    }
                }
            } catch (JFCALCExpErrException e) {
                // will not be here.
            }
        }
        TextStyle style = new TextStyle();
        switch (idx) {
            case 0:
                com.cyzapps.VisualMFP.Color clr = getColorOrNull(listParams.poll());
                if (clr == null) {
                    style = null;
                } else {
                    style.mclr = clr;
                }   break;
            case 1:
                DataClass datumExtra = listParams.poll();
                DataClass datumColor = listParams.poll();
                style = getTextStyle(datumColor, datumExtra);
                break;
            default:
                style = null;	// always assume lineStyle is the last parameter(s) in listParams
                break;
        }
        return style;
    }

    public static class Set_porterduff_modeFunction extends BaseBuiltInFunction {

        public Set_porterduff_modeFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::draw::set_porterduff_mode";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2; // painting extra, porterduff mode.
            mnMinParamNum = 2; // painting extra, porterduff mode.
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClassArray datumReturn;
            DataClass datumPaintingExtra = listParams.pollLast();
            if (datumPaintingExtra.isNull()) {
            	datumReturn = ArrayBasedDictionary.createArrayBasedDict();
            } else {
            	datumReturn = DCHelper.lightCvtOrRetDCArray(datumPaintingExtra);
            }
            datumReturn = ArrayBasedDictionary.setArrayBasedDictValue(datumReturn, PaintingExtraInfo.PORTERDUFFMODE_KEY, listParams.pollLast());
            
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Set_porterduff_modeFunction());}

    public static class Get_porterduff_modeFunction extends BaseBuiltInFunction {

        public Get_porterduff_modeFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::draw::get_porterduff_mode";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1; // painting extra.
            mnMinParamNum = 1; // painting extra.
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClass datumPaintingExtra = listParams.pollLast();
            DataClass datumReturn = ArrayBasedDictionary.getArrayBasedDictValue(datumPaintingExtra, PaintingExtraInfo.PORTERDUFFMODE_KEY);
            if (datumReturn == null) {
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_KEY_NOT_EXIST);
            }
            
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_porterduff_modeFunction());}

    public static PorterDuffMode getDefaultPorterDuffMode() {
    	return PorterDuffMode.SRC_OVER;
    }
    public static PorterDuffMode getPorterDuffMode(DataClass datumVal) throws JFCALCExpErrException {
    	String strMode = DCHelper.lightCvtOrRetDCString(datumVal).getStringValue();
    	if (strMode == null) {
    		throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
    	} else {
    		strMode = strMode.trim().toUpperCase(Locale.US);
    	}
    	PorterDuffMode pdm = getDefaultPorterDuffMode();
    	switch (strMode) {
    	case "CLEAR":
    		pdm = PorterDuffMode.CLEAR;
    		break;
    	case "SRC":
    		pdm = PorterDuffMode.SRC;
    		break;
    	case "DST":
    		pdm = PorterDuffMode.DST;
    		break;
    	case "SRC_OVER":
    		pdm = PorterDuffMode.SRC_OVER;
    		break;
    	case "DST_OVER":
    		pdm = PorterDuffMode.DST_OVER;
    		break;
    	case "SRC_IN":
    		pdm = PorterDuffMode.SRC_IN;
    		break;
    	case "DST_IN":
    		pdm = PorterDuffMode.DST_IN;
    		break;
    	case "SRC_OUT":
    		pdm = PorterDuffMode.SRC_OUT;
    		break;
    	case "DST_OUT":
    		pdm = PorterDuffMode.DST_OUT;
    		break;
    	case "SRC_ATOP":
    		pdm = PorterDuffMode.SRC_ATOP;
    		break;
    	case "DST_ATOP":
    		pdm = PorterDuffMode.DST_ATOP;
    		break;
    	case "XOR":
    		pdm = PorterDuffMode.XOR;
    		break;
    	case "":
    		pdm = getDefaultPorterDuffMode(); // empty string means set default value
    		break;
    	default:
    		throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
    	}
    	return pdm;
    }
    
    public static class Draw_pointFunction extends BaseBuiltInFunction {

        public Draw_pointFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::draw::draw_point";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 6; // dont forget owner info. Without owner ifno, max param number is 5.
            mnMinParamNum = 4; // dont forget owner info. Without owner ifno, min param number is 3.
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            PaintingExtraInfo pei = null;
            if (listParams.size() == mnMaxParamNum) {
            	pei = getPaintingExtraInfo(listParams.poll());
            }
            
            DataClass datumOwnerInfo = listParams.pollLast();
            PaintingCallBackBGrnd pcbBGrnd = PaintingCallBackBGrnd.getPaintingCallBackBGrnd(datumOwnerInfo);

            Display2D display = DisplayLib.get2DDisplay(listParams.pollLast(), false);
            
            PointStyle pointStyle = null;
            try {
                pointStyle = getPointStyle(listParams);
            } catch (Exception e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            if (null == pointStyle) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            LinkedList<double[]> listXY = getCoordListDbls(listParams);
            if (null == listXY || listXY.size() != 1) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            double dX = listXY.getFirst()[0], dY = listXY.getFirst()[1];
            // add call back event and call back when repaint event is called.
            display.addPaintingCallBack(new DrawPointCallBack(pcbBGrnd, display, dX, dY, pointStyle, pei));
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Draw_pointFunction());}

    public static class Draw_lineFunction extends BaseBuiltInFunction {

        public Draw_lineFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::draw::draw_line";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 7;
            mnMinParamNum = 5;
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            PaintingExtraInfo pei = null;
            if (listParams.size() == mnMaxParamNum) {
            	pei = getPaintingExtraInfo(listParams.poll());
            }
            
            DataClass datumOwnerInfo = listParams.pollLast();
            PaintingCallBackBGrnd pcbBGrnd = PaintingCallBackBGrnd.getPaintingCallBackBGrnd(datumOwnerInfo);

            Display2D display = DisplayLib.get2DDisplay(listParams.pollLast(), false);
            
            LineStyle lineStyle = null;
            try {
                lineStyle = getLineStyle(listParams);
            } catch(Exception e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            if (null == lineStyle) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            LinkedList<double[]> listXY = getCoordListDbls(listParams);
            if (null == listXY || listXY.size() != 2) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            double dX1 = listXY.getFirst()[0], dY1 = listXY.getFirst()[1], dX2 = listXY.getLast()[0], dY2 = listXY.getLast()[1];
            // add call back event and call back when repaint event is called.
            display.addPaintingCallBack(new DrawLineCallBack(pcbBGrnd, display, dX1, dY1, dX2, dY2, lineStyle, pei));
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Draw_lineFunction());}

    public static class Draw_polygonFunction extends BaseBuiltInFunction {

        public Draw_polygonFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::draw::draw_polygon";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = -1;
            mnMinParamNum = 7;	// at least callerbackgnd + flatGDI + 3 points + color + frame or fill (if frame, line is always solid and min width).
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            PaintingExtraInfo pei = null;
            if (canBePaintingExtraInfo(listParams.getFirst())) {
            	pei = getPaintingExtraInfo(listParams.poll());
            }
            
            DataClass datumOwnerInfo = listParams.pollLast();
            PaintingCallBackBGrnd pcbBGrnd = PaintingCallBackBGrnd.getPaintingCallBackBGrnd(datumOwnerInfo);
            
            Display2D display = DisplayLib.get2DDisplay(listParams.pollLast(), false);

            // draw mode is <= 0 means fill, >= 1 means frame, and frame width is nDrawMode
            Integer nDrawMode = BuiltInFunctionLib.getInteger(listParams.poll());
            if (null == nDrawMode) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            com.cyzapps.VisualMFP.Color color = getColor(listParams.poll());
            
            LinkedList<double[]> listXY = getCoordListDbls(listParams);
            if (null == listXY || listXY.size() < 3) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            
            // add call back event and call back when repaint event is called.
            display.addPaintingCallBack(new DrawPolygonCallBack(pcbBGrnd, display, listXY, color, nDrawMode, pei));
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Draw_polygonFunction());}

    public static class Draw_rectFunction extends BaseBuiltInFunction {

        public Draw_rectFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::draw::draw_rect";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 8;	// ownerbackground gdi (x y) width height color frame_or_fill pei
            mnMinParamNum = 7;	// ownerbackground gdi (x y) width height color frame_or_fill
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            PaintingExtraInfo pei = null;
            if (listParams.size() == mnMaxParamNum) {
            	pei = getPaintingExtraInfo(listParams.poll());
            }
            
            DataClass datumOwnerInfo = listParams.pollLast();
            PaintingCallBackBGrnd pcbBGrnd = PaintingCallBackBGrnd.getPaintingCallBackBGrnd(datumOwnerInfo);

            Display2D display = DisplayLib.get2DDisplay(listParams.pollLast(), false);

            // draw mode is <= 0 means fill, >= 1 means frame, and frame width is nDrawMode
            Integer nDrawMode = BuiltInFunctionLib.getInteger(listParams.poll());
            if (null == nDrawMode) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            com.cyzapps.VisualMFP.Color color = getColor(listParams.poll());

            Double dHeight = BuiltInFunctionLib.getDouble(listParams.poll());
            if (null == dHeight) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            Double dWidth = BuiltInFunctionLib.getDouble(listParams.poll());
            if (null == dWidth) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            LinkedList<double[]> listXY = getCoordListDbls(listParams);
            if (null == listXY || listXY.size() != 1) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            double dX = listXY.getFirst()[0], dY = listXY.getFirst()[1];
            
            // add call back event and call back when repaint event is called.
            display.addPaintingCallBack(new DrawRectCallBack(pcbBGrnd, display, dX, dY, dWidth, dHeight, color, nDrawMode, pei));
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Draw_rectFunction());}

    public static class Clear_rectFunction extends BaseBuiltInFunction {

        public Clear_rectFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::draw::clear_rect";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 5;	// ownerbackground gdi (x y) width height
            mnMinParamNum = 5;	// ownerbackground gdi (x y) width height
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClass datumOwnerInfo = listParams.pollLast();
            PaintingCallBackBGrnd pcbBGrnd = PaintingCallBackBGrnd.getPaintingCallBackBGrnd(datumOwnerInfo);

            Display2D display = DisplayLib.get2DDisplay(listParams.pollLast(), false);

            Double dHeight = BuiltInFunctionLib.getDouble(listParams.poll());
            if (null == dHeight) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            Double dWidth = BuiltInFunctionLib.getDouble(listParams.poll());
            if (null == dWidth) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            LinkedList<double[]> listXY = getCoordListDbls(listParams);
            if (null == listXY || listXY.size() != 1) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            double dX = listXY.getFirst()[0], dY = listXY.getFirst()[1];
            
            // add call back event and call back when repaint event is called.
            display.addPaintingCallBack(new ClearRectCallBack(pcbBGrnd, display, dX, dY, dWidth, dHeight));
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Clear_rectFunction());}

    public static class Draw_ovalFunction extends BaseBuiltInFunction {

        public Draw_ovalFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::draw::draw_oval";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 8;	// ownerbackground gdi (x, y) x-radius y-radius color frame_or_fill pei
            mnMinParamNum = 7;	// ownerbackground gdi (x,y) x-radius y-radius color frame_or_fill
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            PaintingExtraInfo pei = null;
            if (listParams.size() == mnMaxParamNum) {
            	pei = getPaintingExtraInfo(listParams.poll());
            }
            
            DataClass datumOwnerInfo = listParams.pollLast();
            PaintingCallBackBGrnd pcbBGrnd = PaintingCallBackBGrnd.getPaintingCallBackBGrnd(datumOwnerInfo);

            Display2D display = DisplayLib.get2DDisplay(listParams.pollLast(), false);

            // draw mode is <= 0 means fill, >= 1 means frame, and frame width is nDrawMode
            Integer nDrawMode = BuiltInFunctionLib.getInteger(listParams.poll());
            if (null == nDrawMode) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            com.cyzapps.VisualMFP.Color color = getColor(listParams.poll());

            Double dYRadius = BuiltInFunctionLib.getDouble(listParams.poll());
            if (null == dYRadius) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            Double dXRadius = BuiltInFunctionLib.getDouble(listParams.poll());
            if (null == dXRadius) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            LinkedList<double[]> listXY = getCoordListDbls(listParams);
            if (null == listXY || listXY.size() != 1) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            double dX = listXY.getFirst()[0], dY = listXY.getFirst()[1];
            
            // add call back event and call back when repaint event is called.
            display.addPaintingCallBack(new DrawOvalCallBack(pcbBGrnd, display, dX, dY, dXRadius, dYRadius, color, nDrawMode, pei));
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Draw_ovalFunction());}

    public static class Clear_ovalFunction extends BaseBuiltInFunction {

        public Clear_ovalFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::draw::clear_oval";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 5;	// ownerbackground gdi (x y) width height
            mnMinParamNum = 5;	// ownerbackground gdi (x y) width height
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClass datumOwnerInfo = listParams.pollLast();
            PaintingCallBackBGrnd pcbBGrnd = PaintingCallBackBGrnd.getPaintingCallBackBGrnd(datumOwnerInfo);

            Display2D display = DisplayLib.get2DDisplay(listParams.pollLast(), false);

            Double dHeight = BuiltInFunctionLib.getDouble(listParams.poll());
            if (null == dHeight) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            Double dWidth = BuiltInFunctionLib.getDouble(listParams.poll());
            if (null == dWidth) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            LinkedList<double[]> listXY = getCoordListDbls(listParams);
            if (null == listXY || listXY.size() != 1) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            double dX = listXY.getFirst()[0], dY = listXY.getFirst()[1];
            
            // add call back event and call back when repaint event is called.
            display.addPaintingCallBack(new ClearOvalCallBack(pcbBGrnd, display, dX, dY, dWidth, dHeight));
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Clear_ovalFunction());}

    public static class Draw_imageFunction extends BaseBuiltInFunction {

        public Draw_imageFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::draw::draw_image";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 12;	// gdi path of image, srcx1, srcy1, srcx2, srcy2, destx1, desty1, destx2, desty2, pei
            // there is a middle mode gid path of image, top, left, width ratio, height ratio (neglible)
            mnMinParamNum = 5;	// gdi path of image, top, left
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            boolean bIsFullMode = false;
            if (listParams.size() == mnMaxParamNum || listParams.size() == mnMaxParamNum - 1) {
                bIsFullMode = true;
            } else if (listParams.size() >= mnMinParamNum && listParams.size() <= mnMinParamNum + 3) {
                bIsFullMode = false;
            } else { // if (listParams.size() != mnMaxParamNum || listParams.size() != mnMinParamNum)
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            PaintingExtraInfo pei = null;
            if (listParams.size() == mnMaxParamNum || listParams.size() == mnMinParamNum + 3) {
            	pei = getPaintingExtraInfo(listParams.poll());
            } else if ((listParams.size() == mnMinParamNum + 1 || listParams.size() == mnMinParamNum + 2)
            		&& looksLikeValidPaintingExtraInfo(listParams.getFirst()))	{
            	try {
            		pei = getPaintingExtraInfo(listParams.poll());
            	} catch (Exception e) {
            		// do nothing, which means pei is null.
            	}
            }
            
            DataClass datumOwnerInfo = listParams.pollLast();
            PaintingCallBackBGrnd pcbBGrnd = PaintingCallBackBGrnd.getPaintingCallBackBGrnd(datumOwnerInfo);

            Display2D display = DisplayLib.get2DDisplay(listParams.pollLast(), false);
            // do not validate image handler or path because draw image event will be called every 30ms.
            // we cannot output exception every 30ms.
            DataClass datumImageHandleOrPath = listParams.pollLast();
            if (bIsFullMode) {
                DataClassSingleNum datumSrcX1 = DCHelper.lightCvtOrRetDCSingleNum(listParams.pollLast());
                DataClassSingleNum datumSrcY1 = DCHelper.lightCvtOrRetDCSingleNum(listParams.pollLast());
                DataClassSingleNum datumSrcX2 = DCHelper.lightCvtOrRetDCSingleNum(listParams.pollLast());
                DataClassSingleNum datumSrcY2 = DCHelper.lightCvtOrRetDCSingleNum(listParams.pollLast());
                DataClassSingleNum datumDestX1 = DCHelper.lightCvtOrRetDCSingleNum(listParams.pollLast());
                DataClassSingleNum datumDestY1 = DCHelper.lightCvtOrRetDCSingleNum(listParams.pollLast());
                DataClassSingleNum datumDestX2 = DCHelper.lightCvtOrRetDCSingleNum(listParams.pollLast());
                DataClassSingleNum datumDestY2 = DCHelper.lightCvtOrRetDCSingleNum(listParams.pollLast());
                // add call back event and call back when repaint event is called.
                display.addPaintingCallBack(new FullDrawImageCallBack(
                        pcbBGrnd, display, datumImageHandleOrPath,
                        datumSrcX1.getDataValue().doubleValue(),
                        datumSrcY1.getDataValue().doubleValue(),
                        datumSrcX2.getDataValue().doubleValue(),
                        datumSrcY2.getDataValue().doubleValue(),
                        datumDestX1.getDataValue().doubleValue(),
                        datumDestY1.getDataValue().doubleValue(),
                        datumDestX2.getDataValue().doubleValue(),
                        datumDestY2.getDataValue().doubleValue(),
                        pei));
            } else {
                DataClassSingleNum datumTop = DCHelper.lightCvtOrRetDCSingleNum(listParams.pollLast());
                DataClassSingleNum datumLeft = DCHelper.lightCvtOrRetDCSingleNum(listParams.pollLast());
                double dWidthRatio = 1.0, dHeightRatio = 1.0;
                if (listParams.size() > 0) {
                	dWidthRatio = dHeightRatio = DCHelper.lightCvtOrRetDCMFPDec(listParams.pollLast()).getDataValue().doubleValue();
                }
                if (listParams.size() > 0) {
                	dHeightRatio = DCHelper.lightCvtOrRetDCMFPDec(listParams.pollLast()).getDataValue().doubleValue();
                }
                if (dWidthRatio <= 0 || dHeightRatio <= 0) {
                	throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
                // add call back event and call back when repaint event is called.
                display.addPaintingCallBack(new SimpleDrawImageCallBack(
                        pcbBGrnd, display, datumImageHandleOrPath,
                        datumTop.getDataValue().doubleValue(),
                        datumLeft.getDataValue().doubleValue(),
                        dWidthRatio, dHeightRatio,
                        pei));
            }
            
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Draw_imageFunction());}

    public static class Calculate_text_originFunction extends BaseBuiltInFunction {

        public Calculate_text_originFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::draw::calculate_text_origin";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 8;	// display, string, (x,y), w, h, horAlign, verAlign, [size, font]
            mnMinParamNum = 7;	// display, string, (x,y), w, h, horAlign, verAlign
        }
        
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            TextStyle textStyle = null;
            if (listParams.size() == mnMaxParamNum) {
                try {
                    DataClass datumColor = new DataClassArray(new DataClass[] {
                        new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO),
                        new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO),
                        new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO)
                    });
                    textStyle = getTextStyle(datumColor, listParams.poll());
                } catch (Exception e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
            }
            if (null == textStyle) {
                textStyle = new TextStyle();	// color is optional.
            }

            Display2D display = DisplayLib.get2DDisplay(listParams.pollLast(), false);
            
            DataClassString datumStr = DCHelper.lightCvtOrRetDCString(listParams.pollLast());
            String strText = datumStr.getStringValue();

            int verAlign = DCHelper.lightCvtOrRetDCMFPInt(listParams.poll()).getDataValue().intValue();
            int horAlign = DCHelper.lightCvtOrRetDCMFPInt(listParams.poll()).getDataValue().intValue();
            int w = DCHelper.lightCvtOrRetDCMFPInt(listParams.poll()).getDataValue().intValue();
            int h = DCHelper.lightCvtOrRetDCMFPInt(listParams.poll()).getDataValue().intValue();
            LinkedList<double[]> listXY = getCoordListDbls(listParams);
            if (null == listXY || listXY.size() != 1) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            double dX = listXY.getFirst()[0], dY = listXY.getFirst()[1];
            int[] origin = display.calcTextOrigin(strText, (int)dX, (int)dY, h, w, horAlign, verAlign, textStyle);
            if (origin == null || origin.length != 2) {
            	// this implies font is wrong.
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);
            }
            DataClass[] dataOrigin = new DataClass[origin.length];
            for (int idx = 0; idx < origin.length; idx ++) {
            	dataOrigin[idx] = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(origin[idx]));
            }
            DataClassArray datumReturn = new DataClassArray(dataOrigin);
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Calculate_text_originFunction());}

    public static class Calculate_text_boundaryFunction extends BaseBuiltInFunction {

        public Calculate_text_boundaryFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::draw::calculate_text_boundary";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 4;	// display, string, (x,y), [size, font]
            mnMinParamNum = 3;	// display, string, (x,y)
        }
        
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            TextStyle textStyle = null;
            if (listParams.size() == mnMaxParamNum) {
                try {
                    DataClass datumColor = new DataClassArray(new DataClass[] {
                        new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO),
                        new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO),
                        new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ZERO)
                    });
                    textStyle = getTextStyle(datumColor, listParams.poll());
                } catch (Exception e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
            }
            if (null == textStyle) {
                textStyle = new TextStyle();	// color is optional.
            }

            Display2D display = DisplayLib.get2DDisplay(listParams.pollLast(), false);
            
            DataClassString datumStr = DCHelper.lightCvtOrRetDCString(listParams.pollLast());
            String strText = datumStr.getStringValue();

            LinkedList<double[]> listXY = getCoordListDbls(listParams);
            if (null == listXY || listXY.size() != 1) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            double dX = listXY.getFirst()[0], dY = listXY.getFirst()[1];
            int[] rect = display.calcTextBoundary(strText, (int)dX, (int)dY, textStyle);
            if (rect == null || rect.length != 4) {
            	// this implies font is wrong.
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);
            }
            DataClass[] dataRect = new DataClass[rect.length];
            for (int idx = 0; idx < rect.length; idx ++) {
            	dataRect[idx] = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(rect[idx]));
            }
            DataClassArray datumReturn = new DataClassArray(dataRect);
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Calculate_text_boundaryFunction());}
    
    public static class Draw_textFunction extends BaseBuiltInFunction {

        public Draw_textFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::draw::draw_text";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 7;	// owner info, display, string, (x,y), color, [size, font], pei
            mnMinParamNum = 5;	// owner info, display, string, (x,y), color
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            PaintingExtraInfo pei = null;
            if (listParams.size() == mnMaxParamNum) {
            	pei = getPaintingExtraInfo(listParams.poll());
            }
            
            DataClass datumOwnerInfo = listParams.pollLast();
            PaintingCallBackBGrnd pcbBGrnd = PaintingCallBackBGrnd.getPaintingCallBackBGrnd(datumOwnerInfo);

            Display2D display = DisplayLib.get2DDisplay(listParams.pollLast(), false);
            
            DataClassString datumStr = DCHelper.lightCvtOrRetDCString(listParams.pollLast());
            String strText = datumStr.getStringValue();

            TextStyle textStyle = null;
            try {
                textStyle = getTextStyle(listParams);
            } catch (Exception e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            if (null == textStyle) {
                textStyle = new TextStyle();	// color is optional.
            }

            LinkedList<double[]> listXY = getCoordListDbls(listParams);
            if (null == listXY || listXY.size() != 1) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            double dX = listXY.getFirst()[0], dY = listXY.getFirst()[1];
            display.addPaintingCallBack(new DrawTextCallBack(pcbBGrnd, display, strText, dX, dY, textStyle, 0, pei));
            
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Draw_textFunction());}

    public static class Drop_old_painting_requestsFunction extends BaseBuiltInFunction {

        public Drop_old_painting_requestsFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::draw::drop_old_painting_requests";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;	// painting_background_info GDI
            mnMinParamNum = 2;	// painting_background_info GDI
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClass datumOwnerInfo = listParams.pollLast();
            PaintingCallBackBGrnd pcbBGrnd = PaintingCallBackBGrnd.getPaintingCallBackBGrnd(datumOwnerInfo);

            Display2D display = DisplayLib.get2DDisplay(listParams.pollLast(), false);
            
            display.addPaintingCallBack(new UpdatePaintingCallBack(pcbBGrnd, display));
            
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Drop_old_painting_requestsFunction());}
}
