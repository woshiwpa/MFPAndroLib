package com.cyzapps.PlotAdapter;

import android.content.Context;

import com.cyzapps.VisualMFP.DataSeriesCurve;
import com.cyzapps.VisualMFP.LineStyle;
import com.cyzapps.VisualMFP.PointStyle;
import com.cyzapps.VisualMFP.Position3D;
import com.cyzapps.VisualMFP.LineStyle.LINEPATTERN;

public class XYChartOperator extends com.cyzapps.adapter.XYChartOperator implements ICreateChart {

	/* create chart */
	@Override
	public MFPChart createChart(ChartCreationParam ccpParam1, Context context) {
		
        FlatChart flatChart = new XYChart(context);
        flatChart.mstrChartTitle = mstrChartTitle;
        flatChart.mcolorBkGrnd = new com.cyzapps.VisualMFP.Color(255, 0, 0, 0);
        flatChart.mcolorForeGrnd = new com.cyzapps.VisualMFP.Color(255, 200, 200, 200);
        ((XYChart)flatChart).mstrXAxisName = mstrXTitle;
        ((XYChart)flatChart).mstrYAxisName = mstrYTitle;
        ((XYChart)flatChart).mbShowGrid = mbShowGrid;
        
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
        
        ((XYChart)flatChart).mdXMark1 = 0;
        ((XYChart)flatChart).mdXMark2 = dXMarkInterval;
        ((XYChart)flatChart).mdYMark1 = 0;
        ((XYChart)flatChart).mdYMark2 = dYMarkInterval;
        
        ((XYChart)flatChart).mdXAxisLenInFROM = (mdblXMax - mdblXMin);
        ((XYChart)flatChart).mdYAxisLenInFROM = (mdblYMax - mdblYMin);
		
        ((XYChart)flatChart).mp3CoordLeftBottomInFROM = new Position3D(mdblXMin, mdblYMin);
        
        flatChart.mbUseInit2CalibrateZoom = true;
        flatChart.saveSettings();
        
        for (int nIndex = 0; nIndex < mnNumofCurves; nIndex ++)	{
    		DataSeriesCurve dsv = new DataSeriesCurve();
    		dsv.mstrName = mXYCurves[nIndex].mstrCurveLabel;
    		dsv.mpointStyle = new PointStyle();
    		dsv.mpointStyle.mclr = cvtStr2VMFPColor(mXYCurves[nIndex].mstrPntColor.trim().equals("")?
					(mXYCurves[nIndex].mstrLnColor.trim().equals("")?mXYCurves[nIndex].mstrColor:mXYCurves[nIndex].mstrLnColor)
					:mXYCurves[nIndex].mstrPntColor);
    		dsv.mpointStyle.menumPointShape =  cvtStr2PntShape(mXYCurves[nIndex].mstrPointStyle);
    		dsv.mpointStyle.mdSize = flatChart.mdVerySmallSize;
    		dsv.mlineStyle = new LineStyle();
    		dsv.mlineStyle.mclr = cvtStr2VMFPColor(mXYCurves[nIndex].mstrLnColor.trim().equals("")?
    				mXYCurves[nIndex].mstrColor:mXYCurves[nIndex].mstrLnColor);
    		dsv.mlineStyle.menumLinePattern = (mXYCurves[nIndex].mnLnSize <= 0)?LINEPATTERN.LINEPATTERN_NON:LINEPATTERN.LINEPATTERN_SOLID;
    		dsv.mlineStyle.mdLineWidth = (mXYCurves[nIndex].mnLnSize < 0)?0:mXYCurves[nIndex].mnLnSize;
    		
    		for (int idx = 0; idx < mXYCurves[nIndex].mdbllistX.length; idx ++)	{
    			dsv.add(new Position3D(mXYCurves[nIndex].mdbllistX[idx], mXYCurves[nIndex].mdbllistY[idx]));
    		}
    		((XYChart)flatChart).mDataSet.add(dsv);
        }
        
        return flatChart;
	}

}
