package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.configuration.EnvConfiguration;
import com.akkorhotel.hotel.dao.UserDao;
import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.request.LoginRequest;
import com.akkorhotel.hotel.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserDao userDao;
    private final UuidProvider uuidProvider;
    private final JwtTokenService jwtTokenService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserUtils userUtils;
    private final EnvConfiguration envConfiguration;

    public ResponseEntity<Map<String, String>> register(User user) {
        String error = getValidationError(user);
        if (!isNull(error)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("error", error));
        }

        user.setId(uuidProvider.generateUuid());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setProfileImageUrl(envConfiguration.getDefaultUserProfileImage());
        userDao.save(user);

        error = userUtils.sendRegisterConfirmationEmail(user);
        if (!isNull(error)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(singletonMap("warning", error));
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

        String error = userUtils.sendRegisterConfirmationEmail(user);
        if (!isNull(error)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(singletonMap("error", error));
        }

        return ResponseEntity.ok().body(singletonMap("message", "Confirmation email successfully sent"));
    }

    private String getValidationError(User user) {
        if (userUtils.isInvalidEmail(user.getEmail())) {
            return "The provided email is not valid.";
        }
        if (userUtils.isInvalidUsername(user.getUsername())) {
            return "The username must be between 3 and 11 characters long and must not contain spaces.";
        }
        if (userUtils.isInvalidPassword(user.getPassword())) {
            return "The password does not meet the required criteria.";
        }
        if (userDao.isUserInDatabase(user.getUsername(), user.getEmail())) {
            return "A user with this email or username already exists.";
        }

        return null;
    }

}
