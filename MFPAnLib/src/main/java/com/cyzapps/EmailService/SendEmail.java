package com.cyzapps.EmailService;

import com.cyzapps.AdvRtc.EmailSignalChannelAgent;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by tony on 22/01/2018.
 */

public class SendEmail {

    public static Exception send(Gmail service, String from, String[] replyTo, String[] to, String subject, String body) {
        Properties props = System.getProperties();
        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);
        Exception err = null;

        try {
            // first of all, create JAVA message.
            message.setFrom(new InternetAddress(from));
            InternetAddress[] replyToAddress = new InternetAddress[replyTo.length];
            // To get the array of addresses
            for (int i = 0; i < replyTo.length; i++) {
                replyToAddress[i] = new InternetAddress(replyTo[i]);
            }
            message.setReplyTo(replyToAddress);
            InternetAddress[] toAddress = new InternetAddress[to.length];

            // To get the array of addresses
            for (int i = 0; i < to.length; i++) {
                toAddress[i] = new InternetAddress(to[i]);
            }

            for (int i = 0; i < toAddress.length; i++) {
                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
            }

            message.setSubject(EmailSignalChannelAgent.MSGTITLEPREFIX + subject);
            message.setContent(EmailSignalService.encodeBody(body) + "\r\n\r\n", "text/plain"); // have to + "\r\n\r\n", it seems to be a bug of qq mail.

            // now convert java mail message to google message.
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            message.writeTo(buffer);
            byte[] bytes = buffer.toByteArray();
            String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
            com.google.api.services.gmail.model.Message gmailMessage = new com.google.api.services.gmail.model.Message();
            gmailMessage.setRaw(encodedEmail);

            // now use gmail API to send email.
            gmailMessage = service.users().messages().send("me", gmailMessage).execute();
        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException ae) {
            // this means we need to refresh the token
            ae.printStackTrace();
            err = ae;
        } catch (IOException ie) {
            ie.printStackTrace();
            err = ie;
        } catch (AddressException ae) {
            ae.printStackTrace();
            err = ae;
        } catch (MessagingException me) {
            me.printStackTrace();
            err = me;
        } finally {
            return err;
        }
    }

    /**
     * send message from "from" address to "to" addresses with password "pass", subject "subject" and text body "body".
     * @param host : smtp server.
     * @param port : smtp server port.
     * @param from : from address, should be a gmail, hotmail, outlook or live email.
     * @param replyTo : reply to address(es).
     * @param pass : passwords
     * @param to : to addresses, should be hotmail, outlook or live emails.
     * @param subject : title
     * @param body : body
     * return exception is null if no error. Otherwise returns the exception.
     */
    public static Exception send(String host, int port, int SSLmode, String from, String[] replyTo, String pass, String[] to, String subject, String body) {
        Properties props = System.getProperties();
        if (SSLmode == 1) {
           props.put("mail.smtp.ssl.enable", true);
        } else if (SSLmode == 0) {
            props.put("mail.smtp.ssl.enable", false);
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", pass);
        props.put("mail.smtp.port", "" + port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.connectiontimeout", 180000); // timeout is 3 minutes.
        props.put("mail.smtp.timeout", 180000); // timeout is 3 minutes.

        Session session = Session.getDefaultInstance(props);
        //session.setDebug(true);
        MimeMessage message = new MimeMessage(session);
        Transport transport = null;
        Exception err = null;

        try {
            message.setFrom(new InternetAddress(from));
            InternetAddress[] replyToAddress = new InternetAddress[replyTo.length];
            // To get the array of addresses
            for( int i = 0; i < replyTo.length; i++ ) {
                replyToAddress[i] = new InternetAddress(replyTo[i]);
            }
            message.setReplyTo(replyToAddress);
            InternetAddress[] toAddress = new InternetAddress[to.length];

            // To get the array of addresses
            for( int i = 0; i < to.length; i++ ) {
                toAddress[i] = new InternetAddress(to[i]);
            }

            for( int i = 0; i < toAddress.length; i++) {
                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
            }

            message.setSubject(EmailSignalChannelAgent.MSGTITLEPREFIX + subject);
            message.setContent(EmailSignalService.encodeBody(body) + "\r\n\r\n", "text/plain"); // have to + "\r\n\r\n", it seems to be a bug of qq mail.
            //message.setText(EmailSignalService.encodeBody(body));
            transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            transport.sendMessage(message, message.getAllRecipients());
        }
        catch (AddressException ae) {
            ae.printStackTrace();
            err = ae;
        }
        catch (MessagingException me) {
            me.printStackTrace();
            err = me;
        }
        catch (Exception e) {
            e.printStackTrace();
            err = e;
        }
        finally {
            if (transport != null) {
                try {
                    transport.close();  // try to close transport.
                } catch (MessagingException me) {
                    me.printStackTrace();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            return err;
        }
    }
}
