package com.cyzapps.PlotAdapter;

import android.content.Context;
import com.cyzapps.Jfcalc.TwoDExprDataCache;
import com.cyzapps.VisualMFP.Position3D;

public class XYExprChartOperator extends com.cyzapps.adapter.XYExprChartOperator implements ICreateChart {
	/* create chart */
	@Override
	public MFPChart createChart(ChartCreationParam ccpParam1, Context context) {
		
        XYExprChart xyExprChart = new XYExprChart(context);
        xyExprChart.mstrChartTitle = mstrChartTitle;
        xyExprChart.mcolorBkGrnd = new com.cyzapps.VisualMFP.Color(255, 0, 0, 0);
        xyExprChart.mcolorForeGrnd = new com.cyzapps.VisualMFP.Color(255, 200, 200, 200);
        xyExprChart.mstrXAxisName = mstrXTitle;
        xyExprChart.mstrYAxisName = mstrYTitle;
        xyExprChart.mbShowGrid = mbShowGrid;
        
        double dXMarkInterval = (mdblXMax - mdblXMin)/ccpParam1.mnRecommendedNumOfMarksPerAxis;
        double dTmp1 = Math.pow(10, Math.floor(Math.log10(dXMarkInterval)));
        double dTmp = dXMarkInterval/dTmp1;
        if (dTmp >= 7.5)	{
        	dXMarkInterval = dTmp1 * 10;
        } else if (dTmp >= 3.5)	{
        	dXMarkInterval = dTmp1 * 5;
        } else if (dTmp >= 1.5)	{
        	dXMarkInterval = dTmp1 * 2;
        } else	{
        	dXMarkInterval = dTmp1;
        }
        
        double dYMarkInterval = (mdblYMax - mdblYMin)/ccpParam1.mnRecommendedNumOfMarksPerAxis;
        dTmp1 = Math.pow(10, Math.floor(Math.log10(dYMarkInterval)));
        dTmp = dYMarkInterval/dTmp1;
        if (dTmp >= 7.5)	{
        	dYMarkInterval = dTmp1 * 10;
        } else if (dTmp >= 3.5)	{
        	dYMarkInterval = dTmp1 * 5;
        } else if (dTmp >= 1.5)	{
        	dYMarkInterval = dTmp1 * 2;
        } else	{
        	dYMarkInterval = dTmp1;
        }        
               
        xyExprChart.mdXMark1 = 0;
        xyExprChart.mdXMark2 = dXMarkInterval;
        xyExprChart.mdYMark1 = 0;
        xyExprChart.mdYMark2 = dYMarkInterval;
        
        xyExprChart.mdXAxisLenInFROM = (mdblXMax - mdblXMin);
        xyExprChart.mdYAxisLenInFROM = (mdblYMax - mdblYMin);
		
        xyExprChart.mp3CoordLeftBottomInFROM = new Position3D(mdblXMin, mdblYMin);
        
        xyExprChart.mbUseInit2CalibrateZoom = true;
        xyExprChart.saveSettings();
        
        xyExprChart.m2DExprCurves = m2DExprCurves;
        xyExprChart.m2DExprDataCaches = new TwoDExprDataCache[m2DExprCurves.length];
        for (int idx = 0; idx < m2DExprCurves.length; idx ++) {
            xyExprChart.m2DExprDataCaches[idx] = new TwoDExprDataCache();
        }
        
        return xyExprChart;
	}
}
