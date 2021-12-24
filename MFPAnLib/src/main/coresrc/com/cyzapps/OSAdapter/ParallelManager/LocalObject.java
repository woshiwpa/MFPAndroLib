// MFP project, LocalObject.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.OSAdapter.ParallelManager;

import com.cyzapps.Jfcalc.DCHelper;
import com.cyzapps.Jfcalc.DataClass;
import com.cyzapps.Jfcalc.DataClassArray;
import com.cyzapps.Jfcalc.DataClassSingleNum;
import com.cyzapps.Jfcalc.DataClassString;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jfdatastruct.ArrayBasedDictionary;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class LocalObject {
	protected String protocolName;
	public String getProtocolName() {
		return protocolName;
	}
	
	protected String address;
	public String getAddress() {
		return address;
	}
	
    protected boolean isDown;
    public boolean isShutdown() {
        return isDown;
    }
    
	public static class LocalKey implements Serializable {
		private final String protocolName;
        public String getProtocolName() {
            return protocolName;
        }
		private final String localAddress;
        public String getLocalAddress() {
            return localAddress;
        }
		
		public LocalKey(String protocol, String localAddr) {
			protocolName = protocol;
			localAddress = localAddr;
		}
		
	    @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (!(o instanceof LocalKey)) return false;
	        LocalKey key = (LocalKey) o;
	        return protocolName.equals(key.protocolName) && localAddress.equals(key.localAddress);
	    }

	    @Override
	    public int hashCode() {
	        int result = protocolName.hashCode();
	        result = 31 * result + localAddress.hashCode();
	        return result;
	    }
        
        public DataClass toDataClass() {
            DataClassArray retValue = ArrayBasedDictionary.createArrayBasedDict();
            try {
                DataClass protocolData = new DataClassString(protocolName);
                retValue = ArrayBasedDictionary.setArrayBasedDictValue(retValue, "ProtocolName", protocolData);
                DataClass addressData = new DataClassString(localAddress);
                retValue = ArrayBasedDictionary.setArrayBasedDictValue(retValue, "LocalAddress", addressData);
            } catch (ErrProcessor.JFCALCExpErrException ex) {
                Logger.getLogger(LocalObject.class.getName()).log(Level.SEVERE, null, ex);
                // this will never happen.
            }
            return retValue;
        }
	}

	private ArrayBlockingQueue<ConnectObject> acceptedConnects = new ArrayBlockingQueue<ConnectObject>(1);
    public void enqueConnObj(ConnectObject connObj) {
        // assuming connObj is not null
        // I dont think I need to synchronize it because it always run in a single thread.
        acceptedConnects.clear();
        while(!acceptedConnects.offer(connObj));
    }
    public ConnectObject dequeConnObj() throws InterruptedException {
        // there are two assumptions when this function is called:
        // 1.it is called in MFP accept function immediately after listen function and before any connect arrives;
        // 2.MFP accept will not be called simultaneously by multiple threads
        acceptedConnects.clear(); // clear queue to ensure we always get latest connect
        // it will be blocked
        return acceptedConnects.take();
    }
    // I have decided to merge a map of going out connect address and connect object and a map of comingin
    // connect address and connect object.
	// it is a stupid idea to track address of client because several clients may share the same address if
    // using Nat. But here we can safely assume as long as the device is connected to the same network and
    // the device is on. Its IP does not change.
    // This is why I use a arrayList to store ConnectObject for a address.
	private final Map<String, ArrayList<ConnectObject>> allConnects = new ConcurrentHashMap<String, ArrayList<ConnectObject>>();
    public Set<String> getAllConnectAddrSet() {
        synchronized (allConnects) {
            return allConnects.keySet();
        }
    }
	public int addConnect(String addr, ConnectObject connObj) {
	    synchronized (allConnects) {
	        ArrayList<ConnectObject> connectObjs = allConnects.get(addr);
            if (connectObjs == null) {
                connectObjs = new ArrayList<ConnectObject>();
                connectObjs.add(connObj);
                allConnects.put(addr, connectObjs);
                return 1;
            } else {
                connectObjs.add(0, connObj);
                return connectObjs.size();
            }
        }
    }
    public boolean containConnectAddr(String addr) {
        synchronized (allConnects) {
            ArrayList<ConnectObject> connectObjs = allConnects.get(addr);
            if (null == connectObjs || connectObjs.size() == 0) {
                allConnects.remove(addr);
                return false;
            } else {
                return true;
            }
        }
    }
    public ConnectObject removeConnect(String addr) {
	    synchronized (allConnects) {
            ArrayList<ConnectObject> connectObjs = allConnects.get(addr);
	        if (null == connectObjs || connectObjs.size() == 0) {
                allConnects.remove(addr);
                return null;
            } else {
                ConnectObject connObj = connectObjs.remove(0);
	            if (connectObjs.size() == 0) {
                    allConnects.remove(addr);
                }
                return connObj;
            }
        }
    }
    public ConnectObject removeConnect(ConnectObject connObj) {
        synchronized (allConnects) {
            if (connObj == null) {
                return null;
            } else {
                ArrayList<ConnectObject> connectObjs = allConnects.get(connObj.address);
                if (null == connectObjs || connectObjs.size() == 0) {
                    allConnects.remove(connObj.address);
                    return null;
                } else {
                    if (connectObjs.remove(connObj)) {
                        if (connectObjs.size() == 0) {
                            allConnects.remove(connObj.address);
                        }
                        return connObj;
                    } else {
                        return null;
                    }
                }
            }
        }
    }
    public ArrayList<ConnectObject> removeConnects(String addr) {
        synchronized (allConnects) {
            return allConnects.remove(addr);
        }
    }
    public boolean exists(ConnectObject connObj) {
        synchronized (allConnects) {
            if (connObj == null) {
                return false;
            } else {
                ArrayList<ConnectObject> connectObjs = allConnects.get(connObj.address);
                if (null == connectObjs || connectObjs.size() == 0) {
                    return false;
                } else {
                    return connectObjs.contains(connObj);
                }
            }
        }
    }
    public ConnectObject getConnect(String addr) {
        synchronized (allConnects) {
            ArrayList<ConnectObject> connectObjs = allConnects.get(addr);
            if (null == connectObjs || connectObjs.size() == 0) {
                allConnects.remove(addr);
                return null;
            } else {
                ConnectObject connObj = connectObjs.get(0);
                return connObj;
            }
        }
    }
    public ArrayList<ConnectObject> getConnects(String addr) {
        synchronized (allConnects) {
            ArrayList<ConnectObject> connectObjs = allConnects.get(addr);
            if (null == connectObjs || connectObjs.size() == 0) {
                allConnects.remove(addr);
                return null;
            } else {
                return connectObjs;
            }
        }
    }

    // make SandBoxMessage immutable so that it is thread-safe.
    public static final class SandBoxMessage {
        private final LocalKey interfaceInfo;   // This is the interface of message source, it could be different from interface of transmission connect source.
        private final String connectId; // This is the connectId of message source, it could be different from connectId of transmission connect source.
        private final int callId;   // This is the callId of message source, it could be different from id of the callObj of transmission connect source.
        // the following items are for transmission connect (source & destination objects).
        // they are important in TCPIP + Nat as src's remote maynot be dest local and vice versa
        private final String transConnectSrcLocal;
        private final String transConnectSrcRemote;
        private final String transConnectDestLocal;
        private final String transConnectDestRemote;
        private final DataClass message;
        
        public SandBoxMessage(LocalKey interfInfo, String cntId, int callObjId,
                              String transCntSrcLocal, String transCntSrcRemote, String transCntDestLocal, String transCntDestRemote,
                              DataClass msg) {
            interfaceInfo = interfInfo;
            connectId = cntId;
            callId = callObjId;
            transConnectSrcLocal = transCntSrcLocal;
            transConnectSrcRemote = transCntSrcRemote;
            transConnectDestLocal = transCntDestLocal;
            transConnectDestRemote = transCntDestRemote;
            message = msg;
        }
        
        public LocalKey getInterfaceInfo() {
            return interfaceInfo;
        }

        public String getConnectId() {
            return connectId;
        }

        public int getCallId() { return callId; }

        public String getTransConnectSrcLocal() { return transConnectSrcLocal; }

        public String getTransConnectSrcRemote() { return transConnectSrcRemote; }

        public String getTransConnectDestLocal() { return transConnectDestLocal; }

        public String getTransConnectDestRemote() { return transConnectDestRemote; }

        public DataClass getMessage() { return message; }
        
        @Override
        public boolean equals(Object o) {   // this function is to identify if a sandboxmessage is to or from the same ConnectObject
            if (!(o instanceof SandBoxMessage)) {
                return false;
            } else if (interfaceInfo == null && ((SandBoxMessage) o).interfaceInfo != null) {
                return false;
            } else if ((interfaceInfo == null || interfaceInfo.equals(((SandBoxMessage) o).interfaceInfo))
                    && connectId.equals(((SandBoxMessage) o).connectId)
                    && (callId == ((SandBoxMessage) o).callId || callId == -1 || ((SandBoxMessage) o).callId == -1)) {
                // we do not need to use transmission connection info the compare because with message source
                // interface info, connect id and call id we have been able to know from which call object the
                // message was generated.
                return true;    // this means we come from the call object. callId == -1 means it is a wild card match
            } else {
                return false;   // this means we do not come from the same call object.
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + Objects.hashCode(this.interfaceInfo);
            hash = 17 * hash + Objects.hashCode(this.connectId);
            hash = 17 * hash + this.callId;
            // do not use message to calculate hash code.
            // hash = 17 * hash + Objects.hashCode(this.message);
            return hash;
        }
    }
    
    public final BlockingQueue<SandBoxMessage> sandBoxIncomingMessageQueue = new LinkedBlockingQueue<SandBoxMessage>();    
    
    // Should we use an executor for accept loop as the program will never quit?
    public final ExecutorService exService = Executors.newSingleThreadExecutor();
    
	public LocalObject(String nameOfProtocol, String addr) {
		protocolName = nameOfProtocol;
		address = addr;
        isDown = false;
	}

    // some protocols, like TCPIP may change address when client is moving so matchKey
    // function needs different implementations.
    public abstract Boolean matchKey(LocalKey key);

	// this function returns a two array element, i.e. actual source address and actual destination address.
    // Note that the return value is import for TCPIP because parameter remote and localObject's address may
    // not include port information, while returned actual source address and actual destination address
    // include port information.
	public abstract String[] connect(String remote, boolean reuseExisting) throws ErrProcessor.JFCALCExpErrException, IOException;
    
    public abstract void listen() throws ErrProcessor.JFCALCExpErrException, IOException;
    
    public abstract ConnectObject accept() throws ErrProcessor.JFCALCExpErrException, IOException;
	
    // this function shuts down all the connections and server of the local.
    public void shutdown() {
        isDown = true;
        synchronized (allConnects) {
            for (ArrayList<ConnectObject> connObjs : allConnects.values()) {
                for (ConnectObject connObj : connObjs) {
                    connObj.shutdown();
                }
                connObjs.clear();
            }
            allConnects.clear();
        }
        
        exService.shutdownNow();
    }
    
    public final LocalKey getLocalKey() {
        return new LocalKey(protocolName, address);
    }
}
