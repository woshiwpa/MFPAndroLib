// MFP project, ConnectObject.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.OSAdapter.ParallelManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.FuncEvaluator;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ConnectObject {
    protected LocalObject protocolObject;
	public LocalObject getProtocolObject() {
		return protocolObject;
	}
    protected Boolean isIncoming;
    public Boolean isIncomingConnect() {
        return isIncoming == null? Boolean.TRUE:isIncoming;
    }
    public Boolean isLoopbackConnect() {
        return isIncoming == null;
    }
    protected String address;
    // this is remote address.
    // In TCPIP, if NAT exists, in the server side, getAddress()
    // return's the client side router's external address + port
    // It is different from client's real address
	public String getAddress() {
		return address;
	}
	public String getSourceAddress() { return protocolObject.address; }
	protected ConnectSettings settings;
	public ConnectSettings getSettings() {
		return settings;
	}
    protected ConnectAdditionalInfo addInfo;
    public ConnectAdditionalInfo getAdditionalInfo() {
        return addInfo;
    }
    
    protected Boolean isShutdown;
    public Boolean getIsShutdown() {
        return isShutdown;
    }
    public void setIsShutdown(Boolean isShtdwn) { isShutdown = isShtdwn; }
    
    public static class ConnectSettings {
        public int minMFPVersion = 87;
        public String allRequiredLibName = "";
    }
    
    public static class ConnectAdditionalInfo {
        public LinkedList<String> allUsrLibs = new LinkedList<String>();
        public Map<String, String> files2CopyNameContent = new HashMap<>();
    }

	// a map of call point id and call object. This has to be public because we may dynamically add the remove call.
    public Map<Integer, CallObject> allInCalls = new ConcurrentHashMap<Integer, CallObject>();
    public Map<Integer, CallObject> allOutCalls = new ConcurrentHashMap<Integer, CallObject>();
    // a map from source call id to destination call id. This is needed because client sends
    // package to server without destination call id (because client doesn't know what is destination
    // call id).
    public Map<Integer, Integer> callRemoteSrc2DestMap = new ConcurrentHashMap<Integer, Integer>();

    // a connect object can be either in or out, cannot be both, so we only need one nextCallPoint
    protected Integer nextCallPoint = 0;
    // make this function call atomic to avoid to call points generated at the same time
    public synchronized Integer getNextCallPoint(Boolean isNotTransientCall) {
        if (isNotTransientCall) {
            nextCallPoint ++;
            if (nextCallPoint < 1) {
                nextCallPoint = 1;
            }
            return nextCallPoint;
        } else {
            return 0;
        }
    }

	public ConnectObject(LocalObject po, Boolean isIn, Boolean isShadedCnt, String addr, ConnectSettings config, ConnectAdditionalInfo additionalInfo) {
		protocolObject = po;
        isIncoming = isIn;
		address = addr;
		settings = config;
        addInfo = additionalInfo;
        isShutdown = false;
	}
    
    public void shutdown() {
        // this function just pass a shutdown signal
        isShutdown = true;
    }
    
    public CallObject createCallObject(Boolean isIn, Boolean isNotTransient) {
        if (isNotTransient) {
            Integer callPoint = getNextCallPoint(isNotTransient);
            CallObject callObj = new CallObject(this, callPoint);
            if (isIn) {
                allInCalls.put(callPoint, callObj);
            } else {
                allOutCalls.put(callPoint, callObj);
            }
            return callObj;
        }
        else
        {
            // this call object is for something like message. Transient callObjs are not added into
            // allInCalls / allOutCalls dict. In this way, transient callObj's callId could be the
            // same as a call id in allInCalls / allOutCalls
            return new CallObject(this, getNextCallPoint(isNotTransient));
        }
    }
    
    public CallObject removeCallObject(Boolean isIn, Integer callPoint) {
        if (isIn && allInCalls.containsKey(callPoint)) {
            return allInCalls.remove(callPoint);
        } else if (!isIn && allOutCalls.containsKey(callPoint)) {
            return allOutCalls.remove(callPoint);
        }
        return null;
    }
    
	// sendCallRequest doesn't throw any exception
    // also, when we implement sendCallRequest function, we have to add synchronized
    // keyword as we dont want two sendCallRequest functions interlaced.
	public abstract boolean sendCallRequest(
			CallObject callObj, // the call object. Destination call point is a member variable of the call object
            String cmd, // what's the action will you do?
            String cmdParam,	// cmd parameters, like priority etc.
            String content);	// the content of the command.
	
	// receive call request for connectObj.
    static public class PackedCallRequestInfo implements Serializable {
        int destCallPoint;
        // The following 2 addresses are only useful to TCPIP + NAT
        // because with NAT, srcLocal may not be same as destRemote
        // and destLocal may not be the same as srcRemote.
        String srcLocalAddr;
        String srcRemoteAddr;
        String cmd;
        String cmdParam;
        String content;
    }
    // When we implement sendCallRequest function, we do not add synchronized
    // keyword because receiveCallRequest is called in loop when connection is
    // generated.
	public abstract PackedCallRequestInfo receiveCallRequest();

    // WebRTCConnMan needs not to call this function because its onReceiveData event will call
    // processReceivedCallRequest directly.
    // However, for TCPIPConnMan, this function has to be called at both server side and client
    // side
    public void startReceiveCallRequests() {
        // OK, we use a loop to receive request. Remember packed call request infos
        // are for different call object.
        PackedCallRequestInfo info = null;
        // make sure that receiveCallRequest returns valid info (which means socket
        // is still valid), and the connection is not shutdown.
        while ((info = receiveCallRequest()) != null && !isShutdown) {
            processReceivedCallRequest(info);
        }
    }

    public void processReceivedCallRequest(PackedCallRequestInfo info) {
        CallCommPack callCommPack = null;
        try {
            callCommPack = (CallCommPack) FuncEvaluator.msCommMgr.deserialize(info.content);    // assume FuncEvaluator.msCommMgr is not null
        } catch (IOException | ClassNotFoundException | ClassCastException ex) {
            Logger.getLogger(ConnectObject.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        if (callCommPack.cmd.equals(CallCommPack.INITIALIZE_COMMAND)) {
            CallObject callObj = createCallObject(true,true);
            // client's call comm doesn't know what is remote call point so that
            // its packs always set remote call point to be -1. We need to set it
            // in the callSrcDestMap so connect object can find it.
            callRemoteSrc2DestMap.put(callCommPack.callPoint, callObj.callPoint);
            if (callObj.onReceiveRequestListener != null) {
                callObj.onReceiveRequestListener.onReceiveRequest(callCommPack, info.srcLocalAddr, info.srcRemoteAddr);
            } else {
                Logger.getLogger(ConnectObject.class.getName()).warning("Fail to create call object to respond to INITIALIZE_COMMAND.");
            }
        } else if (callCommPack.cmd.equals(CallCommPack.DATA_COMMAND)) {
            CallObject callObj = createCallObject(true,false);
            if (callObj != null && callObj.onReceiveRequestListener != null) {
                callObj.onReceiveRequestListener.onReceiveRequest(callCommPack, info.srcLocalAddr, info.srcRemoteAddr);
            } else {
                Logger.getLogger(ConnectObject.class.getName()).warning("Fail to create call object to respond to DATA_COMMAND.");
            }
        } else if (allOutCalls.containsKey(info.destCallPoint)
                || (callRemoteSrc2DestMap.containsKey(callCommPack.callPoint)
                    && allInCalls.containsKey(callRemoteSrc2DestMap.get(callCommPack.callPoint)))) {
            // take action only if the call object exists
            CallObject callObj;
            if (!allOutCalls.containsKey(info.destCallPoint)) {
                // client side doesn't have server's call id. This means info.destCallPoint
                // is an invalid value (i.e. -1 as sendCallRequest function assigned an invalid
                // callObj.remoteCallPoint to it). So when server receives client's communication
                // pack, it needs to check callRemoteSrc2DestMap to find destination call point.
                int destCallPnt = callRemoteSrc2DestMap.get(callCommPack.callPoint);
                callObj = allInCalls.get(destCallPnt);
            } else {
                // we are at the client side, info.destCallPoint has been assigned as server
                // side callObj.remoteCallPoint, which is client side corresponding call id.
                int destCallPnt = info.destCallPoint;
                callObj = allOutCalls.get(destCallPnt);
            }
            if (callObj != null && callObj.onReceiveRequestListener != null) {
                callObj.onReceiveRequestListener.onReceiveRequest(callCommPack, info.srcLocalAddr, info.srcRemoteAddr);
            } else if (callObj == null) {
                Logger.getLogger(ConnectObject.class.getName()).warning("Call object with call point " + info.destCallPoint + " is null.");
            } else {
                Logger.getLogger(ConnectObject.class.getName()).warning("Call object with call point " + info.destCallPoint + " onReceiveRequestListener is null.");
            }
        } else {
            Logger.getLogger(ConnectObject.class.getName()).warning("Call object cannot be found from call point " + info.destCallPoint + ".");
        }
    }
}
