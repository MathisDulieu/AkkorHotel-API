package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.UserDao;
import com.akkorhotel.hotel.model.response.GetAllUsersResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserDao userDao;

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

    public ResponseEntity<Map<String, String>> getUserById() {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Map<String, String>> updateUser() {
        return ResponseEntity.ok().build();
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

}
