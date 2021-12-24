// MFP project, Statement_update.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jmfp;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;

import com.cyzapps.Jmfp.ErrorProcessor.ERRORTYPES;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;

public class Statement_update extends StatementType {
	private static final long serialVersionUID = 1L;

	Statement_update(Statement s) {
        mstatement = s;
        mstrType = getTypeStr();
    }
    
    public static String getTypeStr() {
        return "update";
    }

    public Set<String> setVariables;    // the variables to update
    
    @Override
    protected void analyze(String strStatement) throws JMFPCompErrException {
        String[] variables = strStatement.substring(getTypeStr().length()).split(",");
        for (int idx = 0; idx < variables.length; idx ++) {
        	String var = variables[idx].trim();
        	if (var.length() > 0) {
        		int nValidationResult = Statement.validateTypeOrVarOrFuncName(var);
    	        if (nValidationResult == 1) {
    	            ERRORTYPES e = ERRORTYPES.BAD_VARIABLE_NAME;
    	            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
    	        } else if (nValidationResult == 2) {
    	            ERRORTYPES e = ERRORTYPES.IS_KEYWORD;
    	            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
    	        } else if (setVariables.contains(var)) {
                    ERRORTYPES e = ERRORTYPES.DUPLICATE_PARAMETER;
    	            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        		}
        		setVariables.add(var);
        	}
        }
        // update all variables if setVariable is empty
    }

    @Override
    public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) throws InterruptedException {
        return new LinkedList<>();
    }

    @Override
    protected void analyze2(FunctionEntry fe) {
    }
    
    @Override
    public void analyze2(ProgContext progContext, boolean bForceReanalyse) {
    }
}

