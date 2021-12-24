/*
 * MFP project, Statement_catch.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import com.cyzapps.Jfcalc.DCHelper.CurPos;
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
public class Statement_catch extends StatementType    {
	private static final long serialVersionUID = 1L;
	Statement_catch(Statement s)    {
        mstatement = s;
        mstrType = getTypeStr();
    }
    
    public static String getTypeStr() {
        return "catch";
    }

    public String m_strFilter;
    public AbstractExpr maexprFilter;
    @Override
    protected void analyze(String strStatement) throws JMFPCompErrException {
        // No condition is allowed.
        m_strFilter = strStatement.substring(getTypeStr().length()).trim();
        if (m_strFilter.length() == 0) {
            maexprFilter = null;
        } else {
            maexprFilter = new AEInvalid();
        }
    }

    @Override
    public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) throws InterruptedException {
        if (m_strFilter == null || m_strFilter.trim().length() == 0) {
            return new LinkedList<>();
        }
        return ModuleInfo.getReferredModulesFromString(m_strFilter, progContext);
    }

    @Override
    protected void analyze2(FunctionEntry fe) {
        if (m_strFilter.length() > 0) {
            try {
                ProgContext progContext = new ProgContext();
                progContext.mstaticProgContext.setCallingFunc(fe.m_sf);
                maexprFilter = ExprAnalyzer.analyseExpression(m_strFilter, new CurPos(),
                        new LinkedList<Variable>(), progContext);
            } catch (Exception ex) {
                // do not throw exception here. Otherwise, the statement may not execute.
                maexprFilter = new AEInvalid();
            }
        }
    }
    
    @Override
    public void analyze2(ProgContext progContext, boolean bForceReanalyse) {
    	if (bForceReanalyse || maexprFilter == null || maexprFilter.menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_INVALID)  {
            if (m_strFilter.length() > 0) {
                try {
                    maexprFilter = ExprAnalyzer.analyseExpression(m_strFilter, new CurPos(),
                            new LinkedList<Variable>(), progContext);
                } catch (Exception ex) {
                    // do not throw exception here. Otherwise, the statement may not execute.
                    maexprFilter = new AEInvalid();
                }
            }
        }
    }
}

