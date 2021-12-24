// MFP project, StatementProcHelper.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.Jmfp;

import java.util.Map;

import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassString;
import com.cyzapps.Jfcalc.ElemAnalyzer;
import com.cyzapps.Jfcalc.DCHelper.CurPos;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;

public class StatementProcHelper {
    // this function replace all the string expressions in strStatement by a simple string
    // based id. In this way, we can avoid contrigacies when parse the strStatement, for example
    // to divide statement
    // for "a to " to by a
    // by 'to', we will convert the above statement to 
    // for "1" to by a
    // and stores "1" ->  "a to " in the map.
    public static String convertStrExprs(String strStatement, Map<String, String> map) {
    	CurPos curpos = new CurPos();
    	int key = 0;
    	int nStrStart = 0, nStrEnd = 0;
    	String strReturn = "";
    	while (curpos.m_nPos < strStatement.length()) {
    		if (strStatement.charAt(curpos.m_nPos) == '"') {
	    		try {
	    			nStrStart = curpos.m_nPos;
	    			strReturn += strStatement.substring(nStrEnd, nStrStart);
	    			ElemAnalyzer.getString(strStatement, curpos);
	    			String strKey = "\"" + key + "\"";
	    			strReturn += strKey;
	    			key ++;
	    			nStrEnd = curpos.m_nPos;
	    			String strValue = strStatement.substring(nStrStart, nStrEnd);
	    			map.put(strKey, strValue);
	    		} catch (JFCALCExpErrException e) {
	    			// this exception only throws out when we cannot find close quote char
	    			// in this case it is not recognized as a string anyway. And when the
	    			// thread is thrown, we must have arrived at the end of the string. So
	    			// throwing exception doesn't affect final result.
	    			nStrEnd = nStrStart;
	    			break;
	    		}
	    	} else {
	    		curpos.m_nPos ++;
	    	}
    	}
    	if (nStrEnd == 0) {
    		return strStatement;	// no " found.
    	} else {
	    	strReturn += strStatement.substring(nStrEnd, strStatement.length());
	    	return strReturn;
    	}
    }
    
    // convert it back.
    public static String replaceStrExprs(String strStatement, Map<String, String> map) {
    	if (map.isEmpty()) {
    		return strStatement;	// no key-value pair found.
    	}
    	CurPos curpos = new CurPos();
    	int key = 0;
    	int nStrStart = 0, nStrEnd = 0;
    	String strReturn = "";
    	while (curpos.m_nPos < strStatement.length()) {
    		if (strStatement.charAt(curpos.m_nPos) == '"') {
	    		try {
	    			nStrStart = curpos.m_nPos;
	    			strReturn += strStatement.substring(nStrEnd, nStrStart);
	    			DataClass datumStr = ElemAnalyzer.getString(strStatement, curpos);
	    			String strKey = ((DataClassString)datumStr).getStringValue();
	    			nStrEnd = curpos.m_nPos;
	    			String strValue = map.get("\"" + strKey + "\"");
	    			if (null != strValue) {
	    				strReturn += strValue;	// the key exists.
	    			} else {
	    				strReturn += strStatement.substring(nStrStart, nStrEnd);	// the key not exists.
	    			}
	    		} catch (JFCALCExpErrException e) {
	    			// this exception only throws out when we cannot find close quote char
	    			// in this case it is not recognized as a string anyway. And when the
	    			// thread is thrown, we must have arrived at the end of the string. So
	    			// throwing exception doesn't affect final result.
	    			nStrEnd = nStrStart;
	    			break;
	    		}
	    	} else {
	    		curpos.m_nPos ++;
	    	}
    	}
    	if (nStrEnd == 0) {
    		return strStatement;	// no " found.
    	} else {
	    	strReturn += strStatement.substring(nStrEnd, strStatement.length());
	    	return strReturn;
    	}
    }
}
