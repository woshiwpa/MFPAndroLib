/*
 * MFP project, Statement_for.java : Designed and developed by Tony Cui in 2021
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author tonyc
 */
public class Statement_for extends StatementType {
	private static final long serialVersionUID = 1L;

	Statement_for(Statement s) {
        mstatement = s;
        mstrType = getTypeStr();
    }
    
    public static String getTypeStr() {
        return "for";
    }

    public String mstrIndexName;
    boolean mbLocalDefIndex;
    public String mstrStart;
    public AbstractExpr maexprStart;
    public String mstrEnd;
    public AbstractExpr maexprEnd;
    public String mstrStep;
    public AbstractExpr maexprStep;
    
    @Override
    protected void analyze(String strStatement) throws JMFPCompErrException {
    	mstrStart = "";
        maexprStart = new AEInvalid();
        mstrEnd = "";
        maexprEnd = new AEInvalid();
        mstrStep = "";
        maexprStep = new AEInvalid();
        /*
         * for statement should be something like
         * for [variable ]index = expr1 to expr2[ step expr3]
         * where [] means optional.
         * Because there could be "" enclosed string in a for statement,
         * for strAfterFor is converted to lower case at the very beginning is not acceptable.
         */
        Map<String, String> map = new HashMap<String, String>();
        String strAfterFor = StatementProcHelper.convertStrExprs(strStatement.substring(getTypeStr().length()).trim(), map);
        
        String[] strDividedByTos = strAfterFor.split("[ \\t]to[ \\t]");
        if (strDividedByTos.length != 2)    {	// to is a key word, with blank or tab before and after means it cannot be a part of variable or number.
            ERRORTYPES e = ERRORTYPES.INVALID_FOR_STATEMENT;
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        }
        mbLocalDefIndex = false;    // locally defined index variable?
        if (strDividedByTos[0].length() >= "variable".length()
                && strDividedByTos[0].substring(0, "variable".length()).equals("variable"))    {
            mbLocalDefIndex = true;
            strDividedByTos[0] = strDividedByTos[0].substring("variable".length()).trim();
        }
        String[] strDividedByEqs = strDividedByTos[0].split("=");
        if (strDividedByEqs.length < 2)    {
            ERRORTYPES e = ERRORTYPES.INVALID_FOR_STATEMENT;
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        }
        mstrIndexName = Statement.getShrinkedMemberNameStr(strDividedByEqs[0]);
        int nValidationResult = Statement.validateTypeOrVarOrFuncName(mstrIndexName);
        if (nValidationResult == 1 || (nValidationResult == -1 && mbLocalDefIndex)) {
            // if index name is invalid or it is a member variable but the statement is "for variable xxx.xxx ..."
            ERRORTYPES e = ERRORTYPES.BAD_VARIABLE_NAME;
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        } else if (nValidationResult == 2) {
            ERRORTYPES e = ERRORTYPES.IS_KEYWORD;
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        }
        /*
         * In the following statement, substring(m_strIndexName.length()) removes the index name,
         * substring(1) removes '='.
         */
        mstrStart = strDividedByTos[0].trim().substring(strDividedByEqs[0].length()).trim().substring(1);
        if (mstrStart.length() == 0)    {
            ERRORTYPES e = ERRORTYPES.INVALID_FOR_STATEMENT;
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        }
        mstrStart = StatementProcHelper.replaceStrExprs(mstrStart, map);

        String[] strDividedBySteps = strDividedByTos[1].split("[ \\t]step[ \\t]");
        switch (strDividedBySteps.length) {
            case 1:
                // By default, step = 1.
                mstrEnd = strDividedBySteps[0].trim();
                mstrStep = "1";
                if (mstrEnd.length() == 0)    {
                    ERRORTYPES e = ERRORTYPES.INVALID_FOR_STATEMENT;
                    throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
                }   mstrEnd = StatementProcHelper.replaceStrExprs(mstrEnd, map);
                break;
            case 2:
                mstrEnd = strDividedBySteps[0].trim();
                mstrStep = strDividedBySteps[1].trim();
                if (mstrEnd.length() == 0 || mstrStep.length() == 0)    {
                    ERRORTYPES e = ERRORTYPES.INVALID_FOR_STATEMENT;
                    throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
                }   mstrEnd = StatementProcHelper.replaceStrExprs(mstrEnd, map);
                mstrStep = StatementProcHelper.replaceStrExprs(mstrStep, map);
                break;
            default:
                ERRORTYPES e = ERRORTYPES.INVALID_FOR_STATEMENT;
                throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        }
    }

    @Override
    public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) throws InterruptedException {
        LinkedList<ModuleInfo> listStart = ModuleInfo.getReferredModulesFromString(mstrStart, progContext);
        LinkedList<ModuleInfo> listEnd = ModuleInfo.getReferredModulesFromString(mstrEnd, progContext);
        LinkedList<ModuleInfo> listStep = ModuleInfo.getReferredModulesFromString(mstrStep, progContext);
        ModuleInfo.mergeIntoList(listEnd, listStart);
        ModuleInfo.mergeIntoList(listStep, listStart);
        return listStart;
    }

    @Override
    protected void analyze2(FunctionEntry fe) {
        try {
            ProgContext progContext = new ProgContext();
            progContext.mstaticProgContext.setCallingFunc(fe.m_sf);
            maexprStart = ExprAnalyzer.analyseExpression(mstrStart, new DCHelper.CurPos(),
                    new LinkedList<Variable>(), progContext);
        } catch (Exception ex) {
            // do not throw exception here. Otherwise, the statement may not execute.
            maexprStart = new AEInvalid();
        }
		
        try {
            ProgContext progContext = new ProgContext();
            progContext.mstaticProgContext.setCallingFunc(fe.m_sf);
            maexprEnd = ExprAnalyzer.analyseExpression(mstrEnd, new DCHelper.CurPos(),
                    new LinkedList<Variable>(), progContext);
        } catch (Exception ex) {
            // do not throw exception here. Otherwise, the statement may not execute.
            maexprEnd = new AEInvalid();
        }
        try {
            ProgContext progContext = new ProgContext();
            progContext.mstaticProgContext.setCallingFunc(fe.m_sf);
            maexprStep = ExprAnalyzer.analyseExpression(mstrStep, new DCHelper.CurPos(),
                    new LinkedList<Variable>(), progContext);
        } catch (Exception ex) {
            // do not throw exception here. Otherwise, the statement may not execute.
            maexprStep = new AEInvalid();
        }
    }
    
    @Override
    public void analyze2(ProgContext progContext, boolean bForceReanalyse) {
    	if (bForceReanalyse || maexprStart == null || maexprStart.menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_INVALID)  {
            try {
                maexprStart = ExprAnalyzer.analyseExpression(mstrStart, new DCHelper.CurPos(),
                        new LinkedList<Variable>(), progContext);
            } catch (Exception ex) {
                // do not throw exception here. Otherwise, the statement may not execute.
                maexprStart = new AEInvalid();
            }
        }
    	if (bForceReanalyse || maexprEnd == null || maexprEnd.menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_INVALID)  {
            try {
                maexprEnd = ExprAnalyzer.analyseExpression(mstrEnd, new DCHelper.CurPos(),
                        new LinkedList<Variable>(), progContext);
            } catch (Exception ex) {
                // do not throw exception here. Otherwise, the statement may not execute.
                maexprEnd = new AEInvalid();
            }
        }
    	if (bForceReanalyse || maexprStep == null || maexprStep.menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_INVALID)  {
            try {
                maexprStep = ExprAnalyzer.analyseExpression(mstrStep, new DCHelper.CurPos(),
                        new LinkedList<Variable>(), progContext);
            } catch (Exception ex) {
                // do not throw exception here. Otherwise, the statement may not execute.
                maexprStep = new AEInvalid();
            }
        }
    }
}

