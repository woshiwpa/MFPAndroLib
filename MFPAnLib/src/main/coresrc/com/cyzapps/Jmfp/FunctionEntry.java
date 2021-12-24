/*
 * MFP project, FunctionEntry.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import com.cyzapps.Jmfp.ScriptAnalyzer.InFunctionCSManager;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import com.cyzapps.Oomfp.MemberFunction;
import com.cyzapps.adapter.MFPAdapter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author tonyc
 */
public class FunctionEntry extends MemberFunction {
	private static final long serialVersionUID = 1L;
	protected Statement_function m_sf = null;
    public Statement_function getStatementFunction() { return m_sf; }
    protected int m_nStartStatementPos = -1;
    public int getStartStatementPos() { return m_nStartStatementPos; }
    protected Statement_endf m_sendf = null;
    public Statement_endf getStatementEnf() { return m_sendf; }
    protected int m_nEndStatementPos = -1;
    public int getEndStatementPos() { return m_nEndStatementPos; }
    protected int[] m_nlistHelpBlock = null;
    public int[] getListHelpBlock() { return m_nlistHelpBlock; }
    protected String[] m_strLines = new String[0];
    public String[] getStringLines() { return m_strLines; }
    protected Statement[] m_sLines = new Statement[0];
    public Statement[] getStatementLines() { return m_sLines; }

    public FunctionEntry() {
        mstrarrayPathSpace = new String[0];    //String[0] means a built-in path space.
    }
    
    public FunctionEntry(String[] pathSpace, Statement_function sf, int nStartStatementPos, Statement_endf sendf, int nEndStatementPos,
                        int[] listHelpBlock, String[] strLines, Statement[] sLines) {
        mstrarrayPathSpace = pathSpace;    //String[0] means a built-in path space or a session in command line.
        m_sf = sf;
        maccess = m_sf.access;  // set private or public
        m_nStartStatementPos = nStartStatementPos;
        m_sendf = sendf;
        m_nEndStatementPos = nEndStatementPos;
        m_nlistHelpBlock = listHelpBlock;
        m_strLines = strLines;
        m_sLines = sLines;
    }
    
    @Override
    public boolean matchFunctionCall(String strShrinkedRawName, int nParamNum,   // function 1
                                     List<String[]> lCitingSpaces) {    // citing spaces
        int nMinParamNum = 0;
        int nMaxParamNum = 0;
        String strAbsFuncName = m_sf.getFunctionNameWithCS();
        nMinParamNum = nMaxParamNum = m_sf.m_strParams.length;
        if (m_sf.m_bIncludeOptParam) {
            nMinParamNum --;
            nMaxParamNum = -1;
        }
        return matchFunctionCall(strShrinkedRawName, nParamNum, strAbsFuncName, nMinParamNum, nMaxParamNum, lCitingSpaces);
    }
    
    @Override
    public boolean matchFunctionDef(String strShrinkedRawName, int nParamNum, boolean bWithOptionalParam,   // function 1
                                    List<String[]> lCitingSpaces) {    // citing spaces
        int nMinParamNum = 0;
        int nMaxParamNum = 0;
        String strAbsFuncName = m_sf.getFunctionNameWithCS();
        nMinParamNum = nMaxParamNum = m_sf.m_strParams.length;
        if (m_sf.m_bIncludeOptParam) {
            nMinParamNum --;
            nMaxParamNum = -1;
        }
        return matchFunctionDef(strShrinkedRawName, nParamNum, bWithOptionalParam, strAbsFuncName, nMinParamNum, nMaxParamNum, lCitingSpaces);
    }
    
    @Override
    public String getAbsNameWithCS() {
        return m_sf.getFunctionNameWithCS();
    }
    
    @Override
    public String getPureNameWithoutCS() {
        return m_sf.getFunctionPureName();
    }
    
    @Override
    public int getMinNumParam() {
        int nMinParamNum = m_sf.m_strParams.length;
        if (m_sf.m_bIncludeOptParam) {
            nMinParamNum --;
        }
        return nMinParamNum;
    }
    
    @Override
    public int getMaxNumParam() {
        int nMaxParamNum = m_sf.m_strParams.length;
        if (m_sf.m_bIncludeOptParam) {
            nMaxParamNum = -1;
        }
        return nMaxParamNum;
    }
    
    @Override
    public boolean isIncludeOptParam() {
        return m_sf.m_bIncludeOptParam;
    }
    // receive a function name (with or without CS), and seperate it into string arrays with CSs and name
    // the elements in the string array are small case and trimmed.
    public static String[] getNameWithCSArray(String strFunctionName) {
        String[] strarrayNameWithCS = strFunctionName.toLowerCase(Locale.US).split("::");
        for (int idx = 0; idx < strarrayNameWithCS.length; idx ++) {
            strarrayNameWithCS[idx] = strarrayNameWithCS[idx].trim();
        }
        return strarrayNameWithCS;
    }
    
    /**
     * return a shrinked function name or cs name, for example :: Abc will be
     * turned to ::Abc, and aBC :: dEf will be truned to aBC::dEF.
     * this function doesn't change big letters to small letters.
     * @param strName
     * @return 
     */
    public static String getShrinkedName(String strName) {
        // we need to consider two special case: 1). "::", 2). "...::".
        String[] strarrayCS = strName.split("::");
        String strShrinkedRawName = "";
        for (int idx1 = 0; idx1 < strarrayCS.length; idx1 ++) {
            strShrinkedRawName += strarrayCS[idx1].trim();
            if (idx1 < strarrayCS.length - 1) {
                strShrinkedRawName += "::";
            }
        }
        if (strName.endsWith("::") && !strShrinkedRawName.endsWith("::")) {
            strShrinkedRawName += "::";
        }
        return strShrinkedRawName;
    }
}
