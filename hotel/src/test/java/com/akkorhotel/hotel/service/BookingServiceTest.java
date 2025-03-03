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
import com.akkorhotel.hotel.utils.UserUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private BookingDao bookingDao;

    @Mock
    private HotelDao hotelDao;

    @Mock
    private HotelRoomDao hotelRoomDao;

    @Mock
    private UserUtils userUtils;

    @Mock
    private UuidProvider uuidProvider;

    @Mock
    private DateConfiguration dateConfiguration;

    @Test
    void shouldCreateBooking() {
        // Arrange
        String authenticatedUserId = "userId";
        CreateBookingRequest request = new CreateBookingRequest();
        request.setGuests(3);
        request.setHotelId("hotelId");
        request.setHotelRoomId("hotelRoomId");
        request.setCheckInDate(new Date(1677628800000L));
        request.setCheckOutDate(new Date(1677715200000L));

        HotelRoom hotelRoom = HotelRoom.builder().id("hotelRoomId").maxOccupancy(5).price(100.00).build();
        Hotel hotel = Hotel.builder().id("hotelId").rooms(List.of(hotelRoom)).build();

        when(dateConfiguration.newDate()).thenReturn(new Date(1677024000000L));
        when(hotelDao.findById(anyString())).thenReturn(Optional.of(hotel));
        when(hotelRoomDao.findById(anyString())).thenReturn(Optional.of(hotelRoom));
        when(uuidProvider.generateUuid()).thenReturn("generatedUuid");

        // Act
        ResponseEntity<Map<String, String>> response = bookingService.createBooking(authenticatedUserId, request);

        // Assert
        Booking expectedBooking = Booking.builder()
                .id("generatedUuid")
                .userId("userId")
                .hotel(hotel)
                .hotelRoom(hotelRoom)
                .checkInDate(new Date(1677628800000L))
                .checkOutDate(new Date(1677715200000L))
                .guests(3)
                .totalPrice(100.00)
                .isPaid(false)
                .status(BookingStatus.PENDING)
                .build();

        InOrder inOrder = inOrder(dateConfiguration, hotelDao, hotelRoomDao, uuidProvider, bookingDao);
        inOrder.verify(dateConfiguration).newDate();
        inOrder.verify(hotelDao).findById("hotelId");
        inOrder.verify(hotelRoomDao).findById("hotelRoomId");
        inOrder.verify(uuidProvider).generateUuid();
        inOrder.verify(bookingDao).save(expectedBooking);
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(userUtils);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("message", "Booking created successfully"));
    }

    @Test
    void shouldReturnBadRequest_whenGuestsIsZeroOrNegative() {
        // Arrange
        String authenticatedUserId = "userId";
        CreateBookingRequest request = new CreateBookingRequest();
        request.setGuests(0);
        request.setHotelId("hotelId");
        request.setHotelRoomId("hotelRoomId");
        request.setCheckInDate(new Date(1677628800000L));
        request.setCheckOutDate(new Date(1677715200000L));

        when(dateConfiguration.newDate()).thenReturn(new Date(1677024000000L));
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The number of guests must be greater than zero");

        // Act
        ResponseEntity<Map<String, String>> response = bookingService.createBooking(authenticatedUserId, request);

        // Assert
        verify(dateConfiguration).newDate();
        verify(userUtils).getErrorsAsString(List.of("The number of guests must be greater than zero"));
        verifyNoMoreInteractions(userUtils, dateConfiguration);
        verifyNoInteractions(hotelDao, hotelRoomDao, uuidProvider, bookingDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The number of guests must be greater than zero"));
    }

    @Test
    void shouldReturnBadRequest_whenCheckInDateIsNull() {
        // Arrange
        String authenticatedUserId = "userId";
        CreateBookingRequest request = new CreateBookingRequest();
        request.setGuests(3);
        request.setHotelId("hotelId");
        request.setHotelRoomId("hotelRoomId");
        request.setCheckInDate(null);
        request.setCheckOutDate(new Date(1677715200000L));

        when(dateConfiguration.newDate()).thenReturn(new Date(1677024000000L));
        when(userUtils.getErrorsAsString(anyList())).thenReturn("Check-in date is required");

        // Act
        ResponseEntity<Map<String, String>> response = bookingService.createBooking(authenticatedUserId, request);

        // Assert
        verify(dateConfiguration).newDate();
        verify(userUtils).getErrorsAsString(List.of("Check-in date is required"));
        verifyNoMoreInteractions(userUtils, dateConfiguration);
        verifyNoInteractions(hotelDao, hotelRoomDao, uuidProvider, bookingDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Check-in date is required"));
    }

    @Test
    void shouldReturnBadRequest_whenCheckInDateIsBeforeToday() {
        // Arrange
        String authenticatedUserId = "userId";
        CreateBookingRequest request = new CreateBookingRequest();
        request.setGuests(3);
        request.setHotelId("hotelId");
        request.setHotelRoomId("hotelRoomId");
        request.setCheckInDate(new Date(1677024000000L));
        request.setCheckOutDate(new Date(1677715200000L));

        when(dateConfiguration.newDate()).thenReturn(new Date(1677715200000L));
        when(userUtils.getErrorsAsString(anyList())).thenReturn("Check-in date must be after today's date");

        // Act
        ResponseEntity<Map<String, String>> response = bookingService.createBooking(authenticatedUserId, request);

        // Assert
        verify(dateConfiguration).newDate();
        verify(userUtils).getErrorsAsString(List.of("Check-in date must be after today's date"));
        verifyNoMoreInteractions(userUtils, dateConfiguration);
        verifyNoInteractions(hotelDao, hotelRoomDao, uuidProvider, bookingDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Check-in date must be after today's date"));
    }

    @Test
    void shouldReturnBadRequest_whenCheckOutDateIsNull() {
        // Arrange
        String authenticatedUserId = "userId";
        CreateBookingRequest request = new CreateBookingRequest();
        request.setGuests(3);
        request.setHotelId("hotelId");
        request.setHotelRoomId("hotelRoomId");
        request.setCheckInDate(new Date(1677628800000L));
        request.setCheckOutDate(null);

        when(dateConfiguration.newDate()).thenReturn(new Date(1677024000000L));
        when(userUtils.getErrorsAsString(anyList())).thenReturn("Check-out date is required");

        // Act
        ResponseEntity<Map<String, String>> response = bookingService.createBooking(authenticatedUserId, request);

        // Assert
        verify(dateConfiguration).newDate();
        verify(userUtils).getErrorsAsString(List.of("Check-out date is required"));
        verifyNoMoreInteractions(userUtils, dateConfiguration);
        verifyNoInteractions(hotelDao, hotelRoomDao, uuidProvider, bookingDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Check-out date is required"));
    }

    @Test
    void shouldReturnBadRequest_whenCheckOutDateIsBeforeCheckInDate() {
        // Arrange
        String authenticatedUserId = "userId";
        CreateBookingRequest request = new CreateBookingRequest();
        request.setGuests(3);
        request.setHotelId("hotelId");
        request.setHotelRoomId("hotelRoomId");
        request.setCheckInDate(new Date(1677628800000L));
        request.setCheckOutDate(new Date(1677542400000L));

        when(dateConfiguration.newDate()).thenReturn(new Date(1677024000000L));
        when(userUtils.getErrorsAsString(anyList())).thenReturn("Check-out date must be after check-in date");

        // Act
        ResponseEntity<Map<String, String>> response = bookingService.createBooking(authenticatedUserId, request);

        // Assert
        verify(dateConfiguration).newDate();
        verify(userUtils).getErrorsAsString(List.of("Check-out date must be after check-in date"));
        verifyNoMoreInteractions(userUtils, dateConfiguration);
        verifyNoInteractions(hotelDao, hotelRoomDao, uuidProvider, bookingDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Check-out date must be after check-in date"));
    }

    @Test
    void shouldReturnNotFound_whenHotelNotFound() {
        // Arrange
        String authenticatedUserId = "userId";
        CreateBookingRequest request = new CreateBookingRequest();
        request.setGuests(3);
        request.setHotelId("hotelId");
        request.setHotelRoomId("hotelRoomId");
        request.setCheckInDate(new Date(1677628800000L));
        request.setCheckOutDate(new Date(1677715200000L));

        when(dateConfiguration.newDate()).thenReturn(new Date(1677024000000L));
        when(hotelDao.findById(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, String>> response = bookingService.createBooking(authenticatedUserId, request);

        // Assert
        InOrder inOrder = inOrder(dateConfiguration, hotelDao);
        inOrder.verify(dateConfiguration).newDate();
        inOrder.verify(hotelDao).findById("hotelId");
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(userUtils, hotelRoomDao, uuidProvider, bookingDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "Hotel not found"));
    }

    @Test
    void shouldReturnNotFound_whenHotelRoomNotFound() {
        // Arrange
        String authenticatedUserId = "userId";
        CreateBookingRequest request = new CreateBookingRequest();
        request.setGuests(3);
        request.setHotelId("hotelId");
        request.setHotelRoomId("hotelRoomId");
        request.setCheckInDate(new Date(1677628800000L));
        request.setCheckOutDate(new Date(1677715200000L));

        Hotel hotel = Hotel.builder().id("hotelId").rooms(emptyList()).build();

        when(dateConfiguration.newDate()).thenReturn(new Date(1677024000000L));
        when(hotelDao.findById(anyString())).thenReturn(Optional.of(hotel));
        when(hotelRoomDao.findById(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, String>> response = bookingService.createBooking(authenticatedUserId, request);

        // Assert
        InOrder inOrder = inOrder(dateConfiguration, hotelDao, hotelRoomDao);
        inOrder.verify(dateConfiguration).newDate();
        inOrder.verify(hotelDao).findById("hotelId");
        inOrder.verify(hotelRoomDao).findById("hotelRoomId");
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(userUtils, uuidProvider, bookingDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "HotelRoom not found"));
    }

    @Test
    void shouldReturnBadRequest_whenHotelRoomNotInHotel() {
        // Arrange
        String authenticatedUserId = "userId";
        CreateBookingRequest request = new CreateBookingRequest();
        request.setGuests(3);
        request.setHotelId("hotelId");
        request.setHotelRoomId("hotelRoomId1");
        request.setCheckInDate(new Date(1677628800000L));
        request.setCheckOutDate(new Date(1677715200000L));

        HotelRoom hotelRoom1 = HotelRoom.builder().id("hotelRoomId1").maxOccupancy(5).price(100.00).build();
        HotelRoom hotelRoom2 = HotelRoom.builder().id("hotelRoomId2").maxOccupancy(8).price(250.00).build();
        Hotel hotel = Hotel.builder().id("hotelId").rooms(List.of(hotelRoom2)).build();

        when(dateConfiguration.newDate()).thenReturn(new Date(1677024000000L));
        when(hotelDao.findById(anyString())).thenReturn(Optional.of(hotel));
        when(hotelRoomDao.findById(anyString())).thenReturn(Optional.of(hotelRoom1));

        // Act
        ResponseEntity<Map<String, String>> response = bookingService.createBooking(authenticatedUserId, request);

        // Assert
        InOrder inOrder = inOrder(dateConfiguration, hotelDao, hotelRoomDao);
        inOrder.verify(dateConfiguration).newDate();
        inOrder.verify(hotelDao).findById("hotelId");
        inOrder.verify(hotelRoomDao).findById("hotelRoomId1");
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(userUtils, uuidProvider, bookingDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "The specified hotel room was not found in the hotel's room list"));
    }

    @Test
    void shouldReturnBadRequest_whenNumberOfGuestsExceedsMaximumOccupancyOfHotelRoom() {
        // Arrange
        String authenticatedUserId = "userId";
        CreateBookingRequest request = new CreateBookingRequest();
        request.setGuests(52);
        request.setHotelId("hotelId");
        request.setHotelRoomId("hotelRoomId");
        request.setCheckInDate(new Date(1677628800000L));
        request.setCheckOutDate(new Date(1677715200000L));

        HotelRoom hotelRoom = HotelRoom.builder().id("hotelRoomId").maxOccupancy(5).price(100.00).build();
        Hotel hotel = Hotel.builder().id("hotelId").rooms(List.of(hotelRoom)).build();

        when(dateConfiguration.newDate()).thenReturn(new Date(1677024000000L));
        when(hotelDao.findById(anyString())).thenReturn(Optional.of(hotel));
        when(hotelRoomDao.findById(anyString())).thenReturn(Optional.of(hotelRoom));

        // Act
        ResponseEntity<Map<String, String>> response = bookingService.createBooking(authenticatedUserId, request);

        // Assert
        InOrder inOrder = inOrder(dateConfiguration, hotelDao, hotelRoomDao);
        inOrder.verify(dateConfiguration).newDate();
        inOrder.verify(hotelDao).findById("hotelId");
        inOrder.verify(hotelRoomDao).findById("hotelRoomId");
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(userUtils, uuidProvider, bookingDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "The number of guests exceeds the maximum occupancy for this hotel room"));
    }

}