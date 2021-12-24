/*
 * MFP project, Statement_solve.java : Designed and developed by Tony Cui in 2021
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
public class Statement_solve extends StatementType    {
	private static final long serialVersionUID = 1L;

	Statement_solve(Statement s)    {
        mstatement = s;
        mstrType = getTypeStr();
    }
    
    public static String getTypeStr() {
        return "solve";
    }

    public String[] m_strVariables = new String[0];    // variable list

    @Override
    protected void analyze(String strStatement) throws JMFPCompErrException {

        String strVariableList = strStatement.substring(getTypeStr().length()).trim();
        m_strVariables = strVariableList.split(",");
        for (int idx = 0; idx < m_strVariables.length; idx ++)   {
            m_strVariables[idx] = m_strVariables[idx].trim();
            int nValidationResult = Statement.validateTypeOrVarOrFuncName(m_strVariables[idx]);
            if (nValidationResult == 1 || nValidationResult < 0) {  // solve statement doesn't allow member variable as a parameter
                ERRORTYPES e = ERRORTYPES.BAD_VARIABLE_NAME;
                throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
            } else if (nValidationResult == 2) {
                ERRORTYPES e = ERRORTYPES.IS_KEYWORD;
                throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
            }
            for (int idx1 = 0; idx1 < idx; idx1 ++)    {
                if (m_strVariables[idx1].equals(m_strVariables[idx]))    {
                    ERRORTYPES e = ERRORTYPES.REDEFINED_VARIABLE;
                    throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
                }
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
    