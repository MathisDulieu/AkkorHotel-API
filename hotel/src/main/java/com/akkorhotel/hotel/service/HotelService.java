package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.HotelDao;
import com.akkorhotel.hotel.model.Hotel;
import com.akkorhotel.hotel.model.response.GetAllHotelsResponse;
import com.akkorhotel.hotel.model.response.GetHotelResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;

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

    public ResponseEntity<Map<String, GetAllHotelsResponse>> getHotels(String keyword, int page, int pageSize) {
        GetAllHotelsResponse response = GetAllHotelsResponse.builder().build();
        pageSize = getPageSizeValue(pageSize);

        String error = validateRequest(keyword, pageSize, page);
        if (!isNull(error)) {
            response.setError(error);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("error", response));
        }

        long totalHotels = hotelDao.countHotelsByNamePrefix(keyword);
        if (totalHotels == 0) {
            response.setError("No hotel found");
            return ResponseEntity.ok(singletonMap("informations", response));
        }

        int totalPages = getTotalPages(totalHotels, pageSize);
        response.setTotalPages(totalPages);

        if (page > totalPages) {
            response.setError("Requested page exceeds the total number of available pages");
            return ResponseEntity.ok(singletonMap("warning", response));
        }

        response.setHotels(hotelDao.searchHotelsByNamePrefix(keyword, page, pageSize));

        return ResponseEntity.ok(singletonMap("informations", response));
    }

    private int getPageSizeValue(int pageSize) {
        return (pageSize == 0) ? 10 : pageSize;
    }

    private String validateRequest(String keyword, int pageSize, int page) {
        if (pageSize < 0) return "Page size must be greater than or equal to zero";
        if (page < 0) return "Page number must be greater than or equal to zero";
        if (keyword.contains(" ")) return "Search keyword cannot contain spaces";
        return null;
    }

    private int getTotalPages(long totalUsers, int pageSize) {
        return (int) Math.ceil((double) totalUsers / pageSize);
    }
}
