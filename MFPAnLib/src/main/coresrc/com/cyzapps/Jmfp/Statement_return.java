/*
 * MFP project, Statement_return.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import com.cyzapps.Jfcalc.DCHelper;
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
public class Statement_return extends StatementType {
	private static final long serialVersionUID = 1L;
	Statement_return(Statement s) {
        mstatement = s;
        mstrType = getTypeStr();
    }
    
    public static String getTypeStr() {
        return "return";
    }

    public String m_strReturnedExpr;    // we assume that a function should always return something.
    public AbstractExpr maexprReturnedExpr;
    @Override
    protected void analyze(String strStatement) throws JMFPCompErrException {
        m_strReturnedExpr = strStatement.substring(getTypeStr().length()).trim();
        /*
         * function needs not necessarily return something.
        if (m_strReturnedExpr.length() == 0)    {
            ERRORTYPES e = ERRORTYPES.NO_RETURN_IN_FUNCTION;
            throw new JMFPCompErrException(m_nLineNo, e);
        }
        */
        if (m_strReturnedExpr.length() == 0) {
            maexprReturnedExpr = null;
        } else {
        	maexprReturnedExpr = new AEInvalid();
        }
    }

    @Override
    public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) throws InterruptedException {
        return ModuleInfo.getReferredModulesFromString(m_strReturnedExpr, progContext);
    }

    @Override
    protected void analyze2(FunctionEntry fe) {
        if (m_strReturnedExpr.length() > 0) {
            try {
                ProgContext progContext = new ProgContext();
                progContext.mstaticProgContext.setCallingFunc(fe.m_sf);
                maexprReturnedExpr = ExprAnalyzer.analyseExpression(m_strReturnedExpr, new DCHelper.CurPos(),
                        new LinkedList<Variable>(), progContext);
            } catch (Exception ex) {
                // do not throw exception here. Otherwise, the statement may not execute.
                maexprReturnedExpr = new AEInvalid();
            }
        }		
    }
    
    @Override
    public void analyze2(ProgContext progContext, boolean bForceReanalyse) {
    	if (bForceReanalyse || maexprReturnedExpr == null || maexprReturnedExpr.menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_INVALID)  {
            if (m_strReturnedExpr.length() > 0) {
                try {
                    maexprReturnedExpr = ExprAnalyzer.analyseExpression(m_strReturnedExpr, new DCHelper.CurPos(),
                            new LinkedList<Variable>(), progContext);
                } catch (Exception ex) {
                    // do not throw exception here. Otherwise, the statement may not execute.
                    maexprReturnedExpr = new AEInvalid();
                }
            }		
        }
    }
}

