package com.example.swasthpunjab;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    // ⚠️ For real apps, don't hardcode. Use server / remote config.
    private static final String USERNAME = "pathakritesh12345@gmail.com";
    private static final String PASSWORD = "xkxk hgnp ebqg gddt"; // Gmail App Password
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    public static void sendEmail(String to, String subject, String body) throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");                // ✅ Authentication ON
        props.put("mail.smtp.starttls.enable", "true");     // ✅ TLS
        props.put("mail.smtp.host", SMTP_HOST);             // Gmail SMTP
        props.put("mail.smtp.port", SMTP_PORT);             // Port 587

        // 👉 Here is where email authentication is configured
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(USERNAME));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);   // 🚀 Send mail
    }
}
