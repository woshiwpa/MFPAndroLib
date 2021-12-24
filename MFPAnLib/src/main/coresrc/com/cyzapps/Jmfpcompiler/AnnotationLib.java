/*
 * MFP project, AnnotationLib.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfpcompiler;

import com.cyzapps.Jfcalc.BuiltInFunctionLib.BaseBuiltInFunction;
import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.DataClassExtObjRef;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.DataClassString;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.AnnoType_build_asset;
import com.cyzapps.Jmfp.ModuleInfo;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import java.util.LinkedList;

/**
 *
 * @author tony
 */
public class AnnotationLib {
    
    public static void call2Load(boolean bOutput) {
        if (bOutput) {
            System.out.println("Loading " + AnnotationLib.class.getName());
        }
    }
    
    public static class get_all_referred_unitsFunction extends BaseBuiltInFunction {

        public get_all_referred_unitsFunction() {
            mstrProcessedNameWithFullCS = "::mfp_compiler::annotation::compulsory_link::get_all_referred_units";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 0;
        }

        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException {
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            if (listParams.size() == 0) {	// no parameter, which means get all the functions from all the citing spaces
	            ModuleInfo info = new ModuleInfo();
	            info.mnModuleType = ModuleInfo.FUNCTION_MODULE;
	            info.mstrModuleName = "*";
	            info.mnModuleParam1 = -1;
	            DataClassArray datumReturn = new DataClassArray(new DataClass[] {
	                new DataClassExtObjRef(info)
	            });
	            return datumReturn;
            } else {
            	// all the functions for a selected citing space, needs to be tested.
            	DataClass param = listParams.get(0);
            	String strSpace = DCHelper.lightCvtOrRetDCString(param).getStringValue().trim();
            	// note that "::*" doesn't look for all the functions in "::" and its sub citingspaces,
            	// it only look for functions from "::", not its sub citingspaces
            	if (strSpace.endsWith("::")) {
            		strSpace += "*";
            	} else {
            		strSpace += "::*";
            	}
	            ModuleInfo info = new ModuleInfo();
	            info.mnModuleType = ModuleInfo.FUNCTION_MODULE;
	            info.mstrModuleName = strSpace;
	            info.mnModuleParam1 = -1;
	            DataClassArray datumReturn = new DataClassArray(new DataClass[] {
	                new DataClassExtObjRef(info)
	            });
	            return datumReturn;
            }
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new get_all_referred_unitsFunction());}
    
    public static class get_functionsFunction extends BaseBuiltInFunction {

        public get_functionsFunction() {
            mstrProcessedNameWithFullCS = "::mfp_compiler::annotation::compulsory_link::get_functions";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = -1;
            mnMinParamNum = 0;
        }

        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException {
            // get_functions's parameters must be a string based function name with or without partial or full citingspace path
        	// and parameter count is optional.
        	if (listParams.size() < mnMinParamNum)   {
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            LinkedList<DataClassExtObjRef> listModules = new LinkedList<>();
            for (DataClass param : listParams) { // elements in listParams is from last to first. 
                String strFunc = DCHelper.lightCvtOrRetDCString(param).getStringValue();
                if (strFunc.trim().length() == 0) {
                    // not a valid function, continue
                    continue;
                }
                if (strFunc.charAt(strFunc.length() - 1) == ')') {
                    String[] strarrayFuncParts = strFunc.split("\\(");
                    if (strarrayFuncParts.length != 2) {
                        continue;   // not a valid function definition.
                    }
                    String strFuncName = strarrayFuncParts[0].trim();
                    String strFuncParam = strarrayFuncParts[1].substring(0, strarrayFuncParts[1].length() - 1).trim();  // remove the last ).
                    if (strFuncName.length() == 0) {
                        continue;   // not a valid function definition.
                    }
                    int nParamCnt = -1;
                    if (strFuncParam.length() > 0) {
                        try {
                            nParamCnt = Integer.parseInt(strFuncParam);
                        } catch(NumberFormatException e) {
                            continue;   // invalid function definition.
                        }
                    }
                    ModuleInfo info = new ModuleInfo();
                    info.mnModuleType = ModuleInfo.FUNCTION_MODULE;
                    info.mstrModuleName = strFuncName;
                    info.mnModuleParam1 = nParamCnt;
                    listModules.addFirst(new DataClassExtObjRef(info)); // make sure info is added from tail to head.
                } else {
                    ModuleInfo info = new ModuleInfo();
                    info.mnModuleType = ModuleInfo.FUNCTION_MODULE;
                    info.mstrModuleName = strFunc;
                    info.mnModuleParam1 = -1;
                    listModules.addFirst(new DataClassExtObjRef(info)); // make sure info is added from tail to head.
                }
            }
            return new DataClassArray(listModules.toArray(new DataClass[]{}));
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new get_functionsFunction());}
    
    public static class get_classesFunction extends BaseBuiltInFunction {

        public get_classesFunction() {
            mstrProcessedNameWithFullCS = "::mfp_compiler::annotation::compulsory_link::get_classes";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = -1;
            mnMinParamNum = 0;
        }

        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException {
            // get_functions's parameters must be a string based function name with or without partial or full citingspace path
        	// and parameter count is optional.
        	if (listParams.size() < mnMinParamNum)   {
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            LinkedList<DataClassExtObjRef> listModules = new LinkedList<>();
            for (DataClass param : listParams) { // elements in listParams is from last to first. 
                String strFunc = DCHelper.lightCvtOrRetDCString(param).getStringValue();
                if (strFunc.trim().length() == 0) {
                    // not a valid function, continue
                    continue;
                }
                // add constructor function into module list.
                ModuleInfo info = new ModuleInfo();
                info.mnModuleType = ModuleInfo.FUNCTION_MODULE;
                info.mstrModuleName = strFunc;
                info.mnModuleParam1 = 0;
                listModules.addFirst(new DataClassExtObjRef(info)); // make sure info is added from tail to head.
            }
            return new DataClassArray(listModules.toArray(new DataClass[]{}));
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new get_classesFunction());}
    
    public static class copy_to_resourceFunction extends BaseBuiltInFunction {

        public copy_to_resourceFunction() {
            mstrProcessedNameWithFullCS = "::mfp_compiler::annotation::build_asset::copy_to_resource";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2; // source path, destination path
            mnMinParamNum = 2;
        }

        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException {
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass datumSrc = listParams.pollLast();
            DataClass datumDest = DCHelper.lightCvtOrRetDCString(listParams.poll());	// no need to deep copy because dataclassstring is immutable and final.
            if (DCHelper.isDataClassType(datumSrc, DCHelper.DATATYPES.DATUM_STRING)) {
                datumSrc = DCHelper.lightCvtOrRetDCString(datumSrc);
            } else {
                datumSrc = DCHelper.lightCvtOrRetDCArray(datumSrc);
                if (((DataClassArray)datumSrc).getDataListSize() != 3) {
                    throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
                DataClassSingleNum datumSrcType = DCHelper.lightCvtOrRetDCMFPInt(((DataClassArray)datumSrc).getDataAtIndexByRef(new int[]{0}));
                DataClassString datumZipPath = DCHelper.lightCvtOrRetDCString(((DataClassArray)datumSrc).getDataAtIndexByRef(new int[]{1}));
                DataClassString datumZipEntry = DCHelper.lightCvtOrRetDCString(((DataClassArray)datumSrc).getDataAtIndexByRef(new int[]{2}));
                datumSrc = new DataClassArray(new DataClass[] {
                    datumSrcType, datumZipPath, datumZipEntry
                });
            }
            DataClassArray datumReturn = new DataClassArray(new DataClass[] {
            	datumSrc,
                new DataClassString(AnnoType_build_asset.ASSET_RESOURCE_TARGET),
                datumDest
            });
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new copy_to_resourceFunction());}
}
