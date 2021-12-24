/*
 * MFP project, Statement_help.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import java.util.LinkedList;

/**
 *
 * @author tonyc
 */
public class Statement_help extends StatementType {
	private static final long serialVersionUID = 1L;
	Statement_help(Statement s) {
        mstatement = s;
        mstrType = getTypeStr();
    }
    
    public static String getTypeStr() {
        return "help";
    }

    public String m_strHelpTitle;
    @Override
    protected void analyze(String strStatement) throws JMFPCompErrException {
        m_strHelpTitle = strStatement.substring(getTypeStr().length()).trim();
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

