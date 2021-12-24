/*
 * MFP project, PlatformInfo.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.JPlatformHW;

import com.cyzapps.Jfcalc.BuiltInFunctionLib;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.DataClassString;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.OSAdapter.LangFileManager;
import com.cyzapps.OSAdapter.ParallelManager.CallObject;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import java.util.LinkedList;
import java.util.Locale;

/**
 *
 * @author tony
 */
public class PlatformInfo {

    public static void call2Load(boolean bOutput) {
        if (bOutput) {
            System.out.println("Loading " + PlatformInfo.class.getName());
        }
    }
    
    public static class is_running_on_androidFunction extends BuiltInFunctionLib.BaseBuiltInFunction {

        public is_running_on_androidFunction() {
            mstrProcessedNameWithFullCS = "::mfp::platform_hardware::platform_info::is_running_on_android";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 0;
            mnMinParamNum = 0;
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws ErrProcessor.JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            if (FuncEvaluator.msPlatformHWMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_PLATFORM_HARDWARE_LIB_IS_NOT_LOADED);
            }
            Boolean bOnAndroid = FuncEvaluator.msPlatformHWMgr.mlangFileManager.isOnAndroid();
            
            if (bOnAndroid) {
                return new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
            } else {
                return new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
            }
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new is_running_on_androidFunction());}
    
    public static class is_mfp_appFunction extends BuiltInFunctionLib.BaseBuiltInFunction {

        public is_mfp_appFunction() {
            mstrProcessedNameWithFullCS = "::mfp::platform_hardware::platform_info::is_mfp_app";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 0;
            mnMinParamNum = 0;
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws ErrProcessor.JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            if (FuncEvaluator.msPlatformHWMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_PLATFORM_HARDWARE_LIB_IS_NOT_LOADED);
            }
            Boolean bMFPApp = FuncEvaluator.msPlatformHWMgr.mlangFileManager.isMFPApp();
            
            if (bMFPApp) {
                return new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
            } else {
                return new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
            }
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new is_mfp_appFunction());}
    
    public static class get_asset_file_pathFunction extends BuiltInFunctionLib.BaseBuiltInFunction {

        public get_asset_file_pathFunction() {
            mstrProcessedNameWithFullCS = "::mfp::platform_hardware::platform_info::get_asset_file_path";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws ErrProcessor.JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            if (FuncEvaluator.msPlatformHWMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_PLATFORM_HARDWARE_LIB_IS_NOT_LOADED);
            }
            String strAssetFile = DCHelper.lightCvtOrRetDCString(listParams.pollLast()).getStringValue();
            if (strAssetFile == null) {
                return new DataClassNull();
            } else {
                String strAssetFilePath = FuncEvaluator.msPlatformHWMgr.mlangFileManager.getAssetFilePath(strAssetFile);
                if (strAssetFilePath == null) {
                    return new DataClassNull();
                } else {
                    return new DataClassString(strAssetFilePath);
                }
            }
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new get_asset_file_pathFunction());}
    
    public static class is_sandbox_sessionFunction extends BuiltInFunctionLib.BaseBuiltInFunction {

        public is_sandbox_sessionFunction() {
            mstrProcessedNameWithFullCS = "::mfp::platform_hardware::platform_info::is_sandbox_session";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 0;
            mnMinParamNum = 0;
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws ErrProcessor.JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            Long thisThreadId = Thread.currentThread().getId();
            String libPath = null;
            if (CallObject.msmapThreadId2SessionInfo.containsKey(thisThreadId)) {
                libPath = CallObject.msmapThreadId2SessionInfo.get(thisThreadId).getLibPath();
            }
            if (libPath == null) {
                return new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
            } else {
                return new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
            }
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new is_sandbox_sessionFunction());}
    
    public static class get_sandbox_session_lib_pathFunction extends BuiltInFunctionLib.BaseBuiltInFunction {

        public get_sandbox_session_lib_pathFunction() {
            mstrProcessedNameWithFullCS = "::mfp::platform_hardware::platform_info::get_sandbox_session_lib_path";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 0;
            mnMinParamNum = 0;
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws ErrProcessor.JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            Long thisThreadId = Thread.currentThread().getId();
            String libPath = null;
            if (CallObject.msmapThreadId2SessionInfo.containsKey(thisThreadId)) {
                libPath = CallObject.msmapThreadId2SessionInfo.get(thisThreadId).getLibPath();
            }
            if (libPath == null) {
                return new DataClassNull();
            } else {
                return new DataClassString(libPath + LangFileManager.STRING_PATH_DIVISOR);
            }
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new get_sandbox_session_lib_pathFunction());}
    
    public static class get_sandbox_session_resource_pathFunction extends BuiltInFunctionLib.BaseBuiltInFunction {

        public get_sandbox_session_resource_pathFunction() {
            mstrProcessedNameWithFullCS = "::mfp::platform_hardware::platform_info::get_sandbox_session_resource_path";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 0;
            mnMinParamNum = 0;
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws ErrProcessor.JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            Long thisThreadId = Thread.currentThread().getId();
            String libPath = null;
            if (CallObject.msmapThreadId2SessionInfo.containsKey(thisThreadId)) {
                libPath = CallObject.msmapThreadId2SessionInfo.get(thisThreadId).getLibPath();
            }
            if (libPath == null) {
                return new DataClassNull();
            } else {
                return new DataClassString(libPath
                        + LangFileManager.STRING_PATH_DIVISOR
                        + LangFileManager.STRING_SANDBOX_RESOURCE_FOLDER
                        + LangFileManager.STRING_PATH_DIVISOR);
            }
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new get_sandbox_session_resource_pathFunction());}
    
    public static class get_country_languageFunction extends BuiltInFunctionLib.BaseBuiltInFunction {

        public get_country_languageFunction() {
            mstrProcessedNameWithFullCS = "::mfp::platform_hardware::platform_info::get_country_language";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 0;
            mnMinParamNum = 0;
        }

        // this function read value based parameters (i.e. x and y values are given out).
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws ErrProcessor.JFCALCExpErrException, InterruptedException {
            if (listParams.size() > mnMaxParamNum || listParams.size() < mnMinParamNum) {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            Locale l = Locale.getDefault();
            DataClassString datumCountry = new DataClassString(l.getCountry());
            DataClassString datumLanguage = new DataClassString(l.getLanguage());
            DataClass[] arrayCountryLang = new DataClass[] {datumCountry, datumLanguage};
            DataClassArray datumReturn = new DataClassArray(arrayCountryLang);
            
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new get_country_languageFunction());}
}
