/*
 * MFP project, Statement_expression.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.ExprAnalyzer;
import java.util.LinkedList;

/**
 *
 * @author tonyc
 */
public class Statement_expression extends StatementType    {
	private static final long serialVersionUID = 1L;

	Statement_expression(Statement s)    {
        mstatement = s;
        mstrType = getTypeStr();
    }
    
    public static String getTypeStr() {
        return "_expression_";
    }
    public String m_strStatement;
    public AbstractExpr maexprStatement;

    @Override
    protected void analyze(String strStatement) throws JMFPCompErrException   {
        m_strStatement = strStatement.trim();
        if (m_strStatement.length() == 0) {
            maexprStatement = null;
        } else {
        	maexprStatement = new AEInvalid();
        }
        return;
    }

    @Override
    public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) throws InterruptedException {
        if (m_strStatement == null || m_strStatement.trim().length() == 0) {
            return new LinkedList<>();
        }
        return ModuleInfo.getReferredModulesFromString(m_strStatement, progContext);
    }

    @Override
    protected void analyze2(FunctionEntry fe) {
        if (m_strStatement.length() > 0) {
            try {
                ProgContext progContext = new ProgContext();
                progContext.mstaticProgContext.setCallingFunc(fe.m_sf);
                maexprStatement = ExprAnalyzer.analyseExpression(m_strStatement, new DCHelper.CurPos(),
                        new LinkedList<VariableOperator.Variable>(), progContext);
            } catch (Exception ex) {
                // do not throw exception here. Otherwise, the statement may not execute.
                maexprStatement = new AEInvalid();
            }
        }
    }
    
    @Override
    public void analyze2(ProgContext progContext, boolean bForceReanalyse) {
    	if (bForceReanalyse || maexprStatement == null || maexprStatement.menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_INVALID)  {
            if (m_strStatement.length() > 0) {
                try {
                    maexprStatement = ExprAnalyzer.analyseExpression(m_strStatement, new DCHelper.CurPos(),
                            new LinkedList<VariableOperator.Variable>(), progContext);
                } catch (Exception ex) {
                    // do not throw exception here. Otherwise, the statement may not execute.
                    maexprStatement = new AEInvalid();
                }
            }
        }
    }
}

