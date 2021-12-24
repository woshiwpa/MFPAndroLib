/*
 * MFP project, TCPIPConnMan.java : Designed and developed by Tony Cui in 2021
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor
.*/

package com.cyzapps.OSAdapter.ParallelManager;
import com.cyzapps.Jfcalc.ErrProcessor;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author youxi
 */
public class TCPIPConnMan extends ConnectObject {

    public static String getIPAddress(String connPntInfoAddr) throws ErrProcessor.JFCALCExpErrException {
        String[] ipAddrPort = connPntInfoAddr.split(":");
        String ipAddr = ipAddrPort[0].trim();
        if (ipAddr.equals("localhost")) {
            ipAddr = "127.0.0.1";
        }
        Boolean isValidIPAddr = true;
        String groups[] = ipAddr.split("\\.");
        if (groups.length != 4) {
            isValidIPAddr = false;
        } else for (String part : groups) {
            try {
                int partAddr = Integer.parseInt(part);
                if (partAddr < 0 || partAddr > 255) {
                    isValidIPAddr = false;
                    break;
                }
            } catch(Exception e) {
                isValidIPAddr = false;
                break;
            }
        }
        if (!isValidIPAddr) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);            
        }
        return ipAddr;
    }
    
    public static int getIPPort(String connPntInfoAddr) throws ErrProcessor.JFCALCExpErrException {
        String[] ipAddrPort = connPntInfoAddr.split(":");
        String ipPort = "0";   // port == 0 means system will pick up an ephemeral port for you.
        if (ipAddrPort.length > 1) {
            ipPort = ipAddrPort[1].trim();
        }
        int port = 0;
        try {
            port = Integer.parseInt(ipPort);
            if (port < 0 || port > 65535) {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
        } catch(Exception e) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
        }
        return port;
    }
    
    protected Socket socket = null; // can be either client socket or server socket
    
    protected String localIPAddr = "";
    protected int localIPPort = 0;
    
    protected String remoteIPAddr = "";
    public String getRemoteIPAddr() {
        return remoteIPAddr;
    }
    protected int remoteIPPort = 0; // remote IP client port, for server side TCPIP Conn only
    public int getRemoteIPPort() {
        return remoteIPPort;
    }
    public static final int REMOTE_LISTEN_PORT = 62512;
    
    protected AtomicInteger currentCallPoint = new AtomicInteger();

    public TCPIPConnMan(LocalObject po, Boolean isIn, String addr, ConnectSettings config, ConnectAdditionalInfo addInfo)
            throws ErrProcessor.JFCALCExpErrException {
        super(po, isIn, false, addr, config, addInfo);
        localIPAddr = getIPAddress(po.getAddress());
        localIPPort = 0;    // For outgoing connect, I intentionally set localIPPort to zero so that in code server side can easily find client's address.
        if (isIn) {
            localIPPort = getIPPort(po.getAddress());
            if (localIPPort == 0) {
                localIPPort = REMOTE_LISTEN_PORT;
            }
        }
    }

    @Override
    public String getSourceAddress() {
        return localIPAddr + ":" + localIPPort;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                /* failed, but doesn't matter */
            } finally {
                socket = null;
            }
        }
    }

    public void activate() throws IOException, ErrProcessor.JFCALCExpErrException {
        socket = new Socket();
        socket.bind(new InetSocketAddress(localIPAddr, localIPPort));
        localIPPort = socket.getLocalPort();    // Now we know what IP port we are using, we have to update localIPPort here.
        Logger.getLogger(TCPIPConnMan.class.getName()).info(Thread.currentThread() + ": bind " + localIPAddr + ":" + localIPPort);

        remoteIPAddr = TCPIPConnMan.getIPAddress(address);
        remoteIPPort = TCPIPConnMan.getIPPort(address);
        if (remoteIPPort == 0) {
            remoteIPPort = REMOTE_LISTEN_PORT;
        }
        socket.connect(new InetSocketAddress(remoteIPAddr, remoteIPPort));
        Logger.getLogger(TCPIPConnMan.class.getName()).info(Thread.currentThread() + ": connect " + remoteIPAddr + ":" + remoteIPPort);
    }
    
    public void activate(ServerSocket serverSocket) throws IOException {
        Logger.getLogger(TCPIPConnMan.class.getName()).info(Thread.currentThread() + ": Before serverSocket accept");
        socket = serverSocket.accept();
        // update remote (client) IP address and port
        InetSocketAddress remoteSocketAddr = (InetSocketAddress)socket.getRemoteSocketAddress();
        // update address, which is also remote ip address (port is not included).
        // note that remote address will be the key of this incoming connection in the inconnect map.
        address = remoteIPAddr = remoteSocketAddr.getAddress().getHostAddress();
        remoteIPPort = remoteSocketAddr.getPort();
        address += ":" + remoteIPPort;
        Logger.getLogger(TCPIPConnMan.class.getName()).info(Thread.currentThread() + ": After serverSocket accept");
    }

    // we dont want two sendCallRequest interlaced. So this function has to be synchronized.
    @Override
    public synchronized boolean sendCallRequest(CallObject callObj, String cmd, String cmdParam, String content) {
        ObjectOutputStream oOut = null;
        PackedCallRequestInfo packedCallRequestInfo = new PackedCallRequestInfo();
        packedCallRequestInfo.destCallPoint = callObj.remoteCallPoint;
        packedCallRequestInfo.cmd = cmd;
        packedCallRequestInfo.cmdParam = cmdParam;
        packedCallRequestInfo.content = content;
        boolean bRet = false;
        if (socket == null) {
            Logger.getLogger(TCPIPConnMan.class.getName()).info(Thread.currentThread() + ": socket is null which means it hasn't been initialized or has been destroyed, return!");
        } else {
            try {
                //TCP IP send. First of all, we need to set src connect's local and remote.
                packedCallRequestInfo.srcLocalAddr = getSourceAddress();
                packedCallRequestInfo.srcRemoteAddr = address;
                oOut = new ObjectOutputStream(socket.getOutputStream());
                oOut.writeObject(packedCallRequestInfo);
                oOut.flush();
                bRet = true;
            } catch (IOException ex) {
                Logger.getLogger(TCPIPConnMan.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                /* // we shouldn't close oOut because this will shutdown socket.
                try {
                    if (oOut != null) {
                        oOut.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(TCPIPConnMan.class.getName()).log(Level.SEVERE, null, ex);
                }
                */
            }
        }
        return bRet;
    }    

    // receiveCallRequest is started when connection is created. No need to be synchronized
    // and cannot be synchronized.
    @Override
	public PackedCallRequestInfo receiveCallRequest() {
        ObjectInputStream objectInputStream = null;
        PackedCallRequestInfo info = null;
        try {
            if (socket == null) {
                Logger.getLogger(TCPIPConnMan.class.getName()).info(Thread.currentThread() + ": socket is null which means it hasn't been initialized or has been destroyed, return!");
                return null;
            }
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            info = (PackedCallRequestInfo) objectInputStream.readObject();
            // ok, now we have the call request info.
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(TCPIPConnMan.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            /* // we shouldn't close objectInputStream because this will shutdown socket.
            try {
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(TCPIPConnMan.class.getName()).log(Level.SEVERE, null, ex);
            }
            */
        }
        return info;
    }
}
