/*
 * MFP project, Statement_citingspace.java : Designed and developed by Tony Cui in 2021
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
public class Statement_citingspace extends StatementType {
	private static final long serialVersionUID = 1L;

	Statement_citingspace(Statement s) {
        mstatement = s;
        mstrType = getTypeStr();
    }
    
    public static String getTypeStr() {
        return "citingspace";
    }

    public String[] m_strarrayCitingSpace = new String[] { "" };    // make sure they are all small case.
    
    @Override
    protected void analyze(String strStatement) throws JMFPCompErrException {
        // citing space should be small letter
        String strCitingSpace = strStatement.substring(getTypeStr().length()).trim();   // dont worry about the length, it has been checked before.
        String[] strarrayTmp = strCitingSpace.split("\\s+");
        if (strarrayTmp.length == 0) {  // strCitingSpace length can be 0, > 0 etc.
            ERRORTYPES e = ERRORTYPES.INVALID_CITINGSPACE;
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        } else if (strCitingSpace.length() > 0 && strCitingSpace.charAt(strCitingSpace.length() - 1) == ':') {
            ERRORTYPES e = ERRORTYPES.INVALID_CITINGSPACE;   // citingspace shouldn't ended by ::
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        }
        strarrayTmp = strCitingSpace.split("::");
        if (strarrayTmp.length == 0) {
            ERRORTYPES e = ERRORTYPES.INVALID_CITINGSPACE;   // citingspace shouldn't ended by ::
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        }
        
        // at this moment, the citing space can be either relative or abstract.
        m_strarrayCitingSpace = new String[strarrayTmp.length];
        for (int idx = 0; idx < strarrayTmp.length; idx ++) {
            String strThisClass = strarrayTmp[idx].trim();  // :: abc :: ... is allowed
            if (idx > 0 && strThisClass.length() == 0) {
                // from the second class name, class name shouldn't be ""
                ERRORTYPES e = ERRORTYPES.INVALID_CITINGSPACE;
                throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
            }
            m_strarrayCitingSpace[idx] = strThisClass;
        }
        //done!
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
    	// no need to do anything.
    }
	
    // assume parent has been with absolute CS.
    public void convertRelativeCS2Absolute(String[] parentAbsCSArray) {
        if (m_strarrayCitingSpace[0].length() == 0)    { // length of m_strarrayCitingSpace must > 0
            // ok, this has been absolute CS, return.
            return;
        } else if (parentAbsCSArray == null) {
            // no parent citingspace
            String[] strarrayAbsCS = new String[1 + m_strarrayCitingSpace.length];
            strarrayAbsCS[0] = "";
            System.arraycopy(m_strarrayCitingSpace, 0, strarrayAbsCS, 1, m_strarrayCitingSpace.length);
            m_strarrayCitingSpace = strarrayAbsCS;
            return;
        } else {
            String[] strarrayAbsCS = new String[parentAbsCSArray.length
                                                + m_strarrayCitingSpace.length];
            System.arraycopy(parentAbsCSArray, 0, strarrayAbsCS, 0, parentAbsCSArray.length);
            System.arraycopy(m_strarrayCitingSpace, 0, strarrayAbsCS, parentAbsCSArray.length, m_strarrayCitingSpace.length);
            m_strarrayCitingSpace = strarrayAbsCS;
            // now it is absolute CS.
            return;
        }
    }
    
    public String getFullCS() {
        String strCS = "";
        for (int idx = 0; idx < m_strarrayCitingSpace.length; idx ++) {
            if (idx > 0) {
                strCS += "::";
            }
            strCS += m_strarrayCitingSpace[idx];
        }
        return strCS;
    }
    
    public static String getFullCS(String[] strarrayCitingSpace) {
        String strCS = "";
        for (int idx = 0; idx < strarrayCitingSpace.length; idx ++) {
            if (idx > 0) {
                strCS += "::";
            }
            strCS += strarrayCitingSpace[idx];
        }
        return strCS;
    }
}


