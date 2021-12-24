/*
 * MFP project, ScriptAnalysisHelper.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jsma.AEFunction;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.SMErrProcessor;
import com.cyzapps.Jsma.UnknownVarOperator;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;
import java.util.LinkedList;

/**
 *
 * @author tony
 */
public class ScriptAnalysisHelper {
    public static DataClass analyseAExprOrString(AbstractExpr aexpr, String strExpr, Statement sLine,
    		ErrorProcessor.ERRORTYPES errorTypeThrown, ProgContext progContext)
            throws InterruptedException, ErrorProcessor.JMFPCompErrException {
        DataClass datumReturn = null;
        if (aexpr == null || aexpr.menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_INVALID) {
            // seems to be invalid expression, using traditional expression evaluator
            ExprEvaluator exprEvaluator = new ExprEvaluator(progContext);
            DCHelper.CurPos c = new DCHelper.CurPos();
            c.m_nPos = 0;
            try {
                datumReturn = exprEvaluator.evaluateExpression(strExpr, c);
            } catch(ErrProcessor.JFCALCExpErrException e)    {
                ErrorProcessor.ERRORTYPES errType = errorTypeThrown;
                throw new ErrorProcessor.JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType, e);                                        
            }
        } else if (aexpr.menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_FUNCTION) {
            try {
                // evaluateAEFunctionQuick handles both returning nothing and returning something.
                datumReturn = ((AEFunction)aexpr).evaluateAEFunctionQuick(new LinkedList<UnknownVariable>(), progContext);
            } catch (SMErrProcessor.JSmartMathErrException e) {
                ErrorProcessor.ERRORTYPES errType = errorTypeThrown;
                throw new ErrorProcessor.JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType, e);
            } catch (ErrProcessor.JFCALCExpErrException e) {
                ErrorProcessor.ERRORTYPES errType = errorTypeThrown;
                throw new ErrorProcessor.JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType, e);
            }
        } else {
            try {
                datumReturn = aexpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), progContext);
            } catch (SMErrProcessor.JSmartMathErrException e) {
                ErrorProcessor.ERRORTYPES errType = errorTypeThrown;
                throw new ErrorProcessor.JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType, e);
            } catch (ErrProcessor.JFCALCExpErrException e) {
                ErrorProcessor.ERRORTYPES errType = errorTypeThrown;
                throw new ErrorProcessor.JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, errType, e);
            }
        }
        return datumReturn;
    }
}
