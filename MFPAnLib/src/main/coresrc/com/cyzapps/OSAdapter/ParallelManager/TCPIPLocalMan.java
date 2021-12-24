/*
 * MFP project, TCPIPLocalMan.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.OSAdapter.ParallelManager;

import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.OSAdapter.ParallelManager.ConnectObject.ConnectAdditionalInfo;
import com.cyzapps.OSAdapter.ParallelManager.ConnectObject.ConnectSettings;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.cyzapps.OSAdapter.ParallelManager.TCPIPConnMan.REMOTE_LISTEN_PORT;

/**
 *
 * @author youxi
 */
public class TCPIPLocalMan extends LocalObject {
    
    protected ServerSocket serverSocket = null;
    
    protected int incomingConnectIdx = 0;
    
    public TCPIPLocalMan(String addr) {
        super("TCPIP", addr);
    }
    
    public boolean activate() {
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                /* failed, but doesn't matter */
            } finally {
                serverSocket = null;
            }
        }
    }
    
    @Override
    public Boolean matchKey(LocalKey key) {
        if (this.protocolName.equals(key.getProtocolName()) && this.address.equals(key.getLocalAddress())) {
            return true;
        }
        return false;
    }
    
    @Override
    public String[] connect(String address, boolean reuseExisting)
                            throws ErrProcessor.JFCALCExpErrException, IOException     {
        String ipAddr = TCPIPConnMan.getIPAddress(address);
        int ipPort = TCPIPConnMan.getIPPort(address);
        if (ipPort == 0) {
            ipPort = REMOTE_LISTEN_PORT;
        }
        String fullAddress = ipAddr + ":" + ipPort;
        // note that for TCPIP, we cannot reuse a reverse connection because
        // if remote is within NAT, let's say remote address is 10.0.1.11, router's
        // external address is 192.168.1.100, and you have two connections, one is
        // from router and the other is from within NAT. When the server wants to
        // select a reverse to reuse, it cannot identify which one is linked to
        // router and which one is linked to within NAT.
        if (reuseExisting && containConnectAddr(fullAddress)) {
            // if reuse, we should also reuse opposite direction connection.
            ConnectObject connObj = getConnect(fullAddress);
            return new String[] {connObj.getSourceAddress(), fullAddress};
        }
        // for TCPIP protocol, connect object id is always unique because it includes ip address and port
        // as such we need to remove old connections
        if (containConnectAddr(fullAddress)) {
            ConnectObject cntObj;
            while ((cntObj = removeConnect(fullAddress)) !=null){
                // remove the connect from allConnects dictionary. Note that for webRTC, one address always corresponds a single connect
                if (!cntObj.getIsShutdown()) {
                    cntObj.shutdown();
                    try {
                        // wait a while until the exist out connect is disconnected.
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        final TCPIPConnMan tcpipConnMan = new TCPIPConnMan(this, false, fullAddress, new ConnectSettings(), new ConnectAdditionalInfo());
        tcpipConnMan.activate();
        addConnect(fullAddress, tcpipConnMan);   // we reconnect it if the connect has been there.
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // we have to do it in another thread to avoid blocking.
                tcpipConnMan.startReceiveCallRequests();    // dont forget to start to receive call requests from remote
            }
        });
        thread.start();
        return new String[] {tcpipConnMan.getSourceAddress(), fullAddress};
    }

    @Override
    public void listen() throws ErrProcessor.JFCALCExpErrException, IOException {
        String ipAddr = TCPIPConnMan.getIPAddress(address);
        int ipPort = TCPIPConnMan.getIPPort(address);
        if (ipPort == 0) {
            ipPort = REMOTE_LISTEN_PORT;
        }
        if (serverSocket != null) {
            // close old server socket.
            try {
                serverSocket.close();
            } catch (IOException e) {
                /* failed, but doesn't matter */
            } finally {
                serverSocket = null;
            }
        }
        serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(ipAddr, ipPort));
    }
    
    final class MapEntry<K, V> implements Map.Entry<K, V> {
        private final K key;
        private V value;

        public MapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }
    }
    @Override
    public ConnectObject accept() throws ErrProcessor.JFCALCExpErrException, IOException {
        if (serverSocket == null) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_FUNCTION_PROCESSION);
        }
        
        // I thought that it is a stupid idea to track address of client because
        // several clients may share the same address if using Nat and it may change
        // in a mobile network.
        // But actually it is not right. Even with NAT layer, remote addr + remote
        // port is still unique.
        //InetAddress clientAddr = socket.getRemoteSocketAddress();
        //InetSocketAddress addr = (InetSocketAddress)serverSocket.getLocalSocketAddress();
        // but keep in mind that clientAddr, if NAT exists, is the address seen by server
        // which could be different from client's real address.
        // here we do not use an unique id as remote address. We did worry that remote IP address
        // may change in a mobile network. But actually this is not true. However, before
        // accept is called inside activate function, we dont know what remote address is
        // so we use a temporary remote address.
        // In activate function, the remote address will be updated.
        String remoteAddr = "" + (incomingConnectIdx ++);
        final TCPIPConnMan tcpipConnMan = new TCPIPConnMan(this, true, remoteAddr, new ConnectSettings(), new ConnectAdditionalInfo()); // empty remote address means it is server side
        tcpipConnMan.activate(serverSocket);
        // Here we do not use remote port, only remote address as the key.
        addConnect(tcpipConnMan.getAddress(), tcpipConnMan);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // we have to do it in another thread to avoid blocking.
                tcpipConnMan.startReceiveCallRequests();
            }
        });
        thread.start();
        return tcpipConnMan;
    }
}
