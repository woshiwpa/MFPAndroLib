// MFP project, PolarChartOperator.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.adapter;

/**
 * @author tony
 * This class is a sub class of ChartOperator. It does not include createChart function.
 * This function will be placed in a separated interface.
 */
public class PolarChartOperator extends XYChartOperator {
    public String mstrChartType = "multiRangle";
    
    public PolarChartOperator()    {
        super();
        mdblYMin = -Math.PI;
        mdblYMax = Math.PI;
    }
}
