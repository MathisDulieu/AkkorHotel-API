package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.UserDao;
import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.request.UpdateUserRequest;
import com.akkorhotel.hotel.model.response.GetAuthenticatedUserResponse;
import com.akkorhotel.hotel.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final UserUtils userUtils;
    private final BCryptPasswordEncoder passwordEncoder;

    public ResponseEntity<Map<String, GetAuthenticatedUserResponse>> getAuthenticatedUser(User authenticatedUser) {
        GetAuthenticatedUserResponse authenticatedUserResponse = GetAuthenticatedUserResponse.builder()
                .username(authenticatedUser.getUsername())
                .email(authenticatedUser.getEmail())
                .userRole(authenticatedUser.getRole().toString())
                .build();

        return ResponseEntity.ok().body(singletonMap("informations", authenticatedUserResponse));
    }

    public ResponseEntity<Map<String, String>> updateUser(UpdateUserRequest request, User authenticatedUser) {
        List<String> errors = new ArrayList<>();

        validateRequest(errors, request);
        if (errors.isEmpty()) {
            validateNewUsername(errors, request.getUsername(), authenticatedUser);
            validateNewEmail(errors, request.getEmail(), authenticatedUser);
            validateNewPassword(errors, request.getOldPassword(), request.getNewPassword(), authenticatedUser);
        }

        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("errors", getErrorsAsString(errors)));
        }

        userDao.save(authenticatedUser);

        String error = userUtils.sendRegisterConfirmationEmail(authenticatedUser);
        if (!isNull(error)) {
            return ResponseEntity.status(HttpStatus.OK).body(singletonMap("warning", error));
        }

        return ResponseEntity.ok(singletonMap("message", "User details updated successfully"));
    }

    public ResponseEntity<Map<String, String>> deleteUser(String authenticatedUserId) {
        userDao.delete(authenticatedUserId);

        return ResponseEntity.ok(singletonMap("message", "User deleted successfully"));
    }

    public ResponseEntity<Map<String, String>> setUserProfileImage() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Map<String, String>> getUserProfileImage() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Map<String, String>> deleteUserProfileImage() {
        return ResponseEntity.ok().build();
    }

    private String getErrorsAsString(List<String> errors) {
        return String.join(" | ", errors);
    }

    private void validateRequest(List<String> errors, UpdateUserRequest request) {
        if (isNull(request.getEmail()) && isNull(request.getUsername()) && isNull(request.getNewPassword())) {
            errors.add("No values provided for update. Please specify at least one field (email, username, or new password).");
        }
    }

    private void validateNewUsername(List<String> errors, String username, User userToUpdate) {
        if (!isNull(username)) {
            if (userUtils.isInvalidUsername(username)) {
                errors.add("Invalid username: Must be 3-11 characters and cannot contain spaces.");
            }

            if (userDao.isUsernameAlreadyUsed(username)) {
                errors.add("Username already taken: Please choose a different one.");
            }

            if (username.equals(userToUpdate.getUsername())) {
                errors.add("Username cannot be the same as the current one.");
            }

            userToUpdate.setUsername(username);
        }
    }

    private void validateNewEmail(List<String> errors, String email, User userToUpdate) {
        if (!isNull(email)) {
            if (userUtils.isInvalidEmail(email)) {
                errors.add("Invalid email format.");
            }

            if (userDao.isEmailAlreadyUsed(email)) {
                errors.add("This email is already used.");
            }

            if (email.equals(userToUpdate.getEmail())) {
                errors.add("The new email address must be different from the current one.");
            }

            userToUpdate.setIsValidEmail(false);
            userToUpdate.setEmail(email);
        }
    }

    private void validateNewPassword(List<String> errors, String oldPassword, String newPassword, User userToUpdate) {
        if (!isNull(oldPassword) || !isNull(newPassword)) {
            if (userUtils.isInvalidPassword(newPassword)) {
                errors.add("The new password does not meet the required criteria.");
            }

            if (passwordEncoder.matches(newPassword, userToUpdate.getPassword())) {
                errors.add("New password must be different from the old password.");
            }

            if (!passwordEncoder.matches(oldPassword, userToUpdate.getPassword())) {
                errors.add("Old password is incorrect.");
            }

            userToUpdate.setPassword(passwordEncoder.encode(newPassword));
        }
    }

}
