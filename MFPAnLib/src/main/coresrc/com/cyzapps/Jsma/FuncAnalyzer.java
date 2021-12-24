/*
 * MFP project, FuncAnalyzer.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jsma;

import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassClass;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.FuncEvaluator;
import static com.cyzapps.Jfcalc.FuncEvaluator.rethrowParamException;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import com.cyzapps.Oomfp.MFPClassDefinition;
import com.cyzapps.Oomfp.MemberFunction;
import com.cyzapps.Oomfp.SpaceMember;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author youxi
 */
public class FuncAnalyzer {
    // strShrinkedRawName must be small case.
    public static DataClass evaluateFunction(String strFuncOwnerName, DataClass funcOwner, String strShrinkedRawName, LinkedList<AbstractExpr> listParamAExprs, LinkedList<UnknownVarOperator.UnknownVariable> lUnknownVars, ProgContext progContext)
            throws InterruptedException, ErrProcessor.JFCALCExpErrException, SMErrProcessor.JSmartMathErrException
    {
        LinkedList<DataClass> listParamValues = new LinkedList<DataClass>();  /* parameter stack */
        LinkedList<String> listParamRawInputs = new LinkedList<String>();   /* raw strings */
        LinkedList<ErrProcessor.JFCALCExpErrException> listExceptions = new LinkedList<ErrProcessor.JFCALCExpErrException>(); /* exception list */
        for (AbstractExpr param : listParamAExprs) {
            DataClass datumParam = null;
            ErrProcessor.JFCALCExpErrException eParam = null;
            try {
                datumParam = param.evaluateAExprQuick(lUnknownVars, progContext);
                if (datumParam == null)    {
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
                }
            } catch (ErrProcessor.JFCALCExpErrException e) {
                eParam = rethrowParamException(e);
            }
            /* push the parameter and exception */
            listParamValues.add(datumParam);
            listParamRawInputs.add(param.output());
            listExceptions.add(eParam);
        }
        List<String[]> lCitingSpaces = progContext.mstaticProgContext.getCitingSpaces();
        MemberFunction mf = null;
        if (funcOwner != null) {
            // this means it is a member function
            listParamRawInputs.add(strFuncOwnerName);
            listParamValues.add(funcOwner);
            listExceptions.add(null);
            // get funcOwner's citingspace
            DataClassClass funcOwnerObj = DCHelper.lightCvtOrRetDCClass(funcOwner);
            // first of all, let's see if this is a private function.
            // search both public and private functions. get function directly from instance to avoid class def search.
            mf = funcOwnerObj.getClassInstance().getMemberFunction(strFuncOwnerName, strShrinkedRawName, listParamRawInputs.size(), SpaceMember.AccessRestriction.PRIVATE, progContext);
        } else {
            // it is not a member function
            mf = CitingSpaceDefinition.locateFunctionCall(strShrinkedRawName, listParamValues.size(), lCitingSpaces);
        }
        if (mf == null) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_UNDEFINED_FUNCTION);
        }
        return FuncEvaluator.evaluateFunction(mf, strShrinkedRawName, listParamRawInputs, listParamValues, listExceptions, progContext);
    }
    
    
}
