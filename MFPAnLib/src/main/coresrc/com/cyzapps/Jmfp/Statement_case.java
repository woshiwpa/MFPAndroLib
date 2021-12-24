/*
 * MFP project, Statement_case.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import com.cyzapps.Jfcalc.DCHelper.CurPos;
import com.cyzapps.Jmfp.ErrorProcessor.ERRORTYPES;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.AbstractExpr.ABSTRACTEXPRTYPES;
import com.cyzapps.Jsma.ExprAnalyzer;
import java.util.LinkedList;

/**
 *
 * @author tonyc
 */
public class Statement_case extends StatementType {
	private static final long serialVersionUID = 1L;
	Statement_case(Statement s) {
        mstatement = s;
        mstrType = getTypeStr();
    }
    
    public static String getTypeStr() {
        return "case";
    }

    public String m_strCaseExpr;
    public AbstractExpr maexprCaseExpr;
    @Override
    protected void analyze(String strStatement) throws JMFPCompErrException {
        m_strCaseExpr = strStatement.substring(getTypeStr().length()).trim();
        maexprCaseExpr = new AEInvalid();
        if (m_strCaseExpr.length() == 0)    {
            ERRORTYPES e = ERRORTYPES.NEED_EXPRESSION;
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        }
    }

    @Override
    public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) throws InterruptedException {
        return ModuleInfo.getReferredModulesFromString(m_strCaseExpr, progContext);
    }
    
    
    @Override
    public void analyze2(FunctionEntry fe) {
        try {
            ProgContext progContext = new ProgContext();
            progContext.mstaticProgContext.setCallingFunc(fe.m_sf);
            maexprCaseExpr = ExprAnalyzer.analyseExpression(m_strCaseExpr, new CurPos(),
                    new LinkedList<Variable>(), progContext);
        } catch (Exception ex) {
            // do not throw exception here. Otherwise, the statement may not execute.
            maexprCaseExpr = new AEInvalid();
        }
    }
    
    @Override
    public void analyze2(ProgContext progContext, boolean bForceReanalyse) {
    	if (bForceReanalyse || maexprCaseExpr == null || maexprCaseExpr.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_INVALID)  {
            try {
                maexprCaseExpr = ExprAnalyzer.analyseExpression(m_strCaseExpr, new CurPos(),
                        new LinkedList<Variable>(), progContext);
            } catch (Exception ex) {
                // do not throw exception here. Otherwise, the statement may not execute.
                maexprCaseExpr = new AEInvalid();
            }
        }
    }
}

