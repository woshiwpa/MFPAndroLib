/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.PlotAdapter;

import com.cyzapps.VisualMFP.DataSeriesGridSurface;
import com.cyzapps.VisualMFP.Position3D;
import com.cyzapps.VisualMFP.SurfaceStyle;
import com.cyzapps.VisualMFP.SurfaceStyle.SURFACETYPE;

import android.content.Context;

/**
 *
 * @author tonyc
 */
public class OGLChartOperator extends com.cyzapps.adapter.OGLChartOperator implements ICreateChart {
	/* create chart */
	@Override
	public MFPChart createChart(ChartCreationParam ccpParam1, Context context) {
		
        OGLChart oglChart = new OGLChart(context);
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
        
        for (int nIndex = 0; nIndex < mnNumofCurves; nIndex ++)	{
    		DataSeriesGridSurface dsgs = new DataSeriesGridSurface("xy");
            int nDim1 = m3DSurfaces[nIndex].mdblarrayX.length;
            int nDim2 = m3DSurfaces[nIndex].mdblarrayX[0].length;
            Position3D[][] arrayCvtPnts = new Position3D[nDim1][nDim2];
            for (int idx1 = 0; idx1 < nDim1; idx1 ++)   {
                for (int idx2 = 0; idx2 < nDim2; idx2 ++)   {
                    arrayCvtPnts[idx1][idx2] = new Position3D(m3DSurfaces[nIndex].mdblarrayX[idx1][idx2],
                            m3DSurfaces[nIndex].mdblarrayY[idx1][idx2], m3DSurfaces[nIndex].mdblarrayZ[idx1][idx2]);
                }
            }
            dsgs.mstrName = m3DSurfaces[nIndex].mstrCurveLabel;
            dsgs.setByMatrix(arrayCvtPnts);
            dsgs.msurfaceStyle = new SurfaceStyle();
            dsgs.msurfaceStyle.menumSurfaceType = m3DSurfaces[nIndex].mbIsGrid?SURFACETYPE.SURFACETYPE_GRID:SURFACETYPE.SURFACETYPE_SURFACE;
            dsgs.msurfaceStyle.mclrUpFaceMin = cvtStr2VMFPColor(m3DSurfaces[nIndex].mstrMinColor);
            dsgs.msurfaceStyle.mclrDownFaceMin = cvtStr2VMFPColor(m3DSurfaces[nIndex].mstrMinColor1);
            if (m3DSurfaces[nIndex].mdMinColorValue == null)    {
                dsgs.msurfaceStyle.mdUpFaceMinValue = dsgs.msurfaceStyle.mdDownFaceMinValue
                    = ((dsgs.getMode() == 2)?dsgs.getMinCvtedY():((dsgs.getMode()==1)?dsgs.getMinCvtedX():dsgs.getMinCvtedZ()));
            } else  {
                dsgs.msurfaceStyle.mdUpFaceMinValue = dsgs.msurfaceStyle.mdDownFaceMinValue = m3DSurfaces[nIndex].mdMinColorValue;
            }
            dsgs.msurfaceStyle.mclrUpFaceMax = cvtStr2VMFPColor(m3DSurfaces[nIndex].mstrMaxColor);
            dsgs.msurfaceStyle.mclrDownFaceMax = cvtStr2VMFPColor(m3DSurfaces[nIndex].mstrMaxColor1);
            if (m3DSurfaces[nIndex].mdMaxColorValue == null)    {
                dsgs.msurfaceStyle.mdUpFaceMaxValue = dsgs.msurfaceStyle.mdDownFaceMaxValue
                    = ((dsgs.getMode() == 2)?dsgs.getMaxCvtedY():((dsgs.getMode()==1)?dsgs.getMaxCvtedX():dsgs.getMaxCvtedZ()));
            } else  {
                dsgs.msurfaceStyle.mdUpFaceMaxValue = dsgs.msurfaceStyle.mdDownFaceMaxValue = m3DSurfaces[nIndex].mdMaxColorValue;
            }
        

    		oglChart.mDataSet.add(dsgs);
        }
        
        return oglChart;
	}

}
