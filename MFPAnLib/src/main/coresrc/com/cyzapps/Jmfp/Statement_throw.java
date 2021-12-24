/*
 * MFP project, Statement_throw.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jmfp.ErrorProcessor.ERRORTYPES;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.ExprAnalyzer;

import java.util.LinkedList;

/**
 *
 * @author tonyc
 */
public class Statement_throw extends StatementType {
	private static final long serialVersionUID = 1L;
	Statement_throw(Statement s) {
        mstatement = s;
        mstrType = getTypeStr();
    }
    
    public static String getTypeStr() {
        return "throw";
    }

    public String m_strThrownExpr;
    public AbstractExpr maexprThrownExpr;
    @Override
    protected void analyze(String strStatement) throws JMFPCompErrException {
        m_strThrownExpr = strStatement.substring(getTypeStr().length()).trim();
        maexprThrownExpr = new AEInvalid();
        if (m_strThrownExpr.length() == 0)    {
            ERRORTYPES e = ERRORTYPES.NEED_EXPRESSION;
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        }
    }

    @Override
    public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) {
        return new LinkedList<>();
    }

    @Override
    protected void analyze2(FunctionEntry fe) {
        try {
            ProgContext progContext = new ProgContext();
            progContext.mstaticProgContext.setCallingFunc(fe.m_sf);
            maexprThrownExpr = ExprAnalyzer.analyseExpression(m_strThrownExpr, new DCHelper.CurPos(),
                    new LinkedList<Variable>(), progContext);
        } catch (Exception ex) {
            // do not throw exception here. Otherwise, the statement may not execute.
        	maexprThrownExpr = new AEInvalid();
        }
    }
    
    @Override
    public void analyze2(ProgContext progContext, boolean bForceReanalyse) {
    	if (bForceReanalyse || maexprThrownExpr == null || maexprThrownExpr.menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_INVALID)  {
            try {
                maexprThrownExpr = ExprAnalyzer.analyseExpression(m_strThrownExpr, new DCHelper.CurPos(),
                        new LinkedList<Variable>(), progContext);
            } catch (Exception ex) {
                // do not throw exception here. Otherwise, the statement may not execute.
                maexprThrownExpr = new AEInvalid();
            }
        }
    }
}

