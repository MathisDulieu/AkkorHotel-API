package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.UserDao;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserDao userDao;
    private final JwtTokenService jwtToken;

    public ResponseEntity<String> register() {
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

}
