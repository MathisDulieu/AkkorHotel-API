package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.UserDao;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;

    public ResponseEntity<String> getUser() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> updateUser() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> deleteUser() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> setUserProfileImage() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> getUserProfileImage() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> deleteUserProfileImage() {
        return ResponseEntity.ok().build();
    }
}
