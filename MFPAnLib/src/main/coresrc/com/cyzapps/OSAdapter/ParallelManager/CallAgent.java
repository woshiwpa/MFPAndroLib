// MFP project, CallAgent.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.OSAdapter.ParallelManager;

import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.DataClassString;
import com.cyzapps.Jfdatastruct.ArrayBasedDictionary;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.AnnoType_build_asset;
import com.cyzapps.Jmfp.CompileAdditionalInfo;
import com.cyzapps.Jmfp.ErrorProcessor.ERRORTYPES;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import com.cyzapps.Jmfp.ModuleInfo;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.ScriptAnalyzer;
import com.cyzapps.Jmfp.Statement;
import com.cyzapps.Jmfp.Statement_call;
import com.cyzapps.Jmfp.Statement_endcall;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.OSAdapter.LangFileManager;
import com.cyzapps.OSAdapter.LangFileManager.SourceDestPathMapInfo;
import com.cyzapps.OSAdapter.ParallelManager.CallObject.ReturnInfo;
import com.cyzapps.adapter.MFPAdapter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import static com.cyzapps.OSAdapter.ParallelManager.CallObject.msmapThreadId2SessionInfo;
import com.cyzapps.Oomfp.SpaceMember.AccessRestriction;

/**
 *
 * @author tonyc
 */
public class CallAgent {
	public static enum CallBlockState {
		SEND_SUCCESSFUL(0),
		SEND_FAILED(-1),
		RETURN_SUCCESSFUL(1),
		RETURN_SUCCESSFUL_WITH_MISSING_UPDATES(2),
		RETURN_FAILED(-2),
		RETURN_FAILED_WITH_MISSING_UPDATES(-3),
		INTERNAL_ERROR(-100);
		
        private int value; 

        private CallBlockState(int i) { 
            value = i; 
        } 

        public int getValue() { 
            return value; 
        } 
	}
	
	public static enum CallBlockVariableType {
		RETURNED_VARIABLE(0),
		INTERFACE_VARIABLE(1);		
        private int value; 

        private CallBlockVariableType(int i) {
            value = i; 
        } 

        public int getValue() { 
            return value; 
        }
	}
	
    // do not use executor because this may result command line never return
	//public static final int MAX_NUMBER_OF_CONCURRENT_CALLS = 16;
	//ExecutorService executor = Executors.newFixedThreadPool(MAX_NUMBER_OF_CONCURRENT_CALLS);
	Map<Integer, Object> points = new ConcurrentHashMap<Integer, Object>();	// thread safe
    public CallAgent()    {
    }
    
    public void submitCall(
            boolean isCallLocal,    // is it call local?
            final DataClass datumConnect,	// connect dataclass
            DataClass datumSettings,	// settings dataclass (e.g. sync mode or async mode), could be null which means default
            final Statement[] sarrayLines,    // statements of this script
            final int nDeclarationPosition,    // The position of block declaration in sarrayLines
            final int nEndPosition, // end position of the block. If it >= sarrayLines.length, means no endcall statement.
            LinkedList<String> listInterfVarNames,    // The interface variable list
            ProgContext progContext)    // The unknown variable list + variable name space
            throws
            InterruptedException,    // sleep function may throw Interrupted Exception
            IOException,	// failure of serialize may throw IOException
            JMFPCompErrException,
            JFCALCExpErrException    {
    	if (FuncEvaluator.msCommMgr == null) {
        	throw new JFCALCExpErrException(com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES.ERROR_COMMUNICATION_MANAGER_NOT_INITIALIZED);
    	}
    	if (FuncEvaluator.msPlatformHWMgr == null) {
        	throw new JFCALCExpErrException(com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES.ERROR_PLATFORM_HARDWARE_LIB_IS_NOT_LOADED);
    	}
        Statement sLine = sarrayLines[nDeclarationPosition];
        if (sLine.mstatementType.getType().equals(Statement_call.getTypeStr()) == false)  {
            ERRORTYPES e = ERRORTYPES.INVALID_CALL_BLOCK;
            throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);                                                
        }
        
        final String callSettings = ((Statement_call)sLine.mstatementType).mstrSettings;
        
        Set<String> setInterfParams = ((Statement_call)sLine.mstatementType).msetInterfParams;
        final Map<String, Variable> mapInterfVars = new HashMap<String, Variable>();
        for (String varName : setInterfParams) {
            // was VariableOperator.lookUpSpaces(varName, progContext.mdynamicProgContext.mlVarNameSpaces);
            // to support oo programming and member variable, change to
        	Variable var = VariableOperator.lookForVarOrMemberVar(varName, AccessRestriction.PRIVATE, progContext);
            if (var == null)    {
                ERRORTYPES e = ERRORTYPES.UNDEFINED_VARIABLE;
                throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);
            }
            // we need to check if we can add all variables into connection manager first.
            if (FuncEvaluator.msCommMgr.getAllUsedCallVariables().contains(var)) {
                ERRORTYPES e = ERRORTYPES.CANNOT_REUSE_VARIABLE_FOR_CALL_BLOCK;
                throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);
            }
            mapInterfVars.put(varName, var);
        }
        String strRetVarName = "";
        Variable varRet = null;
        if (nEndPosition < sarrayLines.length) {
            sLine = sarrayLines[nEndPosition];
            if (sLine.mstatementType.getType().equals(Statement_endcall.getTypeStr())) {
                strRetVarName = ((Statement_endcall)(sLine.mstatementType)).mstrReturnedVar;
                if (strRetVarName.length() > 0)    {
                    // was VariableOperator.lookUpSpaces(strRetVarName, progContext.mdynamicProgContext.mlVarNameSpaces);
                    // to support oo programming and member variable, change to
                    varRet = VariableOperator.lookForVarOrMemberVar(strRetVarName, AccessRestriction.PRIVATE, progContext);
                    if (varRet == null)    {
                        ERRORTYPES e = ERRORTYPES.UNDEFINED_VARIABLE;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);
                    }
                    if (FuncEvaluator.msCommMgr.getAllUsedCallVariables().contains(varRet)) {
                        ERRORTYPES e = ERRORTYPES.CANNOT_REUSE_VARIABLE_FOR_CALL_BLOCK;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);
                    }
                    if (setInterfParams.contains(strRetVarName)) {
                        ERRORTYPES e = ERRORTYPES.CANNOT_REUSE_VARIABLE_FOR_CALL_BLOCK;
                        throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);
                    }
                }
            } else {
                ERRORTYPES e = ERRORTYPES.INCOMPLETE_BLOCK;
                throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);
            }
        }
        
        final AtomicBoolean returnVarLocked = new AtomicBoolean(false); // this is just a flag. If it is true, main thread knows that return variable is locked.
        if (isCallLocal) {  // call local device
            // if call local, we ingnore interf variables because they are visible anyway.
            if (varRet != null) {
                if (FuncEvaluator.msCommMgr.getAllUsedCallVariables().contains(varRet)) {
                    ERRORTYPES e = ERRORTYPES.CANNOT_REUSE_VARIABLE_FOR_CALL_BLOCK;
                    throw  new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);
                }
                FuncEvaluator.msCommMgr.getAllUsedCallVariables().add(varRet);
            }
            final Variable varReturn = varRet;
            // have to use a new copy of progContext otherwise when main thread finsihes, stack is popped out and new thread is affected. 
            final ProgContext progContextNew = new ProgContext(progContext);
            final Long thisThreadId = Thread.currentThread().getId();
            Thread t = new Thread() {

                @Override
                public void run() {
                    callLocal(returnVarLocked, thisThreadId, callSettings, varReturn, sarrayLines, nDeclarationPosition, progContextNew);
                }
            };
            t.start();
        } else {    // call remote device
            LinkedList<String> listFilePaths = new LinkedList<String>();
            CompileAdditionalInfo cai = new CompileAdditionalInfo();
            try {
                listFilePaths = ModuleInfo.getReferredFilePathsAndOtherInfo4CallBlock(sarrayLines, nDeclarationPosition, progContext.mstaticProgContext.getCallingFunc(), cai);
            } catch (InterruptedException e) {
                e.printStackTrace();
                ERRORTYPES e1 = ERRORTYPES.CANNOT_FIND_LIB;
                throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e1);
            }

            LangFileManager lfm = FuncEvaluator.msPlatformHWMgr.getLangFileManager();
            Map<String, String> mapUserLib2CompiledLib = lfm.mapUsrLib2CompiledLib(listFilePaths);
            Map<String, byte[]> mapUserLibPath2Bytes = new HashMap<String, byte[]>();
            for (String pathSrc : mapUserLib2CompiledLib.keySet()) {
                byte[] bytes = lfm.readFileToBytes(pathSrc);
                mapUserLibPath2Bytes.put(mapUserLib2CompiledLib.get(pathSrc), bytes);
            }

            LinkedList<SourceDestPathMapInfo> srcDestPathMapList = new LinkedList<>();
            Map<String, byte[]> mapUserResourcePath2Bytes = new HashMap<String, byte[]>();
            // output to zip:
            for (CompileAdditionalInfo.AssetCopyCmd acc : cai.mbuildAssetCopyCmds) {
                // ok, now let check which lib the file belongs to.
                if (!acc.mstrDestTarget.equalsIgnoreCase(AnnoType_build_asset.ASSET_RESOURCE_TARGET)) {
                    continue;
                }
                lfm.mapAllNonDirMultiLevelChildResources(acc, srcDestPathMapList);
            }

            // now add resource files
            for (SourceDestPathMapInfo entry : srcDestPathMapList) {
                // todo is it better to convert to inputstream instead of bytes?
                byte[] bytes = lfm.readFileToBytes(entry);
                mapUserResourcePath2Bytes.put(entry.getDestPath(), bytes);
            }

            final CallObject callObj = FuncEvaluator.msCommMgr.createOutCallObject(datumConnect, true); // this is not a permanent call.
            if (callObj == null) {
                ERRORTYPES e = ERRORTYPES.CANNOT_CREATE_CALL_OBJECT;
                throw new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);
            }
            final int point = callObj.getCallPoint();
            callObj.callSettings = callSettings;
            callObj.setInterfVariables(mapInterfVars);
            callObj.callReturnedVariable = varRet;
            // now lets send the block to remote.
            final String cmd = CallCommPack.INITIALIZE_COMMAND;
            CallCommPack callCommPack = new CallCommPack(point, callObj.getCallCommPackIndex(), sLine.mstrFilePath, sarrayLines, nDeclarationPosition, listInterfVarNames, progContext, mapUserLibPath2Bytes, mapUserResourcePath2Bytes);

            final String strSerialized = FuncEvaluator.msCommMgr.serialize(callCommPack);

            // add all variables after a call object is created. So that if call object failed to created, the
            // variables can still be used for other calls.
            for(Map.Entry<String, Variable> nameVar: mapInterfVars.entrySet()) {
                if (FuncEvaluator.msCommMgr.getAllUsedCallVariables().contains(nameVar.getValue())) {
                    ERRORTYPES e = ERRORTYPES.CANNOT_REUSE_VARIABLE_FOR_CALL_BLOCK;
                    throw  new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);
                }
            }
            if (varRet != null) {
                if (FuncEvaluator.msCommMgr.getAllUsedCallVariables().contains(varRet)) {
                    ERRORTYPES e = ERRORTYPES.CANNOT_REUSE_VARIABLE_FOR_CALL_BLOCK;
                    throw  new JMFPCompErrException(sLine.mstrFilePath, sLine.mnStartLineNo, sLine.mnEndLineNo, e);
                }
                FuncEvaluator.msCommMgr.getAllUsedCallVariables().add(varRet);
            }
            final Variable varReturn = varRet;
            Thread t = new Thread() {

                @Override
                public void run() {
                    // callRemote has to run in a different thread as it locks
                    // the returned variable waiting for the returned value.
                    callRemote(returnVarLocked, datumConnect, callObj, mapInterfVars, varReturn, cmd, strSerialized);
                }

            };
            t.start();
        }

        while(!returnVarLocked.get()){
            // we do not continue until return variable is locked.
            // do not use wait() (+ child thread notice()) here because it is possible
            // that child thread has started and noticed but main thread hasn't arrived
            // at wait. When main thread arrives at wait, it is blocked permanently so
            // the whole program hang.
            Thread.yield();
        }
            
    }
        	
    public void callLocal(AtomicBoolean returnVarLocked, Long parentThreadId, String callSettings, Variable varReturn, Statement[] sarrayLines, int nDeclarationPosition, ProgContext progContext) {
        // no interf variables to acquire lock.
        if (varReturn != null) {
                varReturn.acquireLock(true);// release lock should be in the same thread as acquire lock.
        }
        returnVarLocked.set(true); // return variable now is locked, main thread can continue.

        // now map the thread to the right citing space.
        Long thisThreadId = Thread.currentThread().getId();
        if (!msmapThreadId2SessionInfo.containsKey(parentThreadId)) {
            // parent thread is or is spawned from main entity
            if (msmapThreadId2SessionInfo.containsKey(thisThreadId)) {
                msmapThreadId2SessionInfo.remove(thisThreadId);   // this thread id must be a reused thread id.so remove the corresponding csd
            } 
        } else {
            // if this call local is initialized from a sandbox or a call local of a sandbox,
            // use its parent csd, which is the sandbox's csd.
            // topcsd is created when the lib files are loaded so it will not change
            // until we reload the lib file. This means we can use its parent thread's topcsd.
            msmapThreadId2SessionInfo.put(thisThreadId, msmapThreadId2SessionInfo.get(parentThreadId));
        }

        DataClass datum2Ret = ArrayBasedDictionary.createArrayBasedDict();
        DataClass datumValue2Ret = null;
        String errMsg = "";
        try {
            DataClass datumCallBlockState = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(CallBlockState.RETURN_SUCCESSFUL.getValue()));
            ScriptAnalyzer sa = new ScriptAnalyzer();
            try {
                ScriptAnalyzer.InFunctionCSManager inFuncCSMgr = new ScriptAnalyzer.InFunctionCSManager(progContext.mstaticProgContext);
                // I dont think that we should acquire locks for interf variables.
                // otherwise, values of interf variables cannot be updated
                sa.analyzeBlock(sarrayLines, nDeclarationPosition, new LinkedList<Variable>(), inFuncCSMgr, progContext);
            } catch(ScriptAnalyzer.FuncRetException e)    {
                // remember if call block doesnt return any value, e.m_datumReturn is null
                datumValue2Ret = e.m_datumReturn;
            }
            datum2Ret = ArrayBasedDictionary.setArrayBasedDictValue(datum2Ret, "CALL_BLOCK_STATE", datumCallBlockState);
            if (datumValue2Ret != null) {
                // it does return something.
                datum2Ret = ArrayBasedDictionary.setArrayBasedDictValue(datum2Ret, "CALL_BLOCK_RETURN",
                        datumValue2Ret); // cast the returned value to data array                            
            }
        } catch(Exception e)    {
            // ScriptStatementException | JMFPCompErrException | other exception types.
            errMsg = MFPAdapter.outputException(e);
            try {
                DataClass datumCallBlockState = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(CallBlockState.RETURN_FAILED.getValue()));
                datum2Ret = ArrayBasedDictionary.setArrayBasedDictValue(datum2Ret, "CALL_BLOCK_STATE", datumCallBlockState);
                datum2Ret = ArrayBasedDictionary.setArrayBasedDictValue(datum2Ret, "CALL_BLOCK_EXCEPTION_DETAILS",
                        new DataClassString(errMsg));
            } catch (JFCALCExpErrException e1) {
                e1.printStackTrace();    // this should not happen.If happen, something must wrong (maybe run out of memory).
            }
        } finally {
            if (varReturn != null) {
                varReturn.setValue(datum2Ret);  // set value
                FuncEvaluator.msCommMgr.getAllUsedCallVariables().remove(varReturn);  // remove the variable from managed variable list.
                varReturn.releaseLock();    // release lock should be in the same thread as acquire lock.
            }
        }
    }
    
    public void callRemote(AtomicBoolean returnVarLocked, DataClass datumConnect, CallObject callObj, Map<String, Variable> mapInterfVars, Variable varReturn, String cmd, String strSerialized) {
        // I don't think we should acquire locks for interf variables before submit call to remote
        DataClass datumReturn = ArrayBasedDictionary.createArrayBasedDict();
        synchronized(callObj) {
            if (varReturn != null) {
                    varReturn.acquireLock(true);// release lock should be in the same thread as acquire lock.
            }
            returnVarLocked.set(true); // return variable now is locked, main thread can continue.
            try {
                callObj.onReceiveRequestListener = callObj.new OnReceiveRequestListener();
                Boolean sendResult = callObj.getConnectObject().sendCallRequest(callObj,cmd, "", strSerialized); // no exception thrown
                CallBlockState sendCallBlockState = sendResult? CallBlockState.SEND_SUCCESSFUL : CallBlockState.SEND_FAILED;
                DataClass datumCallBlockState = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(sendCallBlockState.getValue()));
                // keep in mind that, CALL_BLOCK_STATE value will not be available until the call block is returned or failed.
                datumReturn = ArrayBasedDictionary.setArrayBasedDictValue(datumReturn, "CALL_BLOCK_STATE", datumCallBlockState);
                if (sendCallBlockState == CallBlockState.SEND_SUCCESSFUL) {
                    callObj.returnInfo = callObj.new ReturnInfo(datumReturn, "");
                    callObj.wait(); // wait until interrupted exception or notify
                    // return info of callObj now is ready
                }
            } catch (IllegalMonitorStateException | InterruptedException e) { // timeout exception at wait.
                callObj.returnInfo = callObj.new ReturnInfo(null, e.getMessage());
                e.printStackTrace();
            } catch (Exception e1) { // throw exception when assign values to datumReturn
                callObj.returnInfo = callObj.new ReturnInfo(datumReturn, e1.getMessage());
                e1.printStackTrace();
            } finally {
                ReturnInfo retInfo = callObj.returnInfo;
                DataClass datum2Ret = retInfo.getDatumReturn();
                String errMsg = retInfo.getErrorMessage();
                if (datum2Ret == null) {
                    datum2Ret = ArrayBasedDictionary.createArrayBasedDict();
                    CallBlockState returnResult = CallBlockState.INTERNAL_ERROR;
                    try {
                        DataClass datumCallBlockState = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(returnResult.getValue()));
                        datum2Ret = ArrayBasedDictionary.setArrayBasedDictValue(datum2Ret, "CALL_BLOCK_STATE", datumCallBlockState);
                        datum2Ret = ArrayBasedDictionary.setArrayBasedDictValue(datum2Ret, "CALL_BLOCK_INTERNAL_ERROR_MESSAGE",
                                new DataClassString(errMsg));
                    } catch (JFCALCExpErrException e1) {
                        e1.printStackTrace();    // this should not happen.If happen, something must wrong (maybe run out of memory).
                    }
                }
                callObj.onReceiveRequestListener = null;
                callObj.clearInterfVariables();	//clear Interface variable immediately after we exit the while loop.

                // do not delete non-transient call object as , in the server side, some threads
                // may still alive.
                //FuncEvaluator.msCommMgr.removeOutCallObject(datumConnect, callObj.getCallPoint());
                if (varReturn != null) {
                    FuncEvaluator.msCommMgr.getAllUsedCallVariables().remove(varReturn);
                    varReturn.setValue(datum2Ret);
                    varReturn.releaseLock();    // release lock should be in the same thread as acquire lock.
                }
            }
        }
    }
}
