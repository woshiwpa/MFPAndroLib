// MFP project, PlotLib.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jfcalc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.util.LinkedList;
import java.util.Locale;

import com.cyzapps.Jfcalc.DCHelper.CurPos;
import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.Operators.OPERATORTYPES;
import com.cyzapps.Jfcalc.BuiltInFunctionLib.BaseBuiltInFunction;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.FuncEvaluator.FunctionInterrupter;
import com.cyzapps.Jfcalc.TwoDExprDataCache.CacheNode;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.ScriptAnalyzer.ScriptInterrupter;
import com.cyzapps.Jmfp.SolveAnalyzer;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AEAssign;
import com.cyzapps.Jsma.AECompare;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AEVar;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.AbstractExpr.ABSTRACTEXPRTYPES;
import com.cyzapps.Jsma.ExprAnalyzer;
import com.cyzapps.Jsma.PtnSlvMultiVarsIdentifier;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import com.cyzapps.adapter.ChartOperator;
import com.cyzapps.VisualMFP.DataSeriesCurve;
import com.cyzapps.VisualMFP.DataSeriesGridSurface;
import com.cyzapps.VisualMFP.LineStyle;
import com.cyzapps.VisualMFP.PointStyle;
import com.cyzapps.VisualMFP.Position3D;
import com.cyzapps.VisualMFP.SurfaceStyle;
import com.cyzapps.VisualMFP.LineStyle.LINEPATTERN;
import com.cyzapps.VisualMFP.SurfaceStyle.SURFACETYPE;
import com.cyzapps.adapter.MFPAdapter;
import com.cyzapps.adapter.OGLExprChartOperator;
import com.cyzapps.adapter.XYChartOperator;

public class PlotLib {

    public static final int MAX_NUMBER_OF_EXPRS_TO_PLOT = 1024;    // was 4;
    public static final int MAX_NUMBER_OF_MULTIXY_DATA_SERIES_TO_PLOT = 1024;
    public static final int MAX_NUMBER_OF_2D_CURVES_TO_PLOT = 1024;
    public static final int MAX_NUMBER_OF_MULTIXYZ_DATA_SERIES_TO_PLOT = 1024;     // was 4;
    public static final int MAX_NUMBER_OF_3D_SURFACES_TO_PLOT = 1024;  // was 4;
    public static final int MAX_NUMBER_OF_PNTS_IN_ONE_CURVE = 65536;
    public static final double EYE_IDENTIFIABLE_LENGTH_RATIO = 0.008;    // a length > 0.008 * axis length is identifiable.
    public static final int MAX_NUMBER_OF_CALIBRATION_FOR_1ST_DRAWN_PNT = 12;   // calculate at most 16 times for first can-be-drawn point
    public static final int MAX_VALID_CALCULATED_PNTS_FOR_1ST_DRAWN_PNT = 8;   // max number of further calculated points.
    public static final int DEFAULT_NUMBER_OF_STEPS_IN_ONE_CURVE = 100;
    public static final int DEFAULT_NUMBER_OF_STEPS_SQR_IN_ONE_SURFACE = 100;
    public static final double TWOD_EXPR_CHART_AXIS_FILL_RATIO = 1.0;    // (x_max - x_min)/(axis_x_max - axis_x_min)
    public static final double THREED_EXPR_CHART_AXIS_FILL_RATIO = 2.0 / 3.0;    // (x_max - x_min)/(axis_x_max - axis_x_min)    

    public static class PlotGraphFunctionInterrupter extends FunctionInterrupter {

        @Override
        public boolean shouldInterrupt() {
            return Thread.currentThread().isInterrupted();
        }

        @Override
        public void interrupt() throws InterruptedException {
            throw new InterruptedException();
        }
    }

    public static class PlotGraphScriptInterrupter extends ScriptInterrupter {

        @Override
        public boolean shouldInterrupt() {
            return Thread.currentThread().isInterrupted();
        }

        @Override
        public void interrupt() throws InterruptedException {
            throw new InterruptedException();
        }
    }

    public static class TwoDCurve {

        public String mstrCurveTitle = "";
        public String mstrPntColor = "white";
        public String mstrPntStyle = "point";
        public int mnPntSize = 1;
        public String mstrLnColor = "white";
        public String mstrLnStyle = "solid";
        public int mnLnSize = 1;
        public String mstrTName = "t";
        public double mdTFrom = 0;
        public double mdTTo = 0;
        public double mdTStep = 0;
        public String mstrXExpr = "";
        public String mstrYExpr = "";
    }

    public static class TwoDExprCurve {

        public String mstrCurveTitle = "";
        public String mstrPntColor = "white";
        public String mstrPntStyle = "point";
        public int mnPntSize = 1;
        public String mstrLnColor = "white";
        public String mstrLnStyle = "solid";
        public int mnLnSize = 1;
        public int mnFunctionVar = 1;    // y = f(x) by default
        public String mstrXExpr = "";
        public String mstrYExpr = "";
        public int mnNumOfSteps = 0;
        public boolean mbAutoStep = false;    // this is obtained from number of steps, not input.
    }

    public static class ThreeDSurface {

        public String mstrCurveTitle = "";
        public Boolean mbIsGrid = true;    // if is grid, then is not filled
        public String mstrMinColor = "white";
        public String mstrMinColor1 = "white";
        public Double mdMinColorValue = null;  // null means min z is corresponding to min color
        public String mstrMaxColor = "white";
        public String mstrMaxColor1 = "white";
        public Double mdMaxColorValue = null;  // null means max z is corresponding to max color
        public String mstrUName = "u";
        public double mdUFrom = 0;
        public double mdUTo = 0;
        public double mdUStep = 0;
        public String mstrVName = "v";
        public double mdVFrom = 0;
        public double mdVTo = 0;
        public double mdVStep = 0;
        public String mstrXExpr = "";
        public String mstrYExpr = "";
        public String mstrZExpr = "";
    }

    public static class ThreeDExprSurface {

        public String mstrCurveLabel = "";
        public Boolean mbIsGrid = true;    // if is grid, then is not filled
        public String mstrMinColor = "white";
        public String mstrMinColor1 = "white";
        public String mstrMaxColor = "white";
        public String mstrMaxColor1 = "white";
        public int mnFunctionVar = 2;    // z = f(x,y) by default
        public String mstrXExpr = "";
        public int mnXNumOfSteps = 0;
        public String mstrYExpr = "";
        public int mnYNumOfSteps = 0;
        public String mstrZExpr = "";
        public int mnZNumOfSteps = 0;
    }

    public static void call2Load(boolean bOutput) {
        if (bOutput) {
            System.out.println("Loading " + PlotLib.class.getName());
        }
    }

    public static class PlotMultiXYFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public static final int PARAM_NUM_FOR_CHART_ONLY = 2;
        public static final int PARAM_NUM_IN_ONE_CURVE = 3;
        public PlotMultiXYFunction() {
        }
        // this function read value based parameters (i.e. x and y values are given out).
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            int nParamNumForChartOnly = PARAM_NUM_FOR_CHART_ONLY;
            int nParamNumIn1Cv = PARAM_NUM_IN_ONE_CURVE;
            int nMaxNumofCvs = MAX_NUMBER_OF_MULTIXY_DATA_SERIES_TO_PLOT;
            
            String strCallingFunc = mstrarrayFullCS[mstrarrayFullCS.length - 1];

            if (listParams.size() > (nParamNumForChartOnly + nMaxNumofCvs * nParamNumIn1Cv)
                    || listParams.size() < (nParamNumForChartOnly + nParamNumIn1Cv) // should be at least one curve, at most 8 curves.
                    || nParamNumIn1Cv * (int) (((double) (listParams.size() - nParamNumForChartOnly)) / nParamNumIn1Cv) != (listParams.size() - nParamNumForChartOnly)) {
                // The first nParamNumForChartOnly params are for chart definition.
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            int nNumofPairs = (int) ((listParams.size() - 2) / 3);
            String strOutput = "";
            for (int nIndex = 0; nIndex < nNumofPairs; nIndex++) {
                // after converting data to new type, now datumXList, datumYList and datumCurveSettings are all DataClassBuiltIn
                DataClassArray datumYList = DCHelper.lightCvtOrRetDCArray(listParams.poll().cloneSelf());
                DataClassArray datumXList = DCHelper.lightCvtOrRetDCArray(listParams.poll().cloneSelf());
                DataClassString datumCurveSettings = DCHelper.lightCvtOrRetDCString(listParams.poll().cloneSelf());
                if (datumXList.getDataListSize() == 0 || datumYList.getDataListSize() == 0
                        || datumXList.getDataListSize() != datumYList.getDataListSize()) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_PARAMETER_NOT_MATCH);
                }
                String strX = "", strY = "";
                for (int nIndex1 = 0; nIndex1 < datumXList.getDataListSize(); nIndex1++) {
                    // we have asserted that data list size != 0
                    datumXList.getDataList()[nIndex1]
                            = DCHelper.lightCvtOrRetDCSingleNum(datumXList.getDataList()[nIndex1]);
                    if (DCHelper.isSingleInteger(datumXList.getDataList()[nIndex1])) {
                        datumXList.getDataList()[nIndex1]
                                = DCHelper.lightCvtOrRetDCMFPInt(datumXList.getDataList()[nIndex1]);
                    }
                    datumYList.getDataList()[nIndex1]
                            = DCHelper.lightCvtOrRetDCSingleNum(datumYList.getDataList()[nIndex1]);
                    if (DCHelper.isSingleInteger(datumYList.getDataList()[nIndex1])) {
                        datumYList.getDataList()[nIndex1]
                                = DCHelper.lightCvtOrRetDCMFPInt(datumYList.getDataList()[nIndex1]);
                    }
                    strX += DCHelper.lightCvtOrRetDCSingleNum(datumXList.getDataList()[nIndex1]).getDataValue().toString();
                    if (nIndex1 < datumXList.getDataListSize() - 1) {
                        strX += ";";
                    } else {
                        strX += "\n";
                    }
                    strY += DCHelper.lightCvtOrRetDCSingleNum(datumYList.getDataList()[nIndex1]).getDataValue().toString();
                    if (nIndex1 < datumYList.getDataListSize() - 1) {
                        strY += ";";
                    } else {
                        strY += "\n";
                    }
                }
                strOutput = strX + strY + datumCurveSettings.getStringValue() + "\n" + strOutput;
            }
            // convert data 2 new type use shallow copy. But if convert to non-reference data, it is same as deep copy.
            DataClassString datumChartSettings = DCHelper.lightCvtOrRetDCString(listParams.poll());
            DataClassString datumChartName = DCHelper.lightCvtOrRetDCString(listParams.poll());
            strOutput = "chart_name:" + ChartOperator.addEscapes(datumChartName.getStringValue()) + ";" + datumChartSettings.getStringValue() + ";number_of_curves:" + String.valueOf(nNumofPairs) + "\n"
                    + strOutput;

            // plot the graph
            if (FuncEvaluator.msgraphPlotter != null) {
                if (!FuncEvaluator.msgraphPlotter.plotGraph(strOutput)) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_PLOT_CHART);
                }
            }

            // save the graph to disk
            if (FuncEvaluator.msfileOperator != null && datumChartName.getStringValue().trim().length() > 0) {
                try {
                    FuncEvaluator.msfileOperator.outputGraphFile(datumChartName.getStringValue(), strOutput);
                } catch (FileNotFoundException e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_FUNCTION_PROCESSION,
                            strCallingFunc, e); // calling function has been small case so no need to change to small case.
                } catch (IOException e) {
                    if (e instanceof ClosedByInterruptException) {
                        throw new InterruptedException();    // this thread is interrupted by another thread.
                    } else {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_FUNCTION_PROCESSION,
                                strCallingFunc, e); // calling function has been small case so no need to change to small case.
                    }
                }
            }
            return null;
        }
    }
    
    public static class Plot_multi_xyFunction extends PlotMultiXYFunction {
		private static final long serialVersionUID = 1L;

        public Plot_multi_xyFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::plot_math::plot_multi_xy";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = PARAM_NUM_FOR_CHART_ONLY + MAX_NUMBER_OF_MULTIXY_DATA_SERIES_TO_PLOT * PARAM_NUM_IN_ONE_CURVE;
            mnMinParamNum = PARAM_NUM_FOR_CHART_ONLY + PARAM_NUM_IN_ONE_CURVE;
        }
    }
    //public final static Plot_multi_xyFunction BUILTINFUNC_Plot_multi_xy = new Plot_multi_xyFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Plot_multi_xyFunction());}
    
    public static class Plot_multi_rangleFunction extends PlotMultiXYFunction {
		private static final long serialVersionUID = 1L;

        public Plot_multi_rangleFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::plot_math::plot_multi_rangle";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = PARAM_NUM_FOR_CHART_ONLY + MAX_NUMBER_OF_MULTIXY_DATA_SERIES_TO_PLOT * PARAM_NUM_IN_ONE_CURVE;
            mnMinParamNum = PARAM_NUM_FOR_CHART_ONLY + PARAM_NUM_IN_ONE_CURVE;
        }
    }
    //public final static Plot_multi_rangleFunction BUILTINFUNC_Plot_multi_rangle = new Plot_multi_rangleFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Plot_multi_rangleFunction());}

    public static class Plot_multi_xyzFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public static final int PARAM_NUM_FOR_CHART_ONLY = 2;
        public static final int PARAM_NUM_IN_ONE_CURVE = 4;
        public Plot_multi_xyzFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::plot_math::plot_multi_xyz";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = PARAM_NUM_FOR_CHART_ONLY + MAX_NUMBER_OF_MULTIXYZ_DATA_SERIES_TO_PLOT * PARAM_NUM_IN_ONE_CURVE;
            mnMinParamNum = PARAM_NUM_FOR_CHART_ONLY + PARAM_NUM_IN_ONE_CURVE;
        }
        // this function read value based parameters (i.e. x, y and z values are given out).
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            int nParamNumForChartOnly = PARAM_NUM_FOR_CHART_ONLY;
            int nParamNumIn1Cv = PARAM_NUM_IN_ONE_CURVE;
            int nMaxNumofCvs = MAX_NUMBER_OF_MULTIXYZ_DATA_SERIES_TO_PLOT;

            String strCallingFunc = mstrarrayFullCS[mstrarrayFullCS.length - 1];

            if (listParams.size() > (nParamNumForChartOnly + nMaxNumofCvs * nParamNumIn1Cv)
                    || listParams.size() < (nParamNumForChartOnly + nParamNumIn1Cv) // should be at least one curve, at most 8 curves.
                    || nParamNumIn1Cv * (int) (((double) (listParams.size() - nParamNumForChartOnly)) / nParamNumIn1Cv) != (listParams.size() - nParamNumForChartOnly)) {
                // The first nParamNumForChartOnly params are for chart definition.
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            int nNumofPairs = (int) ((listParams.size() - 2) / 4);
            String strOutput = "";
            for (int nIndex = 0; nIndex < nNumofPairs; nIndex++) {
                // even if datumZArray, datumYArray and datumXArray are single data type (e.g. integer, double),
                // they can still be converted to a data array.
                DataClass datumZArrayParam = listParams.poll(); // no need to deep copy coz fullfillDataArray will do it.
                int[] narrayZSize = datumZArrayParam.recalcDataArraySize();
                DataClassArray datumZArray = DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datumZArrayParam).fullfillDataArray(narrayZSize, false));
                DataClass datumYArrayParam = listParams.poll(); // no need to deep copy coz fullfillDataArray will do it.
                int[] narrayYSize = datumYArrayParam.recalcDataArraySize();
                DataClassArray datumYArray = DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datumYArrayParam).fullfillDataArray(narrayYSize, false));
                DataClass datumXArrayParam = listParams.poll(); // no need to deep copy coz fullfillDataArray will do it.
                int[] narrayXSize = datumXArrayParam.recalcDataArraySize();
                DataClassArray datumXArray = DCHelper.lightCvtOrRetDCArray(DCHelper.lightCvtOrRetDCArray(datumXArrayParam).fullfillDataArray(narrayXSize, false));
                // convert to string data need not deep copy.
                DataClassString datumCurveSettings = DCHelper.lightCvtOrRetDCString(listParams.poll());
                if (narrayXSize.length != 2 || narrayYSize.length != 2 || narrayZSize.length != 2
                        || narrayXSize[0] == 0 || narrayXSize[1] == 0 || narrayXSize[0] != narrayYSize[0]
                        || narrayXSize[1] != narrayYSize[1] || narrayXSize[0] != narrayZSize[0]
                        || narrayXSize[1] != narrayZSize[1]) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_PARAMETER_NOT_MATCH);
                }
                String strX = "", strY = "", strZ = "";
                for (int nIndex0 = 0; nIndex0 < narrayXSize[0]; nIndex0++) {
                    for (int nIndex1 = 0; nIndex1 < narrayXSize[1]; nIndex1++) {
                        // because datumXArray, datumYArray and datumZArray are all fully populated 2D arrays,
                        // datumXArrayIdx0.getDataList()[nIndex1] must be DataClassBuiltIn.
                        DataClassArray datumXArrayIdx0 = DCHelper.lightCvtOrRetDCArray(datumXArray.getDataList()[nIndex0]);
                        DataClassArray datumYArrayIdx0 = DCHelper.lightCvtOrRetDCArray(datumYArray.getDataList()[nIndex0]);
                        DataClassArray datumZArrayIdx0 = DCHelper.lightCvtOrRetDCArray(datumZArray.getDataList()[nIndex0]);
                        datumXArrayIdx0.getDataList()[nIndex1]
                                = DCHelper.lightCvtOrRetDCMFPDec(datumXArrayIdx0.getDataList()[nIndex1]);
                        if (DCHelper.isSingleInteger(datumXArrayIdx0.getDataList()[nIndex1])) {
                            datumXArrayIdx0.getDataList()[nIndex1] = DCHelper.lightCvtOrRetDCMFPInt(datumXArrayIdx0.getDataList()[nIndex1]);
                        }
                        datumYArrayIdx0.getDataList()[nIndex1]
                                = DCHelper.lightCvtOrRetDCMFPDec(datumYArrayIdx0.getDataList()[nIndex1]);
                        if (DCHelper.isSingleInteger(datumYArrayIdx0.getDataList()[nIndex1])) {
                            datumYArrayIdx0.getDataList()[nIndex1] = DCHelper.lightCvtOrRetDCMFPInt(datumYArrayIdx0.getDataList()[nIndex1]);
                        }
                        datumZArrayIdx0.getDataList()[nIndex1]
                                = DCHelper.lightCvtOrRetDCMFPDec(datumZArrayIdx0.getDataList()[nIndex1]);
                        if (DCHelper.isSingleInteger(datumZArrayIdx0.getDataList()[nIndex1])) {
                            datumZArrayIdx0.getDataList()[nIndex1] = DCHelper.lightCvtOrRetDCMFPInt(datumZArrayIdx0.getDataList()[nIndex1]);
                        }
                        strX += DCHelper.lightCvtOrRetDCSingleNum(datumXArrayIdx0.getDataList()[nIndex1]).getDataValue().toString();
                        if (nIndex1 < datumXArrayIdx0.getDataListSize() - 1) {
                            strX += ",";
                        } else if (nIndex0 < datumXArray.getDataListSize() - 1) {
                            strX += ";";
                        } else {
                            strX += "\n";
                        }
                        strY += DCHelper.lightCvtOrRetDCSingleNum(datumYArrayIdx0.getDataList()[nIndex1]).getDataValue().toString();
                        if (nIndex1 < datumYArrayIdx0.getDataListSize() - 1) {
                            strY += ",";
                        } else if (nIndex0 < datumYArray.getDataListSize() - 1) {
                            strY += ";";
                        } else {
                            strY += "\n";
                        }
                        strZ += DCHelper.lightCvtOrRetDCSingleNum(datumZArrayIdx0.getDataList()[nIndex1]).getDataValue().toString();
                        if (nIndex1 < datumZArrayIdx0.getDataListSize() - 1) {
                            strZ += ",";
                        } else if (nIndex0 < datumZArray.getDataListSize() - 1) {
                            strZ += ";";
                        } else {
                            strZ += "\n";
                        }
                    }
                }
                strOutput = strX + strY + strZ + datumCurveSettings.getStringValue() + "\n" + strOutput;
            }
            // no need to deep copy as we convert to string.
            DataClassString datumChartSettings = DCHelper.lightCvtOrRetDCString(listParams.poll());
            DataClassString datumChartName = DCHelper.lightCvtOrRetDCString(listParams.poll());
            strOutput = "chart_name:" + ChartOperator.addEscapes(datumChartName.getStringValue()) + ";" + datumChartSettings.getStringValue() + ";number_of_curves:" + String.valueOf(nNumofPairs) + "\n"
                    + strOutput;

            // plot the graph
            if (FuncEvaluator.msgraphPlotter3D != null) {
                try {
                    if (!FuncEvaluator.msgraphPlotter3D.plotGraph(strOutput)) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_PLOT_CHART);
                    }
                } catch (UnsatisfiedLinkError e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_PLOT_OGL_CHART_LACK_SYSTEM_SUPPORT_FILES);
                }
            }

            // save the graph to disk
            if (FuncEvaluator.msfileOperator != null && datumChartName.getStringValue().trim().length() > 0) {
                try {
                    FuncEvaluator.msfileOperator.outputGraphFile(datumChartName.getStringValue(), strOutput);
                } catch (FileNotFoundException e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_FUNCTION_PROCESSION,
                            strCallingFunc, e);
                } catch (IOException e) {
                    if (e instanceof ClosedByInterruptException) {
                        throw new InterruptedException();    // this thread is interrupted by another thread.
                    } else {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_FUNCTION_PROCESSION,
                                strCallingFunc, e);
                    }
                }
            }
            return null;
        }
    }
    //public final static Plot_multi_xyzFunction BUILTINFUNC_Plot_multi_xyz = new Plot_multi_xyzFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Plot_multi_xyzFunction());}
    
    public static class Plot2DCurvesFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public static final int PARAM_NUM_FOR_CHART_ONLY = 6;
        public static final int PARAM_NUM_IN_ONE_CURVE = 13;
        public Plot2DCurvesFunction() {
        }
        // this function read expression parameters (i.e. x and y values have to be calculated from expression).
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            int nParamNumForChartOnly = PARAM_NUM_FOR_CHART_ONLY;
            int nParamNumIn1Cv = PARAM_NUM_IN_ONE_CURVE;
            int nMaxNumofCvs = MAX_NUMBER_OF_2D_CURVES_TO_PLOT;

            String strCallingFunc = mstrarrayFullCS[mstrarrayFullCS.length - 1];

            if (listParams.size() > (nParamNumForChartOnly + nMaxNumofCvs * nParamNumIn1Cv)
                    || listParams.size() < (nParamNumForChartOnly + nParamNumIn1Cv) // should be at least one curve, at most 8 curves.
                    || nParamNumIn1Cv * (int) (((double) (listParams.size() - nParamNumForChartOnly)) / nParamNumIn1Cv) != (listParams.size() - nParamNumForChartOnly)) {
                // The first nParamNumForChartOnly params are for chart definition.
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            String strChartType = "multiXY";
            if (strCallingFunc.equals("plot_polar_curves")) {
                strChartType = "multiRangle";
            }
            int nNumofCurves = (int) ((listParams.size() - nParamNumForChartOnly) / nParamNumIn1Cv);
            int nDefaultNumOfPoints = DEFAULT_NUMBER_OF_STEPS_IN_ONE_CURVE + 1;    // was 128;

            // convert to non-array type need not to worry about deep copy so that we can just use convertData2NewType
            DataClassString datumChartName = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strChartName = datumChartName.getStringValue();

            DataClassString datumChartTitle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strChartTitle = datumChartTitle.getStringValue();

            DataClassString datumXTitle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strXTitle = datumXTitle.getStringValue();

            DataClassString datumYTitle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strYTitle = datumYTitle.getStringValue();

            DataClassString datumChartBKColor = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strChartBKColor = datumChartBKColor.getStringValue();

            DataClassString datumShowGrid = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strShowGrid = datumShowGrid.getStringValue();

            TwoDCurve[] twoDCurves = new TwoDCurve[nNumofCurves];
            double[][] arraydTCurves = new double[nNumofCurves][0];
            double[][] arraydXCurves = new double[nNumofCurves][0];
            double[][] arraydYCurves = new double[nNumofCurves][0];
            double[] arraydXAvg = new double[nNumofCurves];
            double[] arraydXSqrAvg = new double[nNumofCurves];
            double[] arraydXStdev = new double[nNumofCurves];
            double[] arraydYAvg = new double[nNumofCurves];
            double[] arraydYSqrAvg = new double[nNumofCurves];
            double[] arraydYStdev = new double[nNumofCurves];

            String[] strarrayCurveSettings = new String[nNumofCurves];
            String[] strarrayXValueList = new String[nNumofCurves];
            String[] strarrayYValueList = new String[nNumofCurves];
            double dXMin = 0, dXMax = 0, dYMin = 0, dYMax = 0;
            String strOutput = "";
            for (int idx = 0; idx < nNumofCurves; idx++) {
                twoDCurves[idx] = new TwoDCurve();

                DataClassString datumCurveTitle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                twoDCurves[idx].mstrCurveTitle = datumCurveTitle.getStringValue();

                DataClassString datumCurvePntColor = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                twoDCurves[idx].mstrPntColor = datumCurvePntColor.getStringValue();

                DataClassString datumCurvePntStyle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                twoDCurves[idx].mstrPntStyle = datumCurvePntStyle.getStringValue();

                DataClassSingleNum datumCurvePntSize = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
                twoDCurves[idx].mnPntSize = (int) datumCurvePntSize.getDataValue().longValue();  // point size shouldn't be too large.

                DataClassString datumCurveLnColor = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                twoDCurves[idx].mstrLnColor = datumCurveLnColor.getStringValue();

                DataClassString datumCurveLnStyle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                twoDCurves[idx].mstrLnStyle = datumCurveLnStyle.getStringValue();

                DataClassSingleNum datumCurveLnSize = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
                twoDCurves[idx].mnLnSize = (int) datumCurveLnSize.getDataValue().longValue();    // line size shouldn't be too large

                DataClassString datumTName = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                twoDCurves[idx].mstrTName = datumTName.getStringValue().trim().toLowerCase(Locale.US);

                DataClassSingleNum datumTFrom = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
                twoDCurves[idx].mdTFrom = datumTFrom.getDataValue().doubleValue();

                DataClassSingleNum datumTTo = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
                twoDCurves[idx].mdTTo = datumTTo.getDataValue().doubleValue();

                DataClassSingleNum datumTStep = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
                twoDCurves[idx].mdTStep = datumTStep.getDataValue().doubleValue();

                DataClassString datumXExpr = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                twoDCurves[idx].mstrXExpr = datumXExpr.getStringValue();

                DataClassString datumYExpr = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                twoDCurves[idx].mstrYExpr = datumYExpr.getStringValue();

                strarrayCurveSettings[idx] = "curve_label:" + ChartOperator.addEscapes(twoDCurves[idx].mstrCurveTitle)
                        + ";point_color:" + ChartOperator.addEscapes(twoDCurves[idx].mstrPntColor) + ";point_style:" + ChartOperator.addEscapes(twoDCurves[idx].mstrPntStyle) + ";point_size:" + twoDCurves[idx].mnPntSize
                        + ";line_color:" + ChartOperator.addEscapes(twoDCurves[idx].mstrLnColor) + ";line_style:" + ChartOperator.addEscapes(twoDCurves[idx].mstrLnStyle) + ";line_size:" + twoDCurves[idx].mnLnSize;
                strarrayXValueList[idx] = "";
                strarrayYValueList[idx] = "";

                int nNumofPntsInCurve = 0;
                double dTStep = twoDCurves[idx].mdTStep;
                if (twoDCurves[idx].mdTStep != 0) {
                    // normal interval mode
                    nNumofPntsInCurve = (int) (Math.floor((twoDCurves[idx].mdTTo - twoDCurves[idx].mdTFrom) / twoDCurves[idx].mdTStep) + 1);
                    if (nNumofPntsInCurve < 1) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CURVE_SHOULD_HAVE_AT_LEAST_ONE_POINT);
                    } else if (nNumofPntsInCurve > MAX_NUMBER_OF_PNTS_IN_ONE_CURVE) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_TOO_MANY_POINTS_TO_PLOT_IN_A_CURVE);
                    }
                } else {
                    nNumofPntsInCurve = nDefaultNumOfPoints;    // assume number of points in a curve in auto mode is 256 so that we can get max and min for x and y
                    dTStep = (twoDCurves[idx].mdTTo - twoDCurves[idx].mdTFrom) / (nNumofPntsInCurve - 1);
                }
                arraydTCurves[idx] = new double[nNumofPntsInCurve];
                arraydXCurves[idx] = new double[nNumofPntsInCurve];
                arraydYCurves[idx] = new double[nNumofPntsInCurve];

                int idxXY = 0;
                double dSumX = 0, dSumXSqr = 0, dSumY = 0, dSumYSqr = 0;
                int nValidXPntNum = 0, nValidYPntNum = 0;
                AbstractExpr aeXExpr = null, aeYExpr = null;
                try {
                    aeXExpr = ExprAnalyzer.analyseExpression(twoDCurves[idx].mstrXExpr, new CurPos(), new LinkedList<Variable>(), progContext);
                    aeYExpr = ExprAnalyzer.analyseExpression(twoDCurves[idx].mstrYExpr, new CurPos(), new LinkedList<Variable>(), progContext);
                    // the following statements are important, so that like sin(x) can be converted to ::mfp::math::sin(x)
                    // otherwise, when a chart is reloaded, or is repainted after creation, a user defined function whose
                    // citing space is not in the default citing spaces cannot be recognized.
                    twoDCurves[idx].mstrXExpr = aeXExpr.output();
                    twoDCurves[idx].mstrYExpr = aeYExpr.output();
                } catch (Exception e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
                while (idxXY < nNumofPntsInCurve) {
                    arraydTCurves[idx][idxXY] = twoDCurves[idx].mdTFrom + dTStep * idxXY;
                    DataClass datumT = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(arraydTCurves[idx][idxXY], true));
                    Variable varT = new Variable(twoDCurves[idx].mstrTName, datumT);
                    LinkedList<Variable> lVarnameSpace = new LinkedList<Variable>();
                    
                    lVarnameSpace.addFirst(varT);
                    progContext.mdynamicProgContext.mlVarNameSpaces.addFirst(lVarnameSpace);
                    //ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
                    //CurPos curPos = new CurPos();
                    DataClass datumXValue = null, datumYValue = null;
                    try {
                        //curPos.m_nPos = 0;
                        //datumXValue =  exprEvaluator.evaluateExpression(twoDCurves[idx].mstrXExpr, curPos);
                        datumXValue = aeXExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);
                        //curPos.m_nPos = 0;
                        //datumYValue = exprEvaluator.evaluateExpression(twoDCurves[idx].mstrYExpr, curPos);
                        datumYValue = aeYExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);
                    } catch (Exception e) {
                        // do nothing here
                    }
                    progContext.mdynamicProgContext.mlVarNameSpaces.removeFirst();
                    if (datumXValue == null) {
                        arraydXCurves[idx][idxXY] = Double.NaN;
                    } else if (datumXValue.getThisOrNull() instanceof DataClassSingleNum) {
                        arraydXCurves[idx][idxXY] = DCHelper.lightCvtOrRetDCSingleNum(datumXValue).getDataValue().doubleValue();
                    } else {    // matrix or complex value. complex value may be changed to double if its image is 0.
                        try {
                            arraydXCurves[idx][idxXY] = DCHelper.lightCvtOrRetDCSingleNum(datumXValue).getDataValue().doubleValue();
                        } catch (Exception e) {
                            arraydXCurves[idx][idxXY] = Double.NaN;
                        }
                    }
                    if (datumYValue == null) {
                        arraydYCurves[idx][idxXY] = Double.NaN;
                    } else if (datumYValue.getThisOrNull() instanceof DataClassSingleNum) {
                        arraydYCurves[idx][idxXY] = DCHelper.lightCvtOrRetDCSingleNum(datumYValue).getDataValue().doubleValue();
                    } else {    // matrix or complex value. Complex value may be changed to double if its image is 0.
                        try {
                            arraydYCurves[idx][idxXY] = DCHelper.lightCvtOrRetDCSingleNum(datumYValue).getDataValue().doubleValue();
                        } catch (Exception e) {
                            arraydYCurves[idx][idxXY] = Double.NaN;
                        }
                    }
                    if (!Double.isNaN(arraydXCurves[idx][idxXY]) && !Double.isInfinite(arraydXCurves[idx][idxXY])
                            && !Double.isNaN(arraydYCurves[idx][idxXY]) && !Double.isInfinite(arraydYCurves[idx][idxXY])) {
                        dSumX += arraydXCurves[idx][idxXY];
                        dSumXSqr += arraydXCurves[idx][idxXY] * arraydXCurves[idx][idxXY];
                        nValidXPntNum++;
                        dSumY += arraydYCurves[idx][idxXY];
                        dSumYSqr += arraydYCurves[idx][idxXY] * arraydYCurves[idx][idxXY];
                        nValidYPntNum++;
                    }
                    // JFreeChart does not support input Infinity so has to be NaN.
                    strarrayXValueList[idx] += Double.isInfinite(arraydXCurves[idx][idxXY]) ? Double.NaN : arraydXCurves[idx][idxXY];
                    strarrayYValueList[idx] += Double.isInfinite(arraydYCurves[idx][idxXY]) ? Double.NaN : arraydYCurves[idx][idxXY];
                    if (idxXY < nNumofPntsInCurve - 1) {
                        strarrayXValueList[idx] += ";";
                        strarrayYValueList[idx] += ";";
                    } else {
                        strarrayXValueList[idx] += "\n";
                        strarrayYValueList[idx] += "\n";
                    }
                    /* using stdev to identify chart range
                     * if (idx == 0 && idxXY == 0)    {
                    dXMin = arraydXCurves[idx][idxXY];
                    dXMax = arraydXCurves[idx][idxXY];
                    dYMin = arraydYCurves[idx][idxXY];
                    dYMax = arraydYCurves[idx][idxXY];
                    } else    {
                    if (dXMin > arraydXCurves[idx][idxXY])    {
                    dXMin = arraydXCurves[idx][idxXY];
                    }
                    if (dXMax < arraydXCurves[idx][idxXY])    {
                    dXMax = arraydXCurves[idx][idxXY];
                    }
                    if (dYMin > arraydYCurves[idx][idxXY])    {
                    dYMin = arraydYCurves[idx][idxXY];
                    }
                    if (dYMax < arraydYCurves[idx][idxXY])    {
                    dYMax = arraydYCurves[idx][idxXY];
                    }
                    }*/
                    idxXY++;
                }
                if (nValidXPntNum > 0) {
                    arraydXAvg[idx] = dSumX / nValidXPntNum;
                    arraydXSqrAvg[idx] = dSumXSqr / nValidXPntNum;
                    arraydXStdev[idx] = Math.sqrt(Math.max(0, arraydXSqrAvg[idx] - arraydXAvg[idx] * arraydXAvg[idx]));
                } else {
                    arraydXAvg[idx] = arraydXSqrAvg[idx] = arraydXStdev[idx] = 0;
                }
                if (nValidYPntNum > 0) {
                    arraydYAvg[idx] = dSumY / nValidYPntNum;
                    arraydYSqrAvg[idx] = dSumYSqr / nValidXPntNum;
                    arraydYStdev[idx] = Math.sqrt(Math.max(0, arraydYSqrAvg[idx] - arraydYAvg[idx] * arraydYAvg[idx]));
                } else {
                    arraydYAvg[idx] = arraydYSqrAvg[idx] = arraydYStdev[idx] = 0;
                }
                double dConfidenceInterval = 3;
                if (idx == 0) {
                    dXMin = arraydXAvg[idx] - dConfidenceInterval * arraydXStdev[idx];
                    dXMax = arraydXAvg[idx] + dConfidenceInterval * arraydXStdev[idx];
                    dYMin = arraydYAvg[idx] - dConfidenceInterval * arraydYStdev[idx];
                    dYMax = arraydYAvg[idx] + dConfidenceInterval * arraydYStdev[idx];
                } else {
                    if (dXMin > arraydXAvg[idx] - dConfidenceInterval * arraydXStdev[idx]) {
                        dXMin = arraydXAvg[idx] - dConfidenceInterval * arraydXStdev[idx];
                    }
                    if (dXMax < arraydXAvg[idx] + dConfidenceInterval * arraydXStdev[idx]) {
                        dXMax = arraydXAvg[idx] + dConfidenceInterval * arraydXStdev[idx];
                    }
                    if (dYMin > arraydYAvg[idx] - dConfidenceInterval * arraydYStdev[idx]) {
                        dYMin = arraydYAvg[idx] - dConfidenceInterval * arraydYStdev[idx];
                    }
                    if (dYMax < arraydYAvg[idx] + dConfidenceInterval * arraydYStdev[idx]) {
                        dYMax = arraydYAvg[idx] + dConfidenceInterval * arraydYStdev[idx];
                    }
                }
            }
            double dDefaultHalfRange = (MFPAdapter.getPlotChartVariableTo() - MFPAdapter.getPlotChartVariableFrom())/2.0;
            if (dXMin == dXMax
                    || (strChartType.equalsIgnoreCase("multiRangle") 
                        && Math.abs(dXMax - dXMin) < 0.00001)) {
                // polar chart is a bit special. For some reason, if xmin = xmax = like 2.67,
                // mdblXMax and mdblXMin are not exactly the same, their difference is so small
                // that R step length will be too small and there will be so many points to draw
                // that final results in outofmemory error.
                dXMin -= dDefaultHalfRange;
                dXMax += dDefaultHalfRange;
            }
            if (dYMin == dYMax) {
                dYMin -= dDefaultHalfRange;
                dYMax += dDefaultHalfRange;
            }

            for (int idx = 0; idx < nNumofCurves; idx++) {
                if (twoDCurves[idx].mdTStep == 0) {
                    LinkedList<Double> listdblX = new LinkedList<Double>();
                    LinkedList<Double> listdblY = new LinkedList<Double>();
                    int idxXY = 0;
                    double dTCurrent = twoDCurves[idx].mdTFrom;
                    double dTStep = (twoDCurves[idx].mdTTo - twoDCurves[idx].mdTFrom) / (nDefaultNumOfPoints - 1);
                    AbstractExpr aeXExpr = null, aeYExpr = null;
                    try {
                        // now the functions in the expressions are with absolute CS, so no need to add progContext
                        aeXExpr = ExprAnalyzer.analyseExpression(twoDCurves[idx].mstrXExpr, new CurPos());
                        aeYExpr = ExprAnalyzer.analyseExpression(twoDCurves[idx].mstrYExpr, new CurPos());
                    } catch (Exception e) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                    }
                    while (idxXY < nDefaultNumOfPoints) {
                        listdblX.add(arraydXCurves[idx][idxXY]);
                        listdblY.add(arraydYCurves[idx][idxXY]);
                        if (idxXY == nDefaultNumOfPoints - 1) {
                            break;
                        }
                        DataClassSingleNum datumT = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dTCurrent + dTStep / 2, true));
                        Variable varT = new Variable(twoDCurves[idx].mstrTName, datumT);
                        LinkedList<Variable> lVarnameSpace = new LinkedList<Variable>();
                        lVarnameSpace.addFirst(varT);
                        progContext.mdynamicProgContext.mlVarNameSpaces.addFirst(lVarnameSpace);
                        //ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
                        //CurPos curPos = new CurPos();
                        DataClass datumXValue = null, datumYValue = null;
                        try {
                            //curPos.m_nPos = 0;
                            //datumXValue = exprEvaluator.evaluateExpression(twoDCurves[idx].mstrXExpr, curPos);
                            datumXValue = aeXExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);
                            //curPos.m_nPos = 0;
                            //datumYValue = exprEvaluator.evaluateExpression(twoDCurves[idx].mstrYExpr, curPos);
                            datumYValue = aeYExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);
                        } catch (Exception e) {
                            // do nothing here
                        }
                        progContext.mdynamicProgContext.mlVarNameSpaces.removeFirst();

                        double dThisX = Double.NaN, dThisY = Double.NaN;
                        if (datumXValue == null) {
                            dThisX = Double.NaN;
                        } else if (datumXValue.getThisOrNull() instanceof DataClassSingleNum) {
                            dThisX = DCHelper.lightCvtOrRetDCSingleNum(datumXValue).getDataValue().doubleValue();
                        } else {    // matrix or complex value. Complex value can be converted to double if image is 0.
                            try {
                                dThisX = DCHelper.lightCvtOrRetDCSingleNum(datumXValue).getDataValue().doubleValue();
                            } catch (Exception e) {
                                dThisX = Double.NaN;
                            }
                        }
                        if (datumYValue == null) {
                            dThisY = Double.NaN;
                        } else if (datumYValue.getThisOrNull() instanceof DataClassSingleNum) {
                            dThisY = DCHelper.lightCvtOrRetDCSingleNum(datumYValue).getDataValue().doubleValue();
                        } else {    // matrix or complex value. Complex value can be converted to double if image is 0.
                            try {
                                dThisY = DCHelper.lightCvtOrRetDCSingleNum(datumYValue).getDataValue().doubleValue();
                            } catch (Exception e) {
                                dThisY = Double.NaN;
                            }
                        }

                        if (!Double.isNaN(arraydXCurves[idx][idxXY]) && !Double.isInfinite(arraydXCurves[idx][idxXY])
                                && !Double.isNaN(arraydXCurves[idx][idxXY + 1]) && !Double.isInfinite(arraydXCurves[idx][idxXY + 1])
                                && !Double.isNaN(dThisX) && !Double.isInfinite(dThisX)) {
                            double dGapTolerance = Math.max(Math.max(arraydXStdev[idx], Math.abs(dThisX - arraydXAvg[idx])),
                                    Math.max(Math.abs(arraydXCurves[idx][idxXY + 1] - arraydXAvg[idx]), Math.abs(arraydXCurves[idx][idxXY] - arraydXAvg[idx])));
                            if (Math.abs((arraydXCurves[idx][idxXY + 1]) + (arraydXCurves[idx][idxXY]) - 2 * (dThisX))
                                    > 1.0 * dGapTolerance) {
                                // abs(left + right - 2* mid) > relative confidence interval, it seems to be a singular point.
                                dThisX = (arraydXCurves[idx][idxXY + 1] + arraydXCurves[idx][idxXY] > 2 * dThisX)
                                        ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                            }
                        }
                        if (!Double.isNaN(arraydYCurves[idx][idxXY]) && !Double.isInfinite(arraydYCurves[idx][idxXY])
                                && !Double.isNaN(arraydYCurves[idx][idxXY + 1]) && !Double.isInfinite(arraydYCurves[idx][idxXY + 1])
                                && !Double.isNaN(dThisY) && !Double.isInfinite(dThisY)) {
                            double dGapTolerance = Math.max(Math.max(arraydYStdev[idx], Math.abs(dThisY - arraydYAvg[idx])),
                                    Math.max(Math.abs(arraydYCurves[idx][idxXY + 1] - arraydYAvg[idx]), Math.abs(arraydYCurves[idx][idxXY] - arraydYAvg[idx])));
                            if (Math.abs((arraydYCurves[idx][idxXY + 1]) + (arraydYCurves[idx][idxXY]) - 2 * (dThisY))
                                    > 1.0 * dGapTolerance) {
                                // abs(left + right - 2* mid) > relative confidence interval, it seems to be a singular point.
                                dThisY = (arraydYCurves[idx][idxXY + 1] + arraydYCurves[idx][idxXY] > 2 * dThisY)
                                        ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                            }
                        }
                        listdblX.add(dThisX);
                        listdblY.add(dThisY);

                        dTCurrent += dTStep;
                        idxXY++;
                    }

                    arraydXCurves[idx] = new double[listdblX.size()];
                    arraydYCurves[idx] = new double[listdblY.size()];
                    strarrayXValueList[idx] = "";
                    strarrayYValueList[idx] = "";
                    for (idxXY = 0; idxXY < listdblX.size(); idxXY++) {
                        arraydXCurves[idx][idxXY] = listdblX.get(idxXY);
                        arraydYCurves[idx][idxXY] = listdblY.get(idxXY);
                        // JFreeChart does not support input Infinity so has to be NaN.
                        strarrayXValueList[idx] += Double.isInfinite(arraydXCurves[idx][idxXY]) ? Double.NaN : arraydXCurves[idx][idxXY];
                        strarrayYValueList[idx] += Double.isInfinite(arraydYCurves[idx][idxXY]) ? Double.NaN : arraydYCurves[idx][idxXY];
                        if (idxXY < listdblX.size() - 1) {
                            strarrayXValueList[idx] += ";";
                            strarrayYValueList[idx] += ";";
                        } else {
                            strarrayXValueList[idx] += "\n";
                            strarrayYValueList[idx] += "\n";
                        }
                    }
                }
                strOutput += "\n" + strarrayXValueList[idx] + strarrayYValueList[idx] + strarrayCurveSettings[idx];
            }

            String strChartSettings = "chart_type:" + strChartType + ";chart_title:" + ChartOperator.addEscapes(strChartTitle)
                    + ";x_title:" + ChartOperator.addEscapes(strXTitle) + ";x_min:" + dXMin + ";x_max:" + dXMax + ";x_labels:10"
                    + ";y_title:" + ChartOperator.addEscapes(strYTitle) + ";y_min:" + dYMin + ";y_max:" + dYMax + ";y_labels:10"
                    + ";background_color:" + ChartOperator.addEscapes(strChartBKColor) + ";show_grid:" + ChartOperator.addEscapes(strShowGrid);
            strOutput = "chart_name:" + ChartOperator.addEscapes(strChartName) + ";" + strChartSettings + ";number_of_curves:" + nNumofCurves + strOutput;

            // plot the graph
            if (FuncEvaluator.msgraphPlotter != null) {
                if (!FuncEvaluator.msgraphPlotter.plotGraph(strOutput)) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_PLOT_CHART);
                }
            }

            // save the graph to disk
            if (FuncEvaluator.msfileOperator != null && strChartName.trim().length() > 0) {
                try {
                    FuncEvaluator.msfileOperator.outputGraphFile(strChartName, strOutput);
                } catch (FileNotFoundException e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_FUNCTION_PROCESSION,
                            strCallingFunc, e);
                } catch (IOException e) {
                    if (e instanceof ClosedByInterruptException) {
                        throw new InterruptedException();    // this thread is interrupted by another thread.
                    } else {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_FUNCTION_PROCESSION,
                                strCallingFunc, e);
                    }
                }
            }
            return null;
        }
    }

    public static class Plot_2d_curvesFunction extends Plot2DCurvesFunction {
		private static final long serialVersionUID = 1L;

        public Plot_2d_curvesFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::plot_math::plot_2d_curves";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = PARAM_NUM_FOR_CHART_ONLY + MAX_NUMBER_OF_2D_CURVES_TO_PLOT * PARAM_NUM_IN_ONE_CURVE;
            mnMinParamNum = PARAM_NUM_FOR_CHART_ONLY + PARAM_NUM_IN_ONE_CURVE;
        }
    }
    //public final static Plot_2d_curvesFunction BUILTINFUNC_Plot_2d_curves = new Plot_2d_curvesFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Plot_2d_curvesFunction());}

    public static class Plot_polar_curvesFunction extends Plot2DCurvesFunction {
		private static final long serialVersionUID = 1L;

        public Plot_polar_curvesFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::plot_math::plot_polar_curves";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = PARAM_NUM_FOR_CHART_ONLY + MAX_NUMBER_OF_2D_CURVES_TO_PLOT * PARAM_NUM_IN_ONE_CURVE;
            mnMinParamNum = PARAM_NUM_FOR_CHART_ONLY + PARAM_NUM_IN_ONE_CURVE;
        }
    }
    //public final static Plot_polar_curvesFunction BUILTINFUNC_Plot_polar_curves = new Plot_polar_curvesFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Plot_polar_curvesFunction());}

    public static LinkedList<DataSeriesCurve> recalc2DExprDataSet(double dXAxisFrom, double dXAxisTo, double dYAxisFrom, double dYAxisTo, double d2DExprAxisFillRatio, TwoDExprCurve[] array2DExprCurves, TwoDExprDataCache[] array2DExprDataCaches) {
        // shouldn't fill the whole chart, so ugly.
        double dXFrom = (dXAxisTo + dXAxisFrom) / 2.0 - (dXAxisTo - dXAxisFrom) / 2.0 * d2DExprAxisFillRatio;
        double dXTo = (dXAxisTo + dXAxisFrom) / 2.0 + (dXAxisTo - dXAxisFrom) / 2.0 * d2DExprAxisFillRatio;
        double dYFrom = (dYAxisTo + dYAxisFrom) / 2.0 - (dYAxisTo - dYAxisFrom) / 2.0 * d2DExprAxisFillRatio;
        double dYTo = (dYAxisTo + dYAxisFrom) / 2.0 + (dYAxisTo - dYAxisFrom) / 2.0 * d2DExprAxisFillRatio;

        LinkedList<DataSeriesCurve> listCurves = new LinkedList<DataSeriesCurve>();
        double[][] arraydXCurves = new double[array2DExprCurves.length][];
        double[][] arraydYCurves = new double[array2DExprCurves.length][];
        double[] arraydXAvg = new double[array2DExprCurves.length];
        double[] arraydXSqrAvg = new double[array2DExprCurves.length];
        double[] arraydXStdev = new double[array2DExprCurves.length];
        double[] arraydYAvg = new double[array2DExprCurves.length];
        double[] arraydYSqrAvg = new double[array2DExprCurves.length];
        double[] arraydYStdev = new double[array2DExprCurves.length];
        AbstractExpr[] aearrayFuncVarExprs = new AbstractExpr[array2DExprCurves.length];
        for (int idx = 0; idx < array2DExprCurves.length; idx++) {
            int nNumofPntsInCurve = 0;
            int nNumOfSteps = array2DExprCurves[idx].mnNumOfSteps;
            nNumofPntsInCurve = nNumOfSteps + 1;    //nNumOfSteps 

            arraydXCurves[idx] = new double[nNumofPntsInCurve];
            arraydYCurves[idx] = new double[nNumofPntsInCurve];

            int idxXY = 0;
            double dSumX = 0, dSumXSqr = 0, dSumY = 0, dSumYSqr = 0;
            int nValidXPntNum = 0, nValidYPntNum = 0;
            double[] arraydInputVarValues = (array2DExprCurves[idx].mnFunctionVar == 0) ? arraydYCurves[idx] : arraydXCurves[idx];
            double[] arraydFuncValues = (array2DExprCurves[idx].mnFunctionVar == 0) ? arraydXCurves[idx] : arraydYCurves[idx];
            double dInputVarFrom = (array2DExprCurves[idx].mnFunctionVar == 0) ? dYFrom : dXFrom;
            double dInputVarTo = (array2DExprCurves[idx].mnFunctionVar == 0) ? dYTo : dXTo;
            String strInputVarExpr = (array2DExprCurves[idx].mnFunctionVar == 0) ? array2DExprCurves[idx].mstrYExpr : array2DExprCurves[idx].mstrXExpr;
            String strFuncVarExpr = (array2DExprCurves[idx].mnFunctionVar == 0) ? array2DExprCurves[idx].mstrXExpr : array2DExprCurves[idx].mstrYExpr;
            AbstractExpr aeFuncVarExpr = AEInvalid.AEINVALID;
            try {
                // functions in strFuncVarExpr has been converted to absolute CS, no need to add progContext.
                aeFuncVarExpr = ExprAnalyzer.analyseExpression(strFuncVarExpr, new CurPos());
            } catch (Exception e) {
            }
            aearrayFuncVarExprs[idx] = aeFuncVarExpr;
            double dStepLen = (dInputVarTo - dInputVarFrom) / (nNumofPntsInCurve - 1);
            int nStepLog2 = (int) Math.floor(Math.log(dStepLen) / Math.log(2.0));
            while (idxXY < nNumofPntsInCurve) {
                DataClass datumInput = new DataClassNull();
                MFPNumeric mfpVarValue = new MFPNumeric(dInputVarFrom + dStepLen * idxXY, true);
                try {
                    datumInput = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpVarValue);
                } catch (JFCALCExpErrException e1) {
                    // do nothing because it should not fail.
                }
                Variable varInput = new Variable(strInputVarExpr.trim().toLowerCase(Locale.US), datumInput);
                LinkedList<Variable> lVarnameSpace = new LinkedList<Variable>();
                lVarnameSpace.addFirst(varInput);
                ProgContext progContext = new ProgContext();
                progContext.mdynamicProgContext.mlVarNameSpaces = new LinkedList<LinkedList<Variable>>();
                progContext.mdynamicProgContext.mlVarNameSpaces.addFirst(lVarnameSpace);
                DataClass datumFuncValue = null;
                CacheNode cNode = array2DExprDataCaches[idx].calcFuncValue(mfpVarValue, nStepLog2, aeFuncVarExpr, varInput, progContext);
                arraydInputVarValues[idxXY] = cNode.mmfpVarValue.doubleValue();
                if (cNode.mdatumFuncValue != null) {
                    try {
                        datumFuncValue = cNode.mdatumFuncValue.cloneSelf();
                    } catch (Exception e) {
                        // do nothing here if the data cannot be cloned.
                    }
                }
                /*try {
                    datumFuncValue = aeFuncVarExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), lVarNameSpaces);
                } catch (Exception e) {
                    // do nothing here
                }*/
                if (datumFuncValue == null) {
                    arraydFuncValues[idxXY] = Double.NaN;
                } else {    // matrix or complex value. complex value may be changed to double if its image is 0.
                    try {
                        arraydFuncValues[idxXY] = DCHelper.lightCvtOrRetDCSingleNum(datumFuncValue).getDataValue().doubleValue();
                    } catch (Exception e) {
                        arraydFuncValues[idxXY] = Double.NaN;
                    }
                }
                if (!Double.isNaN(arraydXCurves[idx][idxXY]) && !Double.isInfinite(arraydXCurves[idx][idxXY])
                        && !Double.isNaN(arraydYCurves[idx][idxXY]) && !Double.isInfinite(arraydYCurves[idx][idxXY])) {
                    dSumX += arraydXCurves[idx][idxXY];
                    dSumXSqr += arraydXCurves[idx][idxXY] * arraydXCurves[idx][idxXY];
                    nValidXPntNum++;
                    dSumY += arraydYCurves[idx][idxXY];
                    dSumYSqr += arraydYCurves[idx][idxXY] * arraydYCurves[idx][idxXY];
                    nValidYPntNum++;
                }
                idxXY++;
            }
            if (nValidXPntNum > 0) {
                arraydXAvg[idx] = dSumX / nValidXPntNum;
                arraydXSqrAvg[idx] = dSumXSqr / nValidXPntNum;
                arraydXStdev[idx] = Math.sqrt(Math.max(0, arraydXSqrAvg[idx] - arraydXAvg[idx] * arraydXAvg[idx]));
            } else {
                arraydXAvg[idx] = arraydXSqrAvg[idx] = arraydXStdev[idx] = 0;
            }
            if (nValidYPntNum > 0) {
                arraydYAvg[idx] = dSumY / nValidYPntNum;
                arraydYSqrAvg[idx] = dSumYSqr / nValidXPntNum;
                arraydYStdev[idx] = Math.sqrt(Math.max(0, arraydYSqrAvg[idx] - arraydYAvg[idx] * arraydYAvg[idx]));
            } else {
                arraydYAvg[idx] = arraydYSqrAvg[idx] = arraydYStdev[idx] = 0;
            }
        }

        for (int idx = 0; idx < array2DExprCurves.length; idx++) {
            if (array2DExprCurves[idx].mbAutoStep) {
                double[] arraydInputVarValues = (array2DExprCurves[idx].mnFunctionVar == 0) ? arraydYCurves[idx] : arraydXCurves[idx];
                double[] arraydFuncValues = (array2DExprCurves[idx].mnFunctionVar == 0) ? arraydXCurves[idx] : arraydYCurves[idx];
                double dFuncValueAvg = (array2DExprCurves[idx].mnFunctionVar == 0) ? arraydXAvg[idx] : arraydYAvg[idx];
                double dFuncValueSqrAvg = (array2DExprCurves[idx].mnFunctionVar == 0) ? arraydXSqrAvg[idx] : arraydYSqrAvg[idx];
                double dFuncStdValue = (array2DExprCurves[idx].mnFunctionVar == 0) ? arraydXStdev[idx] : arraydYStdev[idx];
                double dInputVarFrom = (array2DExprCurves[idx].mnFunctionVar == 0) ? dYFrom : dXFrom;
                double dInputVarTo = (array2DExprCurves[idx].mnFunctionVar == 0) ? dYTo : dXTo;
                String strInputVarExpr = (array2DExprCurves[idx].mnFunctionVar == 0) ? array2DExprCurves[idx].mstrYExpr : array2DExprCurves[idx].mstrXExpr;
                String strFuncVarExpr = (array2DExprCurves[idx].mnFunctionVar == 0) ? array2DExprCurves[idx].mstrXExpr : array2DExprCurves[idx].mstrYExpr;
                AbstractExpr aeFuncVarExpr = aearrayFuncVarExprs[idx];
                LinkedList<Double> listdblX = new LinkedList<Double>();
                LinkedList<Double> listdblY = new LinkedList<Double>();
                int idxXY = 0;
                double dStepLen = (dInputVarTo - dInputVarFrom) / 2 / array2DExprCurves[idx].mnNumOfSteps;
                int nStepLog2 = (int) Math.floor(Math.log(dStepLen) / Math.log(2.0));
                while (idxXY < arraydInputVarValues.length) {
                    listdblX.add(arraydXCurves[idx][idxXY]);
                    listdblY.add(arraydYCurves[idx][idxXY]);
                    double dInputVarCurrent = (array2DExprCurves[idx].mnFunctionVar == 0) ? arraydYCurves[idx][idxXY] : arraydXCurves[idx][idxXY];
                    if (idxXY == arraydInputVarValues.length - 1) {
                        break;
                    }
                    double dInputVarNext = (array2DExprCurves[idx].mnFunctionVar == 0) ? arraydYCurves[idx][idxXY + 1] : arraydXCurves[idx][idxXY + 1];
                    DataClass datumInput = new DataClassNull();
                    double dThisInputValue = (dInputVarCurrent + dInputVarNext) / 2.0;
                    MFPNumeric mfpVarValue = new MFPNumeric(dThisInputValue, true);
                    try {
                        datumInput = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpVarValue);
                    } catch (JFCALCExpErrException e1) {
                        // do nothing because it should not fail
                    }
                    Variable varInput = new Variable(strInputVarExpr.trim().toLowerCase(Locale.US), datumInput);
                    LinkedList<Variable> lVarnameSpace = new LinkedList<Variable>();
                    lVarnameSpace.addFirst(varInput);
                    ProgContext progContext = new ProgContext();
                    progContext.mdynamicProgContext.mlVarNameSpaces = new LinkedList<LinkedList<Variable>>();
                    progContext.mdynamicProgContext.mlVarNameSpaces.addFirst(lVarnameSpace);
                    DataClass datumFuncValue = null;
                    CacheNode cNode = array2DExprDataCaches[idx].calcFuncValue(mfpVarValue, nStepLog2, aeFuncVarExpr, varInput, progContext);
                    dThisInputValue = cNode.mmfpVarValue.doubleValue();
                    if (cNode.mdatumFuncValue != null) {
                        try {
                            datumFuncValue = cNode.mdatumFuncValue.cloneSelf();
                        } catch (Exception e) {
                            // do nothing here if the data cannot be cloned.
                        }
                    }
                    /*try {
                        datumFuncValue = aeFuncVarExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), lVarNameSpaces);
                    } catch (Exception e) {
                        // do nothing here
                    }*/

                    double dThisFuncValue = Double.NaN;
                    if (datumFuncValue == null) {
                        dThisFuncValue = Double.NaN;
                    } else {    // matrix or complex value. Complex value can be converted to double if image is 0.
                        try {
                            dThisFuncValue = DCHelper.lightCvtOrRetDCSingleNum(datumFuncValue).getDataValue().doubleValue();
                        } catch (Exception e) {
                            dThisFuncValue = Double.NaN;
                        }
                    }

                    if (!Double.isNaN(arraydFuncValues[idxXY]) && !Double.isInfinite(arraydFuncValues[idxXY])
                            && !Double.isNaN(arraydFuncValues[idxXY + 1]) && !Double.isInfinite(arraydFuncValues[idxXY + 1])
                            && !Double.isNaN(dThisFuncValue) && !Double.isInfinite(dThisFuncValue)) {
                        double dGapTolerance = Math.max(Math.max(dFuncStdValue, Math.abs(dThisFuncValue - dFuncValueAvg)),
                                Math.max(Math.abs(arraydFuncValues[idxXY + 1] - dFuncValueAvg), Math.abs(arraydFuncValues[idxXY] - dFuncValueAvg)));
                        if (Math.abs((arraydFuncValues[idxXY + 1]) + (arraydFuncValues[idxXY]) - 2 * (dThisFuncValue))
                                > 1.0 * dGapTolerance) {
                            // abs(left + right - 2 * mid) > relative confidence interval, it seems to be a singular point.
                            dThisFuncValue = (arraydFuncValues[idxXY + 1] + arraydFuncValues[idxXY] > 2 * dThisFuncValue)
                                    ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                        }
                    }
                    listdblX.add((array2DExprCurves[idx].mnFunctionVar == 0) ? dThisFuncValue : dThisInputValue);
                    listdblY.add((array2DExprCurves[idx].mnFunctionVar == 1) ? dThisFuncValue : dThisInputValue);

                    idxXY++;
                }

                arraydXCurves[idx] = new double[listdblX.size()];
                arraydYCurves[idx] = new double[listdblY.size()];
                for (idxXY = 0; idxXY < listdblX.size(); idxXY++) {
                    arraydXCurves[idx][idxXY] = listdblX.get(idxXY);
                    arraydYCurves[idx][idxXY] = listdblY.get(idxXY);
                }
            }
            DataSeriesCurve dataCurve = new DataSeriesCurve();
            dataCurve.mstrName = array2DExprCurves[idx].mstrCurveTitle;
            dataCurve.mpointStyle = new PointStyle();
            dataCurve.mpointStyle.mclr = XYChartOperator.cvtStr2VMFPColor(array2DExprCurves[idx].mstrPntColor.trim().equals("")
                    ? (array2DExprCurves[idx].mstrLnColor.trim().equals("") ? "white" : array2DExprCurves[idx].mstrLnColor)
                    : array2DExprCurves[idx].mstrPntColor);
            dataCurve.mpointStyle.menumPointShape = XYChartOperator.cvtStr2PntShape(array2DExprCurves[idx].mstrPntStyle);
            dataCurve.mpointStyle.mdSize = (array2DExprCurves[idx].mnPntSize < 0) ? 0 : array2DExprCurves[idx].mnPntSize;
            dataCurve.mlineStyle = new LineStyle();
            dataCurve.mlineStyle.mclr = XYChartOperator.cvtStr2VMFPColor(array2DExprCurves[idx].mstrLnColor.trim().equals("")
                    ? "white" : array2DExprCurves[idx].mstrLnColor);
            dataCurve.mlineStyle.menumLinePattern = (array2DExprCurves[idx].mnLnSize <= 0) ? LINEPATTERN.LINEPATTERN_NON : LINEPATTERN.LINEPATTERN_SOLID;
            dataCurve.mlineStyle.mdLineWidth = (array2DExprCurves[idx].mnLnSize < 0) ? 0 : array2DExprCurves[idx].mnLnSize;

            if (array2DExprCurves[idx].mstrCurveTitle == null || array2DExprCurves[idx].mstrCurveTitle.length() == 0
                    || (idx < array2DExprCurves.length - 1
                    && (array2DExprCurves[idx + 1].mstrCurveTitle == null || array2DExprCurves[idx + 1].mstrCurveTitle.length() == 0))) {
                // only if we need to plot implicit functions we extend plotting.
                for (int idx1 = 0; idx1 < arraydXCurves[idx].length; idx1++) {
                    if (idx1 > 0) {
                        int nNeedFurtherCalc = 0;
                        double dFuncVarLast = (array2DExprCurves[idx].mnFunctionVar == 0) ? arraydXCurves[idx][idx1 - 1] : arraydYCurves[idx][idx1 - 1];
                        double dFuncVarCurrent = (array2DExprCurves[idx].mnFunctionVar == 0) ? arraydXCurves[idx][idx1] : arraydYCurves[idx][idx1];
                        if (Double.isNaN(dFuncVarLast) && !Double.isNaN(dFuncVarCurrent)) {
                            nNeedFurtherCalc = -1;
                        } else if (Double.isNaN(dFuncVarCurrent) && !Double.isNaN(dFuncVarLast)) {
                            nNeedFurtherCalc = 1;
                        }

                        if (nNeedFurtherCalc != 0) {
                            // one function value is nan, one is not nan
                            LinkedList<Position3D> list2BeAddedPnts = new LinkedList<Position3D>();
                            if (nNeedFurtherCalc == -1) {
                                list2BeAddedPnts.add(new Position3D(arraydXCurves[idx][idx1], arraydYCurves[idx][idx1]));
                            } else {
                                list2BeAddedPnts.add(new Position3D(arraydXCurves[idx][idx1 - 1], arraydYCurves[idx][idx1 - 1]));
                            }

                            double dInputVarLast = (array2DExprCurves[idx].mnFunctionVar == 0) ? arraydYCurves[idx][idx1 - 1] : arraydXCurves[idx][idx1 - 1];
                            double dInputVarCurrent = (array2DExprCurves[idx].mnFunctionVar == 0) ? arraydYCurves[idx][idx1] : arraydXCurves[idx][idx1];
                            String strInputVarExpr = (array2DExprCurves[idx].mnFunctionVar == 0) ? array2DExprCurves[idx].mstrYExpr : array2DExprCurves[idx].mstrXExpr;
                            double dFuncAxisRange = (array2DExprCurves[idx].mnFunctionVar == 0) ? Math.abs(dXTo - dXFrom) : Math.abs(dYTo - dYFrom);
                            AbstractExpr aeFuncVarExpr = aearrayFuncVarExprs[idx];
                            for (int idxTmp = 0; idxTmp < MAX_NUMBER_OF_CALIBRATION_FOR_1ST_DRAWN_PNT; idxTmp++) {   // we calculate at most eight times.
                                if (list2BeAddedPnts.size() > 1) {
                                    double dFuncVarChange = (array2DExprCurves[idx].mnFunctionVar == 0)
                                            ? Math.abs(list2BeAddedPnts.getFirst().getX() - list2BeAddedPnts.get(1).getX())
                                            : Math.abs(list2BeAddedPnts.getFirst().getY() - list2BeAddedPnts.get(1).getY());

                                    if (dFuncVarChange <= dFuncAxisRange * EYE_IDENTIFIABLE_LENGTH_RATIO) {
                                        break;
                                    }
                                }
                                if (list2BeAddedPnts.size() > MAX_VALID_CALCULATED_PNTS_FOR_1ST_DRAWN_PNT) {
                                    break;  // we have calculated four extra points, we will not do more.
                                }
                                double dStepLen = (dInputVarCurrent - dInputVarLast)/2.0;
                                int nStepLog2 = (int) Math.floor(Math.log(dStepLen) / Math.log(2.0));
                                double dThisInputValue = (dInputVarLast + dInputVarCurrent) / 2.0;
                                MFPNumeric mfpVarValue = new MFPNumeric(dThisInputValue, true);
                                DataClass datumInput = new DataClassNull();
                                try {
                                    datumInput = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, mfpVarValue);
                                } catch (JFCALCExpErrException e1) {
                                    // do nothing because it should not fail
                                }
                                Variable varInput = new Variable(strInputVarExpr.trim().toLowerCase(Locale.US), datumInput);
                                LinkedList<Variable> lVarnameSpace = new LinkedList<Variable>();
                                lVarnameSpace.addFirst(varInput);
                                ProgContext progContext = new ProgContext();
                                progContext.mdynamicProgContext.mlVarNameSpaces = new LinkedList<LinkedList<Variable>>();
                                progContext.mdynamicProgContext.mlVarNameSpaces.addFirst(lVarnameSpace);
                                DataClass datumFuncValue = null;
                                Double dThisFuncValue = Double.NaN;
                                CacheNode cNode = array2DExprDataCaches[idx].calcFuncValue(mfpVarValue, nStepLog2, aeFuncVarExpr, varInput, progContext);
                                dThisInputValue = cNode.mmfpVarValue.doubleValue();
                                if (cNode.mdatumFuncValue != null) {
                                    try {
                                        datumFuncValue = cNode.mdatumFuncValue.cloneSelf();
                                        if (datumFuncValue != null) {
                                            dThisFuncValue = DCHelper.lightCvtOrRetDCSingleNum(datumFuncValue).getDataValue().doubleValue();
                                        }
                                    } catch (Exception e) {
                                        // do nothing here if the data cannot be cloned.
                                    }
                                }
                                /*try {
                                    datumFuncValue = aeFuncVarExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), lVarNameSpaces);
                                    if (datumFuncValue != null) {
                                        datumFuncValue.changeDataType(DATATYPES.DATUM_MFPDEC);
                                        dThisFuncValue = datumFuncValue.getDataValue().doubleValue();
                                    }
                                } catch (Exception e) {
                                    // do nothing here
                                }*/

                                if (Double.isNaN(dThisFuncValue)) {
                                    // discard this point
                                    if (nNeedFurtherCalc == -1) {
                                        dInputVarLast = dThisInputValue;
                                    } else {
                                        dInputVarCurrent = dThisInputValue;
                                    }
                                } else {
                                    // add this point.
                                    Position3D pnt = (array2DExprCurves[idx].mnFunctionVar == 0)
                                            ? new Position3D(dThisFuncValue, dThisInputValue)
                                            : new Position3D(dThisInputValue, dThisFuncValue);
                                    if (nNeedFurtherCalc == 1) {
                                        dInputVarLast = dThisInputValue;
                                        dFuncVarLast = dThisFuncValue;
                                        list2BeAddedPnts.add(pnt);
                                    } else {
                                        dInputVarCurrent = dThisInputValue;
                                        dFuncVarCurrent = dThisFuncValue;
                                        list2BeAddedPnts.addFirst(pnt);
                                    }
                                }
                            }
                            for (Position3D pnt : list2BeAddedPnts) {
                                dataCurve.add(pnt);
                            }
                        }
                    }
                    dataCurve.add(new Position3D(arraydXCurves[idx][idx1], arraydYCurves[idx][idx1]));
                }
            } else {
                for (int idx1 = 0; idx1 < arraydXCurves[idx].length; idx1++) {
                    dataCurve.add(new Position3D(arraydXCurves[idx][idx1], arraydYCurves[idx][idx1]));
                }
            }
            listCurves.add(dataCurve);
        }
        return listCurves;
    }

    public static class Plot2DExprCurvesFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public static final int PARAM_NUM_FOR_CHART_ONLY = 10;
        public static final int PARAM_NUM_IN_ONE_CURVE = 11;
        public Plot2DExprCurvesFunction() {
        }
        // this function read expression parameters (i.e. x and y values have to be calculated from expression).
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {

            String strCallingFunc = mstrarrayFullCS[mstrarrayFullCS.length - 1];
        
            String strChartType = "2DExpr";
            if (strCallingFunc.equals("plot_polar_expressions")) {
                strChartType = "polarExpr";
            }
            
            Plot2DExprCurves(strCallingFunc, strChartType, listParams, progContext);
            return null;
        }
        
        // note that strCallingFunc only includes small letter.
        public static void Plot2DExprCurves(String strCallingFunc, String strChartType, LinkedList<DataClass> listParams, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            // Parameters:
            // 1. chart name (i.e. chart file name); 2. chart title; 3. X axis title; 4. X axis from; 5. X axis to;
            // 6. Y axis title; 7. Y axis from; 8. Y axis to; 9. chart background color; 10. show grid or not;
            // 11. curve title; 12. point color; 13. point style; 14. point size; 15. line color;
            // 16. line style; 17. line size; 18. function variable (0 means x, 1 means y);
            // 19. x variable expression; 20. y variable expression; 21. number of steps; ...
            // ... Note that every new curve needs additional 11 parameters (i.e. parameters 11 to 21). At most 8 curves can be included.
            // and function variable's number of steps is ignored.
            int nParamNumForChartOnly = PARAM_NUM_FOR_CHART_ONLY;
            int nParamNumIn1Cv = PARAM_NUM_IN_ONE_CURVE;
            int nMaxNumofCvs = MAX_NUMBER_OF_2D_CURVES_TO_PLOT;
            
            if (listParams.size() > (nParamNumForChartOnly + nMaxNumofCvs * nParamNumIn1Cv)
                    || listParams.size() < (nParamNumForChartOnly + nParamNumIn1Cv) // should be at least one curve, at most 8 curves.
                    || nParamNumIn1Cv * (int) (((double) (listParams.size() - nParamNumForChartOnly)) / nParamNumIn1Cv) != (listParams.size() - nParamNumForChartOnly)) {
                // The first nParamNumForChartOnly params are for chart definition.
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            if (strChartType == null || !strChartType.equalsIgnoreCase("polarExpr")) {
                strChartType = "2DExpr";
            } else {
                strChartType = "polarExpr";
            }
            
            int nNumofCurves = (int) ((listParams.size() - nParamNumForChartOnly) / nParamNumIn1Cv);
            int nDefaultNumOfSteps = DEFAULT_NUMBER_OF_STEPS_IN_ONE_CURVE;    // was 128;

            // if convert data to non-array type, no deep copy required.
            DataClassString datumChartName = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strChartName = datumChartName.getStringValue();

            DataClassString datumChartTitle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strChartTitle = datumChartTitle.getStringValue();

            DataClassString datumXTitle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strXTitle = datumXTitle.getStringValue();

            DataClassSingleNum datumXFrom = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
            double dXFrom = datumXFrom.getDataValue().doubleValue();

            DataClassSingleNum datumXTo = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
            double dXTo = datumXTo.getDataValue().doubleValue();

            DataClassString datumYTitle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strYTitle = datumYTitle.getStringValue();

            DataClassSingleNum datumYFrom = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
            double dYFrom = datumYFrom.getDataValue().doubleValue();

            DataClassSingleNum datumYTo = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
            double dYTo = datumYTo.getDataValue().doubleValue();

            DataClassString datumChartBKColor = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strChartBKColor = datumChartBKColor.getStringValue();

            DataClassString datumShowGrid = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strShowGrid = datumShowGrid.getStringValue();

            if (dXFrom >= dXTo || dYFrom >= dYTo) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }

            TwoDExprCurve[] twoDExprCurves = new TwoDExprCurve[nNumofCurves];
            String strOutput = "";

            for (int idx = 0; idx < nNumofCurves; idx++) {
                twoDExprCurves[idx] = new TwoDExprCurve();

                DataClassString datumCurveTitle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                twoDExprCurves[idx].mstrCurveTitle = datumCurveTitle.getStringValue();

                DataClassString datumPntColor = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                twoDExprCurves[idx].mstrPntColor = datumPntColor.getStringValue();

                DataClassString datumPntStyle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                twoDExprCurves[idx].mstrPntStyle = datumPntStyle.getStringValue();

                DataClassSingleNum datumPntSize = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
                twoDExprCurves[idx].mnPntSize = (int) datumPntSize.getDataValue().longValue();   // point size shouldn't be too large

                DataClassString datumLnColor = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                twoDExprCurves[idx].mstrLnColor = datumLnColor.getStringValue();

                DataClassString datumLnStyle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                twoDExprCurves[idx].mstrLnStyle = datumLnStyle.getStringValue();

                DataClassSingleNum datumLnSize = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
                twoDExprCurves[idx].mnLnSize = (int) datumLnSize.getDataValue().longValue(); // line size shouldn't be too large.

                DataClassSingleNum datumFunctionVar = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
                twoDExprCurves[idx].mnFunctionVar = (int) datumFunctionVar.getDataValue().longValue();
                if (twoDExprCurves[idx].mnFunctionVar < 0 || twoDExprCurves[idx].mnFunctionVar > 1) {
                    twoDExprCurves[idx].mnFunctionVar = 1;    // y by default.
                }

                DataClassString datumXExpr = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                twoDExprCurves[idx].mstrXExpr = datumXExpr.getStringValue();
                try {
                    // convert relative CS to absolute CS.
                    AbstractExpr aeExpr = ExprAnalyzer.analyseExpression(twoDExprCurves[idx].mstrXExpr, new CurPos(), new LinkedList<Variable>(), progContext);
                    twoDExprCurves[idx].mstrXExpr = aeExpr.output();
                } catch (JSmartMathErrException ex) {
                    
                }
                
                DataClassString datumYExpr = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                twoDExprCurves[idx].mstrYExpr = datumYExpr.getStringValue();
                try {
                    // convert relative CS to absolute CS.
                    AbstractExpr aeExpr = ExprAnalyzer.analyseExpression(twoDExprCurves[idx].mstrYExpr, new CurPos(), new LinkedList<Variable>(), progContext);
                    twoDExprCurves[idx].mstrYExpr = aeExpr.output();
                } catch (JSmartMathErrException ex) {
                    
                }

                DataClassSingleNum datumNumOfSteps = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
                twoDExprCurves[idx].mnNumOfSteps = (int) datumNumOfSteps.getDataValue().longValue();
                if (twoDExprCurves[idx].mnNumOfSteps <= 0) {
                    twoDExprCurves[idx].mnNumOfSteps = nDefaultNumOfSteps;
                    twoDExprCurves[idx].mbAutoStep = true;
                }

                int nNumOfPointsIn1Curve = 0;
                if (twoDExprCurves[idx].mnFunctionVar == 0) {
                    if (!Variable.isValidVarNameWithNoCS(twoDExprCurves[idx].mstrYExpr)) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                    }
                } else {    // mnFunctionVar == 1
                    if (!Variable.isValidVarNameWithNoCS(twoDExprCurves[idx].mstrXExpr)) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                    }
                }
                nNumOfPointsIn1Curve = twoDExprCurves[idx].mnNumOfSteps + 1;

                if (nNumOfPointsIn1Curve > MAX_NUMBER_OF_PNTS_IN_ONE_CURVE) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_TOO_MANY_POINTS_TO_PLOT_IN_A_CURVE);
                }
                String strCurveSetting = "curve_label:" + ChartOperator.addEscapes(twoDExprCurves[idx].mstrCurveTitle)
                        + ";point_color:" + ChartOperator.addEscapes(twoDExprCurves[idx].mstrPntColor) + ";point_style:" + ChartOperator.addEscapes(twoDExprCurves[idx].mstrPntStyle) + ";point_size:" + twoDExprCurves[idx].mnPntSize
                        + ";line_color:" + ChartOperator.addEscapes(twoDExprCurves[idx].mstrLnColor) + ";line_style:" + ChartOperator.addEscapes(twoDExprCurves[idx].mstrLnStyle) + ";line_size:" + twoDExprCurves[idx].mnLnSize
                        + ";function_variable:" + twoDExprCurves[idx].mnFunctionVar
                        + ";x_expr:" + ChartOperator.addEscapes(twoDExprCurves[idx].mstrXExpr) + ";y_expr:" + ChartOperator.addEscapes(twoDExprCurves[idx].mstrYExpr)
                        + ";number_of_steps:" + twoDExprCurves[idx].mnNumOfSteps + ";auto_step:" + twoDExprCurves[idx].mbAutoStep;
                strOutput += "\n" + strCurveSetting;
            }
            // shouldn't fill the whole chart, so ugly.
            double dXAxisFrom = (dXTo + dXFrom) / 2.0 - (dXTo - dXFrom) / 2.0 / TWOD_EXPR_CHART_AXIS_FILL_RATIO;
            double dXAxisTo = (dXTo + dXFrom) / 2.0 + (dXTo - dXFrom) / 2.0 / TWOD_EXPR_CHART_AXIS_FILL_RATIO;
            double dYAxisFrom = dYFrom;
            double dYAxisTo = dYTo;
            
            if (strChartType.equalsIgnoreCase("2DExpr")) {
                // ADJUST YAxisFrom and YAxisTo only if it is a 2DExpr chart. Do not do this for polar chart.
                dYAxisFrom = (dYTo + dYFrom) / 2.0 - (dYTo - dYFrom) / 2.0 / TWOD_EXPR_CHART_AXIS_FILL_RATIO;
                dYAxisTo = (dYTo + dYFrom) / 2.0 + (dYTo - dYFrom) / 2.0 / TWOD_EXPR_CHART_AXIS_FILL_RATIO;
            }
            String strChartSettings = "chart_type:" + strChartType + ";chart_title:" + ChartOperator.addEscapes(strChartTitle)
                    + ";x_title:" + ChartOperator.addEscapes(strXTitle) + ";x_min:" + dXAxisFrom + ";x_max:" + dXAxisTo + ";x_labels:10"
                    + ";y_title:" + ChartOperator.addEscapes(strYTitle) + ";y_min:" + dYAxisFrom + ";y_max:" + dYAxisTo + ";y_labels:10"
                    + ";background_color:" + ChartOperator.addEscapes(strChartBKColor) + ";show_grid:" + ChartOperator.addEscapes(strShowGrid);
            strOutput = "chart_name:" + strChartName + ";" + strChartSettings + ";number_of_curves:" + nNumofCurves + strOutput;

            // plot the graph
            if (FuncEvaluator.msgraphPlotter != null) {
                try {
                    if (!FuncEvaluator.msgraphPlotter.plotGraph(strOutput)) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_PLOT_CHART);
                    }
                } catch (UnsatisfiedLinkError e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_PLOT_OGL_CHART_LACK_SYSTEM_SUPPORT_FILES);
                }
            }

            // save the graph to disk
            if (FuncEvaluator.msfileOperator != null && strChartName.trim().length() > 0) {
                try {
                    FuncEvaluator.msfileOperator.outputGraphFile(strChartName, strOutput);
                } catch (FileNotFoundException e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_FUNCTION_PROCESSION,
                            strCallingFunc, e);
                } catch (IOException e) {
                    if (e instanceof ClosedByInterruptException) {
                        throw new InterruptedException();    // this thread is interrupted by another thread.
                    } else {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_FUNCTION_PROCESSION,
                                strCallingFunc, e);
                    }
                }
            }
        }
    }

    public static class Plot_2d_expressionsFunction extends Plot2DExprCurvesFunction {
		private static final long serialVersionUID = 1L;

        public Plot_2d_expressionsFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::plot_math::plot_2d_expressions";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = PARAM_NUM_FOR_CHART_ONLY + MAX_NUMBER_OF_2D_CURVES_TO_PLOT * PARAM_NUM_IN_ONE_CURVE;
            mnMinParamNum = PARAM_NUM_FOR_CHART_ONLY + PARAM_NUM_IN_ONE_CURVE;
        }
    }
    //public final static Plot_2d_expressionsFunction BUILTINFUNC_Plot_2d_expressions = new Plot_2d_expressionsFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Plot_2d_expressionsFunction());}

    public static class Plot_polar_expressionsFunction extends Plot2DExprCurvesFunction {
		private static final long serialVersionUID = 1L;

        public Plot_polar_expressionsFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::plot_math::plot_polar_expressions";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = PARAM_NUM_FOR_CHART_ONLY + MAX_NUMBER_OF_2D_CURVES_TO_PLOT * PARAM_NUM_IN_ONE_CURVE;
            mnMinParamNum = PARAM_NUM_FOR_CHART_ONLY + PARAM_NUM_IN_ONE_CURVE;
        }
    }
    //public final static Plot_polar_expressionsFunction BUILTINFUNC_Plot_polar_expressions = new Plot_polar_expressionsFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Plot_polar_expressionsFunction());}

    public static class Plot_3d_surfacesFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public static final int PARAM_NUM_FOR_CHART_ONLY = 5;
        public static final int PARAM_NUM_IN_ONE_CURVE = 19;
        public Plot_3d_surfacesFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::plot_math::plot_3d_surfaces";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = PARAM_NUM_FOR_CHART_ONLY + MAX_NUMBER_OF_3D_SURFACES_TO_PLOT * PARAM_NUM_IN_ONE_CURVE;
            mnMinParamNum = PARAM_NUM_FOR_CHART_ONLY + PARAM_NUM_IN_ONE_CURVE;
        }

        // this function read expression parameters (i.e. x, y and z values have to be calculated from expression). different from
        // function plot2DCurves, this function does not detect singlar point.
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            // Parameters:
            // 1. chart name (i.e. chart file name); 2. chart title; 3. X axis title; 4. Y axis title; 5.  Z axis title;
            // 6. curve title; 7. grid or not; 8. min color (front side); 9. min color (back side); 10. z value corresponding to min color;
            // 11. max color (front side); 12. max color (back side); 13. z value corresponding to max color; 14. u variable name; 15. u values start from;
            // 16. u values end at; 17. u values' interval; 18. v variable name; 19. v values start from; 20. v values end at; 21. v values' interval;
            // 22. X's expression (based on variables u and v); 23. Y's expression (based on variables u and v); 24. Z's expression (based on variables u and v);
            // ... Note that every new curve needs additional 19 parameters (i.e. parameters 6 to 24). At most 4 curves can be included.
            int nParamNumForChartOnly = PARAM_NUM_FOR_CHART_ONLY;
            int nParamNumIn1Cv = PARAM_NUM_IN_ONE_CURVE;
            int nMaxNumofCvs = MAX_NUMBER_OF_3D_SURFACES_TO_PLOT;

            String strCallingFunc = mstrarrayFullCS[mstrarrayFullCS.length - 1];

            if (listParams.size() > (nParamNumForChartOnly + nMaxNumofCvs * nParamNumIn1Cv)
                    || listParams.size() < (nParamNumForChartOnly + nParamNumIn1Cv) // should be at least one curve, at most 4 curves.
                    || nParamNumIn1Cv * (int) (((double) (listParams.size() - nParamNumForChartOnly)) / nParamNumIn1Cv) != (listParams.size() - nParamNumForChartOnly)) {
                // The first nParamNumForChartOnly params are for chart definition.
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            int nNumofCurves = (int) ((listParams.size() - nParamNumForChartOnly) / nParamNumIn1Cv);
            int nDefaultNumOfPoints = ((int) Math.sqrt(DEFAULT_NUMBER_OF_STEPS_SQR_IN_ONE_SURFACE) + 1) * ((int) Math.sqrt(DEFAULT_NUMBER_OF_STEPS_SQR_IN_ONE_SURFACE) + 1);    // was 256;

            DataClassString datumChartName = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strChartName = datumChartName.getStringValue();

            DataClassString datumChartTitle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strChartTitle = datumChartTitle.getStringValue();

            DataClassString datumXTitle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strXTitle = datumXTitle.getStringValue();

            DataClassString datumYTitle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strYTitle = datumYTitle.getStringValue();

            DataClassString datumZTitle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strZTitle = datumZTitle.getStringValue();

            ThreeDSurface[] threeDSurfaces = new ThreeDSurface[nNumofCurves];
            double[][] arraydUCurves = new double[nNumofCurves][0];
            double[][] arraydVCurves = new double[nNumofCurves][0];
            double[][][] arraydXCurves = new double[nNumofCurves][0][0];
            double[][][] arraydYCurves = new double[nNumofCurves][0][0];
            double[][][] arraydZCurves = new double[nNumofCurves][0][0];
            double[] arraydXAvg = new double[nNumofCurves];
            double[] arraydXStdev = new double[nNumofCurves];
            double[] arraydYAvg = new double[nNumofCurves];
            double[] arraydYStdev = new double[nNumofCurves];
            double[] arraydZAvg = new double[nNumofCurves];
            double[] arraydZStdev = new double[nNumofCurves];

            String[] strarrayCurveSettings = new String[nNumofCurves];
            String[] strarrayXValues = new String[nNumofCurves];
            String[] strarrayYValues = new String[nNumofCurves];
            String[] strarrayZValues = new String[nNumofCurves];
            double dXMin = 0, dXMax = 0, dYMin = 0, dYMax = 0, dZMin = 0, dZMax = 0;
            String strOutput = "";

            int nTotalNumValidPnts = 0;
            for (int idx = 0; idx < nNumofCurves; idx++) {
                threeDSurfaces[idx] = new ThreeDSurface();

                strarrayCurveSettings[idx] = "";
                strarrayXValues[idx] = "";
                strarrayYValues[idx] = "";
                strarrayZValues[idx] = "";

                DataClassString datumCurveTitle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                threeDSurfaces[idx].mstrCurveTitle = datumCurveTitle.getStringValue();

                DataClassSingleNum datumIsGrid = DCHelper.lightCvtOrRetDCMFPBool(listParams.removeLast());
                threeDSurfaces[idx].mbIsGrid = (datumIsGrid.getDataValue().longValue() == 0) ? false : true;

                DataClassString datumMinColor = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                threeDSurfaces[idx].mstrMinColor = datumMinColor.getStringValue();

                DataClassString datumMinColor1 = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                threeDSurfaces[idx].mstrMinColor1 = datumMinColor1.getStringValue();

                DataClass datumMinColorValue = listParams.removeLast().cloneSelf();
                if (!datumMinColorValue.isNull()) {
                    threeDSurfaces[idx].mdMinColorValue = DCHelper.lightCvtOrRetDCMFPDec(datumMinColorValue).getDataValue().doubleValue();
                } else {
                    threeDSurfaces[idx].mdMinColorValue = null;
                }

                DataClassString datumMaxColor = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                threeDSurfaces[idx].mstrMaxColor = datumMaxColor.getStringValue();

                DataClassString datumMaxColor1 = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                threeDSurfaces[idx].mstrMaxColor1 = datumMaxColor1.getStringValue();

                DataClass datumMaxColorValue = listParams.removeLast().cloneSelf();
                if (!datumMaxColorValue.isNull()) {
                    threeDSurfaces[idx].mdMaxColorValue = DCHelper.lightCvtOrRetDCMFPDec(datumMaxColorValue).getDataValue().doubleValue();
                } else {
                    threeDSurfaces[idx].mdMaxColorValue = null;
                }

                DataClassString datumUName = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                threeDSurfaces[idx].mstrUName = datumUName.getStringValue().trim().toLowerCase(Locale.US);

                DataClassSingleNum datumUFrom = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
                threeDSurfaces[idx].mdUFrom = datumUFrom.getDataValue().doubleValue();

                DataClassSingleNum datumUTo = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
                threeDSurfaces[idx].mdUTo = datumUTo.getDataValue().doubleValue();

                DataClassSingleNum datumUStep = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
                threeDSurfaces[idx].mdUStep = datumUStep.getDataValue().doubleValue();

                DataClassString datumVName = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                threeDSurfaces[idx].mstrVName = datumVName.getStringValue().trim().toLowerCase(Locale.US);

                DataClassSingleNum datumVFrom = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
                threeDSurfaces[idx].mdVFrom = datumVFrom.getDataValue().doubleValue();

                DataClassSingleNum datumVTo = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
                threeDSurfaces[idx].mdVTo = datumVTo.getDataValue().doubleValue();

                DataClassSingleNum datumVStep = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
                threeDSurfaces[idx].mdVStep = datumVStep.getDataValue().doubleValue();

                DataClassString datumXExpr = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                threeDSurfaces[idx].mstrXExpr = datumXExpr.getStringValue();
                AbstractExpr aeXExpr = null;
                try {
                    aeXExpr = ExprAnalyzer.analyseExpression(threeDSurfaces[idx].mstrXExpr, new CurPos(), new LinkedList<Variable>(), progContext);
                    threeDSurfaces[idx].mstrXExpr = aeXExpr.output();   // convert relative CS to absolute CS.
                } catch (Exception e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                }

                DataClassString datumYExpr = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                threeDSurfaces[idx].mstrYExpr = datumYExpr.getStringValue();
                AbstractExpr aeYExpr = null;
                try {
                    aeYExpr = ExprAnalyzer.analyseExpression(threeDSurfaces[idx].mstrYExpr, new CurPos(), new LinkedList<Variable>(), progContext);
                    threeDSurfaces[idx].mstrYExpr = aeYExpr.output();   // convert relative CS to absolute CS.
                } catch (Exception e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                }

                DataClassString datumZExpr = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                threeDSurfaces[idx].mstrZExpr = datumZExpr.getStringValue();
                AbstractExpr aeZExpr = null;
                try {
                    aeZExpr = ExprAnalyzer.analyseExpression(threeDSurfaces[idx].mstrZExpr, new CurPos(), new LinkedList<Variable>(), progContext);
                    threeDSurfaces[idx].mstrZExpr = aeZExpr.output();   // convert relative CS to absolute CS.
                } catch (Exception e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                }

                strarrayCurveSettings[idx] = "curve_label:" + ChartOperator.addEscapes(threeDSurfaces[idx].mstrCurveTitle)
                        + ";is_grid:" + threeDSurfaces[idx].mbIsGrid.toString()
                        + ";min_color:" + ChartOperator.addEscapes(threeDSurfaces[idx].mstrMinColor) + ";min_color_1:" + ChartOperator.addEscapes(threeDSurfaces[idx].mstrMinColor1)
                        + ((threeDSurfaces[idx].mdMinColorValue != null) ? (";min_color_value:" + threeDSurfaces[idx].mdMinColorValue) : "")
                        + ";max_color:" + ChartOperator.addEscapes(threeDSurfaces[idx].mstrMaxColor) + ";max_color_1:" + ChartOperator.addEscapes(threeDSurfaces[idx].mstrMaxColor1)
                        + ((threeDSurfaces[idx].mdMaxColorValue != null) ? (";max_color_value:" + threeDSurfaces[idx].mdMaxColorValue) : "");

                int nNumofPntsInCurve = 0, nNumofPntsInU = 0, nNumofPntsInV = 0;
                double dUStep = threeDSurfaces[idx].mdUStep;
                if (threeDSurfaces[idx].mdUStep != 0) {
                    // normal interval mode
                    nNumofPntsInU = (int) (Math.floor((threeDSurfaces[idx].mdUTo - threeDSurfaces[idx].mdUFrom) / threeDSurfaces[idx].mdUStep) + 1);
                    if (nNumofPntsInU < 1) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CURVE_SHOULD_HAVE_AT_LEAST_ONE_POINT);
                    }
                } else {
                    nNumofPntsInU = (int) Math.sqrt(nDefaultNumOfPoints);    // assume number of points in a curve in auto mode is 256 so that we can get max and min for x and y
                    dUStep = (threeDSurfaces[idx].mdUTo - threeDSurfaces[idx].mdUFrom) / (nNumofPntsInU - 1);
                }

                double dVStep = threeDSurfaces[idx].mdVStep;
                if (threeDSurfaces[idx].mdVStep != 0) {
                    // normal interval mode
                    nNumofPntsInV = (int) (Math.floor((threeDSurfaces[idx].mdVTo - threeDSurfaces[idx].mdVFrom) / threeDSurfaces[idx].mdVStep) + 1);
                    if (nNumofPntsInV < 1) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CURVE_SHOULD_HAVE_AT_LEAST_ONE_POINT);
                    }
                } else {
                    nNumofPntsInV = (int) Math.sqrt(nDefaultNumOfPoints);    // assume number of points in a curve in auto mode is 256 so that we can get max and min for x and y
                    dVStep = (threeDSurfaces[idx].mdVTo - threeDSurfaces[idx].mdVFrom) / (nNumofPntsInV - 1);
                }

                nNumofPntsInCurve = nNumofPntsInU * nNumofPntsInV;
                if (nNumofPntsInCurve > MAX_NUMBER_OF_PNTS_IN_ONE_CURVE || nNumofPntsInCurve <= 0) {
                    // nNumofPntsInCurve <= 0 means nNumofPntsInCurve overflow.
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_TOO_MANY_POINTS_TO_PLOT_IN_A_CURVE);
                }

                arraydUCurves[idx] = new double[nNumofPntsInU];
                arraydVCurves[idx] = new double[nNumofPntsInV];
                arraydXCurves[idx] = new double[nNumofPntsInV][nNumofPntsInU];
                arraydYCurves[idx] = new double[nNumofPntsInV][nNumofPntsInU];
                arraydZCurves[idx] = new double[nNumofPntsInV][nNumofPntsInU];

                double dSumX = 0, dSumXSqr = 0, dSumY = 0, dSumYSqr = 0, dSumZ = 0, dSumZSqr = 0;
                int nValidPntNum = 0;
                LinkedList<Variable> lVarnameSpace = new LinkedList<Variable>();
                Variable varV = new Variable(threeDSurfaces[idx].mstrVName);
                lVarnameSpace.addFirst(varV);
                UnknownVariable varUnknownU = new UnknownVariable(threeDSurfaces[idx].mstrUName);
                lVarnameSpace.addFirst(varUnknownU);
                progContext.mdynamicProgContext.mlVarNameSpaces.addFirst(lVarnameSpace);
                LinkedList<UnknownVariable> lUnknownVarnameSpace = new LinkedList<UnknownVariable>();
                lUnknownVarnameSpace.add(varUnknownU);
                for (int idxV = 0; idxV < nNumofPntsInV; idxV++) {
                    arraydVCurves[idx][idxV] = threeDSurfaces[idx].mdVFrom + dVStep * idxV;
                    DataClass datumV = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(arraydVCurves[idx][idxV], true));
                    varV.setValue(datumV);
                    AbstractExpr aeXExprSimplified = aeXExpr, aeYExprSimplified = aeYExpr, aeZExprSimplified = aeZExpr;
                    try {
                        varUnknownU.setValueAssigned(false);
                        aeXExprSimplified = aeXExpr.evaluateAExpr(lUnknownVarnameSpace, progContext);
                        varUnknownU.setValueAssigned(false);
                        aeYExprSimplified = aeYExpr.evaluateAExpr(lUnknownVarnameSpace, progContext);
                        varUnknownU.setValueAssigned(false);
                        aeZExprSimplified = aeZExpr.evaluateAExpr(lUnknownVarnameSpace, progContext);
                    } catch (Exception e) {
                        // do nothing here
                    }
                    for (int idxU = 0; idxU < nNumofPntsInU; idxU++) {
                        arraydUCurves[idx][idxU] = threeDSurfaces[idx].mdUFrom + dUStep * idxU;
                        DataClass datumU = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(arraydUCurves[idx][idxU], true));
                        varUnknownU.setValue(datumU);
                        //ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
                        //CurPos curPos = new CurPos();
                        DataClass datumXValue = null, datumYValue = null, datumZValue = null;
                        try {
                            /*curPos.m_nPos = 0;
                            datumXValue = exprEvaluator.evaluateExpression(threeDSurfaces[idx].mstrXExpr, curPos);
                            curPos.m_nPos = 0;
                            datumYValue = exprEvaluator.evaluateExpression(threeDSurfaces[idx].mstrYExpr, curPos);
                            curPos.m_nPos = 0;
                            datumZValue = exprEvaluator.evaluateExpression(threeDSurfaces[idx].mstrZExpr, curPos);*/
                            datumXValue = aeXExprSimplified.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);
                            datumYValue = aeYExprSimplified.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);
                            datumZValue = aeZExprSimplified.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);
                        } catch (Exception e) {
                            // do nothing here
                        }

                        if (datumXValue == null) {
                            arraydXCurves[idx][idxV][idxU] = Double.NaN;
                        } else if (datumXValue.getThisOrNull() instanceof DataClassSingleNum) {
                            arraydXCurves[idx][idxV][idxU] = DCHelper.lightCvtOrRetDCSingleNum(datumXValue).getDataValue().doubleValue();
                        } else {    // matrix or complex value. complex value may be changed to double if its image is 0.
                            try {
                                arraydXCurves[idx][idxV][idxU] = DCHelper.lightCvtOrRetDCMFPDec(datumXValue).getDataValue().doubleValue();
                            } catch (Exception e) {
                                arraydXCurves[idx][idxV][idxU] = Double.NaN;
                            }
                        }
                        if (datumYValue == null) {
                            arraydYCurves[idx][idxV][idxU] = Double.NaN;
                        } else if (datumYValue.getThisOrNull() instanceof DataClassSingleNum) {
                            arraydYCurves[idx][idxV][idxU] = DCHelper.lightCvtOrRetDCSingleNum(datumYValue).getDataValue().doubleValue();
                        } else {    // matrix or complex value. Complex value may be changed to double if its image is 0.
                            try {
                                arraydYCurves[idx][idxV][idxU] = DCHelper.lightCvtOrRetDCMFPDec(datumYValue).getDataValue().doubleValue();
                            } catch (Exception e) {
                                arraydYCurves[idx][idxV][idxU] = Double.NaN;
                            }
                        }
                        if (datumZValue == null) {
                            arraydZCurves[idx][idxV][idxU] = Double.NaN;
                        } else if (datumZValue.getThisOrNull() instanceof DataClassSingleNum) {
                            arraydZCurves[idx][idxV][idxU] = DCHelper.lightCvtOrRetDCSingleNum(datumZValue).getDataValue().doubleValue();
                        } else {    // matrix or complex value. Complex value may be changed to double if its image is 0.
                            try {
                                datumZValue = DCHelper.lightCvtOrRetDCMFPDec(datumZValue);
                                arraydZCurves[idx][idxV][idxU] = DCHelper.lightCvtOrRetDCMFPDec(datumZValue).getDataValue().doubleValue();
                            } catch (Exception e) {
                                arraydZCurves[idx][idxV][idxU] = Double.NaN;
                            }
                        }
                        if (!Double.isNaN(arraydXCurves[idx][idxV][idxU]) && !Double.isInfinite(arraydXCurves[idx][idxV][idxU])
                                && !Double.isNaN(arraydYCurves[idx][idxV][idxU]) && !Double.isInfinite(arraydYCurves[idx][idxV][idxU])
                                && !Double.isNaN(arraydZCurves[idx][idxV][idxU]) && !Double.isInfinite(arraydZCurves[idx][idxV][idxU])) {
                            nValidPntNum++;
                            nTotalNumValidPnts++;
                            /*if (nTotalNumValidPnts == 1)    {
                            dXMax = arraydXCurves[idx][idxV][idxU];
                            dXMin = arraydXCurves[idx][idxV][idxU];
                            dYMax = arraydYCurves[idx][idxV][idxU];
                            dYMin = arraydYCurves[idx][idxV][idxU];
                            dZMax = arraydZCurves[idx][idxV][idxU];
                            dZMin = arraydZCurves[idx][idxV][idxU];

                            } else    {
                            dXMax = Math.max(dXMax, arraydXCurves[idx][idxV][idxU]);
                            dXMin = Math.min(dXMin, arraydXCurves[idx][idxV][idxU]);
                            dYMax = Math.max(dYMax, arraydYCurves[idx][idxV][idxU]);
                            dYMin = Math.min(dYMin, arraydYCurves[idx][idxV][idxU]);
                            dZMax = Math.max(dZMax, arraydZCurves[idx][idxV][idxU]);
                            dZMin = Math.min(dZMin, arraydZCurves[idx][idxV][idxU]);
                            }*/
                            dSumX += arraydXCurves[idx][idxV][idxU];
                            dSumXSqr += arraydXCurves[idx][idxV][idxU] * arraydXCurves[idx][idxV][idxU];
                            dSumY += arraydYCurves[idx][idxV][idxU];
                            dSumYSqr += arraydYCurves[idx][idxV][idxU] * arraydYCurves[idx][idxV][idxU];
                            dSumZ += arraydZCurves[idx][idxV][idxU];
                            dSumZSqr += arraydZCurves[idx][idxV][idxU] * arraydZCurves[idx][idxV][idxU];
                        }

                        strarrayXValues[idx] += Double.isInfinite(arraydXCurves[idx][idxV][idxU]) ? Double.NaN : arraydXCurves[idx][idxV][idxU];
                        strarrayYValues[idx] += Double.isInfinite(arraydYCurves[idx][idxV][idxU]) ? Double.NaN : arraydYCurves[idx][idxV][idxU];
                        strarrayZValues[idx] += Double.isInfinite(arraydZCurves[idx][idxV][idxU]) ? Double.NaN : arraydZCurves[idx][idxV][idxU];
                        if (idxU < nNumofPntsInU - 1) {
                            strarrayXValues[idx] += ",";
                            strarrayYValues[idx] += ",";
                            strarrayZValues[idx] += ",";
                        } else if (idxV < nNumofPntsInV - 1) {
                            strarrayXValues[idx] += ";";
                            strarrayYValues[idx] += ";";
                            strarrayZValues[idx] += ";";
                        } else {
                            strarrayXValues[idx] += "\n";
                            strarrayYValues[idx] += "\n";
                            strarrayZValues[idx] += "\n";
                        }
                    }
                }
                if (nValidPntNum > 0) {
                    arraydXAvg[idx] = dSumX / nValidPntNum;
                    arraydXStdev[idx] = Math.sqrt(Math.max(0, dSumXSqr / nValidPntNum - arraydXAvg[idx] * arraydXAvg[idx]));
                    arraydYAvg[idx] = dSumY / nValidPntNum;
                    arraydYStdev[idx] = Math.sqrt(Math.max(0, dSumYSqr / nValidPntNum - arraydYAvg[idx] * arraydYAvg[idx]));
                    arraydZAvg[idx] = dSumZ / nValidPntNum;
                    arraydZStdev[idx] = Math.sqrt(Math.max(0, dSumZSqr / nValidPntNum - arraydZAvg[idx] * arraydZAvg[idx]));
                } else {
                    arraydXAvg[idx] = arraydXStdev[idx] = 0;
                    arraydYAvg[idx] = arraydYStdev[idx] = 0;
                    arraydZAvg[idx] = arraydZStdev[idx] = 0;
                }
                double dConfidenceInterval = 3;
                if (idx == 0) {
                    dXMin = arraydXAvg[idx] - dConfidenceInterval * arraydXStdev[idx];
                    dXMax = arraydXAvg[idx] + dConfidenceInterval * arraydXStdev[idx];
                    dYMin = arraydYAvg[idx] - dConfidenceInterval * arraydYStdev[idx];
                    dYMax = arraydYAvg[idx] + dConfidenceInterval * arraydYStdev[idx];
                    dZMin = arraydZAvg[idx] - dConfidenceInterval * arraydZStdev[idx];
                    dZMax = arraydZAvg[idx] + dConfidenceInterval * arraydZStdev[idx];
                } else {
                    if (dXMin > arraydXAvg[idx] - dConfidenceInterval * arraydXStdev[idx]) {
                        dXMin = arraydXAvg[idx] - dConfidenceInterval * arraydXStdev[idx];
                    }
                    if (dXMax < arraydXAvg[idx] + dConfidenceInterval * arraydXStdev[idx]) {
                        dXMax = arraydXAvg[idx] + dConfidenceInterval * arraydXStdev[idx];
                    }
                    if (dYMin > arraydYAvg[idx] - dConfidenceInterval * arraydYStdev[idx]) {
                        dYMin = arraydYAvg[idx] - dConfidenceInterval * arraydYStdev[idx];
                    }
                    if (dYMax < arraydYAvg[idx] + dConfidenceInterval * arraydYStdev[idx]) {
                        dYMax = arraydYAvg[idx] + dConfidenceInterval * arraydYStdev[idx];
                    }
                    if (dZMin > arraydZAvg[idx] - dConfidenceInterval * arraydZStdev[idx]) {
                        dZMin = arraydZAvg[idx] - dConfidenceInterval * arraydZStdev[idx];
                    }
                    if (dZMax < arraydZAvg[idx] + dConfidenceInterval * arraydZStdev[idx]) {
                        dZMax = arraydZAvg[idx] + dConfidenceInterval * arraydZStdev[idx];
                    }
                }
            }
            double dDefaultHalfRange = (MFPAdapter.getPlotChartVariableTo() - MFPAdapter.getPlotChartVariableFrom())/2.0;
            if (dXMin == dXMax) {
                dXMin -= dDefaultHalfRange;
                dXMax += dDefaultHalfRange;
            }
            if (dYMin == dYMax) {
                dYMin -= dDefaultHalfRange;
                dYMax += dDefaultHalfRange;
            }
            if (dZMin == dZMax) {
                dZMin -= dDefaultHalfRange;
                dZMax += dDefaultHalfRange;
            }

            for (int idx = 0; idx < nNumofCurves; idx++) {
                strOutput += "\n" + strarrayXValues[idx] + strarrayYValues[idx] + strarrayZValues[idx] + strarrayCurveSettings[idx];
            }

            String strChartSettings = "chart_type:multiXYZ;chart_title:" + ChartOperator.addEscapes(strChartTitle)
                    + ";x_title:" + ChartOperator.addEscapes(strXTitle) + ";x_min:" + dXMin + ";x_max:" + dXMax + ";x_labels:10"
                    + ";y_title:" + ChartOperator.addEscapes(strYTitle) + ";y_min:" + dYMin + ";y_max:" + dYMax + ";y_labels:10"
                    + ";z_title:" + ChartOperator.addEscapes(strZTitle) + ";z_min:" + dZMin + ";z_max:" + dZMax + ";z_labels:10";
            strOutput = "chart_name:" + ChartOperator.addEscapes(strChartName) + ";" + strChartSettings + ";number_of_curves:" + nNumofCurves + strOutput;

            // plot the graph
            if (FuncEvaluator.msgraphPlotter3D != null) {
                try {
                    if (!FuncEvaluator.msgraphPlotter3D.plotGraph(strOutput)) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_PLOT_CHART);
                    }
                } catch (UnsatisfiedLinkError e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_PLOT_OGL_CHART_LACK_SYSTEM_SUPPORT_FILES);
                }
            }

            // save the graph to disk
            if (FuncEvaluator.msfileOperator != null && strChartName.trim().length() != 0) {
                try {
                    FuncEvaluator.msfileOperator.outputGraphFile(strChartName, strOutput);
                } catch (FileNotFoundException e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_FUNCTION_PROCESSION,
                            strCallingFunc, e);
                } catch (IOException e) {
                    if (e instanceof ClosedByInterruptException) {
                        throw new InterruptedException();    // this thread is interrupted by another thread.
                    } else {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_FUNCTION_PROCESSION,
                                strCallingFunc, e);
                    }
                }
            }
            return null;
        }
    }
    //public final static Plot_3d_surfacesFunction BUILTINFUNC_Plot_3d_surfaces = new Plot_3d_surfacesFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Plot_3d_surfacesFunction());}

    public static LinkedList<DataSeriesGridSurface> recalc3DExprDataSet(double dXAxisFrom, double dXAxisTo, double dYAxisFrom, double dYAxisTo,
            double dZAxisFrom, double dZAxisTo, ThreeDExprSurface[] array3DExprSurfaces) {
        // shouldn't fill the whole chart, so ugly.
        double dXFrom = (dXAxisTo + dXAxisFrom) / 2.0 - (dXAxisTo - dXAxisFrom) / 2.0 * THREED_EXPR_CHART_AXIS_FILL_RATIO;
        double dXTo = (dXAxisTo + dXAxisFrom) / 2.0 + (dXAxisTo - dXAxisFrom) / 2.0 * THREED_EXPR_CHART_AXIS_FILL_RATIO;
        double dYFrom = (dYAxisTo + dYAxisFrom) / 2.0 - (dYAxisTo - dYAxisFrom) / 2.0 * THREED_EXPR_CHART_AXIS_FILL_RATIO;
        double dYTo = (dYAxisTo + dYAxisFrom) / 2.0 + (dYAxisTo - dYAxisFrom) / 2.0 * THREED_EXPR_CHART_AXIS_FILL_RATIO;
        double dZFrom = (dZAxisTo + dZAxisFrom) / 2.0 - (dZAxisTo - dZAxisFrom) / 2.0 * THREED_EXPR_CHART_AXIS_FILL_RATIO;
        double dZTo = (dZAxisTo + dZAxisFrom) / 2.0 + (dZAxisTo - dZAxisFrom) / 2.0 * THREED_EXPR_CHART_AXIS_FILL_RATIO;

        LinkedList<DataSeriesGridSurface> listSurfaces = new LinkedList<DataSeriesGridSurface>();
        Position3D[][][] arrayXYZPnts = new Position3D[array3DExprSurfaces.length][0][0];
        double[][][] arraydXCurves = new double[array3DExprSurfaces.length][0][0];
        double[][][] arraydYCurves = new double[array3DExprSurfaces.length][0][0];
        double[][][] arraydZCurves = new double[array3DExprSurfaces.length][0][0];

        for (int idx = 0; idx < array3DExprSurfaces.length; idx++) {
            int nNumofPntsInV = 0, nNumofPntsInU = 0;
            String strSurfaceMode = null;
            if (array3DExprSurfaces[idx].mnFunctionVar == 0) {
                nNumofPntsInU = array3DExprSurfaces[idx].mnYNumOfSteps + 1;
                nNumofPntsInV = array3DExprSurfaces[idx].mnZNumOfSteps + 1;
                strSurfaceMode = "yz";
            } else if (array3DExprSurfaces[idx].mnFunctionVar == 1) {
                nNumofPntsInU = array3DExprSurfaces[idx].mnXNumOfSteps + 1;
                nNumofPntsInV = array3DExprSurfaces[idx].mnZNumOfSteps + 1;
                strSurfaceMode = "zx";
            } else {
                nNumofPntsInU = array3DExprSurfaces[idx].mnXNumOfSteps + 1;
                nNumofPntsInV = array3DExprSurfaces[idx].mnYNumOfSteps + 1;
                strSurfaceMode = "xy";
            }

            arrayXYZPnts[idx] = new Position3D[nNumofPntsInV][nNumofPntsInU];
            arraydXCurves[idx] = new double[nNumofPntsInV][nNumofPntsInU];
            arraydYCurves[idx] = new double[nNumofPntsInV][nNumofPntsInU];
            arraydZCurves[idx] = new double[nNumofPntsInV][nNumofPntsInU];

            AbstractExpr aeXExpr = AEInvalid.AEINVALID, aeYExpr = AEInvalid.AEINVALID, aeZExpr = AEInvalid.AEINVALID;
            try {
                // no need to use progContext because array3DExprSurfaces[idx].mstrXExpr etc have been converted
                // from relative CS to absolute CS.
                aeXExpr = ExprAnalyzer.analyseExpression(array3DExprSurfaces[idx].mstrXExpr, new CurPos());
                aeYExpr = ExprAnalyzer.analyseExpression(array3DExprSurfaces[idx].mstrYExpr, new CurPos());
                aeZExpr = ExprAnalyzer.analyseExpression(array3DExprSurfaces[idx].mstrZExpr, new CurPos());
            } catch (Exception e) {
            }
            DataClass datumU = new DataClassNull();
            DataClass datumV = new DataClassNull();
            String strUName = "", strVName = "";
            AbstractExpr aeFuncVarExpr = AEInvalid.AEINVALID;
            if (array3DExprSurfaces[idx].mnFunctionVar == 0) {
                strUName = array3DExprSurfaces[idx].mstrYExpr.trim().toLowerCase(Locale.US);
                strVName = array3DExprSurfaces[idx].mstrZExpr.trim().toLowerCase(Locale.US);
            } else if (array3DExprSurfaces[idx].mnFunctionVar == 1) {
                strUName = array3DExprSurfaces[idx].mstrXExpr.trim().toLowerCase(Locale.US);
                strVName = array3DExprSurfaces[idx].mstrZExpr.trim().toLowerCase(Locale.US);
            } else {
                strUName = array3DExprSurfaces[idx].mstrXExpr.trim().toLowerCase(Locale.US);
                strVName = array3DExprSurfaces[idx].mstrYExpr.trim().toLowerCase(Locale.US);
            }
            LinkedList<Variable> lVarnameSpace = new LinkedList<Variable>();
            LinkedList<UnknownVariable> lUnknownVarnameSpace = new LinkedList<UnknownVariable>();
            UnknownVariable varU = new UnknownVariable(strUName);
            lVarnameSpace.addFirst(varU);
            lUnknownVarnameSpace.addFirst(varU);
            Variable varV = new Variable(strVName);
            lVarnameSpace.addFirst(varV);
            LinkedList<LinkedList<Variable>> lVarNameSpaces = new LinkedList<LinkedList<Variable>>();
            lVarNameSpaces.addFirst(lVarnameSpace);
            for (int idxV = 0; idxV < nNumofPntsInV; idxV++) {
                double dValueV;
                if (array3DExprSurfaces[idx].mnFunctionVar == 0) {
                    dValueV = (dZTo - dZFrom) / array3DExprSurfaces[idx].mnZNumOfSteps * idxV + dZFrom;
                    try {
                        datumV = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dValueV, true));
                    } catch (JFCALCExpErrException e) {
                        // do nothing because the exception will not be triggered.
                    }
                    aeFuncVarExpr = aeXExpr;
                } else if (array3DExprSurfaces[idx].mnFunctionVar == 1) {
                    dValueV = (dZTo - dZFrom) / array3DExprSurfaces[idx].mnZNumOfSteps * idxV + dZFrom;
                    try {
                        datumV = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dValueV, true));
                    } catch (JFCALCExpErrException e) {
                        // do nothing because the exception will not be triggered.
                    }
                    aeFuncVarExpr = aeYExpr;
                } else {
                    dValueV = (dYTo - dYFrom) / array3DExprSurfaces[idx].mnYNumOfSteps * idxV + dYFrom;
                    try {
                        datumV = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dValueV, true));
                    } catch (JFCALCExpErrException e) {
                        // do nothing because the exception will not be triggered.
                    }
                    aeFuncVarExpr = aeZExpr;
                }
                varV.setValue(datumV);
                try {
                    varU.setValueAssigned(false);
                    ProgContext progContext = new ProgContext();
                    progContext.mdynamicProgContext.mlVarNameSpaces = lVarNameSpaces;
                    aeFuncVarExpr = aeFuncVarExpr.evaluateAExpr(lUnknownVarnameSpace, progContext);  // simplify aeFuncVar.
                } catch (Exception e) {
                }
                for (int idxU = 0; idxU < nNumofPntsInU; idxU++) {
                    if (array3DExprSurfaces[idx].mnFunctionVar == 0) {
                        arraydYCurves[idx][idxV][idxU] = (dYTo - dYFrom) / array3DExprSurfaces[idx].mnYNumOfSteps * idxU + dYFrom;
                        arraydZCurves[idx][idxV][idxU] = dValueV;
                        try {
                            datumU = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(arraydYCurves[idx][idxV][idxU], true));
                        } catch (JFCALCExpErrException e) {
                            // do nothing because the exception will not be triggered.
                        }
                    } else if (array3DExprSurfaces[idx].mnFunctionVar == 1) {
                        arraydXCurves[idx][idxV][idxU] = (dXTo - dXFrom) / array3DExprSurfaces[idx].mnXNumOfSteps * idxU + dXFrom;
                        arraydZCurves[idx][idxV][idxU] = dValueV;
                        try {
                            datumU = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(arraydXCurves[idx][idxV][idxU], true));
                        } catch (JFCALCExpErrException e) {
                            // do nothing because the exception will not be triggered.
                        }
                    } else {
                        arraydXCurves[idx][idxV][idxU] = (dXTo - dXFrom) / array3DExprSurfaces[idx].mnXNumOfSteps * idxU + dXFrom;
                        arraydYCurves[idx][idxV][idxU] = dValueV;
                        try {
                            datumU = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(arraydXCurves[idx][idxV][idxU], true));
                        } catch (JFCALCExpErrException e) {
                            // do nothing because the exception will not be triggered.
                        }
                    }
                    varU.setValue(datumU);
                    /*ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
                    CurPos curPos = new CurPos();
                    curPos.m_nPos = 0;*/
                    DataClass datumReturn = null;
                    double dReturn = Double.NaN;
                    try {
                        ProgContext progContext = new ProgContext();
                        progContext.mdynamicProgContext.mlVarNameSpaces = lVarNameSpaces;
                        datumReturn = aeFuncVarExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);
                        //datumReturn = exprEvaluator.evaluateExpression(strFuncVarExpr, curPos);
                        dReturn = DCHelper.lightCvtOrRetDCMFPDec(datumReturn).getDataValue().doubleValue();
                    } catch (Exception e) {
                        // do nothing here.
                    }
                    if (array3DExprSurfaces[idx].mnFunctionVar == 0) {
                        arraydXCurves[idx][idxV][idxU] = dReturn;
                    } else if (array3DExprSurfaces[idx].mnFunctionVar == 1) {
                        arraydYCurves[idx][idxV][idxU] = dReturn;
                    } else {
                        arraydZCurves[idx][idxV][idxU] = dReturn;
                    }
                    arrayXYZPnts[idx][idxV][idxU] = new Position3D(arraydXCurves[idx][idxV][idxU],
                            arraydYCurves[idx][idxV][idxU],
                            arraydZCurves[idx][idxV][idxU]);
                }
            }
            DataSeriesGridSurface dataSurface = new DataSeriesGridSurface(strSurfaceMode);
            dataSurface.mstrName = array3DExprSurfaces[idx].mstrCurveLabel;
            dataSurface.msurfaceStyle = new SurfaceStyle();
            dataSurface.msurfaceStyle.menumSurfaceType = array3DExprSurfaces[idx].mbIsGrid ? SURFACETYPE.SURFACETYPE_GRID : SURFACETYPE.SURFACETYPE_SURFACE;
            dataSurface.msurfaceStyle.mclrUpFaceMin = OGLExprChartOperator.cvtStr2VMFPColor(array3DExprSurfaces[idx].mstrMinColor);
            dataSurface.msurfaceStyle.mclrUpFaceMax = OGLExprChartOperator.cvtStr2VMFPColor(array3DExprSurfaces[idx].mstrMaxColor);
            dataSurface.msurfaceStyle.mclrDownFaceMin = OGLExprChartOperator.cvtStr2VMFPColor(array3DExprSurfaces[idx].mstrMinColor1);
            dataSurface.msurfaceStyle.mclrDownFaceMax = OGLExprChartOperator.cvtStr2VMFPColor(array3DExprSurfaces[idx].mstrMaxColor1);
            dataSurface.setByMatrix(arrayXYZPnts[idx]);
            // min value is always min cvted value, max value is always max cvted value.
            dataSurface.msurfaceStyle.mdUpFaceMinValue = dataSurface.msurfaceStyle.mdDownFaceMinValue = ((dataSurface.getMode() == 2) ? dataSurface.getMinCvtedY() : ((dataSurface.getMode() == 1) ? dataSurface.getMinCvtedX() : dataSurface.getMinCvtedZ()));
            dataSurface.msurfaceStyle.mdUpFaceMaxValue = dataSurface.msurfaceStyle.mdDownFaceMaxValue = ((dataSurface.getMode() == 2) ? dataSurface.getMaxCvtedY() : ((dataSurface.getMode() == 1) ? dataSurface.getMaxCvtedX() : dataSurface.getMaxCvtedZ()));
            listSurfaces.add(dataSurface);
        }
        return listSurfaces;
    }

    public static class Plot_3d_expressionsFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public static final int PARAM_NUM_FOR_CHART_ONLY = 11;
        public static final int PARAM_NUM_IN_ONE_CURVE = 13;
        public Plot_3d_expressionsFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::plot_math::plot_3d_expressions";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = PARAM_NUM_FOR_CHART_ONLY + MAX_NUMBER_OF_3D_SURFACES_TO_PLOT * PARAM_NUM_IN_ONE_CURVE;
            mnMinParamNum = PARAM_NUM_FOR_CHART_ONLY + PARAM_NUM_IN_ONE_CURVE;
        }

        // this function read expression parameter for one of x, y and z and plot 3D surface. different from
        // function plot3DSurfaces, this function assumes one of x,y,z is always a function of the other two.
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            String strCallingFunc = mstrarrayFullCS[mstrarrayFullCS.length - 1];
            plot3DExprSurfaces(strCallingFunc, listParams, progContext);
            return null;
        }
        
        // note that strCallingFunc only includes small letter.
        public static void plot3DExprSurfaces(String strCallingFunc, LinkedList<DataClass> listParams, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            // Parameters:
            // 1. chart name (i.e. chart file name); 2. chart title; 3. X axis title; 4. X axis from; 5. X axis to;
            // 6. Y axis title; 7. Y axis from; 8. Y axis to; 9. Z axis title; 10. Z axis from; 11. Z axis to;
            // 12. curve title; 13. grid or not; 14. min color (front side); 15. min color (back side),
            // 16. max color (front side); 17. max color (back side), 18. function variable (0 means x, 1 means y, other is z);
            // 19. x variable expression; 20. x number of steps; 21. y variable expression; 22. y number of steps;
            // 23. z variable expression; 24. z number of steps; ...
            // ... Note that every new curve needs additional 11 parameters (i.e. parameters 12 to 22). At most 4 curves can be included.
            // and function variable's number of steps is ignored.
            int nParamNumForChartOnly = PARAM_NUM_FOR_CHART_ONLY;
            int nParamNumIn1Cv = PARAM_NUM_IN_ONE_CURVE;
            int nMaxNumofCvs = MAX_NUMBER_OF_3D_SURFACES_TO_PLOT;

            if (listParams.size() > (nParamNumForChartOnly + nMaxNumofCvs * nParamNumIn1Cv)
                    || listParams.size() < (nParamNumForChartOnly + nParamNumIn1Cv) // should be at least one curve, at most 4 curves.
                    || nParamNumIn1Cv * (int) (((double) (listParams.size() - nParamNumForChartOnly)) / nParamNumIn1Cv) != (listParams.size() - nParamNumForChartOnly)) {
                // The first nParamNumForChartOnly params are for chart definition.
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            int nNumofCurves = (int) ((listParams.size() - nParamNumForChartOnly) / nParamNumIn1Cv);
            int nDefaultNumOfStepsSqr = DEFAULT_NUMBER_OF_STEPS_SQR_IN_ONE_SURFACE;    // was 256;

            // converting to non-array type needs no deep copy first.
            DataClassString datumChartName = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strChartName = datumChartName.getStringValue();

            DataClassString datumChartTitle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strChartTitle = datumChartTitle.getStringValue();

            DataClassString datumXTitle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strXTitle = datumXTitle.getStringValue();

            DataClassSingleNum datumXFrom = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
            double dXFrom = datumXFrom.getDataValue().doubleValue();

            DataClassSingleNum datumXTo = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
            double dXTo = datumXTo.getDataValue().doubleValue();

            DataClassString datumYTitle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strYTitle = datumYTitle.getStringValue();

            DataClassSingleNum datumYFrom = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
            double dYFrom = datumYFrom.getDataValue().doubleValue();

            DataClassSingleNum datumYTo = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
            double dYTo = datumYTo.getDataValue().doubleValue();

            DataClassString datumZTitle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strZTitle = datumZTitle.getStringValue();

            DataClassSingleNum datumZFrom = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
            double dZFrom = datumZFrom.getDataValue().doubleValue();

            DataClassSingleNum datumZTo = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
            double dZTo = datumZTo.getDataValue().doubleValue();

            if (dXFrom >= dXTo || dYFrom >= dYTo || dZFrom >= dZTo) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }

            ThreeDExprSurface[] threeDExprSurfaces = new ThreeDExprSurface[nNumofCurves];
            String strOutput = "";

            for (int idx = 0; idx < nNumofCurves; idx++) {
                threeDExprSurfaces[idx] = new ThreeDExprSurface();

                DataClassString datumCurveTitle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                threeDExprSurfaces[idx].mstrCurveLabel = datumCurveTitle.getStringValue();

                DataClassSingleNum datumIsGrid = DCHelper.lightCvtOrRetDCMFPBool(listParams.removeLast());
                threeDExprSurfaces[idx].mbIsGrid = (datumIsGrid.getDataValue().longValue() == 0) ? false : true;

                DataClassString datumMinColor = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                threeDExprSurfaces[idx].mstrMinColor = datumMinColor.getStringValue();

                DataClassString datumMinColor1 = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                threeDExprSurfaces[idx].mstrMinColor1 = datumMinColor1.getStringValue();

                DataClassString datumMaxColor = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                threeDExprSurfaces[idx].mstrMaxColor = datumMaxColor.getStringValue();

                DataClassString datumMaxColor1 = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                threeDExprSurfaces[idx].mstrMaxColor1 = datumMaxColor1.getStringValue();

                DataClassSingleNum datumFunctionVar = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
                threeDExprSurfaces[idx].mnFunctionVar = (int) datumFunctionVar.getDataValue().longValue();
                if (threeDExprSurfaces[idx].mnFunctionVar < 0 || threeDExprSurfaces[idx].mnFunctionVar > 2) {
                    threeDExprSurfaces[idx].mnFunctionVar = 2;    // z by default.
                }

                DataClassString datumXExpr = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                threeDExprSurfaces[idx].mstrXExpr = datumXExpr.getStringValue();
                try {
                    // convert relative CS to absolute CS.
                    AbstractExpr aeExpr = ExprAnalyzer.analyseExpression(threeDExprSurfaces[idx].mstrXExpr, new CurPos(), new LinkedList<Variable>(), progContext);
                    threeDExprSurfaces[idx].mstrXExpr = aeExpr.output();
                } catch (JSmartMathErrException ex) {
                    
                }
                DataClassSingleNum datumXNumOfSteps = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
                threeDExprSurfaces[idx].mnXNumOfSteps = (int) datumXNumOfSteps.getDataValue().longValue();
                if (threeDExprSurfaces[idx].mnXNumOfSteps <= 0) {
                    threeDExprSurfaces[idx].mnXNumOfSteps = (int) Math.sqrt(nDefaultNumOfStepsSqr);
                }

                DataClassString datumYExpr = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                threeDExprSurfaces[idx].mstrYExpr = datumYExpr.getStringValue();
                try {
                    // convert relative CS to absolute CS.
                    AbstractExpr aeExpr = ExprAnalyzer.analyseExpression(threeDExprSurfaces[idx].mstrYExpr, new CurPos(), new LinkedList<Variable>(), progContext);
                    threeDExprSurfaces[idx].mstrYExpr = aeExpr.output();
                } catch (JSmartMathErrException ex) {
                    
                }

                DataClassSingleNum datumYNumOfSteps = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
                threeDExprSurfaces[idx].mnYNumOfSteps = (int) datumYNumOfSteps.getDataValue().longValue();
                if (threeDExprSurfaces[idx].mnYNumOfSteps <= 0) {
                    threeDExprSurfaces[idx].mnYNumOfSteps = (int) Math.sqrt(nDefaultNumOfStepsSqr);
                }

                DataClassString datumZExpr = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                threeDExprSurfaces[idx].mstrZExpr = datumZExpr.getStringValue();
                try {
                    // convert relative CS to absolute CS.
                    AbstractExpr aeExpr = ExprAnalyzer.analyseExpression(threeDExprSurfaces[idx].mstrZExpr, new CurPos(), new LinkedList<Variable>(), progContext);
                    threeDExprSurfaces[idx].mstrZExpr = aeExpr.output();
                } catch (JSmartMathErrException ex) {
                    
                }

                DataClassSingleNum datumZNumOfSteps = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
                threeDExprSurfaces[idx].mnZNumOfSteps = (int) datumZNumOfSteps.getDataValue().longValue();
                if (threeDExprSurfaces[idx].mnZNumOfSteps <= 0) {
                    threeDExprSurfaces[idx].mnZNumOfSteps = (int) Math.sqrt(nDefaultNumOfStepsSqr);
                }

                int nNumOfPointsIn1Curve = 0;
                if (threeDExprSurfaces[idx].mnFunctionVar == 0) {
                    if (!Variable.isValidVarNameWithNoCS(threeDExprSurfaces[idx].mstrYExpr)
                            || !Variable.isValidVarNameWithNoCS(threeDExprSurfaces[idx].mstrZExpr)
                            || threeDExprSurfaces[idx].mstrYExpr.equalsIgnoreCase(threeDExprSurfaces[idx].mstrZExpr)) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                    }
                    nNumOfPointsIn1Curve = (threeDExprSurfaces[idx].mnYNumOfSteps + 1) * (threeDExprSurfaces[idx].mnZNumOfSteps + 1);
                } else if (threeDExprSurfaces[idx].mnFunctionVar == 1) {
                    if (!Variable.isValidVarNameWithNoCS(threeDExprSurfaces[idx].mstrXExpr)
                            || !Variable.isValidVarNameWithNoCS(threeDExprSurfaces[idx].mstrZExpr)
                            || threeDExprSurfaces[idx].mstrXExpr.equalsIgnoreCase(threeDExprSurfaces[idx].mstrZExpr)) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                    }
                    nNumOfPointsIn1Curve = (threeDExprSurfaces[idx].mnXNumOfSteps + 1) * (threeDExprSurfaces[idx].mnZNumOfSteps + 1);
                } else {
                    if (!Variable.isValidVarNameWithNoCS(threeDExprSurfaces[idx].mstrXExpr)
                            || !Variable.isValidVarNameWithNoCS(threeDExprSurfaces[idx].mstrYExpr)
                            || threeDExprSurfaces[idx].mstrXExpr.equalsIgnoreCase(threeDExprSurfaces[idx].mstrYExpr)) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                    }
                    nNumOfPointsIn1Curve = (threeDExprSurfaces[idx].mnXNumOfSteps + 1) * (threeDExprSurfaces[idx].mnYNumOfSteps + 1);
                }

                if (nNumOfPointsIn1Curve > MAX_NUMBER_OF_PNTS_IN_ONE_CURVE || nNumOfPointsIn1Curve <= 0) {
                    // nNumOfPointsIn1Curve <= 0 implies that it is too large so that overflow
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_TOO_MANY_POINTS_TO_PLOT_IN_A_CURVE);
                }
                String strCurveSetting = "curve_label:" + ChartOperator.addEscapes(threeDExprSurfaces[idx].mstrCurveLabel)
                        + ";is_grid:" + threeDExprSurfaces[idx].mbIsGrid.toString()
                        + ";min_color:" + ChartOperator.addEscapes(threeDExprSurfaces[idx].mstrMinColor) + ";min_color_1:" + ChartOperator.addEscapes(threeDExprSurfaces[idx].mstrMinColor1)
                        + ";max_color:" + ChartOperator.addEscapes(threeDExprSurfaces[idx].mstrMaxColor) + ";max_color_1:" + ChartOperator.addEscapes(threeDExprSurfaces[idx].mstrMaxColor1)
                        + ";function_variable:" + threeDExprSurfaces[idx].mnFunctionVar
                        + ";x_expr:" + ChartOperator.addEscapes(threeDExprSurfaces[idx].mstrXExpr) + ";x_steps:" + threeDExprSurfaces[idx].mnXNumOfSteps
                        + ";y_expr:" + ChartOperator.addEscapes(threeDExprSurfaces[idx].mstrYExpr) + ";y_steps:" + threeDExprSurfaces[idx].mnYNumOfSteps
                        + ";z_expr:" + ChartOperator.addEscapes(threeDExprSurfaces[idx].mstrZExpr) + ";z_steps:" + threeDExprSurfaces[idx].mnZNumOfSteps;
                strOutput += "\n" + strCurveSetting;
            }
            // shouldn't fill the whole chart, so ugly.
            double dXAxisFrom = (dXTo + dXFrom) / 2.0 - (dXTo - dXFrom) / 2.0 / THREED_EXPR_CHART_AXIS_FILL_RATIO;
            double dXAxisTo = (dXTo + dXFrom) / 2.0 + (dXTo - dXFrom) / 2.0 / THREED_EXPR_CHART_AXIS_FILL_RATIO;
            double dYAxisFrom = (dYTo + dYFrom) / 2.0 - (dYTo - dYFrom) / 2.0 / THREED_EXPR_CHART_AXIS_FILL_RATIO;
            double dYAxisTo = (dYTo + dYFrom) / 2.0 + (dYTo - dYFrom) / 2.0 / THREED_EXPR_CHART_AXIS_FILL_RATIO;
            double dZAxisFrom = (dZTo + dZFrom) / 2.0 - (dZTo - dZFrom) / 2.0 / THREED_EXPR_CHART_AXIS_FILL_RATIO;
            double dZAxisTo = (dZTo + dZFrom) / 2.0 + (dZTo - dZFrom) / 2.0 / THREED_EXPR_CHART_AXIS_FILL_RATIO;
            String strChartSettings = "chart_type:3DExpr;chart_title:" + ChartOperator.addEscapes(strChartTitle)
                    + ";x_title:" + ChartOperator.addEscapes(strXTitle) + ";x_min:" + dXAxisFrom + ";x_max:" + dXAxisTo + ";x_labels:10"
                    + ";y_title:" + ChartOperator.addEscapes(strYTitle) + ";y_min:" + dYAxisFrom + ";y_max:" + dYAxisTo + ";y_labels:10"
                    + ";z_title:" + ChartOperator.addEscapes(strZTitle) + ";z_min:" + dZAxisFrom + ";z_max:" + dZAxisTo + ";z_labels:10";
            strOutput = "chart_name:" + strChartName + ";" + strChartSettings + ";number_of_curves:" + nNumofCurves + strOutput;

            // plot the graph
            if (FuncEvaluator.msgraphPlotter3D != null) {
                try {
                    if (!FuncEvaluator.msgraphPlotter3D.plotGraph(strOutput)) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_PLOT_CHART);
                    }
                } catch (UnsatisfiedLinkError e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_PLOT_OGL_CHART_LACK_SYSTEM_SUPPORT_FILES);
                }
            }

            // save the graph to disk
            if (FuncEvaluator.msfileOperator != null && strChartName.trim().length() != 0) {
                try {
                    FuncEvaluator.msfileOperator.outputGraphFile(strChartName, strOutput);
                } catch (FileNotFoundException e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_FUNCTION_PROCESSION,
                            strCallingFunc, e);
                } catch (IOException e) {
                    if (e instanceof ClosedByInterruptException) {
                        throw new InterruptedException();    // this thread is interrupted by another thread.
                    } else {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_FUNCTION_PROCESSION,
                                strCallingFunc, e);
                    }
                }
            }
        }
    }
    //public final static Plot_3d_expressionsFunction BUILTINFUNC_Plot_3d_expressions = new Plot_3d_expressionsFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Plot_3d_expressionsFunction());}

    public static class Plot_expressionsFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public static final int PARAM_NUM_FOR_CHART_ONLY = 5;
        public static final int PARAM_NUM_IN_ONE_CURVE = 1;
        public Plot_expressionsFunction() {
            mstrProcessedNameWithFullCS = "::mfp::graph_lib::plot_math::plot_expressions";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = PARAM_NUM_FOR_CHART_ONLY + MAX_NUMBER_OF_EXPRS_TO_PLOT * PARAM_NUM_IN_ONE_CURVE;
            mnMinParamNum = PARAM_NUM_FOR_CHART_ONLY + PARAM_NUM_IN_ONE_CURVE;
        }

        // this function plot a group of expressions and draw 2D or 3D charts.
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            int nParamNumForChartOnly = PARAM_NUM_FOR_CHART_ONLY;
            int nParamNumIn1Cv = PARAM_NUM_IN_ONE_CURVE;
            int nMaxNumofCvs = MAX_NUMBER_OF_EXPRS_TO_PLOT;

            String strCallingFunc = mstrarrayFullCS[mstrarrayFullCS.length - 1];

            if (listParams.size() > (nParamNumForChartOnly + nMaxNumofCvs * nParamNumIn1Cv)
                    || listParams.size() < (nParamNumForChartOnly + nParamNumIn1Cv) // should be at least one curve, at most 4 curves.
                    || nParamNumIn1Cv * (int) (((double) (listParams.size() - nParamNumForChartOnly)) / nParamNumIn1Cv) != (listParams.size() - nParamNumForChartOnly)) {
                // The first nParamNumForChartOnly params are for chart definition.
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassString datumChartName = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            DataClassString datumChartTitle = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            DataClassSingleNum datumVarFrom = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
            double dVarFrom = datumVarFrom.getDataValue().doubleValue();
            DataClassSingleNum datumVarTo = DCHelper.lightCvtOrRetDCMFPDec(listParams.removeLast());
            double dVarTo = datumVarTo.getDataValue().doubleValue();
            boolean bUseSystemDefaultPlotRange = false;
            if (!(dVarFrom < dVarTo)) {   // this can handle the case where dVarFrom or To is NAN or INF
                bUseSystemDefaultPlotRange = true;
            }
            DataClassSingleNum datumVarNumOfSteps = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
            int nVarNumOfSteps = (int) datumVarNumOfSteps.getDataValue().longValue();

            String[] strarrayExprs = new String[listParams.size()];
            for (int idx = 0; idx < strarrayExprs.length; idx++) {
                DataClassString datumExpr = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
                String strExpr = datumExpr.getStringValue();
                strarrayExprs[idx] = strExpr;
            }
            LinkedList<AbstractExpr> listaeInputExprs = new LinkedList<AbstractExpr>();
            LinkedList<String> liststrOriginalExprs = new LinkedList<String>();
            LinkedList<AbstractExpr> listaeOriginalExprs = new LinkedList<AbstractExpr>();
            LinkedList<AEVar> listaeInputEqualVars = new LinkedList<AEVar>();
            LinkedList<LinkedList<UnknownVariable>> listlVarUnknownExprsAll = new LinkedList<LinkedList<UnknownVariable>>();
            LinkedList<UnknownVariable> listVarUnknown = new LinkedList<UnknownVariable>();    // The unknown variable list
            LinkedList<int[]> listOriginal2Solved = new LinkedList<int[]>();
            LinkedList<Integer> listSolved2Original = new LinkedList<Integer>();
            for (int idx = 0; idx < strarrayExprs.length; idx++) {
                if (strarrayExprs[idx].trim().length() == 0) {
                    continue;    // empty string
                } else if ((strarrayExprs[idx].length() >= 5 && strarrayExprs[idx].substring(0, 5).toLowerCase(Locale.US).equals("help "))
                        || strarrayExprs[idx].toLowerCase(Locale.US).equals("help")) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);    // help_statement_cannot_be_plotted.
                } else {
                    /* evaluate the expression */
                    CurPos curpos = new CurPos();
                    curpos.m_nPos = 0;
                    AbstractExpr aexpr = AEInvalid.AEINVALID;
                    /* evaluate the expression */
                    try {
                        aexpr = ExprAnalyzer.analyseExpression(strarrayExprs[idx], curpos, new LinkedList<Variable>(), progContext);
                        LinkedList<AbstractExpr> listAEVars = new LinkedList<AbstractExpr>();
                        LinkedList<UnknownVariable> listVarUnknownSingleExpr = new LinkedList<UnknownVariable>();
                        LinkedList<AbstractExpr> listAERootVars = new LinkedList<AbstractExpr>();
                        AbstractExpr[] arrayAEs = new AbstractExpr[1];
                        arrayAEs[0] = aexpr;
                        PtnSlvMultiVarsIdentifier.lookupToSolveVarsInExprs(arrayAEs, listAEVars, listAERootVars, progContext);

                        int nNumOfToSolves = 0;
                        for (int idx1 = 0; idx1 < listAEVars.size(); idx1++) {
                            if (listAEVars.get(idx1) instanceof AEVar) {
                                String strName = ((AEVar) listAEVars.get(idx1)).mstrVariableName;
                                if (VariableOperator.lookUpPreDefined(strName) == null) {    // this variable is not a predefined var nor does it exist
                                    nNumOfToSolves++;
                                    UnknownVariable varUnknown = new UnknownVariable(strName);
                                    // if this variable hasn't been added, add it.
                                    if (UnknownVarOperator.lookUpList(strName, listVarUnknown) == null) {
                                        listVarUnknown.addFirst(varUnknown);    // not use add last coz y=x*sin(x), hope to see y is the second, not the first variable.
                                    }
                                    if (UnknownVarOperator.lookUpList(strName, listVarUnknownSingleExpr) == null) {
                                        listVarUnknownSingleExpr.add(varUnknown);
                                    }
                                }
                            }
                        }

                        if (nNumOfToSolves == 0) {
                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);    // no_variable_to_plot.
                        } else if (aexpr instanceof AEAssign) {
                            if (aexpr.getListOfChildren().size() != 2) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                            } else if (!(aexpr.getListOfChildren().getFirst() instanceof AEVar)) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_ASSIGN_VALUE_TO_ANYTHING_EXCEPT_VARIALBE_USE_EQUAL_INSTEAD_AT_RUN_TIME);    // invalid_expr_to_plot, did_you_use_assign?
                            } else {
                                listaeInputExprs.add(aexpr.getListOfChildren().getLast());
                                listaeInputEqualVars.add((AEVar) aexpr.getListOfChildren().getFirst());
                                liststrOriginalExprs.add(strarrayExprs[idx]);
                                listaeOriginalExprs.add(aexpr);
                                int[] narrayMap = new int[1];
                                narrayMap[0] = listaeInputExprs.size() - 1;
                                listOriginal2Solved.add(narrayMap);
                                listSolved2Original.add(listaeOriginalExprs.size() - 1);
                                listlVarUnknownExprsAll.add(listVarUnknownSingleExpr);
                            }
                        } else if (aexpr instanceof AECompare) {
                            if (((AECompare) aexpr).moptType == OPERATORTYPES.OPERATOR_EQ) {
                                if (aexpr.getListOfChildren().size() != 2) {
                                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);    // does not support multiequations.
                                } else if ((aexpr.getListOfChildren().getFirst() instanceof AEVar) || (aexpr.getListOfChildren().getLast() instanceof AEVar)) {
                                    // let or right is variable
                                    boolean bNoValidUnknownInFirst = true, bNoValidUnknownInLast = true;
                                    if (aexpr.getListOfChildren().getFirst() instanceof AEVar) {
                                        String strVarName = ((AEVar) (aexpr.getListOfChildren().getFirst())).mstrVariableName;
                                        if (UnknownVarOperator.lookUpList(strVarName, listVarUnknownSingleExpr) != null) {
                                            // not a valid unknown var, might be predefined var.
                                            bNoValidUnknownInFirst = false;
                                        }
                                    }
                                    if (bNoValidUnknownInFirst && aexpr.getListOfChildren().getLast() instanceof AEVar) {
                                        String strVarName = ((AEVar) (aexpr.getListOfChildren().getLast())).mstrVariableName;
                                        if (UnknownVarOperator.lookUpList(strVarName, listVarUnknownSingleExpr) != null) {
                                            // not a valid unknown var, might be predefined var.
                                            bNoValidUnknownInLast = false;
                                        }
                                    }
                                    if (bNoValidUnknownInFirst && bNoValidUnknownInLast) {
                                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);    // invalid_expr_to_plot, no variable in either side of the equation.
                                    } else {
                                        if (bNoValidUnknownInFirst) {
                                            listaeInputExprs.add(aexpr.getListOfChildren().getFirst());
                                            listaeInputEqualVars.add((AEVar) aexpr.getListOfChildren().getLast());
                                        } else {
                                            listaeInputExprs.add(aexpr.getListOfChildren().getLast());
                                            listaeInputEqualVars.add((AEVar) aexpr.getListOfChildren().getFirst());
                                        }
                                        liststrOriginalExprs.add(strarrayExprs[idx]);
                                        listaeOriginalExprs.add(aexpr);
                                        int[] narrayMap = new int[1];
                                        narrayMap[0] = listaeInputExprs.size() - 1;
                                        listOriginal2Solved.add(narrayMap);
                                        listSolved2Original.add(listaeOriginalExprs.size() - 1);
                                        listlVarUnknownExprsAll.add(listVarUnknownSingleExpr);
                                    }
                                } else if (listVarUnknownSingleExpr.size() == 2) {
                                    // ok, we have to solve the equation.
                                    // select which variable to solve.
                                    LinkedList<LinkedList<Variable>> lVarNameSpaces = new LinkedList<LinkedList<Variable>>();
                                    LinkedList<Variable> listVars = new LinkedList<Variable>();
                                    listVars.addAll(listVarUnknownSingleExpr);
                                    lVarNameSpaces.add(listVars);
                                    ProgContext progContextTmp = new ProgContext();
                                    progContextTmp.mdynamicProgContext.mlVarNameSpaces = lVarNameSpaces;
                                    // use progContext's citing spaces to guarantee that it call right functions.
                                    progContextTmp.mstaticProgContext.setCitingSpacesExplicitly(progContext.mstaticProgContext.getCitingSpaces());
                                    int nMinAppearanceIdx = 0, nMaxAppearanceIdx = 1;
                                    if (aexpr.getVarAppearanceCnt(listVars.get(nMinAppearanceIdx).getName())
                                            > aexpr.getVarAppearanceCnt(listVars.get(nMaxAppearanceIdx).getName())) {
                                        nMinAppearanceIdx = 1;
                                        nMaxAppearanceIdx = 0;
                                    }
                                    String strEqualVarName = listVars.get(nMinAppearanceIdx).getName();
                                    AEVar aeEqualVarName = new AEVar(strEqualVarName, ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE);
                                    SolveAnalyzer solveAnalyzer = new SolveAnalyzer();  // need to initialize SolveAnalyzer even if solveVarInSingleExpr is static coz mspm needs initialization.
                                    LinkedList<AbstractExpr> listaeSolvedResults = new LinkedList<AbstractExpr>();
                                    try {
                                        listaeSolvedResults = SolveAnalyzer.solveVarInSingleExpr(aexpr, strEqualVarName, listVarUnknownSingleExpr, progContextTmp, true, 0);
                                    } catch (Exception e) {
                                    }
                                    if (listaeSolvedResults.size() == 0) {
                                        // if cannot use one var to solve, then try to use another var.
                                        for (int idxUnknownVar = 0; idxUnknownVar < listVarUnknownSingleExpr.size(); idxUnknownVar++) {
                                            listVarUnknownSingleExpr.get(idxUnknownVar).setValueAssigned(false); // solveVarInSingleExpr may assign value to unknown var
                                        }
                                        strEqualVarName = listVars.get(nMaxAppearanceIdx).getName();
                                        aeEqualVarName = new AEVar(strEqualVarName, ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE);
                                        listaeSolvedResults = SolveAnalyzer.solveVarInSingleExpr(aexpr, strEqualVarName, listVarUnknownSingleExpr, progContextTmp, true, 0);
                                    }
                                    int[] narrayMap = new int[Math.min(4, listaeSolvedResults.size())];
                                    for (int idxResult = 0; idxResult < narrayMap.length; idxResult++) {
                                        // show at most four result.
                                        listaeInputExprs.add(listaeSolvedResults.get(idxResult));
                                        listaeInputEqualVars.add(aeEqualVarName);
                                        narrayMap[idxResult] = listaeInputEqualVars.size() - 1;
                                        listSolved2Original.add(listaeOriginalExprs.size());
                                    }
                                    liststrOriginalExprs.add(strarrayExprs[idx]);
                                    listaeOriginalExprs.add(aexpr);
                                    listOriginal2Solved.add(narrayMap);
                                    listlVarUnknownExprsAll.add(listVarUnknownSingleExpr);
                                } else {    // listVarUnknownSingleExpr.size() == 3
                                    // ok, we have to solve the equation.
                                    // for 3D charts, we solve all the variables.
                                    LinkedList<LinkedList<Variable>> lVarNameSpaces = new LinkedList<LinkedList<Variable>>();
                                    LinkedList<Variable> listVars = new LinkedList<Variable>();
                                    listVars.addAll(listVarUnknownSingleExpr);
                                    lVarNameSpaces.add(listVars);
                                    ProgContext progContextTmp = new ProgContext();
                                    progContextTmp.mdynamicProgContext.mlVarNameSpaces = lVarNameSpaces;
                                    // use progContext's citing spaces to guarantee that it call right functions.
                                    progContextTmp.mstaticProgContext.setCitingSpacesExplicitly(progContext.mstaticProgContext.getCitingSpaces());
                                    LinkedList<AbstractExpr> listaeAllSolvedResults = new LinkedList<AbstractExpr>();
                                    LinkedList<AEVar> listaeAllEqualVarNames = new LinkedList<AEVar>();
                                    for (int idxVar = 0; idxVar < listVars.size(); idxVar++) {
                                        if (idxVar > 0) {
                                            for (int idxUnknownVar = 0; idxUnknownVar < listVarUnknownSingleExpr.size(); idxUnknownVar++) {
                                                listVarUnknownSingleExpr.get(idxUnknownVar).setValueAssigned(false); // solveVarInSingleExpr may assign value to unknown var
                                            }
                                        }
                                        String strEqualVarName = listVars.get(idxVar).getName();
                                        AEVar aeEqualVarName = new AEVar(strEqualVarName, ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE);
                                        SolveAnalyzer solveAnalyzer = new SolveAnalyzer();  // need to initialize SolveAnalyzer even if solveVarInSingleExpr is static coz mspm needs initialization.
                                        LinkedList<AbstractExpr> listaeSolvedResults = new LinkedList<AbstractExpr>();
                                        try {
                                            listaeSolvedResults = SolveAnalyzer.solveVarInSingleExpr(aexpr, strEqualVarName, listVarUnknownSingleExpr, progContextTmp, true, 0);
                                        } catch (Exception e) {
                                        }
                                        if (listaeSolvedResults.size() == 1) {
                                            // ok, only one solution. Good, use it as we need not to consider other solutions based on other vars
                                            listaeAllSolvedResults.clear();
                                            listaeAllEqualVarNames.clear();
                                            listaeAllSolvedResults.add(listaeSolvedResults.getFirst());
                                            listaeAllEqualVarNames.add(aeEqualVarName);
                                            break;
                                        }
                                        for (int idxResult = 0; idxResult < Math.min(2, listaeSolvedResults.size()); idxResult++) {
                                            listaeAllSolvedResults.add(listaeSolvedResults.get(idxResult));
                                            listaeAllEqualVarNames.add(aeEqualVarName);
                                        }
                                    }
                                    int[] narrayMap = new int[listaeAllSolvedResults.size()];
                                    for (int idxResult = 0; idxResult < narrayMap.length; idxResult++) {
                                        // show at most 6 result.
                                        listaeInputExprs.add(listaeAllSolvedResults.get(idxResult));
                                        listaeInputEqualVars.add(listaeAllEqualVarNames.get(idxResult));
                                        narrayMap[idxResult] = listaeInputEqualVars.size() - 1;
                                        listSolved2Original.add(listaeOriginalExprs.size());
                                    }
                                    liststrOriginalExprs.add(strarrayExprs[idx]);
                                    listaeOriginalExprs.add(aexpr);
                                    listOriginal2Solved.add(narrayMap);
                                    listlVarUnknownExprsAll.add(listVarUnknownSingleExpr);
                                }
                            } else {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);    // invalid_expr_to_plot, not == for AECompare.
                            }
                        } else {
                            listaeInputExprs.add(aexpr);
                            listaeInputEqualVars.add(new AEVar());    // invalid AEVar
                            listlVarUnknownExprsAll.add(listVarUnknownSingleExpr);
                            liststrOriginalExprs.add(strarrayExprs[idx]);
                            listaeOriginalExprs.add(aexpr);
                            int[] narrayMap = new int[1];
                            narrayMap[0] = listaeInputExprs.size() - 1;
                            listOriginal2Solved.add(narrayMap);
                            listSolved2Original.add(liststrOriginalExprs.size() - 1);
                        }
                    } catch (Exception e) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);    // invalid_expr_to_plot, cannot analyze expression.
                    }
                }
            }

            if (listaeInputExprs.size() == 0) {
                // no expression to plot, throw cannot plot chart exception.
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_PLOT_CHART);
            } else if (listVarUnknown.size() > 3) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_PLOT_CHART);    // cannot_plot_more_than_3d_chart
            } else {
                boolean bPlot3DChart = false;
                if (listVarUnknown.size() == 3) {
                    bPlot3DChart = true;
                } else if (listVarUnknown.size() == 2) {
                    boolean bAllOmitted2Vars = true;
                    for (int idx = 0; idx < listaeOriginalExprs.size(); idx++) {
                        if ((listaeOriginalExprs.get(idx) instanceof AECompare)
                                || (listaeOriginalExprs.get(idx) instanceof AEAssign)
                                || listlVarUnknownExprsAll.get(idx).size() != 2) {
                            bAllOmitted2Vars = false;
                        }
                    }
                    if (bAllOmitted2Vars) {    // all the expressions are like x*y, y+z, etc.
                        bPlot3DChart = true;
                        listVarUnknown.add(new UnknownVariable(listVarUnknown.get(0).getName() + "_" + listVarUnknown.get(1).getName()));
                    } //else some of the exprs are like sin(x) == y, bPlot3DChart is false
                } else if (listVarUnknown.size() == 1) {
                    listVarUnknown.add(new UnknownVariable("f_" + listVarUnknown.get(0).getName()));
                }
                if (bPlot3DChart) {
                    // three D chart
                    if (listaeInputExprs.size() > nMaxNumofCvs) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_PLOT_CHART_TOO_MANY_CURVES);    // cannot_plot_more_than_8_curves
                    }

                    if (bUseSystemDefaultPlotRange) {
                        dVarFrom = MFPAdapter.getPlotChartVariableFrom();
                        dVarTo = MFPAdapter.getPlotChartVariableTo();
                    }
                    LinkedList<DataClass> listNewParams = new LinkedList<DataClass>();
                    listNewParams.addFirst(datumChartName);
                    listNewParams.addFirst(datumChartTitle);
                    DataClass datumVarUnknown1 = new DataClassString(listVarUnknown.get(0).getName().trim());
                    listNewParams.addFirst(datumVarUnknown1);
                    DataClass datumVar1From = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dVarFrom, true));
                    listNewParams.addFirst(datumVar1From);
                    DataClass datumVar1To = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dVarTo, true));
                    listNewParams.addFirst(datumVar1To);
                    DataClass datumVarUnknown2 = new DataClassString(listVarUnknown.get(1).getName().trim());
                    listNewParams.addFirst(datumVarUnknown2);
                    DataClass datumVar2From = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dVarFrom, true));
                    listNewParams.addFirst(datumVar2From);
                    DataClass datumVar2To = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dVarTo, true));
                    listNewParams.addFirst(datumVar2To);
                    DataClass datumVarUnknown3 = new DataClassString(listVarUnknown.get(2).getName().trim());
                    listNewParams.addFirst(datumVarUnknown3);
                    DataClass datumVar3From = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dVarFrom, true));
                    listNewParams.addFirst(datumVar3From);
                    DataClass datumVar3To = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dVarTo, true));
                    listNewParams.addFirst(datumVar3To);
                    for (int idx = 0; idx < listaeInputExprs.size(); idx++) {
                        String strXExpr = "", strYExpr = "", strZExpr = "";
                        AbstractExpr aexpr = listaeInputExprs.get(idx);
                        int idxOriginal = listSolved2Original.get(idx);
                        AbstractExpr aexprOriginal = listaeOriginalExprs.get(idxOriginal);
                        if (!(aexprOriginal instanceof AECompare) && !(aexprOriginal instanceof AEAssign)) {
                            if (listlVarUnknownExprsAll.get(idxOriginal).size() != listVarUnknown.size() - 1) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_PLOT_CHART);  // cannot_plot, number of unknown variables in this parameter is not equal to total unkowns - 1.
                            } else {
                                for (int idx2 = 0; idx2 < listVarUnknown.size(); idx2++) {
                                    if (UnknownVarOperator.lookUpList(listVarUnknown.get(idx2).getName(), listlVarUnknownExprsAll.get(idxOriginal)) == null) {
                                        try {
                                            listaeInputEqualVars.set(idx, new AEVar(listVarUnknown.get(idx2).getName(), ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE));
                                        } catch (JSmartMathErrException e) {
                                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_PLOT_CHART);  // cannot_plot, cannot set variable's name.
                                        }
                                    }
                                }
                            }
                        }
                        int nFunctionVar = 2;
                        for (int idx1 = 0; idx1 < listVarUnknown.size(); idx1++) {
                            if (listVarUnknown.get(idx1).getName().compareToIgnoreCase(listaeInputEqualVars.get(idx).mstrVariableName) == 0) {
                                // in x + y case this is z
                                nFunctionVar = idx1;
                                try {
                                    if (idx1 == 0) {
                                        strXExpr = aexpr.output();
                                    } else if (idx1 == 1) {
                                        strYExpr = aexpr.output();
                                    } else {
                                        strZExpr = aexpr.output();
                                    }
                                } catch (Exception e) {
                                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_PLOT_CHART);  // cannot_plot, cannot convert an expression to string.
                                }
                            } else {
                                String strExpr = listVarUnknown.get(idx1).getName();
                                if (idx1 == 0) {
                                    strXExpr = strExpr;
                                } else if (idx1 == 1) {
                                    strYExpr = strExpr;
                                } else {
                                    strZExpr = strExpr;
                                }
                            }
                        }
                        DataClass datumExpr = new DataClassString(liststrOriginalExprs.get(idxOriginal));
                        liststrOriginalExprs.set(idxOriginal, "");  //strOriginalExpr no longer used
                        int nNumOfSolvedExprsFromOriginal = listOriginal2Solved.get(idxOriginal).length;
                        listNewParams.addFirst(datumExpr);
                        DataClass datumIsGrid = new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.ZERO);
                        listNewParams.addFirst(datumIsGrid);
                        String strColor1 = ((idxOriginal == 0) ? "cyan" : ((idxOriginal == 1) ? "magenta" : ((idxOriginal == 2) ? "white" : ((idxOriginal == 3) ? "gray" : ((idxOriginal == 4) ? "blue" : ((idxOriginal == 5) ? "green" : ((idxOriginal == 6) ? "yellow" : "red")))))));
                        DataClass datumColor1 = new DataClassString(strColor1);
                        String strColor2 = ((idxOriginal == 0) ? "cyan" : ((idxOriginal == 1) ? "magenta" : ((idxOriginal == 2) ? "white" : ((idxOriginal == 3) ? "gray" : ((idxOriginal == 4) ? "blue" : ((idxOriginal == 5) ? "green" : ((idxOriginal == 6) ? "yellow" : "red")))))));
                        DataClass datumColor2 = new DataClassString(strColor2);
                        String strColor3 = ((idxOriginal == 0) ? "red" : ((idxOriginal == 1) ? "green" : ((idxOriginal == 2) ? "blue" : ((idxOriginal == 3) ? "yellow" : ((idxOriginal == 4) ? "magenta" : ((idxOriginal == 5) ? "white" : ((idxOriginal == 6) ? "cyan" : "gray")))))));
                        DataClass datumColor3 = new DataClassString(strColor3);
                        if (nNumOfSolvedExprsFromOriginal > 1) {
                            datumColor1 = new DataClassString(strColor3);  // if more than 1 solved expressions for this original expression, we use unique color.
                        }
                        String strColor4 = ((idxOriginal == 0) ? "red" : ((idxOriginal == 1) ? "green" : ((idxOriginal == 2) ? "blue" : ((idxOriginal == 3) ? "yellow" : ((idxOriginal == 4) ? "magenta" : ((idxOriginal == 5) ? "white" : ((idxOriginal == 6) ? "cyan" : "gray")))))));
                        DataClass datumColor4 = new DataClassString(strColor4);
                        if (nNumOfSolvedExprsFromOriginal > 1) {
                            datumColor2 = new DataClassString(strColor4);  // if more than 1 solved expressions for this original expression, we use unique color.
                        }
                        listNewParams.addFirst(datumColor1);
                        listNewParams.addFirst(datumColor2);
                        listNewParams.addFirst(datumColor3);
                        listNewParams.addFirst(datumColor4);
                        DataClass datumFunctionVar = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nFunctionVar));
                        listNewParams.addFirst(datumFunctionVar);
                        DataClass datumXExpr = new DataClassString(strXExpr);
                        listNewParams.addFirst(datumXExpr);
                        DataClass datumXNumOfSteps = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nVarNumOfSteps));
                        listNewParams.addFirst(datumXNumOfSteps);
                        DataClass datumYExpr = new DataClassString(strYExpr);
                        listNewParams.addFirst(datumYExpr);
                        DataClass datumYNumOfSteps = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nVarNumOfSteps));
                        listNewParams.addFirst(datumYNumOfSteps);
                        DataClass datumZExpr = new DataClassString(strZExpr);
                        listNewParams.addFirst(datumZExpr);
                        DataClass datumZNumOfSteps = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nVarNumOfSteps));
                        listNewParams.addFirst(datumZNumOfSteps);
                    }
                    //call the underlying plot3DExprSurfaces function.
                    Plot_3d_expressionsFunction.plot3DExprSurfaces(strCallingFunc, listNewParams, progContext);
                } else {
                    // two D chart, we still have at most 4 curves coz color resource is limited.
                    if (listaeInputExprs.size() > nMaxNumofCvs) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_PLOT_CHART_TOO_MANY_CURVES);    // cannot_plot_more_than_4_curves
                    }
                    LinkedList<DataClass> listNewParams = new LinkedList<DataClass>();
                    listNewParams.addFirst(datumChartName);
                    listNewParams.addFirst(datumChartTitle);

                    String strChartType = "2DExpr";
                    double dTheOtherVarFrom = dVarFrom;
                    double dTheOtherVarTo = dVarTo;
                    if (listVarUnknown.get(0).getName().trim().equals("\u03B1") // alpha
                            || listVarUnknown.get(0).getName().trim().equals("\u03B2") // beta
                            || listVarUnknown.get(0).getName().trim().equals("\u03B3") // gamma
                            || listVarUnknown.get(0).getName().trim().equals("\u03B8")) {// theta
                        strChartType = "polarExpr";
                        if (bUseSystemDefaultPlotRange) {
                            dVarFrom = 0;
                            dVarTo = Math.max(Math.abs(MFPAdapter.getPlotChartVariableFrom()),
                                    Math.abs(MFPAdapter.getPlotChartVariableTo()));
                            dTheOtherVarFrom = 0;
                            dTheOtherVarTo = 2 * Math.PI;
                        }
                        // because the first variable is angle, needs to swap first and second variables.
                        UnknownVariable varTemp = listVarUnknown.get(1);
                        listVarUnknown.set(1, listVarUnknown.get(0));
                        listVarUnknown.set(0, varTemp);
                    } else {
                        if (listVarUnknown.get(1).getName().trim().equals("\u03B1") // alpha
                                || listVarUnknown.get(1).getName().trim().equals("\u03B2") // beta
                                || listVarUnknown.get(1).getName().trim().equals("\u03B3") // gamma
                                || listVarUnknown.get(1).getName().trim().equals("\u03B8")) {// theta
                            strChartType = "polarExpr";
                            if (bUseSystemDefaultPlotRange) {
                                dVarFrom = 0;
                                dVarTo = Math.max(Math.abs(MFPAdapter.getPlotChartVariableFrom()),
                                        Math.abs(MFPAdapter.getPlotChartVariableTo()));
                                dTheOtherVarFrom = 0;
                                dTheOtherVarTo = 2 * Math.PI;
                            }
                        } else {
                            if (bUseSystemDefaultPlotRange) {
                                dVarFrom = MFPAdapter.getPlotChartVariableFrom();
                                dVarTo = MFPAdapter.getPlotChartVariableTo();
                                dTheOtherVarFrom = MFPAdapter.getPlotChartVariableFrom();
                                dTheOtherVarTo = MFPAdapter.getPlotChartVariableTo();
                            }
                        }
                    }

                    DataClass datumVarUnknown1 = new DataClassString(listVarUnknown.get(0).getName().trim());
                    listNewParams.addFirst(datumVarUnknown1);
                    DataClass datumVar1From = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dVarFrom, true));
                    listNewParams.addFirst(datumVar1From);
                    DataClass datumVar1To = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dVarTo, true));
                    listNewParams.addFirst(datumVar1To);
                    DataClass datumVarUnknown2 = new DataClassString(listVarUnknown.get(1).getName().trim());
                    listNewParams.addFirst(datumVarUnknown2);
                    DataClass datumVar2From = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dTheOtherVarFrom, true));
                    listNewParams.addFirst(datumVar2From);
                    DataClass datumVar2To = new DataClassSingleNum(DATATYPES.DATUM_MFPDEC, new MFPNumeric(dTheOtherVarTo, true));
                    listNewParams.addFirst(datumVar2To);
                    DataClass datumBKColor = new DataClassString("black");
                    listNewParams.addFirst(datumBKColor);
                    DataClass datumShowGrid = new DataClassString("true");
                    listNewParams.addFirst(datumShowGrid);
                    for (int idx = 0; idx < listaeInputExprs.size(); idx++) {
                        String strXExpr = "", strYExpr = "";
                        AbstractExpr aexpr = listaeInputExprs.get(idx);
                        int idxOriginal = listSolved2Original.get(idx);
                        AbstractExpr aexprOriginal = listaeOriginalExprs.get(idxOriginal);
                        if (!(aexprOriginal instanceof AECompare) && !(aexprOriginal instanceof AEAssign)) {
                            if (listlVarUnknownExprsAll.get(idxOriginal).size() != listVarUnknown.size() - 1) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_PLOT_CHART);  // cannot_plot, number of unknown variables in this parameter is not equal to total unkowns - 1.
                            } else {
                                for (int idx2 = 0; idx2 < listVarUnknown.size(); idx2++) {
                                    if (UnknownVarOperator.lookUpList(listVarUnknown.get(idx2).getName(), listlVarUnknownExprsAll.get(idxOriginal)) == null) {
                                        try {
                                            listaeInputEqualVars.set(idx, new AEVar(listVarUnknown.get(idx2).getName(), ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE));
                                        } catch (JSmartMathErrException e) {
                                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_PLOT_CHART);  // cannot_plot, cannot set variable's name.
                                        }
                                    }
                                }
                            }
                        }
                        int nFunctionVar = 1;
                        for (int idx1 = 0; idx1 < listVarUnknown.size(); idx1++) {
                            if (listVarUnknown.get(idx1).getName().compareToIgnoreCase(listaeInputEqualVars.get(idx).mstrVariableName) == 0) {
                                // in sin(x) case this is y
                                nFunctionVar = idx1;
                                try {
                                    if (idx1 == 0) {
                                        strXExpr = aexpr.output();
                                    } else {
                                        strYExpr = aexpr.output();
                                    }
                                } catch (Exception e) {
                                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_PLOT_CHART);  // cannot_plot, cannot convert an expression to string.
                                }
                            } else {
                                String strExpr = listVarUnknown.get(idx1).getName();
                                if (idx1 == 0) {
                                    strXExpr = strExpr;
                                } else {
                                    strYExpr = strExpr;
                                }
                            }
                        }
                        DataClass datumExpr = new DataClassString(liststrOriginalExprs.get(idxOriginal));
                        liststrOriginalExprs.set(idxOriginal, "");  //strOriginalExpr no longer used
                        listNewParams.addFirst(datumExpr);
                        String strPntColor = ((idxOriginal == 0) ? "red" : ((idxOriginal == 1) ? "green" : ((idxOriginal == 2) ? "blue" : ((idxOriginal == 3) ? "yellow" : ((idxOriginal == 4) ? "magenta" : ((idxOriginal == 5) ? "white" : ((idxOriginal == 6) ? "cyan" : "gray")))))));
                        DataClass datumPntColor = new DataClassString(strPntColor);
                        listNewParams.addFirst(datumPntColor);
                        String strPntShape = "dot";
                        DataClass datumPntShape = new DataClassString(strPntShape);
                        listNewParams.addFirst(datumPntShape);
                        DataClass datumPntSize = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                        listNewParams.addFirst(datumPntSize);
                        String strLnColor = ((idxOriginal == 0) ? "red" : ((idxOriginal == 1) ? "green" : ((idxOriginal == 2) ? "blue" : ((idxOriginal == 3) ? "yellow" : ((idxOriginal == 4) ? "magenta" : ((idxOriginal == 5) ? "white" : ((idxOriginal == 6) ? "cyan" : "gray")))))));
                        DataClass datumLnColor = new DataClassString(strLnColor);
                        listNewParams.addFirst(datumLnColor);
                        String strLnStyle = "solid";
                        DataClass datumLnStyle = new DataClassString(strLnStyle);
                        listNewParams.addFirst(datumLnStyle);
                        DataClass datumLnSize = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, MFPNumeric.ONE);
                        listNewParams.addFirst(datumLnSize);
                        DataClass datumFunctionVar = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nFunctionVar));
                        listNewParams.addFirst(datumFunctionVar);
                        DataClass datumXExpr = new DataClassString(strXExpr);
                        listNewParams.addFirst(datumXExpr);
                        DataClass datumYExpr = new DataClassString(strYExpr);
                        listNewParams.addFirst(datumYExpr);
                        DataClass datumNumOfSteps = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(nVarNumOfSteps));
                        listNewParams.addFirst(datumNumOfSteps);
                    }
                    //call the underlying plot2DExprCurves function.
                    Plot2DExprCurvesFunction.Plot2DExprCurves(strCallingFunc, strChartType, listNewParams, progContext);
                }
            }
            return null;
        }
    }
    //public final static Plot_expressionsFunction BUILTINFUNC_Plot_expressions = new Plot_expressionsFunction();
    static { CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Plot_expressionsFunction());}
}
