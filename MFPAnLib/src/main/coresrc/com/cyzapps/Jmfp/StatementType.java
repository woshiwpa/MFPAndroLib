/*
 * MFP project, StatementType.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import java.io.Serializable;
import java.util.LinkedList;

/**
 *
 * @author tonyc
 */
public abstract class StatementType implements Serializable  {
	private static final long serialVersionUID = 1L;
    
	protected String mstrType = "";    // what is the statement
    
    public Statement mstatement = new Statement("", "", 0);

    public String getType()    {
        return mstrType;
    }

    public abstract LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) throws InterruptedException;

    /*
     * we always assume that strStatement has been trimmed and uncapitalized.
     * and the statement type has been correctly identified.
     */
    protected abstract void analyze(String strStatement)
            throws ErrorProcessor.JMFPCompErrException;

    protected abstract void analyze2(FunctionEntry fe);
    protected abstract void analyze2(ProgContext progContext, boolean bForceReanalyse);
}

