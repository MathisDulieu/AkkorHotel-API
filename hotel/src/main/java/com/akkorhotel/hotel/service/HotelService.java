package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.HotelDao;
import com.akkorhotel.hotel.model.Hotel;
import com.akkorhotel.hotel.model.response.GetHotelResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonMap;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelDao hotelDao;

    public ResponseEntity<Map<String, GetHotelResponse>> getHotel(String hotelId) {
        GetHotelResponse response = GetHotelResponse.builder().build();

        Optional<Hotel> optionalHotel = hotelDao.findById(hotelId);
        if (optionalHotel.isEmpty()) {
            response.setError("Hotel not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(singletonMap("error", response));
        }

        response.setHotel(optionalHotel.get());

        return ResponseEntity.ok(singletonMap("informations", response));
    }

    public ResponseEntity<Map<String, String>> getHotels() {
        return ResponseEntity.ok().build();
    }
}
