package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.UserDao;
import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.response.GetAuthenticatedUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

import static java.util.Collections.singletonMap;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;

    public ResponseEntity<Map<String, GetAuthenticatedUserResponse>> getAuthenticatedUser(User authenticatedUser) {
        GetAuthenticatedUserResponse authenticatedUserResponse = GetAuthenticatedUserResponse.builder()
                .username(authenticatedUser.getUsername())
                .email(authenticatedUser.getEmail())
                .userRole(authenticatedUser.getRole().toString())
                .build();

        return ResponseEntity.ok().body(singletonMap("informations", authenticatedUserResponse));
    }

    public ResponseEntity<Map<String, String>> updateUser() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Map<String, String>> deleteUser() {
        return ResponseEntity.ok().build();
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
}
