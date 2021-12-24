/*
 * MFP project, MemberFunction.java : Designed and developed by Tony Cui in 2021
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Oomfp;

import java.util.List;

/**
 *
 * @author tonyc
 */
public abstract class MemberFunction extends SpaceMember {
	private static final long serialVersionUID = 1L;

	public enum Operability {
        BEHAVIOUR (0),
        INFLUENCE (1);
        private int value; 

        private Operability(int i) { 
            value = i; 
        }
        
        private Operability() {
            value = 0;
        }

        public int getValue() { 
            return value; 
        }
    }
    protected Operability moperability;    // default is behaviour
    
    protected Boolean mbIsAbstract = false;
    
    protected String[] mstrarrayPathSpace = new String[0];    //String[0] means a built-in path space or a session in command line.
    public String[] getPathSpace() {
        return mstrarrayPathSpace;
    }
    public MemberFunction() {}
    public MemberFunction(String[] strarrayPathSpace) {
        mstrarrayPathSpace = strarrayPathSpace;
    }
    // this function identifies if calling style of function 1 matches function 2's definition or not.
    // this function assumes
    // 1. strShrinkedRawName is shrinked and small letter function name with full or partial CS path
    // 2. strAbsFuncName is to be matched shrinked and small letter function name with absolute CS path
    // 3. if nParamNum == -1, do not compare number of parameters
    // 4. nMaxParamNum == -1 means no upper limit.
    // 5. lCitingSpaces can include wild card char (*)
    public static boolean matchFunctionCall(String strShrinkedRawName, int nParamNum,   // function 1
                                            String strAbsFuncName, int nMinParamNum, int nMaxParamNum,  // function 2
                                            List<String[]> lCitingSpaces) { // citing space
        
        if (strAbsFuncName.endsWith(strShrinkedRawName)) {   // avoid unecessary comparison
            if (strAbsFuncName.length() == strShrinkedRawName.length())  {
                // absolute name with cs
                if ((nParamNum >= nMinParamNum && (nMaxParamNum == -1 || nParamNum <= nMaxParamNum))
                        || nParamNum == -1) {
                    // match
                    return true;
                }
            } else if (strAbsFuncName.length() - strShrinkedRawName.length() >= 2
                    && strAbsFuncName.charAt(strAbsFuncName.length() - strShrinkedRawName.length() - 1) == ':'
                    && strAbsFuncName.charAt(strAbsFuncName.length() - strShrinkedRawName.length() - 2) == ':') {
                // relative name
                String strCSPart = "";
                String strNamePart = "::" + strShrinkedRawName;
                for (int idx = 0; idx < lCitingSpaces.size(); idx ++) {
                    String[] strarrayCitingSpace = lCitingSpaces.get(idx);    // strarrayCitingSpace != null and size > 0
                    if (strarrayCitingSpace[strarrayCitingSpace.length - 1].equals("*")) {
                        // means the citing space includes all sub-spaces.
                        for (int idx1 = 0; idx1 < strarrayCitingSpace.length - 1; idx1 ++) {
                            strCSPart += strarrayCitingSpace[idx1] + "::";
                        }
                        if ((strCSPart.length() + strNamePart.length() - 2 <= strAbsFuncName.length()
                                    && strAbsFuncName.startsWith(strCSPart))
                                && ((nParamNum >= nMinParamNum && (nMaxParamNum == -1 || nParamNum <= nMaxParamNum))
                                    || nParamNum == -1)) {
                            // match
                            return true;
                        }
                    } else {
                        // no sub-space to consider.
                        for (int idx1 = 0; idx1 < strarrayCitingSpace.length; idx1 ++) {
                            strCSPart += strarrayCitingSpace[idx1] + "::";
                        }
                        if ((strCSPart.length() + strNamePart.length() - 2 == strAbsFuncName.length()
                                    && strAbsFuncName.startsWith(strCSPart))
                                && ((nParamNum >= nMinParamNum && (nMaxParamNum == -1 || nParamNum <= nMaxParamNum))
                                    || nParamNum == -1)) {
                            // match
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
    
    public abstract boolean matchFunctionCall(String strShrinkedRawName, int nParamNum,   // function 1
                                            List<String[]> lCitingSpaces);    // citing spaces
        
    // this function identifies if function 1's definition matches function 2's definition or not.
    // this function assumes
    // 1. strShrinkedRawName is shrinked and small letter function name with full or partial CS path
    // 2. strAbsFuncName is to be matched shrinked and small letter function name with absolute CS path
    // 3. nParamNum is the minimum number of parameters of function 1. If it is -1, do not compare number of parameters
    // 4. nMaxParamNum == -1 means no upper limit.
    // 5. lCitingSpaces can include wild card char (*)
    public static boolean matchFunctionDef(String strShrinkedRawName, int nParamNum, boolean bWithOptionalParam,  // function 1
                                            String strAbsFuncName, int nMinParamNum, int nMaxParamNum,  // function 2
                                            List<String[]> lCitingSpaces) { // citing space
        
        if (strAbsFuncName.endsWith(strShrinkedRawName)) {   // avoid unecessary comparison
            if (strAbsFuncName.length() == strShrinkedRawName.length())  {
                // absolute name with cs
                if (nParamNum == -1) {
                    return true;
                } else if (bWithOptionalParam) {
                    // with optional parameter
                    if (nParamNum == nMinParamNum && nMinParamNum != nMaxParamNum)  {
                        return true;
                    }
                } else {
                    // without optional parameter
                    if (nParamNum == nMinParamNum && nMinParamNum == nMaxParamNum)  {
                        return true;
                    }
                }
            } else if (strAbsFuncName.length() - strShrinkedRawName.length() >= 2
                    && strAbsFuncName.charAt(strAbsFuncName.length() - strShrinkedRawName.length() - 1) == ':'
                    && strAbsFuncName.charAt(strAbsFuncName.length() - strShrinkedRawName.length() - 2) == ':') {
                // relative name
                String strCSPart = "";
                String strNamePart = "::" + strShrinkedRawName;
                for (int idx = 0; idx < lCitingSpaces.size(); idx ++) {
                    String[] strarrayCitingSpace = lCitingSpaces.get(idx);    // strarrayCitingSpace != null and size > 0
                    if (strarrayCitingSpace[strarrayCitingSpace.length - 1].equals("*")) {
                        // means the citing space includes all sub-spaces.
                        for (int idx1 = 0; idx1 < strarrayCitingSpace.length - 1; idx1 ++) {
                            strCSPart += strarrayCitingSpace[idx1] + "::";
                        }
                        if (strCSPart.length() + strNamePart.length() - 2 <= strAbsFuncName.length()
                                    && strAbsFuncName.startsWith(strCSPart))    {
                            // function names match
                            if (nParamNum == -1) {
                                return true;
                            } else if (bWithOptionalParam) {
                                // with optional parameter
                                if (nParamNum == nMinParamNum && nMinParamNum != nMaxParamNum)  {
                                    return true;
                                }
                            } else {
                                // without optional parameter
                                if (nParamNum == nMinParamNum && nMinParamNum == nMaxParamNum)  {
                                    return true;
                                }
                            }
                        }
                    } else {
                        // no sub-space to consider.
                        for (int idx1 = 0; idx1 < strarrayCitingSpace.length; idx1 ++) {
                            strCSPart += strarrayCitingSpace[idx1] + "::";
                        }
                        if (strCSPart.length() + strNamePart.length() - 2 == strAbsFuncName.length()
                                    && strAbsFuncName.startsWith(strCSPart))    {
                            // function names match
                            if (nParamNum == -1) {
                                return true;
                            } else if (bWithOptionalParam) {
                                // with optional parameter
                                if (nParamNum == nMinParamNum && nMinParamNum != nMaxParamNum)  {
                                    return true;
                                }
                            } else {
                                // without optional parameter
                                if (nParamNum == nMinParamNum && nMinParamNum == nMaxParamNum)  {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
    
    public abstract boolean matchFunctionDef(String strShrinkedRawName, int nParamNum, boolean bWithOptionalParam,   // function 1
                                            List<String[]> lCitingSpaces);    // citing spaces
    
    public abstract String getAbsNameWithCS();
    
    public abstract String getPureNameWithoutCS();
    
    public abstract int getMinNumParam();
    
    public abstract int getMaxNumParam();
    
    public abstract boolean isIncludeOptParam();
}
