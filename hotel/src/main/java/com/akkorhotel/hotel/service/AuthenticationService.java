package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.UserDao;
import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.request.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.akkorhotel.hotel.configuration.EnvConfiguration.getMailRegisterConfirmationLink;
import static com.akkorhotel.hotel.configuration.EnvConfiguration.getMailRegisterSubject;
import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserDao userDao;
    private final UuidProvider uuidProvider;
    private final JwtTokenService jwtTokenService;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}';:\",.<>?|`~])[A-Za-z\\d!@#$%^&*()_+\\-={}';:\",.<>?|`~]{8,}$";

    public ResponseEntity<Map<String, String>> register(User user) {
        String error = getValidationError(user);
        if (!isNull(error)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("error", error));
        }

        user.setId(uuidProvider.generateUuid());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userDao.save(user);

        error = sendRegisterConfirmationEmail(user);
        if (!isNull(error)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(singletonMap("error", error));
        }

        return ResponseEntity.ok(singletonMap("message", "User successfully registered!"));
    }

    public ResponseEntity<Map<String, String>> login(LoginRequest loginRequest) {
        Optional<User> optionalUser = userDao.findByEmail(loginRequest.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(singletonMap("error", "User not found"));
        }

        User user = optionalUser.get();

        if(!user.getIsValidEmail()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(singletonMap("error", "Email is not verified"));
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("error", "Invalid password"));
        }

        return ResponseEntity.ok(singletonMap("token", jwtTokenService.generateToken(user.getId())));
    }

    public ResponseEntity<Map<String, String>> confirmEmail(String token) {
        boolean isTokenValid = jwtTokenService.isEmailTokenValid(token);
        if (!isTokenValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("error", "Invalid or expired token"));
        }

        String userId = jwtTokenService.resolveUserIdFromToken(token);
        Optional<User> optionalUser = userDao.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(singletonMap("error", "User not found"));
        }

        User user = optionalUser.get();
        if (user.getIsValidEmail()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("error", "Email already validated"));
        }

        user.setIsValidEmail(true);
        userDao.save(user);

        return ResponseEntity.ok().body(singletonMap("message", "Email successfully validated"));
    }

    public ResponseEntity<Map<String, String>> resendConfirmationEmail(String email) {
        Optional<User> optionalUser = userDao.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(singletonMap("error", "User not found"));
        }

        User user = optionalUser.get();
        if (user.getIsValidEmail()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("error", "Email already validated"));
        }

        String error = sendRegisterConfirmationEmail(user);
        if (!isNull(error)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(singletonMap("error", error));
        }

        return ResponseEntity.ok().body(singletonMap("message", "Confirmation email successfully sent"));
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
            emailService.sendEmail(user.getEmail(), getMailRegisterSubject(), body);
        } catch (MailException e) {
            return "Failed to send the registration confirmation email. Please try again later.";
        }

        return null;
    }

    private static String getRegisterConfirmationEmailBody(String emailConfirmationToken, String username) {
        String confirmationLink = getMailRegisterConfirmationLink() + emailConfirmationToken;

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
