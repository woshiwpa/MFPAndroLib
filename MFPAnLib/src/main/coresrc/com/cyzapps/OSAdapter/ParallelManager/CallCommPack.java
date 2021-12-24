// MFP project, CallCommPack.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.OSAdapter.ParallelManager;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;

import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassNull;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.Statement;
import com.cyzapps.OSAdapter.LangFileManager;
import com.cyzapps.OSAdapter.ParallelManager.LocalObject.LocalKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CallCommPack implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	public int callPoint;
	public int index;
	// at this moment,
    // TYPE is "initialize": initialization,
    // TYPE is "value": value exchange,
	// TYPE is "return": return,
    // TYPE is "exception": exception
    // TYPE is "data": data message exchange,
	public static final String INITIALIZE_COMMAND = "initialize";
	public static final String VALUE_COMMAND = "value";
	public static final String RETURN_COMMAND = "return";
	public static final String EXCEPTION_COMMAND = "exception";
    public static final String DATA_COMMAND = "data";
	public String cmd;
	/*
	 * -----------------------------------------------------
	 * If cmd type is initialize, map content would be like:
	 * "sarryLines": sarrayLines
     * "nDeclarationPosition": nDeclarationPosition
     * "listInterfVarNames": listInterfVarNames
     * "progContext": progContext
	 * -----------------------------------------------------
	 * If cmd type is maintain, map content would be like:
     * -----------------------------------------------------
	 * If cmd type is value
	 * "variableName": variable name
	 * "variableValue": variable's new value
	 * -----------------------------------------------------
	 * If cmd type is return
	 * "returnedValue": returned value
	 * -----------------------------------------------------
	 * If cmd type is exception
	 * "thrownValue": thrown value
	 * "stack": stack info
	 * -----------------------------------------------------
	 */
	
	public String initFilePath;
	public Statement[] initArrayLines;
	public int initDeclarationPos;
	public LinkedList<String> initInterfVarNames;
	public ProgContext initProgContext;
    
    public static class PathAndBytes implements Serializable {
        public String[] pathPieces; // use string array to store path because the file seperator is os dependant.
        public byte[] bytes;
        public PathAndBytes(String path, byte[] allBytes) {
            if (LangFileManager.STRING_PATH_DIVISOR.equals("\\")) {
                pathPieces = path.split(LangFileManager.STRING_PATH_DIVISOR + LangFileManager.STRING_PATH_DIVISOR);
            } else {
                pathPieces = path.split(LangFileManager.STRING_PATH_DIVISOR);
            }
            bytes = allBytes;
        }
    }
    
    public Set<PathAndBytes> initSetUserLibPath2Bytes;
    public Set<PathAndBytes> initSetUserResourcePath2Bytes;
	public String valueVariableName;
	public DataClass valueVariableValue;
	public DataClass returnReturnedValue;	// not that this can be null.
	public String exceptionMessage;
    public String exceptionDetails;
    public LocalKey interfaceInfoSrc;
    public String connectIdSrc;
    public int parentCallIdSrc;
    public LocalKey interfaceInfoDest;
    public String connectIdDest;
    public int callIdDest;
    public DataClass dataValue;
	
	public CallCommPack(int callPnt, int Idx, String filePath, Statement[] arrayLines, int declarationPos,
						LinkedList<String> interfVarNames, ProgContext progContxt, Map<String, byte[]> mapUserLibPath2Bytes, Map<String, byte[]> mapUserResourcePath2Bytes) {
        callPoint = callPnt;
        index = Idx;
        cmd = INITIALIZE_COMMAND;
        initFilePath = filePath;
        initArrayLines = arrayLines;
        initDeclarationPos = declarationPos;
        initInterfVarNames = interfVarNames;
        initProgContext = progContxt;
        initSetUserLibPath2Bytes = new HashSet<PathAndBytes>();
        for (String path : mapUserLibPath2Bytes.keySet()) {
            initSetUserLibPath2Bytes.add(new PathAndBytes(path, mapUserLibPath2Bytes.get(path)));
        }
        initSetUserResourcePath2Bytes = new HashSet<PathAndBytes>();
        for (String path : mapUserResourcePath2Bytes.keySet()) {
            initSetUserResourcePath2Bytes.add(new PathAndBytes(path, mapUserResourcePath2Bytes.get(path)));
        }
	}

	public CallCommPack(int callPnt, int Idx, String variableName, DataClass variableValue) {
        callPoint = callPnt;
        index = Idx;
        cmd = VALUE_COMMAND;
        valueVariableName = variableName;
        valueVariableValue = variableValue;
	}
	
	public CallCommPack(int callPnt, int Idx, DataClass returnedValue) {
        callPoint = callPnt;
        index = Idx;
        cmd = RETURN_COMMAND;
        returnReturnedValue = returnedValue;
	}
	
	public CallCommPack(int callPnt, int Idx, String exceptionMsg, String exceptionDtl) {
        callPoint = callPnt;
        index = Idx;
        cmd = EXCEPTION_COMMAND;
        exceptionMessage = exceptionMsg;
        exceptionDetails = exceptionDtl;
	}
	
	public CallCommPack(int callPnt, LocalKey interfInfoSrc, String conntIdSrc, int pntCallIdSrc,
            LocalKey interfInfoDest, String conntIdDest, int clIdDest, DataClass dtValue) {
	    // note that callcommpack can be sent from a call object of source connect in an interface
        // to the second local interface then transmitted to a remote interface (via an intermediate
        // connect) and then to a call object of destination connect in the second remote interface.
        // Therefore, at most there are 4 interfaces involved, 1. local source interface; 2. local
        // transmit interface; 3. remote transmit interface; 4. remote destination interface. And
        // there are at most 3 connect objects involved, 1. Source connect object; 2. transmission
        // connect link 3. destination connect object. Therefore, connect Src and connect Dest may
        // have nothing with data transmission. For example, connect Src and connect Dest may both
        // webRTC connect objects while the data link to transmit this CallCommPack is actually TCPIP.
        callPoint = callPnt;
        index = 1;
        cmd = DATA_COMMAND;
        interfaceInfoSrc = interfInfoSrc;
        connectIdSrc = conntIdSrc;
        parentCallIdSrc = pntCallIdSrc;
        interfaceInfoDest = interfInfoDest;
        connectIdDest = conntIdDest;
        callIdDest = clIdDest;
        dataValue = dtValue;
	}
}
