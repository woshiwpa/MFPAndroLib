// MFP project, CallObject.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.OSAdapter.ParallelManager;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Set;

import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.DataClassString;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Oomfp.MFPDataTypeDef;
import com.cyzapps.Jfdatastruct.ArrayBasedDictionary;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.ScriptAnalyzer;
import com.cyzapps.Jmfp.Statement;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jmfp.ScriptAnalyzer.FuncRetException;
import com.cyzapps.Jmfp.ScriptAnalyzer.InFunctionCSManager;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.OSAdapter.LangFileManager;
import static com.cyzapps.OSAdapter.LangFileManager.STRING_DEFAULT_SCRIPT_FOLDER;
import com.cyzapps.OSAdapter.ParallelManager.CallAgent.CallBlockState;
import com.cyzapps.OSAdapter.ParallelManager.CallCommPack.PathAndBytes;
import com.cyzapps.OSAdapter.ParallelManager.LocalObject.SandBoxMessage;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import com.cyzapps.Oomfp.MFPClassDefinition;
import com.cyzapps.Oomfp.SpaceMember.AccessRestriction;
import com.cyzapps.adapter.MFPAdapter;
import com.cyzapps.adapter.MFPAdapter.DefLibPath;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CallObject {
    // the incoming callobject will have the same call point as outgoing callobject. In the outgoing side, all
    // the call points are unique. But the incoming side is not necessary.
	protected Integer callPoint;
	public Integer getCallPoint() {
		return callPoint;
	}
    public Integer remoteCallPoint = -1;    // -1 means remote call point hasn't been set.
	protected ConnectObject connectObject;
	public ConnectObject getConnectObject() {
		return connectObject;
	}
	
    public volatile String callSettings = "";
    
	// the following information is used in the sent end.
	// these member variables are public because they can be set after construction.
	protected Map<String, Variable> mapInterfVariables = new HashMap<String, Variable>();

    public Variable callReturnedVariable = null;
	protected CommBatchListManager commBatchListManager;
	public CommBatchListManager getCommBatchListManager() {
		return commBatchListManager;
	}
	public class ReturnInfo {
		private DataClass datumReturn;
		public DataClass getDatumReturn() { return datumReturn; }
		private String errorMessage;
		public String getErrorMessage() { return errorMessage; }
		public ReturnInfo(DataClass datumRet, String errMsg) {
			datumReturn = datumRet;
			errorMessage = errMsg;
		}
	}
	public volatile ReturnInfo returnInfo;
    
    public static class MFPSessionInfo {
        private final CallObject callObj;
        public CallObject getCallObject() {
            return callObj;
        }
        private final CitingSpaceDefinition csdTop;
        public CitingSpaceDefinition getCSDTop() {
            return csdTop;
        }
        private final String libPath;
        public String getLibPath() {
            return libPath;
        }
        private final String[] libNames;
        
        public LinkedList<String> getAdditionalLibNames() {
            LinkedList<String> additionalLibNames = new LinkedList<String>();
            for (int idx = 0; idx < libNames.length; idx ++) {
                if (!libNames[idx].equals(STRING_DEFAULT_SCRIPT_FOLDER)) {
                    additionalLibNames.add(libNames[idx]);
                }
            }
            return additionalLibNames;
        }
        
        public String[] getAllLibNames() {
            return libNames;
        }
        
        private final Map<String, MFPClassDefinition> classDefinitionMap;
        public Map<String, MFPClassDefinition> getClassDefinitionMap() {
            return classDefinitionMap;
        }
        
        private final Map<String, MFPDataTypeDef> dataTypeDefMap;
        public Map<String, MFPDataTypeDef> getDataTypeDefMap() {
            return dataTypeDefMap;
        }
        
        // both csd and path cannot be null
        public MFPSessionInfo(CallObject callObject, CitingSpaceDefinition csd,
                            Map<String, MFPClassDefinition> classDefMap,
                            Map<String, MFPDataTypeDef> typeDefMap,
                            String path, SortedSet<String> srcLibNames) {
            callObj = callObject;
            csdTop = csd;
            libPath = path;
            libNames = srcLibNames.toArray(new String[0]);
            classDefinitionMap = classDefMap;
            dataTypeDefMap = typeDefMap;
        }
    }
    // map from individual threads to CSD top + source lib path.
    public static Map<Long, MFPSessionInfo> msmapThreadId2SessionInfo = new HashMap<Long, MFPSessionInfo>();
	
    public volatile OnReceiveRequestListener onReceiveRequestListener = null;	// I don't think this needs to be atomic. volatile is enough
	
	public CallObject(ConnectObject conn, int callPnt) {
		connectObject = conn;
		callPoint = callPnt;
		mapInterfVariables = new HashMap<String, Variable>();
		callReturnedVariable = null;
		commBatchListManager = new CommBatchListManager();
		onReceiveRequestListener = new OnReceiveRequestListener();
		returnInfo = new ReturnInfo(null, "");
	}
	
	public void setInterfVariables(Map<String, Variable> mapInterfVars) {
		mapInterfVariables.putAll(mapInterfVars);
		for (Map.Entry<String, Variable> nameVar: mapInterfVariables.entrySet()) {
			InterfVarUpdateRunner r = new InterfVarUpdateRunner(this, nameVar.getKey());
			nameVar.getValue().addWriteUpdateRunner(r);
		}
	}
	
	public void clearInterfVariables() {
		for (Map.Entry<String, Variable> nameVar: mapInterfVariables.entrySet()) {
			InterfVarUpdateRunner r = new InterfVarUpdateRunner(this, nameVar.getKey());
			nameVar.getValue().removeWriteUpdateRunner(r);
		}
		mapInterfVariables.clear();
	}
    
    public final BlockingQueue<SandBoxMessage> sandBoxMessageQueue = new LinkedBlockingQueue<SandBoxMessage>();
    
    public void clearAllSandBoxMessages() {
        // clear all sand box message sent from this call object.
        // this function can only be called locally so that localKey is null.
        SandBoxMessage sbMsg = new SandBoxMessage(null, connectObject.getAddress(), callPoint, "", "", "", "", null);
        // remove all messages from this connect. Note that connectCountpart doesn't involve in comparison
        while (connectObject.protocolObject.sandBoxIncomingMessageQueue.remove(sbMsg));
    }
	
	protected int callCommPackIdx = 0;
	public int getCallCommPackIndex() {
		callCommPackIdx ++;
		return callCommPackIdx;		
	}

	public class OnReceiveRequestListener {
        
        /**
         * This function writes lib files and load citing space definition for this thread. 
         * @param initSetUserLibPath2Bytes set of map from path to source lib file
         * @param initSetUserResourcePath2Bytes set of map from path to resource file
         * @throws IOException 
         */
        public void loadLibs4Request(Set<PathAndBytes> initSetUserLibPath2Bytes, Set<PathAndBytes> initSetUserResourcePath2Bytes) throws IOException {
            LangFileManager langFileManager = FuncEvaluator.msPlatformHWMgr.getLangFileManager();
            // step 1. create temp dir for libs and resources
            File outputFolder = langFileManager.createTempDir();
            String outputFolderPath = outputFolder.getPath();
            // step 2. write source libs (IOException may throw)
            SortedSet<String> libNames = new TreeSet<String>();    // libNames store all lib names relative to outputFolderPath, e.g. Lib0, Lib1, LibUnknown...
            for (PathAndBytes pathAndBytes: initSetUserLibPath2Bytes) {
                if (pathAndBytes.pathPieces.length > 0 && pathAndBytes.pathPieces[0].startsWith(STRING_DEFAULT_SCRIPT_FOLDER)) {
                    libNames.add(pathAndBytes.pathPieces[0]);
                }
                String path = "";
                for (String pathPiece : pathAndBytes.pathPieces) {
                    path += LangFileManager.STRING_PATH_DIVISOR + pathPiece;
                }
                String fullPath = outputFolderPath + path;
                File outputFile = new File(fullPath);
                outputFile.getParentFile().mkdirs();    // ensure that parent folders exist.
                OutputStream os = new FileOutputStream(outputFile);
                os.write(pathAndBytes.bytes);
                os.close();
            }
            // step 3. write resource files (IOException may throw)
            for (PathAndBytes pathAndBytes : initSetUserResourcePath2Bytes) {
                String path = "";
                for (String pathPiece : pathAndBytes.pathPieces) {
                    path += LangFileManager.STRING_PATH_DIVISOR + pathPiece;
                }
                // all the resource files are stored in resource folder
                String fullPath = outputFolderPath + LangFileManager.STRING_PATH_DIVISOR
                        + LangFileManager.STRING_SANDBOX_RESOURCE_FOLDER + path;
                File outputFile = new File(fullPath);
                outputFile.getParentFile().mkdirs();    // ensure that parent folders exist.
                OutputStream os = new FileOutputStream(outputFile);
                os.write(pathAndBytes.bytes);
                os.close();
            }
            // step 4. Now create a copy of citing space definition sys, class definition map sys
            // and datatype definition map sys and set current thread to use this citing space definition,
            // class definition map and datatype definition map.
            CitingSpaceDefinition csdTOPSysNew = new CitingSpaceDefinition(CitingSpaceDefinition.CSD_TOP_SYS);
            Map<String, MFPClassDefinition> classDefMapNew = new HashMap<String, MFPClassDefinition>(MFPClassDefinition.getClassDefinitionMap());
            Map<String, MFPDataTypeDef> dataTypeDefMap = new HashMap<String, MFPDataTypeDef>(MFPDataTypeDef.DATATYPE_DEF_SYS_MAP);
            Long thisThreadId = Thread.currentThread().getId();
            // note that thread id may be reused by JAVA. The following statement ensures
            // old csd mapped to the same thread id is not reused.
            msmapThreadId2SessionInfo.put(thisThreadId, new MFPSessionInfo(CallObject.this, csdTOPSysNew, classDefMapNew, dataTypeDefMap, outputFolderPath, libNames));
            
            // step 5. create a DefLibPath and load remote's user defined lib
            DefLibPath defLibPath = new DefLibPath();
            MFPAdapter.getLibFiles(langFileManager, outputFolderPath, false, defLibPath);
            MFPAdapter.loadLib(defLibPath, false, langFileManager);    // load user defined lib transferred from remote.
        }
        
        /**
         * Separate initial command request so that it can be easily called in a new thread.
         * @param callCommPack 
         */
        public void processInitialCommand(CallCommPack callCommPack) {
            if (!callCommPack.cmd.equals(CallCommPack.INITIALIZE_COMMAND)) {
                return;
            }
            CallObject.this.remoteCallPoint = callCommPack.callPoint;   // callCommPack is from remote so that its callPoint is this call obj's remoteCallPoint.
            String filePath = callCommPack.initFilePath;
            Statement[] sarrayLines = callCommPack.initArrayLines;
            int nDeclarationPosition = callCommPack.initDeclarationPos;
            ProgContext progContext = callCommPack.initProgContext;
            LinkedList<String> listInterfVarNames = callCommPack.initInterfVarNames;
            Set<PathAndBytes> initSetUserLibPath2Bytes = callCommPack.initSetUserLibPath2Bytes;
            Set<PathAndBytes> initSetUserResourcePath2Bytes = callCommPack.initSetUserResourcePath2Bytes;
            
            // now load remote's user lib and create a new citing space definition for this thread.
            // this citing space definition will be used as top level citing space definition for the
            // remote sent script.
            try {
                loadLibs4Request(initSetUserLibPath2Bytes, initSetUserResourcePath2Bytes);
            } catch (IOException ex) {
                // something wrong, return.
                Logger.getLogger(CallObject.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            
            // now let's run the remote sent script.
            // note that we have to use a variable list and a variable map
            // because the variable's order maynot keep in the map.
            Map<String, Variable> mapParams = new HashMap<String, Variable>();
            LinkedList<Variable> lParams = new LinkedList<Variable>();
            try {
                for (String varName: listInterfVarNames) {
                    Variable var = VariableOperator.lookForVarOrMemberVar(varName, AccessRestriction.PRIVATE, progContext);
                    mapParams.put(varName, var);
                    lParams.add(var);
                }
                // we need to setInterfVariables where we will initialize VarUPdateRunner for
                // each interf varaible. Note that here interface variable does NOT include
                // the return variable.
                CallObject.this.setInterfVariables(mapParams);
                // I don't think we need to set update runner for return variable because remote
                // side doesn't care which variable is return variable. After return statement,
                // the remote side fire a RETURN command and send the returned value to the client
                // side and client side save the returned value to return variable and unlock it.

                ScriptAnalyzer sa = new ScriptAnalyzer();
                try {
                    InFunctionCSManager inFuncCSMgr = new InFunctionCSManager(progContext.mstaticProgContext);
                    // I dont think that we should acquire locks for interf variables.
                    // otherwise, values of interf variables cannot be updated
                    sa.analyzeBlock(sarrayLines, nDeclarationPosition, lParams, inFuncCSMgr, progContext);
                } catch(FuncRetException e)    {
                    // remember if call block doesnt return any value, e.m_datumReturn is null
                    CallCommPack callCommPackReturn
                        = new CallCommPack(CallObject.this.getCallPoint(), CallObject.this.getCallCommPackIndex(), e.m_datumReturn);
                    String strSerialized = FuncEvaluator.msCommMgr.serialize(callCommPackReturn);   // assume FuncEvaluator.msCommMgr is not null
                    String cmd = CallCommPack.RETURN_COMMAND;
                    Boolean sendResult = getConnectObject().sendCallRequest(CallObject.this, cmd, "", strSerialized); // no exception thrown
                }
            } catch(Exception e)    {
                // ScriptStatementException | JMFPCompErrException | other exception types.
                CallCommPack callCommPackException
                    = new CallCommPack(CallObject.this.getCallPoint(), CallObject.this.getCallCommPackIndex(), e.getMessage(), MFPAdapter.outputException(e));
                String strSerialized = "";
                try {
                    strSerialized = FuncEvaluator.msCommMgr.serialize(callCommPackException);   // assume FuncEvaluator.msCommMgr is not null
                } catch (IOException e1) {
                    // This will not happen
                }
                String cmd = CallCommPack.EXCEPTION_COMMAND;
                Boolean sendResult = getConnectObject().sendCallRequest(CallObject.this, cmd, "", strSerialized); // no exception thrown
            } finally {
                CallObject.this.onReceiveRequestListener = null;
                CallObject.this.clearInterfVariables();	// clear Interface variable immediately after we exit the while loop.
                //CallObject.this.clearAllSandBoxMessages();  // do not clear all messages left in the sandbox message queues. They might be useful.
                // we do not remove the call object id from connect object even after the call block returns.
                // this is because the call block may starts a thread and receive message in the thread.
                // if we remove the call object id from its connect object, message sender will never be able
                // to send message to that thread.
                //connectObject.allCalls.remove(callPoint);	// we do not need the call object after everything sorted out.
            }
        }
		// this function doesn't throw an exception. If there is an exception occurs when setting datumReturn,
		// this function will return the datumReturn's initial state.
		// datumReturn set to null means there is an internal error. Note that returnInfo assignment should be
		// in the statement before notice and return and should only occur once in the function.
		public void onReceiveRequest(CallCommPack callCommPack, String srcConnectLocalAddr, String srcConnectRemoteAddr) {
	    	commBatchListManager.add(callCommPack);
	
	    	if (callCommPack.cmd.equals(CallCommPack.INITIALIZE_COMMAND)) { // this call object is server and callCommPack is from remote client
                final CallCommPack callCommPackRef = callCommPack;
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // run the remote sent script in a different thread so that we can use remote's user lib
                        processInitialCommand(callCommPackRef);
                    }
                });
                t.start(); 
	    	} else if (callCommPack.cmd.equals(CallCommPack.RETURN_COMMAND)) { // This call object is client and the call comm pack is from remote server.
	    		// this is return
		    	CallBlockState returnResult = CallBlockState.RETURN_SUCCESSFUL;
		    	synchronized(CallObject.this) {
		    		DataClass datum2Ret = ArrayBasedDictionary.createArrayBasedDict();
		    		try {
				    	DataClass datumCallBlockState = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(returnResult.getValue()));
				    	datum2Ret = ArrayBasedDictionary.setArrayBasedDictValue(datum2Ret, "CALL_BLOCK_STATE", datumCallBlockState);
			    		if (callCommPack.returnReturnedValue != null) {
                            // it does return something.
                            datum2Ret = ArrayBasedDictionary.setArrayBasedDictValue(datum2Ret, "CALL_BLOCK_RETURN",
                                    callCommPack.returnReturnedValue); // cast the returned value to data array                            
                        }
				    	returnInfo = new ReturnInfo(datum2Ret, "");
		    		} catch (Exception e) {
				    	returnInfo = new ReturnInfo(null, e.getMessage());	// this shouldn't happen
		    		}
			    	CallObject.this.notify();    // ok, now I notify the blocked CallAgent that I am finished.
		    	}
	    	} else if (callCommPack.cmd.equals(CallCommPack.VALUE_COMMAND)) { // can be either from server or client
	    		// this is to post value update to the running script.
	    		String variableName = callCommPack.valueVariableName;
	    		for (Map.Entry<String, Variable> nameVar : mapInterfVariables.entrySet()) {
	        		if (nameVar.getKey().equals(variableName)) {
	        			// find the variable name
                        InterfVarUpdateRunner r = new InterfVarUpdateRunner(CallObject.this, nameVar.getKey());
                        // notice all others but not my remote
	        			nameVar.getValue().setValueSkipNotice((DataClass)callCommPack.valueVariableValue, r);
	        			break;
	        		}
	        	}
	    	} else if (callCommPack.cmd.equals(CallCommPack.EXCEPTION_COMMAND)) { // This call object is client and the call comm pack is from remote server
	    		// this is an exception
		    	CallBlockState returnResult = CallBlockState.RETURN_FAILED_WITH_MISSING_UPDATES;
	    		DataClass datum2Ret = ArrayBasedDictionary.createArrayBasedDict();

	    		synchronized(CallObject.this) {
			    	try {
				    	DataClass datumCallBlockState = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(returnResult.getValue()));
				    	datum2Ret = ArrayBasedDictionary.setArrayBasedDictValue(datum2Ret, "CALL_BLOCK_STATE", datumCallBlockState);
				    	datum2Ret = ArrayBasedDictionary.setArrayBasedDictValue(datum2Ret, "CALL_BLOCK_EXCEPTION_DETAILS",
				    			new DataClassString(callCommPack.exceptionDetails));
			    		returnInfo = new ReturnInfo(datum2Ret, callCommPack.exceptionMessage);
			    	} catch (Exception e) {
			    		returnInfo = new ReturnInfo(null, e.getMessage());	// this shouldn't happen
			    	}
			    	CallObject.this.notify();    // ok, now I notify the blocked CallAgent that I am finished.
		    	}
	    	} else if (callCommPack.cmd.equals(CallCommPack.DATA_COMMAND)) {
                int destCallId = callCommPack.callIdDest;
                String destConnectId = callCommPack.connectIdDest;
                LocalObject.LocalKey destInterface = callCommPack.interfaceInfoDest;
                if (destInterface == null) {
                    // destination is main entity
                    LocalObject localObj = CallObject.this.getConnectObject().getProtocolObject();
                    try {
                        LocalObject.SandBoxMessage sbm = new LocalObject.SandBoxMessage(
                                callCommPack.interfaceInfoSrc,
                                callCommPack.connectIdSrc,
                                callCommPack.parentCallIdSrc,
                                srcConnectLocalAddr,
                                srcConnectRemoteAddr,
                                CallObject.this.getConnectObject().getSourceAddress(),
                                CallObject.this.getConnectObject().getAddress(),   // CallObject.this is the callObj of the transmission connect, not message dest callObj
                                callCommPack.dataValue);
                        localObj.sandBoxIncomingMessageQueue.put(sbm);
                    } catch (InterruptedException ex) {
                        // will not be here.
                        Logger.getLogger(OnReceiveRequestListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    // destination is a sandbox
                    String strProtocol = destInterface.getProtocolName();
                    String strAddress = destInterface.getLocalAddress();
                    LocalObject.LocalKey destLocalInfo = new LocalObject.LocalKey(strProtocol, strAddress);
                    CommunicationManager commMgr = FuncEvaluator.msCommMgr;
                    LocalObject localObj = commMgr.findLocal(destLocalInfo);
                    if (localObj == null) {
                        Logger.getLogger(OnReceiveRequestListener.class.getName())
                                .log(Level.SEVERE, "Local object cannot be found from local key {0}.",
                                        destLocalInfo.getProtocolName() + "-" + destLocalInfo.getLocalAddress());
                    } else {
                        CallObject callObj = null;
                        if (localObj.containConnectAddr(destConnectId)) {
                            ConnectObject connectObj = localObj.getConnect(destConnectId);
                            if (connectObj.allInCalls.containsKey(destCallId)) {
                                callObj = connectObj.allInCalls.get(destCallId);
                            } else {
                                Logger.getLogger(OnReceiveRequestListener.class.getName())
                                        .log(Level.SEVERE, "Call object cannot be found from local key {0} connect id {1} call id {2}.",
                                                new Object[] {
                                                    destLocalInfo.getProtocolName() + "-" + destLocalInfo.getLocalAddress(),
                                                    destConnectId, destCallId
                                                });
                            }
                        } else {
                            Logger.getLogger(OnReceiveRequestListener.class.getName())
                                    .log(Level.SEVERE, "Connect object cannot be found from local key {0} connect id {1}.",
                                            new Object[] {
                                                destLocalInfo.getProtocolName() + "-" + destLocalInfo.getLocalAddress(),
                                                destConnectId, destCallId
                                            });
                        }
                        if (callObj != null) {
                            // it is sent from remote, so use remote source info
                            LocalObject.SandBoxMessage sbm = new LocalObject.SandBoxMessage(
                                    callCommPack.interfaceInfoSrc,
                                    callCommPack.connectIdSrc,
                                    callCommPack.parentCallIdSrc,
                                    srcConnectLocalAddr,
                                    srcConnectRemoteAddr,
                                    CallObject.this.getConnectObject().getSourceAddress(),
                                    CallObject.this.getConnectObject().getAddress(),   // CallObject.this is the callObj of the transmission connect, not message dest callObj
                                    callCommPack.dataValue);

                            try {
                                callObj.sandBoxMessageQueue.put(sbm);
                            } catch (InterruptedException ex) {
                                // this will never happen.
                                Logger.getLogger(OnReceiveRequestListener.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
                // no need to remove transient call from dictionary
                //connectObject.removeCallObject(callPoint);
            }
	    	// We do not check the order of call comm pack. if return or exception, notify the waiting thread and quit
	    	return;
	    }
	}
}
