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
import com.akkorhotel.hotel.model.response.GetBookingsResponse;
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

    @Test
    void shouldReturnBooking() {
        // Arrange
        String authenticatedUserId = "userId";
        String bookingId = "bookingId";

        HotelRoom hotelRoom = HotelRoom.builder().id("hotelRoomId").maxOccupancy(5).price(100.00).build();
        Hotel hotel = Hotel.builder().id("hotelId").rooms(List.of(hotelRoom)).build();

        Booking booking = Booking.builder()
                .id("bookingId")
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

        when(bookingDao.findById(anyString())).thenReturn(Optional.of(booking));

        // Act
        ResponseEntity<Map<String, GetBookingResponse>> response = bookingService.getBooking(authenticatedUserId, bookingId);

        // Assert
        GetBookingResponse expectedResponse = GetBookingResponse.builder()
                .booking(booking)
                .build();

        verify(bookingDao).findById(bookingId);
        verifyNoMoreInteractions(bookingDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("informations", expectedResponse));
    }

    @Test
    void shouldReturnNotFound_whenBookingDoesNotExist() {
        // Arrange
        String authenticatedUserId = "userId";
        String bookingId = "bookingId";

        when(bookingDao.findById(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, GetBookingResponse>> response = bookingService.getBooking(authenticatedUserId, bookingId);

        // Assert
        GetBookingResponse expectedResponse = GetBookingResponse.builder()
                .error("Booking not found")
                .build();

        verify(bookingDao).findById(bookingId);
        verifyNoMoreInteractions(bookingDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", expectedResponse));
    }

    @Test
    void shouldReturnForbidden_whenUserIsNotOwnerOfBooking() {
        // Arrange
        String authenticatedUserId = "userId";
        String bookingId = "bookingId";

        HotelRoom hotelRoom = HotelRoom.builder().id("hotelRoomId").maxOccupancy(5).price(100.00).build();
        Hotel hotel = Hotel.builder().id("hotelId").rooms(List.of(hotelRoom)).build();

        Booking booking = Booking.builder()
                .id("bookingId")
                .userId("otherUserId")
                .hotel(hotel)
                .hotelRoom(hotelRoom)
                .checkInDate(new Date(1677628800000L))
                .checkOutDate(new Date(1677715200000L))
                .guests(3)
                .totalPrice(100.00)
                .isPaid(false)
                .status(BookingStatus.PENDING)
                .build();

        when(bookingDao.findById(anyString())).thenReturn(Optional.of(booking));

        // Act
        ResponseEntity<Map<String, GetBookingResponse>> response = bookingService.getBooking(authenticatedUserId, bookingId);

        // Assert
        GetBookingResponse expectedResponse = GetBookingResponse.builder()
                .error("You do not have permission to access this booking")
                .build();

        verify(bookingDao).findById(bookingId);
        verifyNoMoreInteractions(bookingDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", expectedResponse));
    }

    @Test
    void shouldUpdateBooking() {
        // Arrange
        String authenticatedUserId = "userId";
        UpdateBookingRequest request = new UpdateBookingRequest();
        request.setBookingId("bookingId");
        request.setGuests(3);
        request.setCheckInDate(new Date(1704067200000L));
        request.setCheckOutDate(new Date(1704499200000L));

        HotelRoom hotelRoom = HotelRoom.builder().maxOccupancy(5).price(50.0).build();

        Booking booking = Booking.builder()
                .id("bookingId")
                .checkInDate(new Date(1705276800000L))
                .checkOutDate(new Date(1705612800000L))
                .userId("userId")
                .hotelRoom(hotelRoom)
                .status(BookingStatus.CONFIRMED)
                .isPaid(true)
                .totalPrice(200.0)
                .build();

        when(bookingDao.findById(anyString())).thenReturn(Optional.of(booking));
        when(dateConfiguration.newDate()).thenReturn(new Date(1677024000000L));

        // Act
        ResponseEntity<Map<String, String>> response = bookingService.updateBooking(authenticatedUserId, request);

        // Assert
        Booking expectedBooking = Booking.builder()
                .id("bookingId")
                .checkInDate(new Date(1704067200000L))
                .checkOutDate(new Date(1704499200000L))
                .status(BookingStatus.PENDING)
                .isPaid(false)
                .userId("userId")
                .hotelRoom(hotelRoom)
                .totalPrice(250.0)
                .guests(3)
                .build();

        InOrder inOrder = inOrder(bookingDao, dateConfiguration);
        inOrder.verify(bookingDao).findById("bookingId");
        inOrder.verify(dateConfiguration).newDate();
        inOrder.verify(bookingDao).save(expectedBooking);
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(userUtils);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("message", "Booking updated successfully"));
    }

    @Test
    void shouldReturnBadRequest_whenBookingIdIsNull() {
        // Arrange
        String authenticatedUserId = "userId";
        UpdateBookingRequest request = new UpdateBookingRequest();
        request.setBookingId(null);
        request.setGuests(3);
        request.setCheckInDate(new Date(1704067200000L));
        request.setCheckOutDate(new Date(1704499200000L));

        // Act
        ResponseEntity<Map<String, String>> response = bookingService.updateBooking(authenticatedUserId, request);

        // Assert
        verifyNoInteractions(userUtils, bookingDao, dateConfiguration);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "BookingId is required"));
    }

    @Test
    void shouldReturnNotFound_whenBookingDoesNotExistInDatabase() {
        // Arrange
        String authenticatedUserId = "userId";
        UpdateBookingRequest request = new UpdateBookingRequest();
        request.setBookingId("bookingId");
        request.setGuests(3);
        request.setCheckInDate(new Date(1704067200000L));
        request.setCheckOutDate(new Date(1704499200000L));

        when(bookingDao.findById(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, String>> response = bookingService.updateBooking(authenticatedUserId, request);

        // Assert
        verify(bookingDao).findById("bookingId");
        verifyNoMoreInteractions(bookingDao);
        verifyNoInteractions(userUtils, dateConfiguration);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "Booking not found"));
    }

    @Test
    void shouldReturnForbiddenError_whenUserIsNotOwnerOfBooking() {
        // Arrange
        String authenticatedUserId = "userId";
        UpdateBookingRequest request = new UpdateBookingRequest();
        request.setBookingId("bookingId");
        request.setGuests(3);
        request.setCheckInDate(new Date(1704067200000L));
        request.setCheckOutDate(new Date(1704499200000L));

        HotelRoom hotelRoom = HotelRoom.builder().maxOccupancy(5).price(50.0).build();

        Booking booking = Booking.builder()
                .id("bookingId")
                .checkInDate(new Date(1705276800000L))
                .checkOutDate(new Date(1705612800000L))
                .userId("notUserId")
                .hotelRoom(hotelRoom)
                .status(BookingStatus.CONFIRMED)
                .isPaid(true)
                .totalPrice(200.0)
                .build();

        when(bookingDao.findById(anyString())).thenReturn(Optional.of(booking));

        // Act
        ResponseEntity<Map<String, String>> response = bookingService.updateBooking(authenticatedUserId, request);

        // Assert
        verify(bookingDao).findById("bookingId");
        verifyNoMoreInteractions(bookingDao);
        verifyNoInteractions(userUtils, dateConfiguration);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "You do not have permission to access this booking"));
    }

    @Test
    void shouldReturnBadRequest_whenRequestValidationFails() {
        // Arrange
        String authenticatedUserId = "userId";
        UpdateBookingRequest request = new UpdateBookingRequest();
        request.setBookingId("bookingId");

        HotelRoom hotelRoom = HotelRoom.builder().maxOccupancy(5).price(50.0).build();

        Booking booking = Booking.builder()
                .id("bookingId")
                .checkInDate(new Date(1705276800000L))
                .checkOutDate(new Date(1705612800000L))
                .userId("userId")
                .hotelRoom(hotelRoom)
                .status(BookingStatus.CONFIRMED)
                .isPaid(true)
                .totalPrice(200.0)
                .build();

        when(bookingDao.findById(anyString())).thenReturn(Optional.of(booking));
        when(userUtils.getErrorsAsString(anyList())).thenReturn("At least one field must be provided for the update");

        // Act
        ResponseEntity<Map<String, String>> response = bookingService.updateBooking(authenticatedUserId, request);

        // Assert
        InOrder inOrder = inOrder(bookingDao, userUtils);
        inOrder.verify(bookingDao).findById("bookingId");
        inOrder.verify(userUtils).getErrorsAsString(List.of("At least one field must be provided for the update"));
        inOrder.verifyNoMoreInteractions();

        verifyNoMoreInteractions(bookingDao, userUtils);
        verifyNoInteractions(dateConfiguration);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "At least one field must be provided for the update"));
    }

    @Test
    void shouldReturnBadRequest_whenGuestsExceedMaxOccupancy() {
        // Arrange
        String authenticatedUserId = "userId";
        UpdateBookingRequest request = new UpdateBookingRequest();
        request.setBookingId("bookingId");
        request.setGuests(5);
        request.setCheckInDate(new Date(1704067200000L));
        request.setCheckOutDate(new Date(1704499200000L));

        HotelRoom hotelRoom = HotelRoom.builder().maxOccupancy(4).price(50.0).build();

        Booking booking = Booking.builder()
                .id("bookingId")
                .checkInDate(new Date(1705276800000L))
                .checkOutDate(new Date(1705612800000L))
                .userId("userId")
                .hotelRoom(hotelRoom)
                .status(BookingStatus.CONFIRMED)
                .isPaid(true)
                .totalPrice(200.0)
                .build();

        when(bookingDao.findById(anyString())).thenReturn(Optional.of(booking));
        when(dateConfiguration.newDate()).thenReturn(new Date(1677024000000L));

        // Act
        ResponseEntity<Map<String, String>> response = bookingService.updateBooking(authenticatedUserId, request);

        // Assert
        InOrder inOrder = inOrder(bookingDao, dateConfiguration);
        inOrder.verify(bookingDao).findById("bookingId");
        inOrder.verify(dateConfiguration).newDate();
        inOrder.verifyNoMoreInteractions();

        verifyNoMoreInteractions(bookingDao);
        verifyNoInteractions(userUtils);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "The number of guests exceeds the maximum occupancy for this hotel room"));
    }

    @Test
    void shouldDeleteBooking() {
        // Arrange
        String authenticatedUserId = "userId";
        String bookingId = "bookingId";

        Booking booking = Booking.builder()
                .id("bookingId")
                .checkInDate(new Date(1705276800000L))
                .checkOutDate(new Date(1705612800000L))
                .userId("userId")
                .hotelRoom(null)
                .status(BookingStatus.CONFIRMED)
                .isPaid(true)
                .totalPrice(200.0)
                .build();

        when(bookingDao.findById(anyString())).thenReturn(Optional.of(booking));

        // Act
        ResponseEntity<Map<String, String>> response = bookingService.deleteBooking(authenticatedUserId, bookingId);

        // Assert
        InOrder inOrder = inOrder(bookingDao);
        inOrder.verify(bookingDao).findById("bookingId");
        inOrder.verify(bookingDao).delete("bookingId");
        inOrder.verifyNoMoreInteractions();

        verifyNoMoreInteractions(bookingDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("message", "Booking deleted successfully"));
    }

    @Test
    void shouldReturnBadRequest_whenBookingIdToDeleteIsNull() {
        // Arrange
        String authenticatedUserId = "userId";

        // Act
        ResponseEntity<Map<String, String>> response = bookingService.deleteBooking(authenticatedUserId, null);

        // Assert
        verifyNoInteractions(bookingDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "BookingId is required"));
    }

    @Test
    void shouldReturnNotFound_whenBookingToDeleteDoesNotExist() {
        // Arrange
        String authenticatedUserId = "userId";
        String bookingId = "bookingId";

        when(bookingDao.findById(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, String>> response = bookingService.deleteBooking(authenticatedUserId, bookingId);

        // Assert
        verify(bookingDao).findById("bookingId");
        verifyNoMoreInteractions(bookingDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "Booking not found"));
    }

    @Test
    void shouldReturnForbidden_whenUserIsNotOwnerOfBookingToDelete() {
        // Arrange
        String authenticatedUserId = "userId";
        String bookingId = "bookingId";

        Booking booking = Booking.builder()
                .id("bookingId")
                .checkInDate(new Date(1705276800000L))
                .checkOutDate(new Date(1705612800000L))
                .userId("not_userId")
                .hotelRoom(null)
                .status(BookingStatus.CONFIRMED)
                .isPaid(true)
                .totalPrice(200.0)
                .build();

        when(bookingDao.findById(anyString())).thenReturn(Optional.of(booking));

        // Act
        ResponseEntity<Map<String, String>> response = bookingService.deleteBooking(authenticatedUserId, bookingId);

        // Assert
        verify(bookingDao).findById("bookingId");
        verifyNoMoreInteractions(bookingDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "You are not allowed to delete this booking"));
    }

    @Test
    void shouldReturnBookingsWithMatchingUserId() {
        // Arrange
        String authenticatedUserId = "userId";

        Booking booking1 = Booking.builder()
                .id("bookingId1")
                .checkInDate(new Date(1705276800000L))
                .checkOutDate(new Date(1705612800000L))
                .userId("userId")
                .hotelRoom(null)
                .status(BookingStatus.CONFIRMED)
                .isPaid(true)
                .totalPrice(200.0)
                .build();

        Booking booking2 = Booking.builder()
                .id("bookingId2")
                .checkInDate(new Date(1705276800000L))
                .checkOutDate(new Date(1705612800000L))
                .userId("userId")
                .hotelRoom(null)
                .status(BookingStatus.CONFIRMED)
                .isPaid(true)
                .totalPrice(200.0)
                .build();

        when(bookingDao.getBookings(anyString())).thenReturn(List.of(booking1, booking2));

        // Act
        ResponseEntity<Map<String, GetBookingsResponse>> response = bookingService.getBookings(authenticatedUserId);

        // Assert
        GetBookingsResponse expectedResponse = GetBookingsResponse.builder()
                .bookings(List.of(booking1, booking2))
                .build();

        verify(bookingDao).getBookings("userId");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("informations", expectedResponse));
    }

}