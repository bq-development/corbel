package io.corbel.notifications.service;

import com.google.common.base.Charsets;
import io.corbel.notifications.model.Domain;
import io.corbel.notifications.model.NotificationTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by Alberto J. Rubio
 */
public class EmailNotificationsService implements NotificationsService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailNotificationsService.class);
    private String host;
    private String port;

    public EmailNotificationsService(String host, String port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void send(Domain domain, NotificationTemplate notificationTemplate, String... recipients) {
        try {
            LOG.info("Sending email to: {}" + Arrays.toString(recipients));
            Properties props = new Properties();
            props.setProperty("mail.smtp.host", host);
            props.setProperty("mail.smtp.port", port);
            MimeMessage message = new MimeMessage(Session.getDefaultInstance(props, null));
            message.setFrom(new InternetAddress(notificationTemplate.getSender()));
            for (String recipient : recipients) {
                message.addRecipient(Message.RecipientType.BCC, new InternetAddress(recipient));
            }
            message.setSubject(notificationTemplate.getTitle(), Charsets.UTF_8.toString());
            message.setContent(notificationTemplate.getText(), "text/html; charset=utf-8");
            Transport.send(message);
            LOG.info("Email was sent to: {}" + Arrays.toString(recipients));
        } catch (MessagingException e) {
            LOG.error("Sending mail error: {}", e.getMessage(), e);
        }
    }
}