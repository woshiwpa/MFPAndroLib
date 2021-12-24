/*
 * MFP project, Statement_function.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import com.cyzapps.Jmfp.ErrorProcessor.ERRORTYPES;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import com.cyzapps.Oomfp.MFPClassDefinition;
import com.cyzapps.Oomfp.SpaceMember.AccessRestriction;
import com.cyzapps.adapter.MFPAdapter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author tonyc
 */
public class Statement_function extends StatementType {
	private static final long serialVersionUID = 1L;
	Statement_function(Statement s) {
        mstatement = s;
        mstrType = getTypeStr();
    }
    
    public static String getTypeStr() {
        return "function";
    }
    public AccessRestriction access = AccessRestriction.PUBLIC;
    private String ownerClassDefFullNameWithCS = null;  // if it is null, means not a member function. This variable is initialized after class defintion is constructed
    private transient MFPClassDefinition ownerClassDefinition = null;   // ownerClassDefinition is very expensive to serialize so it will not be serialized
    public MFPClassDefinition getOwnerClassDef() {
        if (ownerClassDefFullNameWithCS == null) {
            // if it is null, means not a member function. This variable is initialized after class defintion is constructed
            return null;
        } else {
            if (ownerClassDefinition == null) {
                // ownerClassDefinition hasn't been cached.
                ownerClassDefinition = MFPClassDefinition.getClassDefinitionMap().get(ownerClassDefFullNameWithCS);
            }
            return ownerClassDefinition;
        }
    }
    public void setOwnerClassDef(MFPClassDefinition clsDef) {
        if (clsDef == null) {
            ownerClassDefFullNameWithCS = null;
            ownerClassDefinition = null;
        } else {
            ownerClassDefFullNameWithCS = clsDef.selfTypeDef.toString();
            ownerClassDefinition = clsDef;
        }
    }
    private String m_strFuncRawName;    // make sure it is always small case. raw name can with partial cs
    private String m_strFuncShrinkedName;  // shrinked name (all blanks removed) that can with partial cs.
    private String m_strFuncPureName;   // name without cs.
    private String m_strFunctionNameWithCS = "";  // function full name with citing space. make sure it is always small case.
    public LinkedList<String[]> m_lCitingSpaces = new LinkedList<>();  // citing spaces. The first one is current citingspace. make sure they are small case.
    public String[] m_strParams = new String[0];    // parameter list
    public boolean m_bIncludeOptParam = false;    // include optional parameters?
    @Override
    protected void analyze(String strStatement) throws JMFPCompErrException    {
        String strAccess = strStatement.split("\\s+")[0];
        if (strAccess.equals(AccessRestriction.PRIVATE.toString())) {
            access = AccessRestriction.PRIVATE;
            strStatement = strStatement.substring(strAccess.length()).trim();
        } else if (strAccess.equals(AccessRestriction.PUBLIC.toString())) {
            strStatement = strStatement.substring(strAccess.length()).trim();
        }
        String strFunctionDef = strStatement.substring(getTypeStr().length()).trim();    // function name defined from char 8.
        m_strFuncRawName = strFunctionDef.split("\\(")[0].trim();    // trim to prevent declaration like mytest ()
        String[] strarrayPartialCS = m_strFuncRawName.split("::");
        m_strFuncShrinkedName = "";
        int nValidationResult;
        for (int idx = 0; idx < strarrayPartialCS.length; idx ++) {
            String str = strarrayPartialCS[idx].trim();
            if (idx > 0) {
                m_strFuncShrinkedName += "::";
            } else if (idx == strarrayPartialCS.length - 1) {
                m_strFuncPureName = str;
            }
            m_strFuncShrinkedName += str;
            if (idx == 0 && str.length() == 0 && strarrayPartialCS.length > 1) {
                continue;   // first part of CS can be an empty string.
            }
            nValidationResult = Statement.validateTypeOrVarOrFuncName(str);
            if (nValidationResult == 1 || nValidationResult < 0) {
                ERRORTYPES e = ERRORTYPES.BAD_FUNCTION_NAME;
                throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
            } else if (nValidationResult == 2) {
                ERRORTYPES e = ERRORTYPES.IS_KEYWORD;
                throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
            }
        }
        String strParamList = strFunctionDef.substring(m_strFuncRawName.length()).trim();
        if (strParamList.length() < 2)    {
            // parameter list should at least be made up of '(' and ')'
            ERRORTYPES e = ERRORTYPES.NO_PARAMETER_DEFINITION_BORDER;
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);                
        } else if (strParamList.charAt(0) != '(' || strParamList.charAt(strParamList.length() - 1) != ')')    {
            // parameter list should at least be made up of '(' and ')'
            ERRORTYPES e = ERRORTYPES.NO_PARAMETER_DEFINITION_BORDER;
            throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);                
        }
        strParamList = strParamList.substring(1, strParamList.length()-1).trim();
        if (strParamList.equals("") == false)    {
            m_strParams = strParamList.split(",");
            for (int index = 0; index < m_strParams.length; index ++)    {
                m_strParams[index] = m_strParams[index].trim();
                if (m_strParams[index].equals("...") && (index == m_strParams.length - 1))    {
                    m_strParams[index] = "opt_argv";
                    m_bIncludeOptParam = true;
                } else  {
                    nValidationResult = Statement.validateTypeOrVarOrFuncName(m_strParams[index]);
                    if (index == 0 && m_strParams[index].equals("self")) {
                        continue;   // self is a key word, but it is allowed as the first parameter.
                    } else if (nValidationResult == 1 || nValidationResult < 0) {
                        ERRORTYPES e = ERRORTYPES.BAD_VARIABLE_NAME;
                        throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
                    } else if (nValidationResult == 2) {
                        ERRORTYPES e = ERRORTYPES.IS_KEYWORD;
                        throw new JMFPCompErrException(mstatement.mstrFilePath, mstatement.mnStartLineNo, mstatement.mnEndLineNo, e);
                    }
                }
            }
        }
        
    }

    @Override
    public LinkedList<ModuleInfo> getReferredModules(ProgContext progContext) {
        return new LinkedList<ModuleInfo>();
    }

    @Override
    protected void analyze2(FunctionEntry fe) {
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
    
    public String getFunctionPureName() {
        return m_strFuncPureName;
    }
    public String getFunctionNameWithCS() {
        // set it the first time we use it.
        if (m_strFunctionNameWithCS.length() > 0) {
            return m_strFunctionNameWithCS; // m_strFunctionNameWithCS has been set and no need to set again
        } else {
            String[] strarrayCS = m_lCitingSpaces.peek();
            if (strarrayCS == null || strarrayCS.length == 0) {
                m_strFunctionNameWithCS = "::" + m_strFuncShrinkedName;
            } else {
                for (String str : strarrayCS) {
                    m_strFunctionNameWithCS += str + "::";
                }
                m_strFunctionNameWithCS += m_strFuncShrinkedName;
            }
            return m_strFunctionNameWithCS;
        }
    }
    
    @Override
    public void analyze2(ProgContext progContext, boolean bForceReanalyse) {
    	// no need to do anything.
    }
}
