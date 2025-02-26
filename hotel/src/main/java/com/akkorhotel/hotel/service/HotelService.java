package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.HotelDao;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelDao hotelDao;

    public ResponseEntity<Map<String, String>> getHotel() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Map<String, String>> getHotels() {
        return ResponseEntity.ok().build();
    }
}
