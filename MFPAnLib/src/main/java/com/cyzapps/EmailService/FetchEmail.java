package com.cyzapps.EmailService;

import androidx.annotation.NonNull;
import android.util.Log;

import com.cyzapps.AdvRtc.EmailSignalChannelAgent;
import com.google.api.client.util.StringUtils;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.SortedSet;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;

/**
 * Created by tony on 22/01/2018.
 */

public class FetchEmail {
    private final static String TAG = "New_AdvRtcapp_Debug";
    private final static int MAX_NUMBER_OF_EMAILS_TO_RETRIEVE = 9;

    public static class MsgEvent implements Comparable {
        public Date sentTime;
        public Date receiveTime;
        public String title;
        public String from;
        public String replyTo;
        public String body;

        public boolean processed = false;

        @Override
        public boolean equals(Object o) {
            return o != null && compareTo(o) == 0;
        }

        @Override
        public int compareTo(@NonNull Object o) {
            // If the object is compared with itself then return true
            if (o == this) {
                return 0;
            }

            /* Check if o is an instance of MsgEvent or not
            "null instanceof [type]" also returns false */
            if (!(o instanceof MsgEvent)) {
                return Integer.MIN_VALUE;
            }

            MsgEvent me = (MsgEvent)o;

            String replyToAddr = replyTo == null?"":replyTo;
            String replyToAddr2Comp = me.replyTo == null?"":me.replyTo;
            long timeGap = sentTime.getTime() - me.sentTime.getTime();
            if (timeGap != 0) {
                return (timeGap > 0)?1:-1;
            } else if (!from.equals(me.from)) {
                return from.compareTo(from);
            } else if (!replyToAddr.equals(replyToAddr2Comp)) {
                return replyToAddr.compareTo(replyToAddr2Comp);
            } else if (!title.equals(me.title)) {
                return title.compareTo(me.title);
            } else {
                return body.compareTo(me.body);
            }
        }
    };

    public static class FetchReturnInfo {
        public Date dateFetched;
        public Exception err;
        public FetchReturnInfo(Date d, Exception e) {
            dateFetched = d;
            err = e;
        }
    };

    public static String getNakedEmailAddr(String emailAddr) {
        if (emailAddr == null || emailAddr.trim().length() == 0) {
            return "";  // no valid naked email address found.
        } else {
            String[] parts = emailAddr.split("<");
            for (int idx = parts.length - 1; idx >= 0; idx --) {
                if (parts[idx].contains("@")) {
                    // this is the email addr.
                    String email2Return = parts[parts.length - 1].trim();
                    if (email2Return.endsWith(">")) {
                        email2Return = email2Return.substring(0, email2Return.length() - 1);
                        email2Return = email2Return.trim().toLowerCase(Locale.US);  // all the emails are converted to lower case.
                    }
                    return email2Return;
                }
            }
            return "";  // no valid naked email address found.
        }
    }

    // both fetch and send share the same service.
    public static FetchReturnInfo fetch(Gmail service, Date lastFetchedDate, SortedSet<MsgEvent> sortedMsgEvents, boolean deleteReceivedMsg, boolean deleteSentMsg) {
        final String GMAIL_API_INBOX = "INBOX";
        final String GMAIL_API_SENT_BOX = "SENT";
        final String GMAIL_API_SPAM_BOX = "SPAM";
        Date now = new Date();
        Exception err = null;
        try {
            ListMessagesResponse messagesRespose;
            List<com.google.api.services.gmail.model.Message> m = null;

            for (int idxEmailBox = 0; idxEmailBox < 3; idxEmailBox++) {
                ArrayList<String> ids = new ArrayList<String>();
                String emailBoxName = GMAIL_API_INBOX;
                if (idxEmailBox == 0) {
                    emailBoxName = GMAIL_API_SPAM_BOX;
                } else if (idxEmailBox == 2) {
                    emailBoxName = GMAIL_API_SENT_BOX;
                } else {
                    emailBoxName = GMAIL_API_INBOX;
                }
                ids.add(emailBoxName);
                messagesRespose = service.users().messages().list("me")
                        .setLabelIds(ids).setIncludeSpamTrash(true)
                        .setQ(EmailSignalChannelAgent.MSGTITLEPREFIX)
                        .setMaxResults(new Long(MAX_NUMBER_OF_EMAILS_TO_RETRIEVE))
                        .execute();
                m = messagesRespose.getMessages();
                String deliverTo = "";
                String from = "";
                String replyTo = null;
                String subject = "";
                String content = "";
                Date sentDate = null;
                Date receivedDate = null;
                if (m != null) {    // m!= null means find some messages
                    for (int i = m.size() - 1; i >= 0; i--) {
                        Boolean msgAdded = false;
                        Log.d(TAG, "FetchEmail.fetch : Fetch from " + emailBoxName + " before saving to sortedMsgEvents, sortedMsgEvents size is " + sortedMsgEvents.size());
                        com.google.api.services.gmail.model.Message item = m.get(i);
                        com.google.api.services.gmail.model.Message message = service.users().messages().get("me", item.getId()).setFormat("full").execute();
                        MessagePartBody body = message.getPayload().getBody();
                        List<MessagePartHeader> headers = message.getPayload().getHeaders();

                        if ((!emailBoxName.equals(GMAIL_API_SENT_BOX)) && (!headers.isEmpty())) {
                            long timestamp = message.getInternalDate();  // received date.
                            receivedDate = new Date(timestamp);
                            for (MessagePartHeader header : headers) {
                                String name = header.getName();
                                switch (name) {
                                    case "From":
                                        from = getNakedEmailAddr(header.getValue());
                                        break;
                                    case "Reply-To":
                                        replyTo = header.getValue();
                                        break;
                                    case "To":
                                        deliverTo = header.getValue();
                                        break;
                                    case "Subject":
                                        subject = header.getValue();
                                        break;
                                    case "Date":    // this should be the sent date.
                                        String dateStr = header.getValue();
                                        if (dateStr.contains(","))
                                            dateStr = dateStr.substring(dateStr.indexOf(",") + 2, dateStr.length());
                                        ;
                                        String timestampFormat = "dd MMM yyyy HH:mm:ss Z";
                                        timestamp = new SimpleDateFormat(timestampFormat, Locale.US).parse(dateStr).getTime();
                                        sentDate = new Date(timestamp);
                                        break;
                                }
                            }
                            if (sentDate != null && from.length() != 0 && subject.startsWith(EmailSignalChannelAgent.MSGTITLEPREFIX)
                                    && body != null && !body.isEmpty()) {
                                byte[] bytes = body.decodeData();
                                if (bytes != null) {
                                    String mailText = StringUtils.newStringUtf8(bytes);
                                    if (!mailText.isEmpty()) {
                                        content = mailText;

                                        // now message is read.
                                        MsgEvent me = new MsgEvent();
                                        me.title = subject.substring(EmailSignalChannelAgent.MSGTITLEPREFIX.length());
                                        me.from = from;
                                        me.replyTo = replyTo;
                                        me.sentTime = sentDate;
                                        me.receiveTime = (receivedDate == null) ? new Date() : receivedDate;
                                        me.body = EmailSignalService.decodeBody(content.trim());    // it is possible that "\r\n\r\n" maybe appended if fetch from qq mail
                                        if (msgAdded = sortedMsgEvents.add(me)) {
                                            // this is not a duplicate.
                                            Log.d(TAG, "FetchEmail.getMessageEventType : Fetch add (" + me.hashCode()
                                                    + "), time: " + me.receiveTime
                                                    + ", title : " + me.title);
                                        }
                                    }
                                }
                            }
                        }
                        Log.d(TAG, "FetchEmail.fetch : Fetch from " + emailBoxName + " after saving to sortedMsgEvents, sortedMsgEvents size is " + sortedMsgEvents.size());

                        if (deleteReceivedMsg) {
                            // if they are spam, we can delete all of them. Otherwise, we only delete added ones.
                            if (emailBoxName.equals(GMAIL_API_SPAM_BOX) || (emailBoxName.equals(GMAIL_API_INBOX) && msgAdded)) {
                                service.users().messages().trash("me", item.getId()).execute();
                            }
                        }

                        if (deleteSentMsg && emailBoxName.equals(GMAIL_API_SENT_BOX)) {
                            // we delete all found sent messages.
                            service.users().messages().trash("me", item.getId()).execute();
                        }
                    }
                }
            }
        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException ae) {
            // this means we need to refresh the token
            ae.printStackTrace();
            err = ae;
        } catch (IOException e) {
            e.printStackTrace();
            err = e;
        } catch (ParseException e) {
            e.printStackTrace();
            err = e;
        }
        return new FetchReturnInfo(now, err);
    }

    public static FetchReturnInfo fetch(String host, int port, int SSLmode, String storeType, String user,
                                        String password, Date lastFetchedDate, SortedSet<MsgEvent> sortedMsgEvents, boolean deleteReceivedMsg, boolean deleteSentMsg) {
        Log.d(TAG, "FetchEmail.fetch : Fetch starts, listMsgEvents size is " + sortedMsgEvents.size());
        Date now = new Date();
        Store store = null;
        Folder emailFolderInBox = null;
        Folder emailFolderSpamBox = null;
        Folder emailFolderOutBox = null;
        Exception err = null;
        try {
            // create properties field
            Properties properties = new Properties();
            //properties.setProperty("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            //properties.setProperty("mail.imaps.socketFactory.fallback", "false");
            //properties.setProperty("mail.imaps.socketFactory.port", "" + port);
            if (SSLmode == 1) {
                properties.put("mail.smtp.ssl.enable", true);
            } else if (SSLmode == 0) {
                properties.put("mail.smtp.ssl.enable", false);
            } else {
                properties.put("mail.smtp.starttls.enable", "true");
            }
            properties.put("mail.imaps.host", host);
            properties.setProperty("mail.imaps.port", "" + port);
            // timeout is 3 minutes. for connection only because some big mails take longtime to download.
            properties.put("mail.imap.connectiontimeout", 180000);

            Session emailSession = Session.getDefaultInstance(properties, null);
            // emailSession.setDebug(true);

            // create the POP3 store object and connect with the pop server
            store = emailSession.getStore(storeType);

            store.connect(host, port, user, password);
            Folder[] folderList = new Folder[0];
            if (host.endsWith("gmail.com") || host.endsWith("google.com")) {
                // gmail
                folderList = store.getFolder("[Gmail]").list();
            } else {
                folderList = store.getDefaultFolder().list();
            }
            if (host.endsWith("qq.com")) {
                Thread.sleep(10000); // this is needed for some emails, e.g. qq.com, otherwise, emailFolderInBox.open will return an old status.
            }
            // get inbox folder
            emailFolderInBox = store.getFolder("INBOX");

            // fetch emails from Spam box first. FIrst of all, we need to open spam folder.
            for (int i = 0; i < folderList.length; i++) {
                String folderName = folderList[i].getFullName();
                if (folderName.toLowerCase(Locale.US).indexOf("spam") >= 0) {
                    emailFolderSpamBox = store.getFolder(folderName);
                    break;
                }
            }
            if (emailFolderSpamBox == null) {
                // spam folder may have a different name from spam
                for (int i = 0; i < folderList.length; i++) {
                    String folderName = folderList[i].getFullName();
                    if (folderName.toLowerCase(Locale.US).indexOf("junk") >= 0) {
                        emailFolderSpamBox = store.getFolder(folderName);
                        break;
                    }
                }
            }
            if (emailFolderSpamBox == null) {
                // in Yahoo, spam folder is actually bulk mail folder
                for (int i = 0; i < folderList.length; i++) {
                    String folderName = folderList[i].getFullName();
                    if (folderName.toLowerCase(Locale.US).equals("bulk mail")) {
                        emailFolderSpamBox = store.getFolder(folderName);
                        break;
                    }
                }
            }

            if (emailFolderSpamBox != null) {
                if (deleteReceivedMsg) {
                    emailFolderSpamBox.open(Folder.READ_WRITE);
                } else {
                    emailFolderSpamBox.open(Folder.READ_ONLY);
                }
                // retrieve the messages from the folder in an array and print it
                int messageCnt = emailFolderSpamBox.getMessageCount();
                LinkedList<Message> listReceivedMsgs = new LinkedList<Message>();
                if (messageCnt > 0) { // find some emails.
                    Message[] messages = emailFolderSpamBox.getMessages(Math.max(1, messageCnt - MAX_NUMBER_OF_EMAILS_TO_RETRIEVE), messageCnt);

                    for (int i = messages.length - 1; i >= 0; i--) {
                        Message message = messages[i];
                        //Log.d(TAG,"Fetched email : " + message.getSubject());
                        // do not check stale messages here.
                        listReceivedMsgs.add(message);
                    }

                    Message[] arrayReceivedMsgs = listReceivedMsgs.toArray(new Message[0]);
                    // we shouldn't copy the spam emails to inbox because this will make the emails disordered.
                    // In other words, the copied spams will be ahead of real inputs.
                    for (Message msg : arrayReceivedMsgs) {
                        Log.d(TAG, "FetchEmail.fetch : Fetch from spam before getMessageEventType, sortedMsgEvents size is " + sortedMsgEvents.size());
                        getMessageEventType(msg, sortedMsgEvents, deleteReceivedMsg);
                        Log.d(TAG, "FetchEmail.fetch : Fetch from spam after getMessageEventType, sortedMsgEvents size is " + sortedMsgEvents.size());
                    }

                    if (deleteReceivedMsg) {
                        // because they are spam, we can delete all of them.
                        emailFolderSpamBox.setFlags(arrayReceivedMsgs, new Flags(Flags.Flag.DELETED), true);
                    }
                }
            }

            // then open inbox
            if (deleteReceivedMsg) {
                emailFolderInBox.open(Folder.READ_WRITE);
            } else {
                emailFolderInBox.open(Folder.READ_ONLY);
            }
            // retrieve the messages from the folder in an array and print it
            int messageCnt = emailFolderInBox.getMessageCount();
            Log.d(TAG, "FetchEmail.fetch : emailFolderInBox.getMessageCount() returns " + messageCnt);
            if (messageCnt > 0) { // find some emails.
                Message[] messages = emailFolderInBox.getMessages(Math.max(1, messageCnt - 9), messageCnt);

                for (int i = messages.length - 1; i >= 0; i--) {
                    Message message = messages[i];
                    //Log.d(TAG,"Fetched email : " + message.getSubject());
                    // here do not check if email is staled because we will do it in the upper level any way.
                    Log.d(TAG, "FetchEmail.fetch : Fetch from inbox before getMessageEventType, sortedMsgEvents size is " + sortedMsgEvents.size());
                    getMessageEventType(message, sortedMsgEvents, deleteReceivedMsg);
                    Log.d(TAG, "FetchEmail.fetch : Fetch from inbox after getMessageEventType, sortedMsgEvents size is " + sortedMsgEvents.size());
                }
            }

            if (deleteSentMsg) {    // delete sent messages.
                // the folder name should start with "sent". If no folder starts with sent, it may
                // starts with "out"
                for (int i = 0; i < folderList.length; i++) {
                    String folderName = folderList[i].getFullName();
                    if (folderName.toLowerCase(Locale.US).indexOf("sent") >= 0) {
                        emailFolderOutBox = store.getFolder(folderName);
                        break;
                    }
                }
                if (emailFolderOutBox == null) {
                    // no sent folder.
                    for (int i = 0; i < folderList.length; i++) {
                        String folderName = folderList[i].getFullName();
                        if (folderName.toLowerCase(Locale.US).endsWith("out")
                                || folderName.toLowerCase(Locale.US).endsWith("out box")
                                || folderName.toLowerCase(Locale.US).endsWith("outbox")) {
                            emailFolderOutBox = store.getFolder(folderName);
                            break;
                        }
                    }
                }
                if (emailFolderOutBox != null) {
                    emailFolderOutBox.open(Folder.READ_WRITE);
                    // retrieve the messages from the folder in an array and print it
                    messageCnt = emailFolderOutBox.getMessageCount();
                    if (messageCnt > 0) { // find some emails.
                        Message[] messages = emailFolderOutBox.getMessages(Math.max(1, messageCnt - 9), messageCnt);

                        for (int i = messages.length - 1; i >= 0; i--) {
                            Message message = messages[i];
                            if (message.getSubject().startsWith(EmailSignalChannelAgent.MSGTITLEPREFIX)) {
                                message.setFlag(Flags.Flag.DELETED, true);    // delete sent message
                            }
                        }
                    }
                }
            }
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            err = e;
        } catch (MessagingException e) {
            e.printStackTrace();
            err = e;
        } catch (Exception e) {
            e.printStackTrace();
            err = e;
        } finally {
            // close the store and folder objects
            if (emailFolderOutBox != null) {
                try {
                    emailFolderOutBox.close(deleteSentMsg);
                } catch (MessagingException e) {
                    e.printStackTrace();
                } catch (Exception e1) { // there might be invalid argument exception.
                    e1.printStackTrace();
                }
            }
            if (emailFolderSpamBox != null) {
                try {
                    emailFolderSpamBox.close(deleteSentMsg);
                } catch (MessagingException e) {
                    e.printStackTrace();
                } catch (Exception e1) { // there might be invalid argument exception.
                    e1.printStackTrace();
                }
            }
            if (emailFolderInBox != null) {
                try {
                    emailFolderInBox.close(deleteReceivedMsg);
                } catch (MessagingException e) {
                    e.printStackTrace();
                } catch (Exception e1) { // there might be invalid argument exception.
                    e1.printStackTrace();
                }
            }
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException e) {
                    e.printStackTrace();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            return new FetchReturnInfo(now, err);
        }
    }

    public static MsgEvent getMessageEventType(Message m, SortedSet<MsgEvent> sortedMsgEvents, boolean deleteReceivedMsg) {
        try {
            if (m.getSubject() == null || m.getSentDate() == null || m.getReceivedDate() == null
                    || m.getFrom() == null || m.getFrom().length != 1
                    || m.getReplyTo() == null || m.getReplyTo().length != 1
                    || !m.isMimeType("text/plain")) {
                return null;
            }
            if (m.getSubject().startsWith(EmailSignalChannelAgent.MSGTITLEPREFIX)) {
                MsgEvent me = new MsgEvent();
                me.title = m.getSubject().substring(EmailSignalChannelAgent.MSGTITLEPREFIX.length());
                me.from = (null == m.getFrom())? null : ((InternetAddress) m.getFrom()[0]).getAddress();
                me.replyTo = (null == m.getReplyTo())? null : ((InternetAddress) m.getReplyTo()[0]).getAddress();
                me.sentTime = m.getSentDate();
                me.receiveTime = m.getReceivedDate();
                String content = m.getContent() == null? "":m.getContent().toString();
                if (deleteReceivedMsg) {
                    m.setFlag(Flags.Flag.DELETED, true);    // delete received message
                }
                me.body = EmailSignalService.decodeBody(content.trim());    // it is possible that "\r\n\r\n" maybe appended if fetch from qq mail
                if (sortedMsgEvents.add(me)) {
                    // this is not a duplicate.
                    Log.d(TAG, "FetchEmail.getMessageEventType : Fetch add (" + me.hashCode()
                            + "), time: " + me.receiveTime
                            + ", title : " + me.title);
                    return me;
                } else {
                    return null;    // do not insert duplicate.
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
