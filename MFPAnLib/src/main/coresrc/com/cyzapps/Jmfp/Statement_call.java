// MFP project, Statement_call.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jmfp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jmfp.ErrorProcessor.ERRORTYPES;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.ExprAnalyzer;

public class Statement_call extends StatementType {
	private static final long serialVersionUID = 1L;

	Statement_call(Statement s) {
        mstatement = s;
        mstrType = getTypeStr();
    }
    
    public static String getTypeStr() {
        return "call";
    }

    public Boolean mbIsCallLocal;
    public String mstrConnect;
    public AbstractExpr maexprConnect;
    public String mstrSettings;
    public AbstractExpr maexprSettings;
    public Set<String> msetInterfParams;

	@Override
	protected void analyze(String strStatement) throws JMFPCompErrException {
		mstrConnect = "";
		maexprConnect = new AEInvalid();
		mstrSettings = "";
		maexprSettings = new AEInvalid();
		msetInterfParams = new HashSet<String>();
        /*
         * call statement should be something like
         * call connection [with settings] [on params]
         * where [] means optional.
         */
        Map<String, String> map = new HashMap<String, String>();
        String strAfterCall = StatementProcHelper.convertStrExprs(strStatement.substring(getTypeStr().length()).trim(), map);
        
        String[] strDividedByWithOn = (strAfterCall + " ").split("[ \\t](with|on)[ \\t]");  // add a space to avoid misparse call local on (end by on).
        if (strDividedByWithOn.length < 1)    {	// with and on can be omitted.
            ERRORTYPES e = ERRORTYPES.INVALID_CALL_STATEMENT;
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        }
        mstrConnect = strDividedByWithOn[0].trim();
        if (mstrConnect.length() == 0) {
            ERRORTYPES e = ERRORTYPES.INVALID_CALL_STATEMENT;
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        }
        
        mbIsCallLocal = mstrConnect.equals("local");
        
        String strAfterConnection = strAfterCall.substring(mstrConnect.length()).trim();
        String strAfterSettings = strAfterConnection;
        mstrConnect = StatementProcHelper.replaceStrExprs(mstrConnect, map);
        if (strAfterConnection.startsWith("with")) {
        	String[] strDividedByOn = strAfterConnection.substring("with".length()).trim().split("[ \\t]on[ \\t]");
        	mstrSettings = strDividedByOn[0].trim();
            if (mstrSettings.length() == 0) {
                ERRORTYPES e = ERRORTYPES.INVALID_CALL_STATEMENT;
                throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
            }
            strAfterSettings = strAfterCall.substring(mstrSettings.length()).trim();
            mstrSettings = StatementProcHelper.replaceStrExprs(mstrSettings, map);
        } else {
        	mstrSettings = "";	// no settings.
        }
        
        if (strAfterSettings.startsWith("on")) {
        	String[] allInterfParams = strAfterSettings.substring("on".length()).trim().split(",");
        	for (int idx = 0; idx < allInterfParams.length; idx ++) {
        		String thisInterfParam = Statement.getShrinkedMemberNameStr(allInterfParams[idx]);
        		if (thisInterfParam.length() > 0) {
        	        int nValidationResult = Statement.validateTypeOrVarOrFuncName(thisInterfParam);
        	        if (nValidationResult == 1) {
        	            ERRORTYPES e = ERRORTYPES.BAD_VARIABLE_NAME;
        	            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        	        } else if (nValidationResult == 2) {
        	            ERRORTYPES e = ERRORTYPES.IS_KEYWORD;
        	            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        	        } else if (msetInterfParams.contains(thisInterfParam)) {
        	        	ERRORTYPES e = ERRORTYPES.DUPLICATE_PARAMETER;
        	            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
            	    }
        			msetInterfParams.add(thisInterfParam);	// this is a variable name, no string included.
        		}
        	}
        } // else, no interface parameters.
	}

	@Override
	public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) throws InterruptedException {
        LinkedList<ModuleInfo> listConnect = ModuleInfo.getReferredModulesFromString(mstrConnect, progContext);
        LinkedList<ModuleInfo> listSettings = new LinkedList<ModuleInfo>();
        if (mstrSettings.length() > 0) {
            listSettings = ModuleInfo.getReferredModulesFromString(mstrSettings, progContext);
        }
        ModuleInfo.mergeIntoList(listSettings, listConnect);
        return listConnect;
	}

	@Override
	protected void analyze2(FunctionEntry fe) {
        try {
            ProgContext progContext = new ProgContext();
            progContext.mstaticProgContext.setCallingFunc(fe.m_sf);
            maexprConnect = ExprAnalyzer.analyseExpression(mstrConnect, new DCHelper.CurPos(),
                    new LinkedList<Variable>(), progContext);
        } catch (Exception ex) {
            // do not throw exception here. Otherwise, the statement may not execute.
            maexprConnect = new AEInvalid();
        }
		
        if (mstrSettings.trim().length() == 0) {
        	maexprSettings = new AEInvalid();
        } else {
	        try {
	            ProgContext progContext = new ProgContext();
	            progContext.mstaticProgContext.setCallingFunc(fe.m_sf);
	            maexprSettings = ExprAnalyzer.analyseExpression(mstrSettings, new DCHelper.CurPos(),
	                    new LinkedList<Variable>(), progContext);
	        } catch (Exception ex) {
	            // do not throw exception here. Otherwise, the statement may not execute.
	            maexprSettings = new AEInvalid();
	        }
		}
	}

	@Override
	protected void analyze2(ProgContext progContext, boolean bForceReanalyse) {
    	if (bForceReanalyse || maexprConnect == null || maexprConnect.menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_INVALID)  {
            try {
                maexprConnect = ExprAnalyzer.analyseExpression(mstrConnect, new DCHelper.CurPos(),
                        new LinkedList<Variable>(), progContext);
            } catch (Exception ex) {
                // do not throw exception here. Otherwise, the statement may not execute.
                maexprConnect = new AEInvalid();
            }
        }
    	if ((bForceReanalyse || maexprSettings == null || maexprSettings.menumAEType == AbstractExpr.ABSTRACTEXPRTYPES.ABSTRACTEXPR_INVALID)
    			&& mstrSettings.trim().length() > 0) {
            try {
                maexprSettings = ExprAnalyzer.analyseExpression(mstrSettings, new DCHelper.CurPos(),
                        new LinkedList<Variable>(), progContext);
            } catch (Exception ex) {
                // do not throw exception here. Otherwise, the statement may not execute.
                maexprSettings = new AEInvalid();
            }
        }
	}

}
