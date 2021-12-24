/*
 * MFP project, Statement_variable.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.ElemAnalyzer;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.DCHelper.CurPos;
import com.cyzapps.Jmfp.ErrorProcessor.ERRORTYPES;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.ExprAnalyzer;
import com.cyzapps.Oomfp.SpaceMember.AccessRestriction;
import java.util.LinkedList;
import java.util.Locale;

/**
 *
 * @author tonyc
 */
public class Statement_variable extends StatementType {
	private static final long serialVersionUID = 1L;

	Statement_variable(Statement s) {
        mstatement = s;
        mstrType = getTypeStr();
    }
    
    public static String getTypeStr() {
        return "variable";
    }

    public AccessRestriction access = AccessRestriction.PUBLIC;
    public String[] mstrVariables = new String[0];    // variable list
    public String[] mstrVarValues = new String[0]; // variable initial values
    public AbstractExpr[] maexprVarValues = new AbstractExpr[0];    // abstract expr initial values.
    
    public boolean misSelf = false; // if false, it is static, otherwise, it is a member variable.

    @Override
    protected void analyze(String strStatement) throws JMFPCompErrException {
        String strAccess = strStatement.split("\\s+")[0];
        if (strAccess.equals(AccessRestriction.PRIVATE.toString())) {
            access = AccessRestriction.PRIVATE;
            strStatement = strStatement.substring(strAccess.length()).trim();
        } else if (strAccess.equals(AccessRestriction.PUBLIC.toString())) {
            strStatement = strStatement.substring(strAccess.length()).trim();
        }
        String strVariableList = strStatement.substring(getTypeStr().length()).trim();
        if (strVariableList.matches("^self\\s.*$")) {
            // no need to use "^(?i)self\\s.*$" as strStatement has been lower cased.
            // it is starts with self
            misSelf = true;
            strVariableList = strVariableList.substring("self".length()).trim();
        }
        if (strVariableList.length() == 0)    {
            ERRORTYPES e = ERRORTYPES.NO_VARIABLE;
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        }

        LinkedList<String> listVariableDefs = new LinkedList<>();
        int nRoundBracketLevel = 0;
        int nSquareBracketLevel = 0;
        int nBraceBracketLevel = 0;
        int nLastComma = -1;
        for (int index = 0; index < strVariableList.length(); index ++)    {
            if (strVariableList.charAt(index) == '"')    {
            	try {
            		CurPos curpos = new CurPos();
            		curpos.m_nPos = index;
            		ElemAnalyzer.getString(strVariableList, curpos);
            		index = curpos.m_nPos;
            	} catch(JFCALCExpErrException ex) {
            		// the only reason is no close quatation.
                    ERRORTYPES e = ERRORTYPES.CANNOT_FIND_CLOSE_QUATATION_MARK_FOR_STRING;
                    throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
                }
            	if (index >= strVariableList.length())   {
                    listVariableDefs.add(strVariableList.substring(nLastComma + 1));
                    break;
                }
            }
            switch (strVariableList.charAt(index)) {
                case '(':
                    nRoundBracketLevel ++;
                    break;
                case ')':
                    nRoundBracketLevel --;
                    break;
                case '[':
                    nSquareBracketLevel ++;
                    break;
                case ']':
                    nSquareBracketLevel --;
                    break;
                case '{':
                    nBraceBracketLevel ++;
                    break;
                case '}':
                    nBraceBracketLevel --;
                    break;
                default:
                    break;
            }
            if (nRoundBracketLevel < 0 || nSquareBracketLevel < 0 || nBraceBracketLevel < 0)    {
                ERRORTYPES e = ERRORTYPES.NO_OPEN_BRACKET;
                throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
            } else if (nRoundBracketLevel > 0 || nSquareBracketLevel > 0 || nBraceBracketLevel > 0)    {
                continue;
            } else    {    // not in any brackets.
                if (strVariableList.charAt(index) == ',')    {
                    listVariableDefs.add(strVariableList.substring(nLastComma + 1, index));
                    nLastComma = index;
                } else if (index == strVariableList.length() - 1)    {
                    listVariableDefs.add(strVariableList.substring(nLastComma + 1));
                }
            }
        }
        if (nRoundBracketLevel > 0 || nSquareBracketLevel > 0 || nBraceBracketLevel > 0)    {
            ERRORTYPES e = ERRORTYPES.NO_CLOSE_BRACKET;
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        }
        if (listVariableDefs.isEmpty())    {
            ERRORTYPES e = ERRORTYPES.NO_VARIABLE;
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        }
        mstrVariables = new String[listVariableDefs.size()];
        mstrVarValues = new String[listVariableDefs.size()];
        maexprVarValues = new AbstractExpr[listVariableDefs.size()];
        for (int idx = 0; idx < listVariableDefs.size(); idx ++)    {
        	mstrVarValues[idx] = "";
            maexprVarValues[idx] = null;
        }
        for (int idx = 0; idx < listVariableDefs.size(); idx ++)    {
            String[] strlistVarDef = listVariableDefs.get(idx).split("=");
            if (strlistVarDef.length == 0)    {
                ERRORTYPES e = ERRORTYPES.BAD_VARIABLE_NAME;
                throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
            } else    {
                mstrVariables[idx] = strlistVarDef[0].trim();
                int nValidationResult = Statement.validateTypeOrVarOrFuncName(mstrVariables[idx]);
                if (nValidationResult == 1 || nValidationResult < 0) {
                    ERRORTYPES e = ERRORTYPES.BAD_VARIABLE_NAME;
                    throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
                } else if (nValidationResult == 2) {
                    ERRORTYPES e = ERRORTYPES.IS_KEYWORD;
                    throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
                }
                for (int idx1 = 0; idx1 < idx; idx1 ++)    {
                    if (mstrVariables[idx1].equals(mstrVariables[idx]))    {
                        ERRORTYPES e = ERRORTYPES.REDEFINED_VARIABLE;
                        throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
                    }
                }
                if (strlistVarDef[0].length() == listVariableDefs.get(idx).length())    {
                    // no =
                    mstrVarValues[idx] = "";
                    maexprVarValues[idx] = null;
                } else {
                    // character listVariableDefs.charAt(strlistVarDef[0].length()) must be =
                    mstrVarValues[idx] = listVariableDefs.get(idx).substring(strlistVarDef[0].length() + 1).trim();
                    if (mstrVarValues[idx].length() == 0)    {
                        ERRORTYPES e = ERRORTYPES.NEED_EXPRESSION;
                        throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
                    }
                    maexprVarValues[idx] = new AEInvalid();
                }
            }
        }
    }

    @Override
    public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) throws InterruptedException {
        LinkedList<ModuleInfo> listInfos = new LinkedList<>();
        for (int idx = 0; idx < mstrVarValues.length; idx ++) {
            if (mstrVarValues[idx] == null || mstrVarValues[idx].trim().length() == 0) {
                continue;
            }
            LinkedList<ModuleInfo> listThisInfos = ModuleInfo.getReferredModulesFromString(mstrVarValues[idx], progContext);
            ModuleInfo.mergeIntoList(listThisInfos, listInfos);
        }

        return listInfos;
    }

    @Override
    protected void analyze2(FunctionEntry fe) {
        for (int idx = 0; idx < maexprVarValues.length; idx ++)    {
            if (mstrVarValues[idx] != null && mstrVarValues[idx].length() > 0) {
                try {
                    ProgContext progContext = new ProgContext();
                    progContext.mstaticProgContext.setCallingFunc(fe.m_sf);
                    maexprVarValues[idx] = ExprAnalyzer.analyseExpression(mstrVarValues[idx], new DCHelper.CurPos(),
                            new LinkedList<Variable>(), progContext);
                } catch (Exception ex) {
                    // do not throw exception here. Otherwise, the statement may not execute.
                    maexprVarValues[idx] = new AEInvalid();
                }
            }
        }
    }
    
    @Override
    public void analyze2(ProgContext progContext, boolean bForceReanalyse) {
        for (int idx = 0; idx < maexprVarValues.length; idx ++)    {
            if (bForceReanalyse || maexprVarValues[idx] == null || maexprVarValues[idx].menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_INVALID)  {
                if (mstrVarValues[idx] != null && mstrVarValues[idx].length() > 0) {
                    try {
                        maexprVarValues[idx] = ExprAnalyzer.analyseExpression(mstrVarValues[idx], new DCHelper.CurPos(),
                                new LinkedList<Variable>(), progContext);
                    } catch (Exception ex) {
                        // do not throw exception here. Otherwise, the statement may not execute.
                        maexprVarValues[idx] = new AEInvalid();
                    }
                }
            }           
        }
    }
}

