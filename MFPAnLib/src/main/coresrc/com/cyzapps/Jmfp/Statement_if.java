/*
 * MFP project, Statement_if.java : Designed and developed by Tony Cui in 2021
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
public class Statement_if extends StatementType {
	private static final long serialVersionUID = 1L;
	Statement_if(Statement s) {
        mstatement = s;
        mstrType = getTypeStr();
    }
    
    public static String getTypeStr() {
        return "if";
    }

    public String m_strCondition;
    public AbstractExpr maexprCondition;
    @Override
    protected void analyze(String strStatement) throws JMFPCompErrException {
        m_strCondition = strStatement.substring(getTypeStr().length()).trim();
        maexprCondition = new AEInvalid();
        if (m_strCondition.length() == 0)    {
            ERRORTYPES e = ERRORTYPES.NO_CONDITION;
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        }            
    }

    @Override
    public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) throws InterruptedException {
        return ModuleInfo.getReferredModulesFromString(m_strCondition, progContext);
    }

    @Override
    protected void analyze2(FunctionEntry fe) {
        try {
            ProgContext progContext = new ProgContext();
            progContext.mstaticProgContext.setCallingFunc(fe.m_sf);
            maexprCondition = ExprAnalyzer.analyseExpression(m_strCondition, new DCHelper.CurPos(),
                    new LinkedList<Variable>(), progContext);
        } catch (Exception ex) {
            // do not throw exception here. Otherwise, the statement may not execute.
            maexprCondition = new AEInvalid();
        }
    }
    
    @Override
    public void analyze2(ProgContext progContext, boolean bForceReanalyse) {
    	if (bForceReanalyse || maexprCondition == null || maexprCondition.menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_INVALID)  {
            try {
                maexprCondition = ExprAnalyzer.analyseExpression(m_strCondition, new DCHelper.CurPos(),
                        new LinkedList<Variable>(), progContext);
            } catch (Exception ex) {
                // do not throw exception here. Otherwise, the statement may not execute.
                maexprCondition = new AEInvalid();
            }
        }
    }
}
    
