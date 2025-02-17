package com.akkorhotel.hotel.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

import static com.akkorhotel.hotel.configuration.EnvConfiguration.getAppEmail;
import static com.akkorhotel.hotel.configuration.EnvConfiguration.getMailModifiedUsername;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "utf-8");

            helper.setFrom(getAppEmail(), getMailModifiedUsername());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body + getEmailSignature(), true);

            javaMailSender.send(mimeMessage);
        } catch (MailException | MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Error sending email: " + e.getMessage(), e);
        }
    }

    private static String getEmailSignature() {
        return "<br>"
                + "<p>Cordialement,<br>"
                + "L'équipe Akkor Hotel.</p>"
                + "<p>Akkor Hotel - Réservez les meilleurs hôtels partout dans le monde.</p>"
                + "<p>📞 Assistance : <a href=\"tel:+33123456789\">+33 1 23 45 67 89</a><br>"
                + "📩 Email : <a href=\"mailto:support@akkorhotel.com\">support@akkorhotel.com</a><br>"
                + "🌐 Site web : <a href=\"https://www.akkorhotel.com\">https://www.akkorhotel.com</a><br>"
                + "📱 Disponible sur iOS et Android !</p>";
    }

}
