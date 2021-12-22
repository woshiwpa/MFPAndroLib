package com.cyzapps.PlotAdapter;

import android.content.Context;
import com.cyzapps.Jfcalc.TwoDExprDataCache;

public class PolarExprChartOperator extends com.cyzapps.adapter.PolarExprChartOperator implements ICreateChart {

	/* create chart */
	@Override
	public MFPChart createChart(ChartCreationParam ccpParam1, Context context) {
		
        PolarExprChart polarExprChart = new PolarExprChart(context);
        polarExprChart.mstrChartTitle = mstrChartTitle;
        polarExprChart.mcolorBkGrnd = new com.cyzapps.VisualMFP.Color(255, 0, 0, 0);
        polarExprChart.mcolorForeGrnd = new com.cyzapps.VisualMFP.Color(255, 200, 200, 200);
        polarExprChart.mstrXAxisName = mstrXTitle;
        polarExprChart.mstrYAxisName = mstrYTitle;
        polarExprChart.mbShowGrid = mbShowGrid;
        
        polarExprChart.mdXAxisLenInFROM = Math.max(Math.abs(mdblXMax), Math.abs(mdblXMin));	// support negative value.
        double dXAxisShownRange = ((mdblXMax * mdblXMin) >= 0)?Math.abs(mdblXMax - mdblXMin)
        		:polarExprChart.mdXAxisLenInFROM;
        double dXMarkInterval = dXAxisShownRange/ccpParam1.mnRecommendedNumOfMarksPerAxis;
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
        
        double dYMarkInterval = (mdblYMax - mdblYMin)/8;

        polarExprChart.mdXMark1 = 0;
        polarExprChart.mdXMark2 = dXMarkInterval;
        polarExprChart.mdYMark1 = 0;
        polarExprChart.mdYMark2 = dYMarkInterval;
        
        polarExprChart.mcaYAxis.mdValueFrom = mdblYMin;
        polarExprChart.mcaYAxis.mdValueTo = mdblYMax;

        polarExprChart.mbUseInit2CalibrateZoom = true;
        polarExprChart.saveSettings();
        
        polarExprChart.m2DExprCurves = m2DExprCurves;
        polarExprChart.m2DExprDataCaches = new TwoDExprDataCache[m2DExprCurves.length];
        for (int idx = 0; idx < m2DExprCurves.length; idx ++) {
        	polarExprChart.m2DExprDataCaches[idx] = new TwoDExprDataCache();
        }
        
        return polarExprChart;
	}
}
