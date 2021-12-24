/*
 * MFP project, Statement_endclass.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import java.util.LinkedList;
import java.util.Locale;

/**
 *
 * @author youxi
 */
public class Statement_endclass extends StatementType {
	private static final long serialVersionUID = 1L;

	Statement_endclass(Statement s) {
        mstatement = s;
        mstrType = getTypeStr();
    }
    
    public static String getTypeStr() {
        return "endclass";
    }

    @Override
    protected void analyze(String strStatement) throws ErrorProcessor.JMFPCompErrException {
        if (strStatement.equals(getTypeStr()) == false)    {
            ErrorProcessor.ERRORTYPES e = ErrorProcessor.ERRORTYPES.UNRECOGNIZED_STATEMENT;
            throw new ErrorProcessor.JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
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
