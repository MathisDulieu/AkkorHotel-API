package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.UserDao;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserDao userDao;
    private final JwtTokenService jwtToken;

    public ResponseEntity<String> getAllUsers() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> getUserById() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> updateUser() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> getAllUserBookings() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> getAllHotelBookings() {
        return ResponseEntity.ok().build();
    }
}
