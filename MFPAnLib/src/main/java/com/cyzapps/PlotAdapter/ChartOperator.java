package com.cyzapps.PlotAdapter;

import android.content.Context;

public class ChartOperator extends com.cyzapps.adapter.ChartOperator implements ICreateChart {

	@Override
	public MFPChart createChart(ChartCreationParam ccpParam1, Context context)	{
		return new MFPChart(context);
	}
}
