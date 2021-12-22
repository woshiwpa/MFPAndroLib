/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.PlotAdapter;

import android.content.Context;
import android.content.SharedPreferences;

import com.cyzapps.Jfcalc.IOLib;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.OSAdapter.MFP4AndroidFileMan;
import com.cyzapps.adapter.AndroidStorageOptions;
import com.cyzapps.adapter.MFPAdapter;
import com.cyzapps.mfpanlib.MFPAndroidLib;

/**
 *
 * @author tonyc
 */
public class MFPChart {
    public static final String DEFAULT_PLOT_VAR_FROM = "smc_plot_var_from";
    public static final String DEFAULT_PLOT_VAR_TO = "smc_plot_var_to";

	Context mcontext;

	public MFPChart(Context context)	{
		mcontext = context;
	}
	
	public void saveSettings()	{
		
	}
	
	public void restoreSettings()	{
		
	}

	public boolean readAppSettings(MFP4AndroidFileMan mfp4AndroidFileMan) {
		//Read app's preferences
		SharedPreferences settings = MFPAndroidLib.getContext().getSharedPreferences(MFPAndroidLib.getSettingsConfig(), 0);
		boolean bReturn = false;
		if (settings != null) {
			MFPAdapter.msdPlotChartVariableFrom = settings.getFloat(DEFAULT_PLOT_VAR_FROM, (float)MFPAdapter.msdPlotChartVariableFrom);
			MFPAdapter.msdPlotChartVariableTo = settings.getFloat(DEFAULT_PLOT_VAR_TO, (float)MFPAdapter.msdPlotChartVariableTo);

			AndroidStorageOptions.setSelectedStoragePath(
					settings.getString(AndroidStorageOptions.SELECTED_STORAGE_PATH,
							AndroidStorageOptions.getSelectedStoragePath()));
			bReturn = true;
		} else	{
			bReturn = false;
		}
		IOLib.msstrWorkingDir = mfp4AndroidFileMan.getAppBaseFullPath();	// set the initial working directory.
		return bReturn;
	}
}
