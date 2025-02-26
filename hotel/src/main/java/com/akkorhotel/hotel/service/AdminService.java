package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.UserDao;
import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.UserRole;
import com.akkorhotel.hotel.model.request.AdminUpdateUserRequest;
import com.akkorhotel.hotel.model.response.GetAllUsersResponse;
import com.akkorhotel.hotel.model.response.GetUserByIdResponse;
import com.akkorhotel.hotel.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserDao userDao;
    private final UserUtils userUtils;

    public ResponseEntity<Map<String, GetAllUsersResponse>> getAllUsers(String keyword, int page, int pageSize) {
        GetAllUsersResponse response = GetAllUsersResponse.builder().build();

        String error = validateRequest(keyword, pageSize, page);
        if (!isNull(error)) {
            response.setError(error);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("error", response));
        }

        long totalUsers = userDao.countUsersByUsernamePrefix(keyword);
        if (totalUsers == 0) {
            response.setError("No users found");
            return ResponseEntity.ok(singletonMap("users", response));
        }

        int totalPages = getTotalPages(totalUsers, pageSize);

        if (page > totalPages) {
            response.setError("Requested page exceeds the total number of available pages");
            return ResponseEntity.ok(singletonMap("warning", response));
        }

        response.setUsers(userDao.searchUsersByUsernamePrefix(keyword, page, pageSize));
        response.setTotalPages(totalPages);

        return ResponseEntity.ok(singletonMap("users", response));
    }

    public ResponseEntity<Map<String, GetUserByIdResponse>> getUserById(String userId) {
        GetUserByIdResponse response = GetUserByIdResponse.builder().build();
        Optional<User> optionalUser = userDao.findById(userId);

        if (optionalUser.isEmpty()) {
            response.setError("User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(singletonMap("error", response));
        }

        User user = optionalUser.get();
        if (user.getRole().equals(UserRole.ADMIN)) {
            response.setError("Admin users cannot be retrieved");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(singletonMap("error", response));
        }

        user.setPassword(null);
        response.setUser(user);

        return ResponseEntity.ok(singletonMap("user", response));
    }

    public ResponseEntity<Map<String, String>> updateUser(String userId, AdminUpdateUserRequest request) {
        Optional<User> optionalUser = userDao.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(singletonMap("error", "User not found"));
        }

        User user = optionalUser.get();
        List<String> errors = new ArrayList<>();

        validateRequest(errors, request);
        if (errors.isEmpty()) {
            validateNewUsername(errors, request.getUsername(), user);
            validateNewEmail(errors, request.getEmail(), user);
            validateNewRole(errors, request.getRole(), user);
            validateIsValidEmailValue(errors, request.getIsValidEmail(), user);
        }

        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("errors", userUtils.getErrorsAsString(errors)));
        }

        userDao.save(user);

        return ResponseEntity.ok(singletonMap("message", "User with id: " + userId + " updated successfully"));
    }

    public ResponseEntity<Map<String, String>> createHotel() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Map<String, String>> updateHotel() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Map<String, String>> deleteHotel() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Map<String, String>> getAllUserBookings() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Map<String, String>> getAllHotelBookings() {
        return ResponseEntity.ok().build();
    }

    private int getTotalPages(long totalUsers, int pageSize) {
        return (int) Math.ceil((double) totalUsers / pageSize);
    }

    private String validateRequest(String keyword, int pageSize, int page) {
        if (pageSize < 0) return "Page size must be greater than or equal to zero";
        if (page < 0) return "Page number must be greater than or equal to zero";
        if (keyword.contains(" ")) return "Search keyword cannot contain spaces";
        return null;
    }

    private void validateRequest(List<String> errors, AdminUpdateUserRequest request) {
        if (isNull(request.getEmail()) && isNull(request.getUsername()) && isNull(request.getIsValidEmail()) && isNull(request.getRole())) {
            errors.add("No values provided for update. Please specify at least one field (email, username, isValidEmail or role)");
        }
    }

    private void validateNewUsername(List<String> errors, String username, User userToUpdate) {
        if (!isNull(username)) {
            if (userUtils.isInvalidUsername(username)) {
                errors.add("The provided username is invalid. It must be between 3 and 11 characters long and cannot contain spaces");
            }

            if (userDao.isUsernameAlreadyUsed(username)) {
                errors.add("The username '" + username + "' is already in use by another account. Please choose a different one");
            }

            if (username.equals(userToUpdate.getUsername())) {
                errors.add("The new username must be different from the current one");
            }

            userToUpdate.setUsername(username);
        }
    }

    private void validateNewEmail(List<String> errors, String email, User userToUpdate) {
        if (!isNull(email)) {
            if (userUtils.isInvalidEmail(email)) {
                errors.add("The provided email format is invalid. Please enter a valid email address");
            }

            if (userDao.isEmailAlreadyUsed(email)) {
                errors.add("The email address '" + email + "' is already associated with another account");
            }

            if (email.equals(userToUpdate.getEmail())) {
                errors.add("The new email address must be different from the current one");
            }

            userToUpdate.setEmail(email);
        }
    }

    private void validateNewRole(List<String> errors, String role, User userToUpdate) {
        if (!isNull(role)) {
            if (role.equals(userToUpdate.getRole().toString())) {
                errors.add("The new role must be different from the current one");
            }

            try {
                UserRole newRole = UserRole.valueOf(role.toUpperCase());
                userToUpdate.setRole(newRole);
            } catch (IllegalArgumentException e) {
                errors.add("Invalid role: " + role + ". Allowed values are: " + Arrays.toString(UserRole.values()));
            }
        }
    }

    private void validateIsValidEmailValue(List<String> errors, Boolean isValidEmail, User user) {
        if (!isNull(isValidEmail)) {
            if (user.getIsValidEmail().equals(isValidEmail)) {
                errors.add("The email verification status is already set to the provided value. No changes were made.");
            }

            user.setIsValidEmail(isValidEmail);
        }
    }

}
