/*
 * MFP project, Statement_else.java : Designed and developed by Tony Cui in 2021
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
public class Statement_else extends StatementType {
	private static final long serialVersionUID = 1L;

	Statement_else(Statement s) {
        mstatement = s;
        mstrType = getTypeStr();
    }
    
    public static String getTypeStr() {
        return "else";
    }

    @Override
    protected void analyze(String strStatement) throws JMFPCompErrException {
        if (strStatement.equals(getTypeStr()) == false)    {
            ERRORTYPES e = ERRORTYPES.UNRECOGNIZED_STATEMENT;
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        }

    }

    @Override
    public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) {
        return new LinkedList<ModuleInfo>();
    }

    @Override
    protected void analyze2(FunctionEntry fe) {
    }
    
    @Override
    public void analyze2(ProgContext progContext, boolean bForceReanalyse) {
    	// no need to do anything.
    }
}

