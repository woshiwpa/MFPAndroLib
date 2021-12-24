/*
 * MFP project, Statement_slvreto.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import com.cyzapps.Jmfp.ErrorProcessor.ERRORTYPES;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import java.util.LinkedList;
import java.util.Locale;

/**
 *
 * @author tonyc
 */
public class Statement_slvreto extends StatementType    {
	private static final long serialVersionUID = 1L;
	Statement_slvreto(Statement s)    {
        mstatement = s;
        mstrType = getTypeStr();
    }
    
    public static String getTypeStr() {
        return "slvreto";
    }

    public String mstrReturnedVar = "";    // if it is an empty string, then ignore returned rootspace.
    @Override
    protected void analyze(String strStatement) throws JMFPCompErrException {
        if (strStatement.substring(0, getTypeStr().length()).equals(getTypeStr()) == false)    {
            ERRORTYPES e = ERRORTYPES.UNRECOGNIZED_STATEMENT;
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        }
        mstrReturnedVar = Statement.getShrinkedMemberNameStr(strStatement.substring(getTypeStr().length()));
        if (mstrReturnedVar.length() != 0)   { // this means solve returns something
            int nValidationResult = Statement.validateTypeOrVarOrFuncName(mstrReturnedVar);
            if (nValidationResult == 1) {
                ERRORTYPES e = ERRORTYPES.BAD_VARIABLE_NAME;
                throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
            } else if (nValidationResult == 2) {
                ERRORTYPES e = ERRORTYPES.IS_KEYWORD;
                throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
            }
        }
    }

    @Override
    public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) {
        return new LinkedList<>();
    }

    @Override
    protected void analyze2(FunctionEntry fe) {
    }
    
    @Override
    public void analyze2(ProgContext progContext, boolean bForceReanalyse) {
    	// no need to do anything.
    }
}

