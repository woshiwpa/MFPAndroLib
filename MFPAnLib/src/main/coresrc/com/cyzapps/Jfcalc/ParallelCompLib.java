// MFP project, ParallelCompLib.java : Designed and developed by Tony Cui in 2021

package com.cyzapps.Jfcalc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.cyzapps.Jfcalc.BuiltInFunctionLib.BaseBuiltInFunction;
import com.cyzapps.Jfcalc.DCHelper.DATATYPES;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.Operators.CalculateOperator;
import com.cyzapps.Jfdatastruct.ArrayBasedDictionary;
import com.cyzapps.Jmfp.ModuleInfo;
import com.cyzapps.Jmfp.ProgContext;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jmfpcompiler.AnnotationLib;
import com.cyzapps.OSAdapter.ParallelManager.CommunicationManager;
import com.cyzapps.OSAdapter.LangFileManager;
import com.cyzapps.OSAdapter.ParallelManager.CallCommPack;
import com.cyzapps.OSAdapter.ParallelManager.CallObject;
import com.cyzapps.OSAdapter.ParallelManager.ConnectObject;
import com.cyzapps.OSAdapter.ParallelManager.LocalObject;
import com.cyzapps.OSAdapter.ParallelManager.LocalObject.SandBoxMessage;
import com.cyzapps.Oomfp.CitingSpaceDefinition;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParallelCompLib {

    public static void call2Load(boolean bOutput) {
        if (bOutput) {
            System.out.println("Loading " + ParallelCompLib.class.getName());
        }
    }
    
    public static class Get_all_host_addressesFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Get_all_host_addressesFunction() {
            mstrProcessedNameWithFullCS = "::mfp::paracomp::host::get_all_host_addresses";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 0;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            // make sure the protocol name is capitalized.
            String strProtocolName = "";
            if (listParams.size() > 0) {
                strProtocolName = DCHelper.lightCvtOrRetDCString(listParams.removeLast()).getStringValue().trim().toUpperCase(Locale.US);
            }
            CommunicationManager commMgr = FuncEvaluator.msCommMgr;
            if (commMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_COMMUNICATION_MANAGER_NOT_INITIALIZED);
            }
            
            Map<String, Map<String, Set<String>>> allAddresses = commMgr.getAllAddresses(strProtocolName);
            
            DataClass datumReturn = ArrayBasedDictionary.createArrayBasedDict();
            for(Map.Entry<String, Map<String, Set<String>>> entry: allAddresses.entrySet()) {
                DataClass datumAddressList = ArrayBasedDictionary.createArrayBasedDict();
                for (Map.Entry<String, Set<String>> entry1: entry.getValue().entrySet()) {
                    String[] addrs = entry1.getValue().toArray(new String[0]);
                    DataClass[] allAddressArray = new DataClass[addrs.length];
                    for (int idx = 0; idx < addrs.length; idx ++) {
                        allAddressArray[idx] = new DataClassString(addrs[idx]);
                    }
                    DataClass datumAddrArray = new DataClassArray(allAddressArray);
                    datumAddressList = ArrayBasedDictionary.setArrayBasedDictValue(datumAddressList, entry1.getKey(), datumAddrArray);
                }
                datumReturn = ArrayBasedDictionary.setArrayBasedDictValue(datumReturn, entry.getKey(), datumAddressList);
            }
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_all_host_addressesFunction());}
        
    public static class Get_local_host_addressFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Get_local_host_addressFunction() {
            mstrProcessedNameWithFullCS = "::mfp::paracomp::host::get_local_host_address";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            // make sure the protocol name is capitalized.
            String strProtocolName = DCHelper.lightCvtOrRetDCString(listParams.removeLast()).getStringValue().trim().toUpperCase(Locale.US);
            String strAdditionalParam = "";
            if (listParams.size() > 0) {
                strAdditionalParam = DCHelper.lightCvtOrRetDCString(listParams.removeLast()).getStringValue();
            }
            CommunicationManager commMgr = FuncEvaluator.msCommMgr;
            if (commMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_COMMUNICATION_MANAGER_NOT_INITIALIZED);
            }
            
            String strAddress = commMgr.getLocalHost(strProtocolName, strAdditionalParam);
            
            DataClass datumReturn = new DataClassString(strAddress);
            return datumReturn;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_local_host_addressFunction());}

    public static class Set_local_host_addressFunction extends BaseBuiltInFunction {
        private static final long serialVersionUID = 1L;

        public Set_local_host_addressFunction() {
            mstrProcessedNameWithFullCS = "::mfp::paracomp::host::set_local_host_address";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 4;
            mnMinParamNum = 4;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            // make sure the protocol name is capitalized.
            String strProtocolName = DCHelper.lightCvtOrRetDCString(listParams.removeLast()).getStringValue().trim().toUpperCase(Locale.US);
            if (!strProtocolName.equals("WEBRTC")) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            String strInterfaceName = DCHelper.lightCvtOrRetDCString(listParams.removeLast()).getStringValue().trim().toLowerCase(Locale.US);
            String strAddress = DCHelper.lightCvtOrRetDCString(listParams.removeLast()).getStringValue().trim().toLowerCase(Locale.US);
            if (!CommunicationManager.isValidEmailAddr(strAddress)) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);    // email address is invalid
            }
            DataClassArray arrayAdditionalInfo = DCHelper.lightCvtOrRetDCArray(listParams.removeLast());
            String[] arrayAddInfoStrs = new String[arrayAdditionalInfo.getDataListSize()];
            for (int idx = 0; idx < arrayAddInfoStrs.length; idx ++) {
                int[] idxArray = new int[1];
                idxArray[0] = idx;
                arrayAddInfoStrs[idx]
                        = DCHelper.lightCvtOrRetDCString(arrayAdditionalInfo.getDataAtIndex(idxArray)).getStringValue().trim();
            }

            CommunicationManager commMgr = FuncEvaluator.msCommMgr;
            if (commMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_COMMUNICATION_MANAGER_NOT_INITIALIZED);
            }
            String[] addresses = new String[1];
            addresses[0] = strAddress;
            String[][] additionalInfo = new String[1][];
            additionalInfo[0] = arrayAddInfoStrs;
            boolean settingResult = commMgr.setLocalAddess(strProtocolName, strInterfaceName, addresses, additionalInfo);
            if (!settingResult) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }

            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Set_local_host_addressFunction());}

    public static class Generate_interfaceFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Generate_interfaceFunction() {
            mstrProcessedNameWithFullCS = "::mfp::paracomp::connect::generate_interface";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            CommunicationManager commMgr = FuncEvaluator.msCommMgr;
            if (commMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_COMMUNICATION_MANAGER_NOT_INITIALIZED);
            }
            // protocol must be trimmed and up cased before saving
            String strProtocol = DCHelper.lightCvtOrRetDCString(listParams.removeLast()).getStringValue().trim().toUpperCase(Locale.US);
            String strAddress = "";
            if (listParams.size() > 0) {
                // address is provided.
                strAddress = DCHelper.lightCvtOrRetDCString(listParams.removeLast()).getStringValue().trim();	// address must be trimmed too
            } else {
                // address is not provided
                strAddress = commMgr.getLocalHost(strProtocol, null);
            }

            boolean generateLocalResult = commMgr.generateLocal(new LocalObject.LocalKey(strProtocol, strAddress));
            if (!generateLocalResult) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GENERATE_LOCAL);
            }

            DataClass datumReturn = ArrayBasedDictionary.createArrayBasedDict();
            datumReturn = ArrayBasedDictionary.setArrayBasedDictValue(datumReturn, "PROTOCOL",
            		new DataClassString(strProtocol));
            datumReturn = ArrayBasedDictionary.setArrayBasedDictValue(datumReturn, "ADDRESS",
            		new DataClassString(strAddress));

            return datumReturn;
        }
    }
    //public final static FcloseFunction BUILTINFUNC_Fclose = new Get_interfaceFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Generate_interfaceFunction());}

    public static class Initialize_localFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Initialize_localFunction() {
            mstrProcessedNameWithFullCS = "::mfp::paracomp::connect::initialize_local";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClass datumLocalInterface = listParams.removeLast();
            DataClass datumSettings = ArrayBasedDictionary.createArrayBasedDict();
            if (listParams.size() > 0) {
            	datumSettings = listParams.removeLast();
            }
            DataClass datumProtocol = ArrayBasedDictionary.getArrayBasedDictValue(
            		datumLocalInterface,
            		"PROTOCOL"
            		);
            String strProtocol = "";
            if (null == datumProtocol) {
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else {
            	strProtocol = DCHelper.lightCvtOrRetDCString(datumProtocol).getStringValue().trim();
            }
            DataClass datumAddress = ArrayBasedDictionary.getArrayBasedDictValue(
            		datumLocalInterface,
            		"ADDRESS"
            		);
            String strAddress = "";
            if (null == datumAddress) {
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else {
            	strAddress = DCHelper.lightCvtOrRetDCString(datumAddress).getStringValue().trim();
            }
            
            LocalObject.LocalKey localInfo = new LocalObject.LocalKey(strProtocol, strAddress);
            CommunicationManager commMgr = FuncEvaluator.msCommMgr;
            if (commMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_COMMUNICATION_MANAGER_NOT_INITIALIZED);
            }
            boolean ret = commMgr.initLocal(localInfo, true); // always reuse existing local
            return new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, ret ? MFPNumeric.TRUE : MFPNumeric.FALSE);
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Initialize_localFunction());}

    public static class Close_localFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Close_localFunction() {
            mstrProcessedNameWithFullCS = "::mfp::paracomp::connect::close_local";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClass datumLocalInterface = listParams.removeLast();
            DataClass datumProtocol = ArrayBasedDictionary.getArrayBasedDictValue(
            		datumLocalInterface,
            		"PROTOCOL"
            		);
            String strProtocol = "";
            if (null == datumProtocol) {
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else {
            	strProtocol = DCHelper.lightCvtOrRetDCString(datumProtocol).getStringValue().trim();
            }
            DataClass datumAddress = ArrayBasedDictionary.getArrayBasedDictValue(
            		datumLocalInterface,
            		"ADDRESS"
            		);
            String strAddress = "";
            if (null == datumAddress) {
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else {
            	strAddress = DCHelper.lightCvtOrRetDCString(datumAddress).getStringValue().trim();
            }
            
            LocalObject.LocalKey localInfo = new LocalObject.LocalKey(strProtocol, strAddress);
            CommunicationManager commMgr = FuncEvaluator.msCommMgr;
            if (commMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_COMMUNICATION_MANAGER_NOT_INITIALIZED);
            }
            LocalObject localObj = commMgr.removeLocal(localInfo);
            
            if (localObj != null) {
                localObj.shutdown();
            }   // not need to throw exception if interface is unavailable.
            return null;
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Close_localFunction());}

    public static class Close_connectionFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Close_connectionFunction() {
            mstrProcessedNameWithFullCS = "::mfp::paracomp::connect::close_connection";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            // this function is for client side, not sever side
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            String strLocalProtocol = "", strLocalAddress = "", strRemoteProtocol = "", strRemoteAddress = "";
            // if pass-in a connect object
            DataClass datumConnect = listParams.removeLast();
            DataClass datumProtocol = ArrayBasedDictionary.getArrayBasedDictValue(datumConnect, "PROTOCOL");
            DataClass datumLocalAddr = ArrayBasedDictionary.getArrayBasedDictValue(datumConnect, "LOCAL_ADDRESS");
            DataClass datumRemoteAddr = ArrayBasedDictionary.getArrayBasedDictValue(datumConnect, "ADDRESS");
            if (null == datumProtocol || null == datumLocalAddr || null == datumRemoteAddr) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            strLocalProtocol = strRemoteProtocol = DCHelper.lightCvtOrRetDCString(datumProtocol).getStringValue();
            strLocalAddress = DCHelper.lightCvtOrRetDCString(datumLocalAddr).getStringValue();
            strRemoteAddress = DCHelper.lightCvtOrRetDCString(datumRemoteAddr).getStringValue();

            LocalObject.LocalKey localInfo = new LocalObject.LocalKey(strLocalProtocol, strLocalAddress);
            CommunicationManager commMgr = FuncEvaluator.msCommMgr;
            if (commMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_COMMUNICATION_MANAGER_NOT_INITIALIZED);
            }
            LocalObject localObj = commMgr.findLocal(localInfo);
            
            if (localObj != null) {
                ConnectObject connObj = localObj.removeConnect(strRemoteAddress);
                if (connObj != null) {
                    connObj.shutdown();
                    return null;
                }
            }
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CONNECT_UNAVAILABLE);
        }
    }
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Close_connectionFunction());}
    
    public static class ListenFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public ListenFunction() {
            mstrProcessedNameWithFullCS = "::mfp::paracomp::connect::listen";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClass datumLocalInterface = listParams.removeLast();
            DataClass datumProtocol = ArrayBasedDictionary.getArrayBasedDictValue(
            		datumLocalInterface,
            		"PROTOCOL"
            		);
            String strProtocol = "";
            if (null == datumProtocol) {
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else {
            	strProtocol = DCHelper.lightCvtOrRetDCString(datumProtocol).getStringValue().trim();
            }
            DataClass datumAddress = ArrayBasedDictionary.getArrayBasedDictValue(
            		datumLocalInterface,
            		"ADDRESS"
            		);
            String strAddress = "";
            if (null == datumAddress) {
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else {
            	strAddress = DCHelper.lightCvtOrRetDCString(datumAddress).getStringValue().trim();
            }
            
            LocalObject.LocalKey localConnPntInfo = new LocalObject.LocalKey(strProtocol, strAddress);
            
            CommunicationManager commMgr = FuncEvaluator.msCommMgr;
            if (commMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_COMMUNICATION_MANAGER_NOT_INITIALIZED);
            }
            // now connect to remote.
            final LocalObject localObj = commMgr.findLocal(localConnPntInfo);
            if (localObj == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INTERFACE_UNAVAILABLE);
            }
            try {
                localObj.listen();
                localObj.exService.submit(new Runnable() {
                    @Override
                    public void run() {
                        while(!localObj.isShutdown()) {
                            try {
                                try {
                                    ConnectObject connObj = localObj.accept();
                                    localObj.enqueConnObj(connObj);
                                } catch(IOException ex1) {
                                    ex1.printStackTrace();
                                    continue;  // should I break if returned exception is not null.
                                }
                            } catch(JFCALCExpErrException ex2) {
                                Logger.getLogger(ParallelCompLib.class.getName()).log(Level.SEVERE, null, ex2);
                                // should I break?
                            }
                        }
                    }
                });
                return new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.TRUE);
            } catch (IOException e) {
                e.printStackTrace();
                return new DataClassSingleNum(DATATYPES.DATUM_MFPBOOL, MFPNumeric.FALSE);
            }
        }
    }
    //public final static FcloseFunction BUILTINFUNC_Fclose = new Initialize_localFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new ListenFunction());}

    public static class AcceptFunction extends BaseBuiltInFunction {
        private static final long serialVersionUID = 1L;

        public AcceptFunction() {
            mstrProcessedNameWithFullCS = "::mfp::paracomp::connect::accept";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 1;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }

            DataClass datumLocalInterface = listParams.removeLast();
            DataClass datumProtocol = ArrayBasedDictionary.getArrayBasedDictValue(
                    datumLocalInterface,
                    "PROTOCOL"
            );
            String strProtocol = "";
            if (null == datumProtocol) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else {
                strProtocol = DCHelper.lightCvtOrRetDCString(datumProtocol).getStringValue().trim();
            }
            DataClass datumAddress = ArrayBasedDictionary.getArrayBasedDictValue(
                    datumLocalInterface,
                    "ADDRESS"
            );
            String strAddress = "";
            if (null == datumAddress) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else {
                strAddress = DCHelper.lightCvtOrRetDCString(datumAddress).getStringValue().trim();
            }

            LocalObject.LocalKey localConnPntInfo = new LocalObject.LocalKey(strProtocol, strAddress);

            CommunicationManager commMgr = FuncEvaluator.msCommMgr;
            if (commMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_COMMUNICATION_MANAGER_NOT_INITIALIZED);
            }
            // now connect to remote.
            final LocalObject localObj = commMgr.findLocal(localConnPntInfo);
            if (localObj == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INTERFACE_UNAVAILABLE);
            }
            try {
                ConnectObject connObj = localObj.dequeConnObj();
                DataClass datumReturn = ArrayBasedDictionary.createArrayBasedDict();
                DataClass datumConnect = ArrayBasedDictionary.createArrayBasedDict();
                DataClass datumSettings = ArrayBasedDictionary.createArrayBasedDict();
                datumConnect = ArrayBasedDictionary.setArrayBasedDictValue(datumConnect, "PROTOCOL",
                        new DataClassString(localObj.getProtocolName()));
                datumConnect = ArrayBasedDictionary.setArrayBasedDictValue(datumConnect, "LOCAL_ADDRESS",
                        new DataClassString(localObj.getAddress()));
                datumConnect = ArrayBasedDictionary.setArrayBasedDictValue(datumConnect, "SOURCE_ADDRESS",
                        new DataClassString(connObj.getSourceAddress()));
                datumConnect = ArrayBasedDictionary.setArrayBasedDictValue(datumConnect, "ADDRESS",
                        new DataClassString(connObj.getAddress()));
                datumSettings = ArrayBasedDictionary.setArrayBasedDictValue(datumSettings, "MIN_MFP_VERSION",
                        new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(connObj.getSettings().minMFPVersion)));
                datumSettings = ArrayBasedDictionary.setArrayBasedDictValue(datumSettings, "REQUIRED_LIBS",
                        new DataClassString(connObj.getSettings().allRequiredLibName));
                datumReturn = ArrayBasedDictionary.setArrayBasedDictValue(datumReturn, "CONNECT", datumConnect);
                // we assume datumSettings will never change and we use its reference instead of a copy.
                datumReturn = ArrayBasedDictionary.setArrayBasedDictValue(datumReturn, "SETTINGS", datumSettings);
                return datumReturn;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return new DataClassNull();
            }
        }
    }
    //public final static FcloseFunction BUILTINFUNC_Fclose = new Initialize_localFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new AcceptFunction());}

    public static class Get_incoming_connectFunction extends BaseBuiltInFunction {
        private static final long serialVersionUID = 1L;

        public Get_incoming_connectFunction() {
            mstrProcessedNameWithFullCS = "::mfp::paracomp::connect::get_incoming_connect";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 2;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }

            DataClass datumLocalInterface = listParams.removeLast();
            DataClass datumProtocol = ArrayBasedDictionary.getArrayBasedDictValue(
                    datumLocalInterface,
                    "PROTOCOL"
            );
            String strProtocol = "";
            if (null == datumProtocol) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else {
                strProtocol = DCHelper.lightCvtOrRetDCString(datumProtocol).getStringValue().trim();
            }
            DataClass datumAddress = ArrayBasedDictionary.getArrayBasedDictValue(
                    datumLocalInterface,
                    "ADDRESS"
            );
            String strAddress = "";
            if (null == datumAddress) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else {
                strAddress = DCHelper.lightCvtOrRetDCString(datumAddress).getStringValue().trim();
            }

            LocalObject.LocalKey localConnPntInfo = new LocalObject.LocalKey(strProtocol, strAddress);

            CommunicationManager commMgr = FuncEvaluator.msCommMgr;
            if (commMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_COMMUNICATION_MANAGER_NOT_INITIALIZED);
            }
            // now connect to remote.
            final LocalObject localObj = commMgr.findLocal(localConnPntInfo);
            if (localObj == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INTERFACE_UNAVAILABLE);
            }

            DataClass datumRemoteAddress = listParams.removeLast();
            String remoteAddr = DCHelper.lightCvtOrRetDCString(datumRemoteAddress).getStringValue();
            ConnectObject connObj = localObj.getConnect(remoteAddr);
            if (connObj != null) {
                DataClass datumReturn = ArrayBasedDictionary.createArrayBasedDict();
                DataClass datumConnect = ArrayBasedDictionary.createArrayBasedDict();
                DataClass datumSettings = ArrayBasedDictionary.createArrayBasedDict();
                datumConnect = ArrayBasedDictionary.setArrayBasedDictValue(datumConnect, "PROTOCOL",
                        new DataClassString(localObj.getProtocolName()));
                datumConnect = ArrayBasedDictionary.setArrayBasedDictValue(datumConnect, "LOCAL_ADDRESS",
                        new DataClassString(localObj.getAddress()));
                datumConnect = ArrayBasedDictionary.setArrayBasedDictValue(datumConnect, "SOURCE_ADDRESS",
                        new DataClassString(connObj.getSourceAddress()));
                datumConnect = ArrayBasedDictionary.setArrayBasedDictValue(datumConnect, "ADDRESS",
                        new DataClassString(connObj.getAddress()));
                datumSettings = ArrayBasedDictionary.setArrayBasedDictValue(datumSettings, "MIN_MFP_VERSION",
                        new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(connObj.getSettings().minMFPVersion)));
                datumSettings = ArrayBasedDictionary.setArrayBasedDictValue(datumSettings, "REQUIRED_LIBS",
                        new DataClassString(connObj.getSettings().allRequiredLibName));
                datumReturn = ArrayBasedDictionary.setArrayBasedDictValue(datumReturn, "CONNECT", datumConnect);
                // we assume datumSettings will never change and we use its reference instead of a copy.
                datumReturn = ArrayBasedDictionary.setArrayBasedDictValue(datumReturn, "SETTINGS", datumSettings);
                return datumReturn;
            } else {
                return new DataClassNull();
            }
        }
    }
    //public final static FcloseFunction BUILTINFUNC_Fclose = new Initialize_localFunction();
    static {CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_incoming_connectFunction());}

    public static class ConnectFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public ConnectFunction() {
            mstrProcessedNameWithFullCS = "::mfp::paracomp::connect::connect";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 4;
            mnMinParamNum = 2;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            
            DataClass datumLocalInterface = listParams.removeLast();	// local interface is still required since we may have more than 1 local interface.
            DataClass datumRemoteInterface = listParams.removeLast();
            Boolean bNotReuseExistingConnect = false;   // by default, we always reuse existing connect.
            if (listParams.size() > 0) {
            	DataClass datumNotReuseExistingConnect = listParams.removeLast();
                bNotReuseExistingConnect = DCHelper.lightCvtOrRetDCMFPBool(datumNotReuseExistingConnect)
                                        .getDataValue().booleanValue();
            }
            DataClass datumSettings = ArrayBasedDictionary.createArrayBasedDict();
            if (listParams.size() > 0) {
            	datumSettings = listParams.removeLast();
            }
            DataClass datumLocalProtocol = ArrayBasedDictionary.getArrayBasedDictValue(
            		datumLocalInterface,
            		"PROTOCOL"
            		);
            String strLocalProtocol = "";
            if (null == datumLocalProtocol) {
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else {
            	strLocalProtocol = DCHelper.lightCvtOrRetDCString(datumLocalProtocol).getStringValue().trim();
            }
            DataClass datumLocalAddress = ArrayBasedDictionary.getArrayBasedDictValue(
            		datumLocalInterface,
            		"ADDRESS"
            		);
            String strLocalAddress = "";
            if (null == datumLocalAddress) {
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else {
            	strLocalAddress = DCHelper.lightCvtOrRetDCString(datumLocalAddress).getStringValue().trim();
            }
            DataClass datumRemoteProtocol = ArrayBasedDictionary.getArrayBasedDictValue(
            		datumRemoteInterface,
            		"PROTOCOL"
            		);
            String strRemoteProtocol = "";
            if (null == datumRemoteProtocol) {
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else {
            	strRemoteProtocol = DCHelper.lightCvtOrRetDCString(datumLocalProtocol).getStringValue().trim();
            }
            DataClass datumRemoteAddress = ArrayBasedDictionary.getArrayBasedDictValue(
            		datumRemoteInterface,
            		"ADDRESS"
            		);
            String strRemoteAddress = "";
            if (null == datumRemoteAddress) {
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else {
            	strRemoteAddress = DCHelper.lightCvtOrRetDCString(datumRemoteAddress).getStringValue().trim();
            }
            if (!strLocalProtocol.equals(strRemoteProtocol)) {
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_PARAMETER_VALUES_CONTRADICT);
            }
            int nMinVersion = 87;
            DataClass datumMinVersion = ArrayBasedDictionary.getArrayBasedDictValue(datumSettings, "MIN_MFP_VERSION");
            if (null != datumMinVersion) {
            	datumMinVersion = DCHelper.try2LightCvtOrRetDCMFPInt(datumMinVersion);
            }
            if (null != datumMinVersion) {
            	nMinVersion = ((DataClassSingleNum)datumMinVersion).getDataValue().intValue();
            }
            String strAllRequiredLibNames = "";
            DataClass datumRequiredLibNames = ArrayBasedDictionary.getArrayBasedDictValue(datumSettings, "REQUIRED_LIBS");
            if (null != datumRequiredLibNames) {
            	datumRequiredLibNames = DCHelper.try2LightCvtOrRetDCString(datumRequiredLibNames);
            }
            if (null != datumRequiredLibNames) {
            	strAllRequiredLibNames = ((DataClassString)datumRequiredLibNames).getStringValue().trim();
            }
            // note that here lib name must be full citing space name, not relative citing space.
            // the reason is clear, we dont want to cause any ambiguity.
            String[] strArrayLibNames = strAllRequiredLibNames.split(";");
            Set<String> paths2Copy = new HashSet<String>();
            for (String strLibName: strArrayLibNames) {
            	String strTrimmedName = strLibName.trim();
            	if (strTrimmedName.length() == 0) {
            		continue;
            	}
            	DataClassString param = new DataClassString(strTrimmedName);
	            LinkedList<DataClass> listLibNames = new LinkedList<DataClass>();
            	listLibNames.add(0, param);
                LinkedList<String> listInputRawNames = new LinkedList<String>();
                listInputRawNames.add(0, "\"" + strTrimmedName + "\""); // we do not worry about special character escaping because this shouldn't happen
            	DataClass datumAllModules;
            	if (strTrimmedName.endsWith("*")) {
            		// this is a citingspace
                    datumAllModules = new AnnotationLib.get_all_referred_unitsFunction().callAction(listLibNames, listInputRawNames, progContext);
            	} else {
            		// this is a function
                    datumAllModules = new AnnotationLib.get_functionsFunction().callAction(listLibNames, listInputRawNames, progContext);
            	}
                DataClass[] modulesList = DCHelper.lightCvtOrRetDCArray(datumAllModules).getDataList();
                for (DataClass moduleData: modulesList) {
                	ModuleInfo moduleInfo = (ModuleInfo) DCHelper.lightCvtOrRetDCExtObjRef(moduleData).getExternalObject();
                	try {
	                	LinkedList<String> allPaths = ModuleInfo.getReferredFilePathsAndOtherInfo(moduleInfo.mstrModuleName,
									                    			moduleInfo.mnModuleParam1,
									                    			true,
									                    			null);
	                	paths2Copy.addAll(allPaths);	// add all paths,Note that here path is full path, not relative path
                    } catch (InterruptedException ex) {
                        // interrupted exception will not be swallowed.
                        ex.printStackTrace();
                    }
                }
            }
            
            Map<String, String> files2Copy = new HashMap<String, String>();
            for(String filePath: paths2Copy) {
                StringBuilder contentBuilder = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new FileReader(filePath)))
                {
             
                    String sCurrentLine;
                    while ((sCurrentLine = br.readLine()) != null)
                    {
                        contentBuilder.append(sCurrentLine).append("\n");
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                String fileContent = contentBuilder.toString();
                files2Copy.put(filePath, fileContent);
            };
            LangFileManager langFileManager = FuncEvaluator.msPlatformHWMgr.getLangFileManager();
            String strDefaultUsrLib = langFileManager.getScriptFolderFullPath();
            try {
                strDefaultUsrLib = IOLib.getCanonicalPath(strDefaultUsrLib);
            } catch (ErrProcessor.JFCALCExpErrException e) {
                // if cannot get canonical path, use original path.
            }
            LinkedList<String> listAllUsrLibs = new LinkedList<String>();
            listAllUsrLibs.add(strDefaultUsrLib);
            listAllUsrLibs.addAll(langFileManager.getAdditionalUserLibs()); // all lib paths are canonical.
            
            LocalObject.LocalKey localConnPntInfo = new LocalObject.LocalKey(strLocalProtocol, strLocalAddress);
                        
            ConnectObject.ConnectSettings connSettings = new ConnectObject.ConnectSettings();
            connSettings.minMFPVersion =nMinVersion;
            connSettings.allRequiredLibName = strAllRequiredLibNames;
            
            ConnectObject.ConnectAdditionalInfo connAddiInfo = new ConnectObject.ConnectAdditionalInfo();
            connAddiInfo.allUsrLibs.addAll(listAllUsrLibs);
            connAddiInfo.files2CopyNameContent.putAll(files2Copy);
            
            CommunicationManager commMgr = FuncEvaluator.msCommMgr;
            if (commMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_COMMUNICATION_MANAGER_NOT_INITIALIZED);
            }
            // now connect to remote.
            LocalObject localObj = commMgr.findLocal(localConnPntInfo);
            if (localObj == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INTERFACE_UNAVAILABLE);
            }
            DataClass datumReturn = ArrayBasedDictionary.createArrayBasedDict();
            DataClass datumConnect = ArrayBasedDictionary.createArrayBasedDict();
            try {
                String[] addresses = localObj.connect(strRemoteAddress, !bNotReuseExistingConnect);
                datumConnect = ArrayBasedDictionary.setArrayBasedDictValue(datumConnect, "PROTOCOL",
                        new DataClassString(strLocalProtocol));
                datumConnect = ArrayBasedDictionary.setArrayBasedDictValue(datumConnect, "LOCAL_ADDRESS",
                        new DataClassString(strLocalAddress));
	            datumConnect = ArrayBasedDictionary.setArrayBasedDictValue(datumConnect, "SOURCE_ADDRESS",
	            		new DataClassString(addresses[0]));
	            datumConnect = ArrayBasedDictionary.setArrayBasedDictValue(datumConnect, "ADDRESS",
	            		new DataClassString(addresses[1]));
	            datumReturn = ArrayBasedDictionary.setArrayBasedDictValue(datumReturn, "CONNECT", datumConnect);
	            // we assume datumSettings will never change and we use its reference instead of a copy.
	            datumReturn = ArrayBasedDictionary.setArrayBasedDictValue(datumReturn, "SETTINGS", datumSettings);
            } catch (IOException connectEx) {
                datumReturn = ArrayBasedDictionary.setArrayBasedDictValue(datumReturn, "CONNECT", new DataClassNull());
                datumReturn = ArrayBasedDictionary.setArrayBasedDictValue(datumReturn, "ERROR", new DataClassString(connectEx.getMessage()));
            } catch (ErrProcessor.JFCALCExpErrException timeoutEx) {
                datumReturn = ArrayBasedDictionary.setArrayBasedDictValue(datumReturn, "CONNECT", new DataClassNull());
                datumReturn = ArrayBasedDictionary.setArrayBasedDictValue(datumReturn, "ERROR", new DataClassString(timeoutEx.getMessage()));
            }
        	return datumReturn;
        }
    }
    //public final static FcloseFunction BUILTINFUNC_Fclose = new Initialize_localFunction();
    static { CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new ConnectFunction());}

    
    public static class Suspend_until_condFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Suspend_until_condFunction() {
            mstrProcessedNameWithFullCS = "::mfp::paracomp::threading::suspend_until_cond";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 4;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }

            DataClass datumVariableName = listParams.removeLast();	// The value of the variable to wait on, but we don't need it.
            //String variableName = DCHelper.lightCvtOrRetDCString(datumVariableName).getStringValue();
            String variableName = listParamRawInputs.removeLast().toLowerCase(Locale.US);      // The input variable name.
            
            VariableOperator.Variable var = VariableOperator.lookUpPreDefined(variableName);
            if (var != null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_VARIABLE_NAME);    // variable cannot be a predefined variable
            }
                    
            var = VariableOperator.lookForVarOrMemberVar(variableName, AccessRestriction.PRIVATE, progContext);
            if (var == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_VARIABLE_NAME);
            }
            Boolean checkAtNextWrite = true;
            if (listParams.size() > 0) {
                DataClass datumCheckAtNextWrite = listParams.removeLast();
                checkAtNextWrite = DCHelper.lightCvtOrRetDCMFPBool(datumCheckAtNextWrite).getDataValue().booleanValue();
            }
            
            String operatorStr = "";  // this means if a write happens, condition is satisfied
            if (listParams.size() > 0) {
                DataClass datumOperator = listParams.removeLast();
                operatorStr = DCHelper.lightCvtOrRetDCString(datumOperator).getStringValue().trim();
            }
            
            if (!operatorStr.equals("") && !operatorStr.equals("==") && !operatorStr.equals("!=") && !operatorStr.equals(">")
                    && !operatorStr.equals("<") && !operatorStr.equals(">=") && !operatorStr.equals("<=")) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            
            DataClass datumValue = null;
            if (listParams.size() > 0) {
            	datumValue = listParams.removeLast();
            }
            if (!checkAtNextWrite)  {
                // this means we need to compare datumValue with the variable's value now
                // even before any writing happens.
                // because no writing has happened, we do not need to worry about "" operator.
                if (datumValue != null) {
                    // this means we need to compare the variable's value with datumValue.
                    // however, condition may satisfied before we monitor the variable.
                    if (operatorStr.equals("==")) {
                        if (datumVariableName.isEqual(datumValue)) {
                            return null;
                        }
                    } else if (operatorStr.equals("!=")) {
                        if (!datumVariableName.isEqual(datumValue)) {
                            return null;
                        }
                    } else {
                        CalculateOperator COPTROperator;
                        if (operatorStr.equals(">")) {
                            COPTROperator = new CalculateOperator(Operators.OPERATORTYPES.OPERATOR_LARGER, 2);
                        } else if (operatorStr.equals("<")) {
                            COPTROperator = new CalculateOperator(Operators.OPERATORTYPES.OPERATOR_SMALLER, 2);
                        } else if (operatorStr.equals(">=")) {
                            COPTROperator = new CalculateOperator(Operators.OPERATORTYPES.OPERATOR_NOSMALLER, 2);
                        } else { // if (operatorStr.equals("<=")) {
                            COPTROperator = new CalculateOperator(Operators.OPERATORTYPES.OPERATOR_NOLARGER, 2);
                        }
                        DataClass result = ExprEvaluator.evaluateTwoOperandCell(datumVariableName, COPTROperator, datumValue);
                        DataClassSingleNum resultVal = DCHelper.lightCvtOrRetDCMFPBool(result);
                        if (resultVal.getDataValue().booleanValue()) {
                            return null;
                        }
                    }
                } else {
                    // this means we compare the variable's value with itself
                    if (operatorStr.equals("==") || operatorStr.equals(">=") || operatorStr.equals("<=")) {
                        return null;
                    }
                }
            }
            
            // condition hasn't been satisified. So we continue.
            var.acquireLock(true);
            // the following statement has to be placed in the lock. And the lock will not be interrupted.
            VariableOperator.Variable.InterruptSuspensionCondition isc
                    = var.addInterruptSuspensionCond(operatorStr, datumValue);
            var.releaseLock();
            synchronized(isc) {
                try {
                    isc.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ParallelCompLib.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        	return null;
        }
    }
    //public final static FcloseFunction BUILTINFUNC_Fclose = new Initialize_localFunction();
    static { CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Suspend_until_condFunction());}
    
    public static class Get_all_connect_call_idsFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Get_all_connect_call_idsFunction() {
            mstrProcessedNameWithFullCS = "::mfp::paracomp::connect::get_all_connect_call_ids";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 3;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }

            DataClass datumLocalInterface = listParams.removeLast();
            DataClass datumProtocol = ArrayBasedDictionary.getArrayBasedDictValue(
            		datumLocalInterface,
            		"PROTOCOL"
            		);
            String strProtocol = "";
            if (null == datumProtocol) {
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else {
            	strProtocol = DCHelper.lightCvtOrRetDCString(datumProtocol).getStringValue().trim();
            }
            DataClass datumAddress = ArrayBasedDictionary.getArrayBasedDictValue(
            		datumLocalInterface,
            		"ADDRESS"
            		);
            String strAddress = "";
            if (null == datumAddress) {
            	throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else {
            	strAddress = DCHelper.lightCvtOrRetDCString(datumAddress).getStringValue().trim();
            }
            
            LocalObject.LocalKey localInfo = new LocalObject.LocalKey(strProtocol, strAddress);
            CommunicationManager commMgr = FuncEvaluator.msCommMgr;
            if (commMgr == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_COMMUNICATION_MANAGER_NOT_INITIALIZED);
            }
            LocalObject localObj = commMgr.findLocal(localInfo);
            if (localObj == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INTERFACE_UNAVAILABLE);
            }

            String connectId = null;
            if (listParams.size() > 0) {
            	DataClass datumConnectId = listParams.removeLast();
                connectId = DCHelper.lightCvtOrRetDCString(datumConnectId).getStringValue().trim();
            }
            
            if (connectId == null) {
                // we return all connect ids
                Iterator<String> itr = localObj.getAllConnectAddrSet().iterator();
                int numberOfConnects = localObj.getAllConnectAddrSet().size();
                DataClass[] listConnectIds = new DataClass[numberOfConnects];
                int idx = 0;
                while (itr.hasNext()) {
                    DataClassString datum = new DataClassString(itr.next());
                    listConnectIds[idx] = datum;
                    idx ++;
                }
                DataClass retValue = new DataClassArray(listConnectIds);
                return retValue;
            } else {
                // we return all incoming or outgoing call ids for a particular connect id
                // if the connect id doesn't exist, return null
                if (localObj.containConnectAddr(connectId)) {
                    DataClass datumIsIn = listParams.removeLast();
                    boolean  bIsIn = DCHelper.lightCvtOrRetDCMFPBool(datumIsIn).getDataValue().booleanValue();
                    ConnectObject connectObj = localObj.getConnect(connectId);
                    if (bIsIn) {
                        Iterator<Integer> itr = connectObj.allInCalls.keySet().iterator();
                        int numberOfCalls = connectObj.allInCalls.size();
                        DataClass[] listCallIds = new DataClass[numberOfCalls];
                        int idx = 0;
                        while (itr.hasNext()) {
                            DataClassSingleNum datum = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(itr.next()));
                            listCallIds[idx] = datum;
                            idx++;
                        }
                        DataClass retValue = new DataClassArray(listCallIds);
                        return retValue;
                    } else {
                        Iterator<Integer> itr = connectObj.allOutCalls.keySet().iterator();
                        int numberOfCalls = connectObj.allOutCalls.size();
                        DataClass[] listCallIds = new DataClass[numberOfCalls];
                        int idx = 0;
                        while (itr.hasNext()) {
                            DataClassSingleNum datum = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(itr.next()));
                            listCallIds[idx] = datum;
                            idx++;
                        }
                        DataClass retValue = new DataClassArray(listCallIds);
                        return retValue;
                    }
                } else {
                    return new DataClassNull();
                }
            }
        }
    }
    //public final static FcloseFunction BUILTINFUNC_Fclose = new Initialize_localFunction();
    static { CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_all_connect_call_idsFunction());}
    public static class Receive_sandbox_messageFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Receive_sandbox_messageFunction() {
            mstrProcessedNameWithFullCS = "::mfp::paracomp::connect::receive_sandbox_message";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 2;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException, InterruptedException
        {
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            // the last parameter is waiting time. 0 means no waiting time (i.e. immediately return
            // the message or a null value). < 0 means blocked if no message, >0 means wait for a while.
            // time is based on ms.
            DataClass datumWaitingTime = listParams.removeFirst();
            long lWaitingTime = DCHelper.lightCvtOrRetDCMFPDec(datumWaitingTime).getDataValue().longValue();
            
            
            if (listParams.size() > 0) {
                // retrieve message for the whole interface
                DataClass datumLocalInterface = listParams.removeLast();
                DataClass datumProtocol = ArrayBasedDictionary.getArrayBasedDictValue(
                        datumLocalInterface,
                        "PROTOCOL"
                        );
                String strProtocol = "";
                if (null == datumProtocol) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                } else {
                    strProtocol = DCHelper.lightCvtOrRetDCString(datumProtocol).getStringValue().trim();
                }
                DataClass datumAddress = ArrayBasedDictionary.getArrayBasedDictValue(
                        datumLocalInterface,
                        "ADDRESS"
                        );
                String strAddress = "";
                if (null == datumAddress) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                } else {
                    strAddress = DCHelper.lightCvtOrRetDCString(datumAddress).getStringValue().trim();
                }

                LocalObject.LocalKey localInfo = new LocalObject.LocalKey(strProtocol, strAddress);
                CommunicationManager commMgr = FuncEvaluator.msCommMgr;
                if (commMgr == null) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_COMMUNICATION_MANAGER_NOT_INITIALIZED);
                }
                LocalObject localObj = commMgr.findLocal(localInfo);
                if (localObj == null) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INTERFACE_UNAVAILABLE);
                }

                SandBoxMessage sbm = null;
                // we return a message from any call object
                if (lWaitingTime >= 0) {
                    sbm = localObj.sandBoxIncomingMessageQueue.poll(lWaitingTime, TimeUnit.MILLISECONDS);
                } else {
                    // wait indefinitely
                    sbm = localObj.sandBoxIncomingMessageQueue.take();
                }
                if (sbm == null) {
                    // no message available
                    return new DataClassNull();
                } else {
                    DataClassArray retValue = ArrayBasedDictionary.createArrayBasedDict();
                    DataClass localKeyData = (sbm.getInterfaceInfo() == null)
                            ? new DataClassNull() : sbm.getInterfaceInfo().toDataClass();
                    DataClass connectId = new DataClassString(sbm.getConnectId());
                    DataClass callId = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(sbm.getCallId()));
                    DataClass transConnectSrcLocal = new DataClassString(sbm.getTransConnectSrcLocal());
                    DataClass transConnectSrcRemote = new DataClassString(sbm.getTransConnectSrcRemote());
                    DataClass transConnectDestLocal = new DataClassString(sbm.getTransConnectDestLocal());
                    DataClass transConnectDestRemote = new DataClassString(sbm.getTransConnectDestRemote());
                    retValue = ArrayBasedDictionary.setArrayBasedDictValue(retValue, "InterfaceInfo", localKeyData);
                    retValue = ArrayBasedDictionary.setArrayBasedDictValue(retValue, "ConnectId", connectId);
                    retValue = ArrayBasedDictionary.setArrayBasedDictValue(retValue, "CallId", callId);
                    retValue = ArrayBasedDictionary.setArrayBasedDictValue(retValue, "TransConnectSrcLocal", transConnectSrcLocal);
                    retValue = ArrayBasedDictionary.setArrayBasedDictValue(retValue, "TransConnectSrcRemote", transConnectSrcRemote);
                    retValue = ArrayBasedDictionary.setArrayBasedDictValue(retValue, "TransConnectDestLocal", transConnectDestLocal);
                    retValue = ArrayBasedDictionary.setArrayBasedDictValue(retValue, "TransConnectDestRemote", transConnectDestRemote);
                    retValue = ArrayBasedDictionary.setArrayBasedDictValue(retValue, "Message", sbm.getMessage());
                    return retValue;
                }
            } else {
                // retrieve message for the current call object
                Long thisThreadId = Thread.currentThread().getId();
                if (CallObject.msmapThreadId2SessionInfo.containsKey(thisThreadId)) {
                    CallObject callObj = CallObject.msmapThreadId2SessionInfo.get(thisThreadId).getCallObject();
                    SandBoxMessage sbm = null;
                    if (lWaitingTime >= 0) {
                        sbm = callObj.sandBoxMessageQueue.poll(lWaitingTime, TimeUnit.MILLISECONDS);
                    } else {
                        // wait indefinitely
                        sbm = callObj.sandBoxMessageQueue.take();
                    }
                    if (sbm == null) {
                        return new DataClassNull();
                    } else {
                        DataClassArray retValue = ArrayBasedDictionary.createArrayBasedDict();
                        DataClass localKeyData = (sbm.getInterfaceInfo() == null)
                                ? new DataClassNull() : sbm.getInterfaceInfo().toDataClass();
                        DataClass connectId = new DataClassString(sbm.getConnectId());
                        DataClass callId = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(sbm.getCallId()));
                        DataClass transConnectSrcLocal = new DataClassString(sbm.getTransConnectSrcLocal());
                        DataClass transConnectSrcRemote = new DataClassString(sbm.getTransConnectSrcRemote());
                        DataClass transConnectDestLocal = new DataClassString(sbm.getTransConnectDestLocal());
                        DataClass transConnectDestRemote = new DataClassString(sbm.getTransConnectDestRemote());
                        retValue = ArrayBasedDictionary.setArrayBasedDictValue(retValue, "InterfaceInfo", localKeyData);
                        retValue = ArrayBasedDictionary.setArrayBasedDictValue(retValue, "ConnectId", connectId);
                        retValue = ArrayBasedDictionary.setArrayBasedDictValue(retValue, "CallId", callId);
                        retValue = ArrayBasedDictionary.setArrayBasedDictValue(retValue, "TransConnectSrcLocal", transConnectSrcLocal);
                        retValue = ArrayBasedDictionary.setArrayBasedDictValue(retValue, "TransConnectSrcRemote", transConnectSrcRemote);
                        retValue = ArrayBasedDictionary.setArrayBasedDictValue(retValue, "TransConnectDestLocal", transConnectDestLocal);
                        retValue = ArrayBasedDictionary.setArrayBasedDictValue(retValue, "TransConnectDestRemote", transConnectDestRemote);
                        retValue = ArrayBasedDictionary.setArrayBasedDictValue(retValue, "Message", sbm.getMessage());
                        return retValue;
                    }
                } else {
                    return new DataClassNull();
                }
            }
        }
    }
    //public final static FcloseFunction BUILTINFUNC_Fclose = new Initialize_localFunction();
    static { CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Receive_sandbox_messageFunction());}
    
    public static class Send_sandbox_messageFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Send_sandbox_messageFunction() {
            mstrProcessedNameWithFullCS = "::mfp::paracomp::connect::send_sandbox_message";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 5;
            mnMinParamNum = 1;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }

            // datumMessage is message. It is the last parameter. 
            // I don't think we need to clone parameter datumMessage even if its
            // value can be a reference to a complex dataclass type. This is
            // because it will be serialized anyway.
            DataClass datumMessage = listParams.removeFirst();  // I dont think we need to .cloneSelf();
           
            switch (listParams.size()) {
                case 3:
                {
                    // Send message to a sand box from local main entity or a sandbox.
                    // Both source and destination are in local device. The source sandbox
                    // can be the same as the destination sandbox.
                    DataClass datumLocalInterface = listParams.removeLast();
                    DataClass datumProtocol = ArrayBasedDictionary.getArrayBasedDictValue(
                            datumLocalInterface,
                            "PROTOCOL"
                    );
                    String strProtocol = "";
                    if (null == datumProtocol) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                    } else {
                        strProtocol = DCHelper.lightCvtOrRetDCString(datumProtocol).getStringValue().trim();
                    }
                    DataClass datumAddress = ArrayBasedDictionary.getArrayBasedDictValue(
                            datumLocalInterface,
                            "ADDRESS"
                    );
                    String strAddress = "";
                    if (null == datumAddress) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                    } else {
                        strAddress = DCHelper.lightCvtOrRetDCString(datumAddress).getStringValue().trim();
                    }
                    LocalObject.LocalKey localInfo = new LocalObject.LocalKey(strProtocol, strAddress);
                    CommunicationManager commMgr = FuncEvaluator.msCommMgr;
                    if (commMgr == null) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_COMMUNICATION_MANAGER_NOT_INITIALIZED);
                    }
                    LocalObject localObj = commMgr.findLocal(localInfo);
                    if (localObj == null) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INTERFACE_UNAVAILABLE);
                    }
                    DataClass datumConnectId = listParams.removeLast();
                    String connectId = DCHelper.lightCvtOrRetDCString(datumConnectId).getStringValue().trim();
                    DataClass datumCallId = listParams.removeLast();
                    int callId = DCHelper.lightCvtOrRetDCMFPInt(datumCallId).getDataValue().intValue();
                    CallObject callObj = null;
                    if (localObj.containConnectAddr(connectId)) {
                        ConnectObject connectObj = localObj.getConnect(connectId);
                        if (connectObj.allInCalls.containsKey(callId)) {
                            callObj = connectObj.allInCalls.get(callId);
                        } else {
                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CALL_OBJECT_UNAVAILABLE);
                        }
                    } else {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CONNECT_UNAVAILABLE);
                    }
                    if (callObj == null) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CALL_OBJECT_UNAVAILABLE);
                    }
                    Long thisThreadId = Thread.currentThread().getId();
                    SandBoxMessage sbm;
                    if (CallObject.msmapThreadId2SessionInfo.containsKey(thisThreadId)) {
                        // it is sent from a local sandbox. We need to obtain source information.
                        CallObject callObjSrc = CallObject.msmapThreadId2SessionInfo.get(thisThreadId).getCallObject();
                        ConnectObject connectObjSrc = callObjSrc.getConnectObject();
                        int callIdSrc = callObjSrc.getCallPoint();
                        String connectIdSrc = connectObjSrc.getAddress();
                        LocalObject localObjSrc = connectObjSrc.getProtocolObject();
                        LocalObject.LocalKey localInterfaceSrc = localObjSrc.getLocalKey();
                        // because it is a local to local message, transmission connect information is always "".
                        sbm = new SandBoxMessage(localInterfaceSrc, connectIdSrc, callIdSrc, "", "", "", "", datumMessage);
                    } else {
                        // it is sent from local main entity, so localKey (i.e. interface info) is null,
                        // connect key is "", connect id is "" and call id is 0.
                        sbm = new SandBoxMessage(null,  "", 0, "", "","", "", datumMessage);
                    }
                    try {
                        callObj.sandBoxMessageQueue.put(sbm);
                    } catch (InterruptedException ex) {
                        // this will never happen.
                        Logger.getLogger(ParallelCompLib.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                }
                case 0:
                {
                    // send message to local main entity from a local sandbox. Both source and destination
                    // are in local.
                    // Note that we cannot send message from local main entity to local entity here because
                    // we don't know what the destination interface is.
                    // Also note that the local sandbox must send the message to local main entity's message
                    // queue for the interface that accept the sandbox's call. Therefore, in the message,
                    // the InterfaceInfo field is null.
                    Long thisThreadId = Thread.currentThread().getId();
                    if (CallObject.msmapThreadId2SessionInfo.containsKey(thisThreadId)) {
                        CallObject callObj = CallObject.msmapThreadId2SessionInfo.get(thisThreadId).getCallObject();
                        ConnectObject connectObj = callObj.getConnectObject();
                        int callId = callObj.getCallPoint();
                        String connectId = connectObj.getAddress();
                        LocalObject localObj = callObj.getConnectObject().getProtocolObject();
                        LocalObject.LocalKey localInterface = localObj.getLocalKey();
                        try {
                            // connectLocalSeenFromRemote is "" because it is a local message
                            // because it is a local to local message, transmission connect information is always "".
                            SandBoxMessage sbm = new SandBoxMessage(localInterface, connectId, callId, "", "", "", "", datumMessage);
                            localObj.sandBoxIncomingMessageQueue.put(sbm);
                        } catch (InterruptedException ex) {
                            // will not be here.
                            Logger.getLogger(ParallelCompLib.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_NOT_IN_A_SANDBOX_SESSION);
                    }
                    break;
                }
                case 4: // send message to a remote main entity or a remote sandbox from local main entity or a sandbox.
                case 1:
                {
                    // send message to a remote main entity or a remote sandbox from local main entity or a sandbox.
                    // source device (i.e. local) and destination device (i.e. remote) is connected by a connection,
                    // which is the first parameter. However, note that if destination is a remote sandbox, the remote
                    // sandbox is not necessarily a call initialized from local. The remote sandbox can be a call
                    // initialized from any device as long as it is running in the remote device.
                    LocalObject.LocalKey destLocalInfo = null;
                    String destConnectId = "";
                    int destCallId = 0;
                    // listParams.size() == 4 (i.e. number of parameters is 5 as we have read the message) means that
                    // destination is a remote sandbox. listParams.size() == 1 (i.e. number of parameters is 2 as we
                    // have read the message) means that destination is remote device's main entity.
                    if (listParams.size() == 4) {
                        // destination call id is the parameter before message
                        DataClass datumDestCallId = listParams.removeFirst();
                        destCallId = DCHelper.lightCvtOrRetDCMFPInt(datumDestCallId).getDataValue().intValue();
                        // destination connect id is the parameter before dest call id
                        DataClass datumDestConnectId = listParams.removeFirst();
                        destConnectId = DCHelper.lightCvtOrRetDCString(datumDestConnectId).getStringValue().trim();
                        // destination interface is the parameter before destination connect id
                        DataClass datumDestInterface = listParams.removeFirst();
                        DataClass datumDestProtocol = ArrayBasedDictionary.getArrayBasedDictValue(
                                datumDestInterface,
                                "PROTOCOL"
                        );
                        String strDestProtocol = "";
                        if (null == datumDestProtocol) {
                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                        } else {
                            strDestProtocol = DCHelper.lightCvtOrRetDCString(datumDestProtocol).getStringValue().trim();
                        }
                        DataClass datumDestAddress = ArrayBasedDictionary.getArrayBasedDictValue(
                                datumDestInterface,
                                "ADDRESS"
                        );
                        String strDestAddress = "";
                        if (null == datumDestAddress) {
                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                        } else {
                            strDestAddress = DCHelper.lightCvtOrRetDCString(datumDestAddress).getStringValue().trim();
                        }
                        destLocalInfo = new LocalObject.LocalKey(strDestProtocol, strDestAddress);
                    }
                    // connect object is the 1st parameter
                    DataClass datumConnect = listParams.removeFirst();
                    CallObject callObj = FuncEvaluator.msCommMgr.createOutCallObject(datumConnect, false);
                    if (callObj == null) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CALL_OBJECT_UNAVAILABLE);
                    }
                    final int point = callObj.getCallPoint();
                    // now lets send the block to remote.
                    final String cmd = CallCommPack.DATA_COMMAND;
                    Long thisThreadId = Thread.currentThread().getId();
                    CallCommPack callCommPack;
                    if (CallObject.msmapThreadId2SessionInfo.containsKey(thisThreadId)) {
                        // this is in a sand box
                        CallObject parentCallObj = CallObject.msmapThreadId2SessionInfo.get(thisThreadId).getCallObject();
                        ConnectObject connectOfParentCallObj = parentCallObj.getConnectObject();
                        LocalObject localObjOfParentCallObj = connectOfParentCallObj.getProtocolObject();
                        LocalObject.LocalKey localInterfaceOfParentCallObj = localObjOfParentCallObj.getLocalKey();
                        int parentCallId = parentCallObj.getCallPoint();
                        String connectOfParentCallId = connectOfParentCallObj.getAddress();
                        callCommPack = new CallCommPack(point, localInterfaceOfParentCallObj, connectOfParentCallId, parentCallId,
                                destLocalInfo, destConnectId, destCallId, datumMessage);
                    } else {
                        // this is in the main entity.
                        callCommPack = new CallCommPack(point, null, "", 0,
                                destLocalInfo, destConnectId, destCallId, datumMessage);
                    }
                    try {
                        String strSerialized = FuncEvaluator.msCommMgr.serialize(callCommPack);
                        Boolean sendResult = callObj.getConnectObject().sendCallRequest(callObj,cmd, "", strSerialized); // no exception thrown
                        if (!sendResult) {
                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_FAIL_TO_SEND_MESSAGE_TO_REMOTE);   // if data cannot be serialized, its value is invalid.
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(ParallelCompLib.class.getName()).log(Level.SEVERE, null, ex);
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_VALUE);   // if data cannot be serialized, its value is invalid.
                    }

                    // the call object is not longer needed after the data message is sent. However,
                    // we do not need to delete the transient call because it is not saved in the dictionary.
                    //FuncEvaluator.msCommMgr.removeOutCallObject(datumConnect, callObj.getCallPoint());
                    break;
                }
                default:
                    // (listParams.size() != 3 && listParams.size() != 1) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }
            return null;
        }
    }
    //public final static FcloseFunction BUILTINFUNC_Fclose = new Initialize_localFunction();
    static { CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Send_sandbox_messageFunction());}
    
    public static class Get_call_infoFunction extends BaseBuiltInFunction {
		private static final long serialVersionUID = 1L;

        public Get_call_infoFunction() {
            mstrProcessedNameWithFullCS = "::mfp::paracomp::connect::get_call_info";
            mstrarrayFullCS = mstrProcessedNameWithFullCS.split("::");
            mnMaxParamNum = 0;
            mnMinParamNum = 0;
        }
        @Override
        public DataClass callAction(LinkedList<DataClass> listParams, LinkedList<String> listParamRawInputs, ProgContext progContext) throws JFCALCExpErrException
        {
            // this function returns [callId, connectId, localObj] of the current call session.
            // if not in a call session, returns null.
            if (listParams.size() < mnMinParamNum || listParams.size() > mnMaxParamNum)   {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            }

            Long thisThreadId = Thread.currentThread().getId();
            if (CallObject.msmapThreadId2SessionInfo.containsKey(thisThreadId)) {
                CallObject callObj = CallObject.msmapThreadId2SessionInfo.get(thisThreadId).getCallObject();
                ConnectObject connectObj = callObj.getConnectObject();
                int callId = callObj.getCallPoint();
                DataClass datumCallId = new DataClassSingleNum(DATATYPES.DATUM_MFPINT, new MFPNumeric(callId));
                String connectId = connectObj.getAddress();
                DataClass datumConnectId = new DataClassString(connectId);
                LocalObject localObj = callObj.getConnectObject().getProtocolObject();
                DataClass datumLocalObj = localObj.getLocalKey().toDataClass();
                DataClass[] callInfoArray = new DataClass[3];
                callInfoArray[0] = datumCallId;
                callInfoArray[1] = datumConnectId;
                callInfoArray[2] = datumLocalObj;
                DataClass datumRet = new DataClassArray(callInfoArray);
                return datumRet;
            } else {
                return new DataClassNull();
            }
        }
    }
    //public final static FcloseFunction BUILTINFUNC_Fclose = new Initialize_localFunction();
    static { CitingSpaceDefinition.CSD_TOP_SYS.addMemberNoExcept(new Get_call_infoFunction());}
}
