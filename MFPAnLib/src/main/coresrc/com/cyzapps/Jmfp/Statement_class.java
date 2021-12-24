/*
 * MFP project, Statement_class.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import com.cyzapps.Jfcalc.DCHelper.CurPos;
import static com.cyzapps.Jfcalc.ElemAnalyzer.getShrinkedFuncNameStr;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jmfp.ErrorProcessor.ERRORTYPES;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import com.cyzapps.Oomfp.SpaceMember.AccessRestriction;
import com.cyzapps.adapter.MFPAdapter;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tonyc
 */
public class Statement_class extends StatementType {
	private static final long serialVersionUID = 1L;

	Statement_class(Statement s) {
        mstatement = s;
        mstrType = getTypeStr();
    }
    
    public static String getTypeStr() {
        return "class";
    }
    
    public AccessRestriction access = AccessRestriction.PUBLIC;
    private String className = ""; // name without cs.
    private String classNameWithCS = "";  // class full name with citing space. make sure it is always small case.
    public LinkedList<String[]> m_lCitingSpaces = new LinkedList<>();  // citing spaces. The first one is current citingspace. make sure they are small case.
    public String[] superNameArray =  new String[] {"::mfp::lang::object"};

    @Override
    protected void analyze(String strStatement) throws JMFPCompErrException {
        // strStatement should have been trimmed with all small letters.
        String strAccess = strStatement.split("\\s+")[0];
        if (strAccess.equals(AccessRestriction.PRIVATE.toString())) {
            access = AccessRestriction.PRIVATE;
            strStatement = strStatement.substring(strAccess.length()).trim();
        } else if (strAccess.equals(AccessRestriction.PUBLIC.toString())) {
            strStatement = strStatement.substring(strAccess.length()).trim();
        }
        String strClassNameSupers = strStatement.substring(getTypeStr().length()).trim();   // dont worry about the length, it has been checked before.
        String[] strarrayTmp = strClassNameSupers.split(":");
        if (strClassNameSupers.length() > 0 && strClassNameSupers.charAt(strClassNameSupers.length() - 1) == ':') {
            ERRORTYPES e = ERRORTYPES.INVALID_CLASS;   // class definition shouldn't be ended by :
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        }
        
        className = strarrayTmp[0].trim();
        String superNames = strClassNameSupers.substring(className.length()).trim();
        LinkedList<String> listSuperNames = new LinkedList<String>();
        if (superNames.length() == 0) {
            superNames = "::mfp::lang::object";
        } else if (superNames.startsWith(":")) {
            superNames = superNames.substring(1);    // remove the :. And because strClassNameSupers is not ended with :, superNames must not be empty.
        }
        strarrayTmp = superNames.split(",");
        if (strarrayTmp.length == 0) {
            ERRORTYPES e = ERRORTYPES.INVALID_CLASS;   // class shouldn't ended by ,
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        } else if (superNames.length() > 0 && superNames.charAt(superNames.length() - 1) == ',') {
            ERRORTYPES e = ERRORTYPES.INVALID_CLASS;   // class shouldn't ended by ,
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
        } else {
            for (int idx = 0; idx < strarrayTmp.length; idx ++) {
                String thisSuperName = strarrayTmp[idx].trim();
                try {
                    thisSuperName = getShrinkedFuncNameStr(thisSuperName, new CurPos(), "");    // it has been converted to lower case, dont do it again.
                } catch (ErrProcessor.JFCALCExpErrException ex) {
                    ERRORTYPES e = ERRORTYPES.INVALID_CLASS;   // super class name is wrong.
                    throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
                }
                if (thisSuperName.length() == 0) {
                    ERRORTYPES e = ERRORTYPES.INVALID_CLASS;   // a super name is invalid
                    throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
                }
                listSuperNames.add(thisSuperName);
            }
        }
        superNameArray = listSuperNames.toArray(new String[0]);
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
	
    // this function should be called only once. Note that lUsingCitingSpacesStack always
    // has one more element than lCitingSpaceStack.
    // lUsingCitingSpacesStack cannot be null.
    public void setCitingSpaces(LinkedList<String[]> lCitingSpaceStack,
                                LinkedList<LinkedList<String[]>> lUsingCitingSpacesStack) {
        if (m_lCitingSpaces.size() > 0) {
            m_lCitingSpaces.clear(); // means reset in this case.
        }
        // find out referred citingspaces (citingspaces statements & using citingspace statements)
        List<String[]> lAllCSs = MFPAdapter.getReferredCitingSpaces(lCitingSpaceStack, lUsingCitingSpacesStack);
        m_lCitingSpaces.addAll(lAllCSs);
    }
    
    public String getClassPureName() {
        return className;
    }
    public String getClassNameWithCS() {
        // set it the first time we use it.
        if (classNameWithCS.length() > 0) {
            return classNameWithCS; // m_strFunctionNameWithCS has been set and no need to set again
        } else {
            String[] strarrayCS = m_lCitingSpaces.peek();
            if (strarrayCS == null || strarrayCS.length == 0) {
                classNameWithCS = "::" + className;
            } else {
                for (String str : strarrayCS) {
                    classNameWithCS += str + "::";
                }
                classNameWithCS += className;
            }
            return classNameWithCS;
        }
    }
}


