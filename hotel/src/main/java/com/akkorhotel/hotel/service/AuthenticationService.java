package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.UserDao;
import com.akkorhotel.hotel.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserDao userDao;
    private final UuidProvider uuidProvider;
    private final JwtTokenService jwtTokenService;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

//    private static final String MAIL_REGISTER_SUBJECT = System.getenv("MAIL_REGISTER_SUBJECT");
    private static final String MAIL_REGISTER_SUBJECT = "Confirmez votre inscription à Akkor Hotel";
//    private static final String MAIL_REGISTER_CONFIRMATION_LINK = System.getenv("MAIL_REGISTER_CONFIRMATION_LINK");
    private static final String MAIL_REGISTER_CONFIRMATION_LINK = "https://www.akkorhotel.com/valider-l-email/";

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}';:\",.<>?|`~])[A-Za-z\\d!@#$%^&*()_+\\-={}';:\",.<>?|`~]{8,}$";

    public ResponseEntity<String> register(User user) {
        String error = getValidationError(user);
        if (!isNull(error)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        user.setId(uuidProvider.generateUuid());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userDao.save(user);

        error = sendRegisterConfirmationEmail(user);
        if (!isNull(error)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> login() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> confirmEmail() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> resendConfirmationEmail() {
        return ResponseEntity.ok().build();
    }

    private String getValidationError(User user) {
        if (isInvalidEmail(user.getEmail())) {
            return "The provided email is not valid.";
        }
        if (isInvalidUsername(user.getUsername())) {
            return "The username must be between 3 and 11 characters long and must not contain spaces.";
        }
        if (isInvalidPassword(user.getPassword())) {
            return "The password does not meet the required criteria.";
        }
        if (userDao.isUserInDatabase(user.getUsername(), user.getEmail())) {
            return "A user with this email or username already exists.";
        }

        return null;
    }

    private boolean isInvalidEmail(String email) {
        return isNull(email) || !Pattern.compile(EMAIL_REGEX).matcher(email).matches();
    }

    private boolean isInvalidUsername(String username) {
        return isNull(username) || username.length() < 3 || username.length() > 11 || username.contains(" ");
    }

    private boolean isInvalidPassword(String password) {
        return isNull(password) || !password.matches(PASSWORD_REGEX);
    }

    private String sendRegisterConfirmationEmail(User user) {
        String emailConfirmationToken = jwtTokenService.generateEmailConfirmationToken(user.getId());

        String body = getRegisterConfirmationEmailBody(emailConfirmationToken, user.getUsername());

        try {
            emailService.sendEmail(user.getEmail(), MAIL_REGISTER_SUBJECT, body);
        } catch (MailException e) {
            return "Failed to send the registration confirmation email. Please try again later.";
        }

        return null;
    }

    private static String getRegisterConfirmationEmailBody(String emailConfirmationToken, String username) {
        String confirmationLink = MAIL_REGISTER_CONFIRMATION_LINK + emailConfirmationToken;

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
