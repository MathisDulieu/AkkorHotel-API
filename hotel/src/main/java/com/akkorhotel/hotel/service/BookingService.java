package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.BookingDao;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingDao bookingDao;
    private final JwtTokenService jwtToken;

    public ResponseEntity<String> createBooking() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> getBooking() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> updateBooking() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> deleteBooking() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> listBookings() {
        return ResponseEntity.ok().build();
    }
}
