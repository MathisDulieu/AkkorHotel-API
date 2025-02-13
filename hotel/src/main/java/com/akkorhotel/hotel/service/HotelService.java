package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.HotelDao;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelDao hotelDao;
    private final JwtTokenService jwtToken;

    public ResponseEntity<String> createHotel() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> getHotel() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> updateHotel() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> deleteHotel() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> getHotels() {
        return ResponseEntity.ok().build();
    }
}
