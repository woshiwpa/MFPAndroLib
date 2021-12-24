// MFP project, CommunicationManager.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.OSAdapter.ParallelManager;

import com.cyzapps.Jfcalc.DCHelper;
import java.io.IOException;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfdatastruct.ArrayBasedDictionary;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.OSAdapter.ParallelManager.LocalObject.LocalKey;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CommunicationManager {

    /**
     * get local host information
     * @param protocolName : protocol name must have been trimmed and upper case, i.e. WEBRTC or TCPIP
     * @param additionalParam
     * @return
     * @throws ErrProcessor.JFCALCExpErrException
     */
    public abstract String getLocalHost(String protocolName, String additionalParam) throws ErrProcessor.JFCALCExpErrException ;

    /**
     * get all addresses of the protocol
     * @param protocolName : protocol name must have been trimmed and upper case, i.e. WEBRTC or TCPIP
     * @return
     * @throws ErrProcessor.JFCALCExpErrException
     */
    public abstract Map<String, Map<String, Set<String>>> getAllAddresses(String protocolName) throws ErrProcessor.JFCALCExpErrException;

    /**
     * set address(es) for an interface of a protocol. Note that it only works for WEBRTC, never call this
     * function for TCPIP. If new address(es) are set, return True, else return False.
     * @param protocolName
     * @param interfaceName
     * @param addresses
     * @param additionalInfo
     * @return
     */
    public abstract boolean setLocalAddess(String protocolName, String interfaceName, String[] addresses, String[][] additionalInfo);
    public abstract boolean generateLocal(LocalKey localInfo);

    // a map of remote come-in connect objects
    protected Map<LocalObject.LocalKey, LocalObject> allLocals = new ConcurrentHashMap<LocalObject.LocalKey, LocalObject>();
    public Boolean existLocal(LocalObject.LocalKey localKey) {
        for(Map.Entry<LocalObject.LocalKey, LocalObject> entry: allLocals.entrySet()) {
            if (entry.getValue().matchKey(localKey)) {
                return true;
            }
        }
        return false;
    }
    public LocalObject findLocal(LocalObject.LocalKey localKey) {
        for(Map.Entry<LocalObject.LocalKey, LocalObject> entry: allLocals.entrySet()) {
            if (entry.getValue().matchKey(localKey)) {
                return entry.getValue();
            }
        }
        return null;
    }
	    
    public abstract boolean initLocal(LocalKey localInfo, boolean reuseExisting) throws ErrProcessor.JFCALCExpErrException;

    // this set stores all return variables used by all the call blocks.
	// cannot simply use name because variables in different namespaces or blocks may have
	// same name.
	private final Set<VariableOperator.Variable> allUsedCallVariables = Collections.synchronizedSet(new HashSet<VariableOperator.Variable>());
	public Set<VariableOperator.Variable> getAllUsedCallVariables() {
        return allUsedCallVariables;
    }

	// is the connect object existing?
	public ConnectObject findConnectObject(DataClass datumConnect) {
		try {
			DataClass datumProtocol = ArrayBasedDictionary.getArrayBasedDictValue(datumConnect, "PROTOCOL");
            DataClass datumLocalAddr = ArrayBasedDictionary.getArrayBasedDictValue(datumConnect, "LOCAL_ADDRESS");
            DataClass datumSrcAddr = ArrayBasedDictionary.getArrayBasedDictValue(datumConnect, "SOURCE_ADDRESS");
			DataClass datumAddress = ArrayBasedDictionary.getArrayBasedDictValue(datumConnect, "ADDRESS");
			if (null != datumProtocol && null != datumLocalAddr && null != datumSrcAddr && null != datumAddress) {
				String protocolName = DCHelper.lightCvtOrRetDCString(datumProtocol).getStringValue();
				String localAddress = DCHelper.lightCvtOrRetDCString(datumLocalAddr).getStringValue();
				LocalObject.LocalKey localKey = new LocalObject.LocalKey(protocolName, localAddress);
				LocalObject localObj = findLocal(localKey);
                if (localObj == null) {
                    return null;    // no local found.
                }
				String addr = DCHelper.lightCvtOrRetDCString(datumAddress).getStringValue();
				if (!localObj.containConnectAddr(addr)) {
					// no connect found
					return null;
				}
				ConnectObject conn = localObj.getConnect(addr);
				return conn;
			}
		} catch (ErrProcessor.JFCALCExpErrException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
    
    // this is client side call object.
    public CallObject createOutCallObject(DataClass datumConnect, Boolean isNotTransientCall) {
        ConnectObject connObj = findConnectObject(datumConnect);
        if (connObj != null) {
            CallObject callObj = connObj.createCallObject(false, isNotTransientCall);
            return callObj;
        }
		return null;
    }
    
    // this is client side call object.
    public CallObject removeOutCallObject(DataClass datumConnect, Integer callPoint) {
        ConnectObject connObj = findConnectObject(datumConnect);
        if (connObj != null) {
            CallObject callObj = connObj.removeCallObject(false, callPoint);
            return callObj;
        }
		return null;
    }

	public CallCommPack getReceivedCallRequestInfo(String callReqReturn) throws ClassNotFoundException, ClassCastException, IOException {
		Object obj = deserialize(callReqReturn);
		CallCommPack ret = (CallCommPack) obj;
		return ret;
	}

    public LocalObject removeLocal(LocalObject.LocalKey localKey) {
        return allLocals.remove(localKey);
    }
    
	public abstract String serialize(Object sobj) throws IOException;
	
    public abstract Object deserialize(String s) throws IOException, ClassNotFoundException;

    // assume the email address has been lowercased.
    public static boolean isRecommendedEmailAddr(String email) {
        if (email.endsWith("@outlook.com") || email.endsWith("@hotmail.com")
                || email.endsWith("@gmail.com") || email.endsWith("@qq.com")
                || email.endsWith("@163.com") || email.endsWith("@126.com") || email.endsWith("@yeah.net")
                || email.endsWith("@sohu.com") || email.endsWith("@yahoo.com") || email.endsWith("@mail.com")) {
            return true;
        }
        return false;
    }

    public static class EmailInfo {
        // initialize the strings to avoid NullPointerException.
        public String emailAddress = "";
        public String emailPassword = "";
        public String emailSmtpServer = "";
        public int emailSmtpPort;
        public int emailSmtpSSLSupport = 0; // 0 means not support, 1 means support, others means not sure
        public String emailImapServer = "";
        public int emailImapPort;
        public int emailImapSSLSupport = 0; // 0 means not support, 1 means support, others means not sure
    }

    public static boolean isValidEmailAddr(String email) {
        if (email == null) {
            return false;
        } else {
            String strEmail = email.trim();
            String[] parts = strEmail.split("@");
            if (parts.length != 2) {
                return false;
            } else if (!isValidHostname(parts[1])) {
                return false;
            } else if (parts[0].trim().length() != parts[0].length()) {
                return false;
            } else if (parts[0].length() < 1) {
                return false;
            } else {
                for (int idx = 0; idx < parts[0].length(); idx ++) {
                    if (parts[0].substring(idx, idx + 1).trim().length() == 0) {
                        return false;   // should include no blank.
                    }
                }
            }
        }
        return true;
    }
    public static boolean isValidHostname(String server) {
        if (server == null) {
            return false;
        } else {
            String strSvr = server.trim();
            if (strSvr.length() < 4) {
                return false;
            } else {
                String[] parts = strSvr.split("\\.");
                if (parts.length < 2) {
                    return false;
                } else if (parts[parts.length - 1].length() < 2) {
                    return false;
                } else {
                    for (String str : parts) {
                        if (str.length() < 1) {
                            return false;
                        } else if (str.trim().length() != str.length()) {
                            return false;
                        } else {
                            for (int idx = 0; idx < str.length(); idx ++) {
                                if (str.substring(idx, idx + 1).trim().length() == 0) {
                                    return false;   // should include no blank.
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean isValidEmailInfo(EmailInfo emailInfo) {
        if (!isValidEmailAddr(emailInfo.emailAddress)) {
            return false;
        }
        if (emailInfo.emailPassword == null || emailInfo.emailPassword.trim().length() == 0) {
            return false;
        }
        if (!isValidHostname(emailInfo.emailSmtpServer)) {
            return false;
        }
        if (emailInfo.emailSmtpPort <= 0) {
            return false;
        }
        if (!isValidHostname(emailInfo.emailImapServer)) {
            return false;
        }
        if (emailInfo.emailImapPort <= 0) {
            return false;
        }

        // no need to validate ssl
        return true;
    }

    public static String getImapServer(String emailAddr) {
        String emailAddrLower = emailAddr.trim().toLowerCase(Locale.US);
        if (emailAddrLower.endsWith("@gmail.com")) {
            return "imap.gmail.com";
        } else if (emailAddrLower.endsWith("@outlook.com") || emailAddrLower.endsWith("@hotmail.com") || emailAddrLower.endsWith("@msn.com")) {
            return "imap-mail.outlook.com";
        } else if (emailAddrLower.endsWith("@qq.com")) {
            return "imap.qq.com";
        } else if (emailAddrLower.endsWith("@163.com")) {
            return "imap.163.com";
        } else if (emailAddrLower.endsWith("@126.com")) {
            return "imap.126.com";
        } else if (emailAddrLower.endsWith("@yeah.net")) {
            return "imap.yeah.net";
        } else if (emailAddrLower.endsWith("@sohu.com")) {
            return "imap.sohu.com";
        } else if (emailAddrLower.endsWith("@yahoo.com")) {
            return "imap.mail.yahoo.com";
        } else if (emailAddrLower.endsWith("@mail.com")) {
            return "imap.mail.com";
        } else {
            return null;
        }
    }

    public static int getImapPort(String emailAddr) {
        String emailAddrLower = emailAddr.trim().toLowerCase(Locale.US);
        if (emailAddrLower.endsWith("@gmail.com")) {
            return 993;
        } else if (emailAddrLower.endsWith("@outlook.com") || emailAddrLower.endsWith("@hotmail.com") || emailAddrLower.endsWith("@msn.com")) {
            return 993;
        } else if (emailAddrLower.endsWith("@qq.com")) {
            return 993;
        } else if (emailAddrLower.endsWith("@163.com") || emailAddrLower.endsWith("@126.com") || emailAddrLower.endsWith("@yeah.net")) {
            return 993;
        } else if (emailAddrLower.endsWith("@sohu.com")) {
            return 993;
        } else if (emailAddrLower.endsWith("@yahoo.com")) {
            return 993;
        } else if (emailAddrLower.endsWith("@mail.com")) {
            return 993;
        } else {
            return -1;
        }
    }

    public static String getSmtpServer(String emailAddr) {
        String emailAddrLower = emailAddr.trim().toLowerCase(Locale.US);
        if (emailAddrLower.endsWith("@gmail.com")) {
            return "smtp.gmail.com";
        } else if (emailAddrLower.endsWith("@outlook.com") || emailAddrLower.endsWith("@hotmail.com") || emailAddrLower.endsWith("@msn.com")) {
            return "smtp-mail.outlook.com";
        } else if (emailAddrLower.endsWith("@qq.com")) {
            return "smtp.qq.com";
        } else if (emailAddrLower.endsWith("@163.com")) {
            return "smtp.163.com";
        } else if (emailAddrLower.endsWith("@126.com")) {
            return "smtp.126.com";
        } else if (emailAddrLower.endsWith("@yeah.net")) {
            return "smtp.yeah.net";
        } else if (emailAddrLower.endsWith("@sohu.com")) {
            return "smtp.sohu.com";
        } else if (emailAddrLower.endsWith("@yahoo.com")) {
            return "smtp.mail.yahoo.com";
        } else if (emailAddrLower.endsWith("@mail.com")) {
            return "smtp.mail.com";
        } else {
            return null;
        }
    }

    public static int getSmtpPort(String emailAddr) {
        String emailAddrLower = emailAddr.trim().toLowerCase(Locale.US);
        if (emailAddrLower.endsWith("@gmail.com")) {
            return 587;
        } else if (emailAddrLower.endsWith("@outlook.com") || emailAddrLower.endsWith("@hotmail.com") || emailAddrLower.endsWith("@msn.com")) {
            return 587;
        } else if (emailAddrLower.endsWith("@qq.com")) {
            return 587;
        } else if (emailAddrLower.endsWith("@163.com") || emailAddrLower.endsWith("@126.com") || emailAddrLower.endsWith("@yeah.net")) {
            return 465;
        } else if (emailAddrLower.endsWith("@sohu.com")) {
            return 25;
        } else if (emailAddrLower.endsWith("@yahoo.com")) {
            return 465;
        } else if (emailAddrLower.endsWith("@mail.com")) {
            return 587;
        } else {
            return -1;
        }
    }

    public static Integer getSSL(String emailAddr) {
        String emailAddrLower = emailAddr.trim().toLowerCase(Locale.US);
        if (emailAddrLower.endsWith("@gmail.com")) {
            return -1;
        } else if (emailAddrLower.endsWith("@outlook.com") || emailAddrLower.endsWith("@hotmail.com") || emailAddrLower.endsWith("@msn.com")) {
            return -1;
        } else if (emailAddrLower.endsWith("@qq.com")) {
            return -1;
        } else if (emailAddrLower.endsWith("@163.com") || emailAddrLower.endsWith("@126.com") || emailAddrLower.endsWith("@yeah.net")) {
            return -1;
        } else if (emailAddrLower.endsWith("@sohu.com")) {
            return -1;
        } else if (emailAddrLower.endsWith("@yahoo.com")) {
            return 1;
        } else if (emailAddrLower.endsWith("@mail.com")) {
            return -1;
        } else {
            return null;
        }
    }
}
