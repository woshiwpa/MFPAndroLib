/*
 * MFP project, ReflectionLib.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.BuiltInFunctionLib.BaseBuiltInFunction;
import com.cyzapps.Jfcalc.DCHelper.CurPos;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import com.cyzapps.Oomfp.MemberFunction;
import com.cyzapps.Shellman.CitingSpaceMan;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import static com.cyzapps.Jfcalc.ElemAnalyzer.getShrinkedFuncNameStr;
import com.cyzapps.Jmfp.FunctionEntry;

/**
 *
 * @author tony
 */
public class ReflectionLib {

    public static void call2Load(boolean bOutput) {
        if (bOutput) {
            System.out.println("Loading " + ReflectionLib.class.getName());
        }
    }
    
    public static class Get_func_fullnameFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

		public Get_func_fullnameFunction() {
            mstrProcessedNameWithFullCS = "::mfp::reflection::get_func_fullname";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 3;
            mnMinParamNum = 1;
        }
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClassString datumFuncName = DCHelper.lightCvtOrRetDCString(listParams.removeLast());
            String strShrinkedLowCaseName = getShrinkedFuncNameStr(datumFuncName.getStringValue(), new CurPos(), "").toLowerCase(Locale.US);

            int nNumOfParams = -1;
            if (listParams.size() > 0) {
                DataClassSingleNum datumFuncParamCnt = DCHelper.lightCvtOrRetDCMFPInt(listParams.removeLast());
                nNumOfParams = datumFuncParamCnt.getDataValue().intValue();
            }
            List<String[]> lCitingSpaces = new LinkedList<>();
            lCitingSpaces.addAll(progContext.mstaticProgContext.getCitingSpaces());
            if (listParams.size() > 0) {
                lCitingSpaces.clear();
                DataClassArray datumCitingSpaces = DCHelper.lightCvtOrRetDCArray(listParams.removeLast());
                for (int idx = 0; idx < datumCitingSpaces.getDataListSize(); idx ++) {
                    DataClass datumRawElem = datumCitingSpaces.getDataAtIndexByRef(new int[] {idx});
                    if (datumRawElem == null) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                    }
                    DataClassString datumCitingSpace = DCHelper.lightCvtOrRetDCString(datumRawElem);
                    String strCS = datumCitingSpace.getStringValue().trim();
                    // getShrinkedNameStr doesn't support * at the end of cs or pure blank ("") as it is for function name.
                    String strShrinkedCS = FunctionEntry.getShrinkedName(strCS);
                    String[] strarrayCitingSpace = CitingSpaceMan.getAbsCitingSpace(strShrinkedCS,
                            progContext.mstaticProgContext.getCitingSpaces());
                    if (strarrayCitingSpace == null) {
                        // this could help when strShrinkedCS is like "...::"
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER); 
                    }
                    lCitingSpaces.add(strarrayCitingSpace);
                }
            }

            MemberFunction mf = CitingSpaceDefinition.locateFunctionCall(strShrinkedLowCaseName, nNumOfParams, lCitingSpaces);
            if (mf == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_UNDEFINED_FUNCTION);
            }
            return new DataClassString(mf.getAbsNameWithCS());
        }
    }
    //public final static FopenFunction BUILTINFUNC_Fopen = new FopenFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_func_fullnameFunction());}
    
    public static class Get_type_fullnameFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

		public Get_type_fullnameFunction() {
            mstrProcessedNameWithFullCS = "::mfp::reflection::get_type_fullname";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            DataClass param = listParams.removeLast();
            String typeFullName = param.getTypeFullName();
            return new DataClassString(typeFullName);
        }
    }
    //public final static FopenFunction BUILTINFUNC_Fopen = new FopenFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_type_fullnameFunction());}
}
