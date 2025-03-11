package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.configuration.DateConfiguration;
import com.akkorhotel.hotel.dao.BookingDao;
import com.akkorhotel.hotel.dao.HotelDao;
import com.akkorhotel.hotel.dao.HotelRoomDao;
import com.akkorhotel.hotel.model.Booking;
import com.akkorhotel.hotel.model.BookingStatus;
import com.akkorhotel.hotel.model.Hotel;
import com.akkorhotel.hotel.model.HotelRoom;
import com.akkorhotel.hotel.model.request.CreateBookingRequest;
import com.akkorhotel.hotel.model.request.UpdateBookingRequest;
import com.akkorhotel.hotel.model.response.GetBookingResponse;
import com.akkorhotel.hotel.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingDao bookingDao;
    private final UuidProvider uuidProvider;
    private final UserUtils userUtils;
    private final HotelDao hotelDao;
    private final HotelRoomDao hotelRoomDao;
    private final DateConfiguration dateConfiguration;

    public ResponseEntity<Map<String, String>> createBooking(String authenticatedUserId, CreateBookingRequest request) {
        List<String> errors = new ArrayList<>();
        validateRequest(errors, request);
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("errors", userUtils.getErrorsAsString(errors)));
        }

        Optional<Hotel> optionalHotel = hotelDao.findById(request.getHotelId());
        if (optionalHotel.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(singletonMap("error", "Hotel not found"));
        }

        Hotel hotel = optionalHotel.get();

        Optional<HotelRoom> optionalHotelRoom = hotelRoomDao.findById(request.getHotelRoomId());
        if (optionalHotelRoom.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(singletonMap("error", "HotelRoom not found"));
        }

        HotelRoom hotelRoom = optionalHotelRoom.get();

        if (!hotel.getRooms().contains(hotelRoom)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("error", "The specified hotel room was not found in the hotel's room list"));
        }

        if (request.getGuests() > hotelRoom.getMaxOccupancy()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("error", "The number of guests exceeds the maximum occupancy for this hotel room"));
        }

        double totalPrice = calculateTotalPrice(request.getCheckInDate(), request.getCheckOutDate(), hotelRoom.getPrice());

        Booking booking = Booking.builder()
                .id(uuidProvider.generateUuid())
                .totalPrice(totalPrice)
                .guests(request.getGuests())
                .userId(authenticatedUserId)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .hotelRoom(hotelRoom)
                .hotel(hotel)
                .build();

        bookingDao.save(booking);

        return ResponseEntity.ok(singletonMap("message", "Booking created successfully"));
    }

    public ResponseEntity<Map<String, GetBookingResponse>> getBooking(String authenticatedUserId, String bookingId) {
        GetBookingResponse response = GetBookingResponse.builder().build();

        Optional<Booking> optionalBooking = bookingDao.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            response.setError("Booking not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(singletonMap("error", response));
        }

        Booking booking = optionalBooking.get();

        if (!booking.getUserId().equals(authenticatedUserId)) {
            response.setError("You do not have permission to access this booking");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(singletonMap("error", response));
        }

        response.setBooking(booking);
        return ResponseEntity.ok(singletonMap("informations", response));
    }

    public ResponseEntity<Map<String, String>> updateBooking(String authenticatedUserId, UpdateBookingRequest request) {
        if (isNull(request.getBookingId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("error", "BookingId is required"));
        }

        Optional<Booking> optionalBooking = bookingDao.findById(request.getBookingId());
        if (optionalBooking.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(singletonMap("error", "Booking not found"));
        }

        Booking booking = optionalBooking.get();

        if (!booking.getUserId().equals(authenticatedUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(singletonMap("error", "You do not have permission to access this booking"));
        }

        List<String> errors = new ArrayList<>();
        validateRequest(errors, request, booking.getCheckInDate(), booking.getCheckOutDate());
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("errors", userUtils.getErrorsAsString(errors)));
        }

        if (booking.getHotelRoom().getMaxOccupancy() < request.getGuests()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("error", "The number of guests exceeds the maximum occupancy for this hotel room"));
        }

        setBookingValues(request, booking);
        bookingDao.save(booking);

        return ResponseEntity.ok(singletonMap("message", "Booking updated successfully"));
    }

    public ResponseEntity<Map<String, String>> deleteBooking(String authenticatedUserId, String bookingId) {
        if (isNull(bookingId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(singletonMap("error", "BookingId is required"));
        }

        Optional<Booking> optionalBooking = bookingDao.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(singletonMap("error", "Booking not found"));
        }

        Booking booking = optionalBooking.get();
        if (!booking.getUserId().equals(authenticatedUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(singletonMap("error", "You are not allowed to delete this booking"));
        }

        bookingDao.delete(bookingId);

        return ResponseEntity.ok(singletonMap("message", "Booking deleted successfully"));
    }

    public ResponseEntity<String> getBookings() {
        return ResponseEntity.ok().build();
    }

    private void setBookingValues(UpdateBookingRequest request, Booking booking) {
        booking.setGuests(request.getGuests() != 0 ? request.getGuests() : booking.getGuests());
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaid(false);
        booking.setTotalPrice(calculateTotalPrice(request.getCheckInDate(), request.getCheckOutDate(), booking.getHotelRoom().getPrice()));
    }

    private void validateRequest(List<String> errors, UpdateBookingRequest request, Date checkInDate, Date checkOutDate) {
        if (request.getGuests() == 0 && isNull(request.getCheckInDate()) && isNull(request.getCheckOutDate())) {
            errors.add("At least one field must be provided for the update");
        } else {
            request.setCheckInDate(isNull(request.getCheckInDate()) ? checkInDate : request.getCheckInDate());
            request.setCheckOutDate(isNull(request.getCheckOutDate()) ? checkOutDate : request.getCheckOutDate());
            Date today = dateConfiguration.newDate();

            if (request.getGuests() < 0) {
                errors.add("The number of guests must be greater than zero");
            }
            if (!request.getCheckInDate().after(today)) {
                errors.add("Check-in date must be after today's date");
            }
            if (!request.getCheckOutDate().after(request.getCheckInDate())) {
                errors.add("Check-out date must be after check-in date");
            }
        }
    }

    private void validateRequest(List<String> errors, CreateBookingRequest request) {
        if (request.getGuests() <= 0) {
            errors.add("The number of guests must be greater than zero");
        }

        Date today = dateConfiguration.newDate();

        if (isNull(request.getCheckInDate())) {
            errors.add("Check-in date is required");
        } else if (!request.getCheckInDate().after(today)) {
            errors.add("Check-in date must be after today's date");
        }

        if (isNull(request.getCheckOutDate())) {
            errors.add("Check-out date is required");
        } else if (request.getCheckInDate() != null && !request.getCheckOutDate().after(request.getCheckInDate())) {
            errors.add("Check-out date must be after check-in date");
        }
    }

    private double calculateTotalPrice(Date checkInDate, Date checkOutDate, double roomPrice) {
        LocalDate checkInLocalDate = checkInDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate checkOutLocalDate = checkOutDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        long numberOfNights = ChronoUnit.DAYS.between(checkInLocalDate, checkOutLocalDate);

        return roomPrice * numberOfNights;
    }

}
