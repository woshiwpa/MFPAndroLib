package com.cyzapps.PlotAdapter;

import android.content.Context;

import com.cyzapps.VisualMFP.Position3D;

public class OGLExprChartOperator extends com.cyzapps.adapter.OGLExprChartOperator implements ICreateChart {
	/* create chart */
	@Override
	public MFPChart createChart(ChartCreationParam ccpParam1, Context context) {
		
        OGLExprChart oglChart = new OGLExprChart(context);
        oglChart.mstrChartTitle = mstrChartTitle;
        oglChart.mstrXAxisName = mstrXTitle;
        oglChart.mstrYAxisName = mstrYTitle;
        oglChart.mstrZAxisName = mstrZTitle;
        
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
               
        double dZMarkInterval = (mdblZMax - mdblZMin)/ccpParam1.mnRecommendedNumOfMarksPerAxis;
        dTmp1 = Math.pow(10, Math.floor(Math.log10(dZMarkInterval)));
        dTmp = dZMarkInterval/dTmp1;
        if (dTmp >= 7.5)	{
        	dZMarkInterval = dTmp1 * 10;
        } else if (dTmp >= 3.5)	{
        	dZMarkInterval = dTmp1 * 5;
        } else if (dTmp >= 1.5)	{
        	dZMarkInterval = dTmp1 * 2;
        } else	{
        	dZMarkInterval = dTmp1;
        }
        
        oglChart.mdXMark1 = 0;
        oglChart.mdXMark2 = dXMarkInterval;
        oglChart.mdYMark1 = 0;
        oglChart.mdYMark2 = dYMarkInterval;
        oglChart.mdZMark1 = 0;
        oglChart.mdZMark2 = dZMarkInterval;
        
        oglChart.mdXAxisLenInFROM = (mdblXMax - mdblXMin);
        oglChart.mdYAxisLenInFROM = (mdblYMax - mdblYMin);
        oglChart.mdZAxisLenInFROM = (mdblZMax - mdblZMin);
		
        oglChart.mp3OriginInFROM = new Position3D((mdblXMax + mdblXMin)/2.0, (mdblYMax + mdblYMin)/2.0, (mdblZMax + mdblZMin)/2.0);
        
        // save calculator_settings here if needed.
        oglChart.saveSettings();
        
        oglChart.m3DExprSurfaces = m3DExprSurfaces;
        
        return oglChart;
	}
}
