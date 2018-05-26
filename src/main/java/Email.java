package main.java;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Properties;
import java.util.Scanner;

import javax.mail.AuthenticationFailedException;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Email {
    private String username;
    private String passwordPath;

    public Email(String username, String passwordPath) {
        this.username = username;
        this.passwordPath = passwordPath;
    }

    public String getPassword(String path) {
        String password = "";

        try {
            Scanner in = new Scanner(new FileReader(path));
            password = in.nextLine().trim();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return password;
    }

    public void sendMail(String to, String subject, String content) {
        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.user", username);

        Session session = Session.getInstance(props,
                                              new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, getPassword(passwordPath));
            }
        });

        try {
            javax.mail.internet.MimeMessage message = new MimeMessage(session);

            // Set sender.
            message.setFrom(new InternetAddress(username));

            // Set recipient.
            message.setRecipients(RecipientType.TO,
                                  InternetAddress.parse(to));

            // Set subject
            message.setSubject(subject);

            // Send message content
            message.setContent(content, "text/html; charset=utf-8");

            // Send message
            Transport.send(message);
            System.out.println("Email sent to " + to + " successfully");
        } catch (AuthenticationFailedException e1) {
            System.out.println("ERROR: Less secure apps access for gmail account disabled");
        } catch (MessagingException e2) {
            e2.printStackTrace();
        }
    }
}
