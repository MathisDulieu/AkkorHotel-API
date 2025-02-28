package com.akkorhotel.hotel.utils;

import com.akkorhotel.hotel.configuration.EnvConfiguration;
import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.service.EmailService;
import com.akkorhotel.hotel.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;


@Component
@RequiredArgsConstructor
public class UserUtils {

    private final JwtTokenService jwtTokenService;
    private final EmailService emailService;
    private final EnvConfiguration envConfiguration;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}';:\",.<>?|`~])[A-Za-z\\d!@#$%^&*()_+\\-={}';:\",.<>?|`~]{8,}$";

    public boolean isInvalidEmail(String email) {
        return isNull(email) || !Pattern.compile(EMAIL_REGEX).matcher(email).matches();
    }

    public boolean isInvalidUsername(String username) {
        return isNull(username) || username.length() < 3 || username.length() > 11 || username.contains(" ");
    }

    public String getErrorsAsString(List<String> errors) {
        return String.join(" | ", errors);
    }

    public boolean isInvalidPassword(String password) {
        return isNull(password) || !password.matches(PASSWORD_REGEX);
    }

    public String sendRegisterConfirmationEmail(User user) {
        String emailConfirmationToken = jwtTokenService.generateEmailConfirmationToken(user.getId());

        String body = getRegisterConfirmationEmailBody(emailConfirmationToken, user.getUsername());

        try {
            emailService.sendEmail(user.getEmail(), envConfiguration.getMailRegisterSubject(), body);
        } catch (MailException e) {
            return "Failed to send the registration confirmation email. Please try again later.";
        }

        return null;
    }

    private String getRegisterConfirmationEmailBody(String emailConfirmationToken, String username) {
        String confirmationLink = envConfiguration.getMailRegisterConfirmationLink() + emailConfirmationToken;

        return "<html>"
                + "<body>"
                + "<h2>Bienvenue " + username + " !</h2>"
                + "<p>Merci de vous être inscrit sur notre application.</p>"
                + "<p>Pour activer votre compte, veuillez cliquer sur le lien suivant :</p>"
                + "<p><a href=\"" + confirmationLink + "\">Confirmer mon email</a></p>"
                + "<p>Si vous n'avez pas créé de compte, veuillez ignorer cet email.</p>"
                + "</body>"
                + "</html>";
    }

}
