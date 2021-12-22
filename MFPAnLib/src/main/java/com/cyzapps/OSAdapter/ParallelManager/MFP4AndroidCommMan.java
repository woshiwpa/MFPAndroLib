/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.OSAdapter.ParallelManager;

import com.cyzapps.AdvRtc.EmailSignalChannelAgent;
import com.cyzapps.AdvRtc.MmediaPeerConnectionParams;
import com.cyzapps.AdvRtc.Peer;
import com.cyzapps.AdvRtc.RtcAgent;
import com.cyzapps.AdvRtc.RtcAppClient;
import com.cyzapps.AdvRtc.RtcListener;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.mfpanlib.MFPAndroidLib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;
import org.webrtc.MediaStream;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cyzapps.AdvRtc.RtcAppClient.CONNECT_SETTINGS;
import static com.cyzapps.AdvRtc.RtcAppClient.LOCAL_EMAIL_TYPE;
import static com.cyzapps.AdvRtc.RtcAppClient.EMAIL_ADDRESS;
import static com.cyzapps.AdvRtc.RtcAppClient.EMAIL_PASSWORD;
import static com.cyzapps.AdvRtc.RtcAppClient.IMAP_PORT;
import static com.cyzapps.AdvRtc.RtcAppClient.IMAP_SERVER;
import static com.cyzapps.AdvRtc.RtcAppClient.IMAP_SSL;
import static com.cyzapps.AdvRtc.RtcAppClient.SMTP_PORT;
import static com.cyzapps.AdvRtc.RtcAppClient.SMTP_SERVER;
import static com.cyzapps.AdvRtc.RtcAppClient.SMTP_SSL;

/**
 *
 * @author youxi
 */
public class MFP4AndroidCommMan extends CommunicationManager implements RtcListener {
    public static final String TAG = "New_AdvRtcapp_Debug";
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";

    @Override
    public String getLocalHost(String protocolName, String additionalInfo) throws JFCALCExpErrException {
        if (protocolName.equals("TCPIP")) {
            try {
                InetAddress localhost = InetAddress.getLocalHost();
                String address = localhost.getHostAddress();
                if (additionalInfo != null && additionalInfo.trim().length() > 0) {
                    int port = Integer.parseInt(additionalInfo);
                    if (port <= 0 || port >= 65536) {
                        throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
                    }
                    address = address + ":" + port;
                }
                return address;
            } catch (UnknownHostException ex) {
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_UNKNOWN_HOST);
            } catch (NumberFormatException ex1) {
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
        } else if (protocolName.equals("WEBRTC")) {
            return RtcAppClient.getLocalAddress();
        } else {
            throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
        }
    }

    @Override
    public boolean setLocalAddess(String protocolName, String interfaceName, String[] addresses, String[][] additionalInfo) {
        if (protocolName.equals("WEBRTC")) {
            // webrtc can only set interface name "main" and can only have one address
            if (addresses.length == 0 || additionalInfo.length != addresses.length) {
                return false;
            }
            String strAddr = addresses[0];
            if (additionalInfo[0].length < 2) {
                return false;
            }
            String addressType = additionalInfo[0][0];  // not used at this moment
            String password = additionalInfo[0][1];
            //Save preferences
            SharedPreferences settings = MFPAndroidLib.getContext().getSharedPreferences(CONNECT_SETTINGS, 0);
            if (settings != null) {
                int localEmailType = 0;
                try {
                    localEmailType = Integer.parseInt(addressType);
                }
                catch (NumberFormatException ex){
                    ex.printStackTrace();
                }
                String smtpServer = getSmtpServer(strAddr);
                if (smtpServer == null) {
                    if (additionalInfo[0].length < 3) {
                        return false;
                    } else {
                        smtpServer = additionalInfo[0][2];
                    }
                }
                int smtpPort = 25;
                try {
                    if ((smtpPort = getSmtpPort(strAddr)) == -1) {
                        if (additionalInfo[0].length < 4) {
                            return false;
                        } else {
                            smtpPort = Integer.parseInt(additionalInfo[0][3]);
                        }
                    }
                }
                catch (NumberFormatException ex){
                    ex.printStackTrace();
                }
                Integer smtpSSL = 0;
                try {
                    if ((smtpSSL = getSSL(strAddr)) == null) {
                        if (additionalInfo[0].length < 5) {
                            return false;
                        } else {
                            smtpSSL = Integer.parseInt(additionalInfo[0][4]);
                        }
                    }
                }
                catch (NumberFormatException ex){
                    ex.printStackTrace();
                }
                String imapServer = getImapServer(strAddr);
                if (imapServer == null) {
                    if (additionalInfo[0].length < 6) {
                        return false;
                    } else {
                        imapServer = additionalInfo[0][5];
                    }
                }
                int imapPort = 25;
                try {
                    if ((imapPort = getImapPort(strAddr)) == -1) {
                        if (additionalInfo[0].length < 7) {
                            return false;
                        } else {
                            imapPort = Integer.parseInt(additionalInfo[0][6]);
                        }
                    }
                }
                catch (NumberFormatException ex){
                    ex.printStackTrace();
                }
                Integer imapSSL = 143;
                try {
                    if ((imapSSL = getSSL(strAddr)) == null) {
                        if (additionalInfo[0].length < 8) {
                            return false;
                        } else {
                            imapSSL = Integer.parseInt(additionalInfo[0][7]);
                        }
                    }
                }
                catch (NumberFormatException ex){
                    ex.printStackTrace();
                }

                settings.edit().putInt(LOCAL_EMAIL_TYPE, localEmailType)
                        .putString(EMAIL_ADDRESS, strAddr)
                        .putString(EMAIL_PASSWORD, password)   // additionalInfo[0] is email type, by default is "smtpimap"
                        .putString(SMTP_SERVER, smtpServer)
                        .putInt(SMTP_PORT, smtpPort)
                        .putInt(SMTP_SSL, smtpSSL)
                        .putString(IMAP_SERVER, imapServer)
                        .putInt(IMAP_PORT, imapPort)
                        .putInt(IMAP_SSL, imapSSL)
                        .commit();
                RtcAppClient.readSettings();    // update WEBRTC local address
                return true;
            } else	{
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public Map<String, Map<String, Set<String>>> getAllAddresses(String protocolName) throws JFCALCExpErrException {
        Map<String, Map<String, Set<String>>> map2Ret = new HashMap<String, Map<String, Set<String>>>();
        if (protocolName.trim().length() == 0 || protocolName.equals("TCPIP")) {
            InetAddress candidateAddress = null;
            Map<String, Set<String>> ifaceAddrs = new HashMap<String, Set<String>>();
            // Iterate all NICs (network interface cards)..
            Enumeration ifaces;
            try {
                ifaces = NetworkInterface.getNetworkInterfaces();
            } catch (SocketException ex) {
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INTERFACE_UNAVAILABLE);
            }
            for (; ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // Iterate all IP addresses assigned to each card...
                String ifaceName = iface.getDisplayName();
                Set<String> addrSet = new HashSet<String>();
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    String addr = inetAddr.getHostAddress();
                    addrSet.add(addr);
                }
                ifaceAddrs.put(ifaceName, addrSet);
            }
            map2Ret.put("TCPIP", ifaceAddrs);
        } else if (protocolName.equals("WEBRTC")) {
            Map<String, Set<String>> ifaceAddrs = new HashMap<String, Set<String>>();
            Set<String> addrSet = new HashSet<String>();
            addrSet.add(RtcAppClient.getLocalAddress());
            ifaceAddrs.put("main", addrSet);
            map2Ret.put("WEBRTC", ifaceAddrs);
        } else {
            throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
        }
        return map2Ret;
    }

    @Override
    public boolean generateLocal(LocalObject.LocalKey localInfo) {
        if (localInfo.getProtocolName().equals("WEBRTC")) {
            if (!MFPAndroidLib.getRtcAppClient().emailSignalChannelAgent.isStarted()) {    //emailSignalChannelAgent is initialized in rtcAppClient's constructor so that it cannot be null.
                // startSignalService is only executed in UI thread, it is garanteed synchronized.
                MFPAndroidLib.getRtcAppClient().startSignalService(MFPAndroidLib.getContext(), false, false, false);
            }
            // wait for 400ms
            while (!MFPAndroidLib.getRtcAppClient().emailSignalChannelAgent.isStarted()) {
                try {
                    synchronized (this) {
                        this.wait(400);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();    // seems that the activity is assigned.
                }
            }

            if (!MFPAndroidLib.getRtcAppClient().emailSignalChannelAgent.isStarted()) {
                return false;
            }

            final MmediaPeerConnectionParams params = new MmediaPeerConnectionParams(
                    true, false, 0, 0, 30, 1, VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, true);
            // set RTC Listener. Note that rtcListener should be implemented in commManager instead of WEBRTCLocalMan. This is
            // because WEBRTCLocalMan is for each localObject. So there are multiple WEBRTCLocalMan. But rtcListener should be
            // just one.
            MFPAndroidLib.getRtcAppClient().setRtcListener(this, params);

            MFPAndroidLib.getRtcAppClient().emailSignalChannelAgent.registerClient();
            return true;
        } else {
            return true;
        }
    }

    @Override
    public boolean initLocal(LocalObject.LocalKey localInfo, boolean reuseExisting) throws JFCALCExpErrException {
        // protocol and address are both case sensative
        if (localInfo.getProtocolName().equals("TCPIP")) {
            if (reuseExisting && existLocal(localInfo)) {
                return true;
            } else {
                TCPIPLocalMan tcpipLocalMan = new TCPIPLocalMan(localInfo.getLocalAddress());
                if (tcpipLocalMan.activate()){
                    allLocals.put(localInfo, tcpipLocalMan);
                    return true;
                } else {
                    return false;
                }
            }
        } else if (localInfo.getProtocolName().equals("WEBRTC")) {
            if (reuseExisting && existLocal(localInfo)) {
                return true;
            } else {
                WEBRTCLocalMan webrtcLocalMan = new WEBRTCLocalMan(localInfo.getLocalAddress());
                if (webrtcLocalMan.activate()){
                    allLocals.put(localInfo, webrtcLocalMan);
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
        }
    }

    @Override
    public String serialize(Object sobj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(sobj);
        oos.close();
        String s = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);   // if not use NO_WRAP, JAVA<->Android communication may fail.
        return s;
    }

    @Override
    public Object deserialize(String s) throws IOException, ClassNotFoundException {
        byte[] data = Base64.decode(s, Base64.DEFAULT);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Object o  = ois.readObject();
        ois.close();
        return o;
    }

    @Override
    public void onLocalStream(MediaStream localStream) {

    }

    @Override
    public void onAddRemoteStream(RtcAgent agent, Peer peer, MediaStream remoteStream) {

    }

    @Override
    public void onRemoveRemoteStream(RtcAgent agent, Peer peer, MediaStream remoteStream) {

    }

    @Override
    public void onStatusChanged(RtcAgent agent, Peer peer, String newStatus) {
        Log.d(TAG, "MFP4AndroidCommMan.onStatusChanged : Agent " + agent.mAgentId + " remote address " + peer.remoteAddress + " session id " + peer.currentSessionId + " new status " + newStatus);
        String txt = "Agent " + agent.mAgentId + " remote address " + peer.remoteAddress + " session id " + peer.currentSessionId + " new status " + newStatus;
        onLogInfo(txt, MFPAndroidLib.RTC_STATE_CHANGE_INFO);
    }

    @Override
    public void onReceivedData(RtcAgent client, final Peer peer, final String command) {
        Log.d(TAG, "MFP4AndroidCommMan.onReceivedData : Agent " + client.mAgentId + " remote address " + peer.remoteAddress + " session id " + peer.currentSessionId + " command is " + command);
        try {
            JSONObject json = new JSONObject(command);
            String data = json.getString("data");
            ConnectObject.PackedCallRequestInfo info = (ConnectObject.PackedCallRequestInfo) FuncEvaluator.msCommMgr.deserialize(data);
            // todo can send message to remote sandbox send from a server to client? if so, onReceivedData might be called by outgoing connect obj
            peer.connectObj.processReceivedCallRequest(info);
        } catch (IOException | ClassNotFoundException | ClassCastException | org.json.JSONException ex) {
            Logger.getLogger(ConnectObject.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onLogInfo(String info, int type) {
        Log.d(TAG, "onLogInfo " + type + " : " + info);
    }
}
