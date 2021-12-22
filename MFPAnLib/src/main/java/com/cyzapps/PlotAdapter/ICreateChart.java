/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.PlotAdapter;

import android.content.Context;

import com.cyzapps.adapter.ChartOperator.ChartCreationParam;

/**
 *
 * @author tony
 */
public interface ICreateChart {
    public MFPChart createChart(ChartCreationParam ccpParam1, Context context);
}
