package com.akkorhotel.hotel.controller;

import com.akkorhotel.hotel.model.Booking;
import com.akkorhotel.hotel.model.request.CreateBookingRequest;
import com.akkorhotel.hotel.model.request.UpdateBookingRequest;
import com.akkorhotel.hotel.model.response.GetBookingResponse;
import com.akkorhotel.hotel.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.text.SimpleDateFormat;
import java.util.Date;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private BookingController bookingController;

    @Mock
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();
    }

    @Test
    void shouldCreateBookingSuccessfully() throws Exception {
        // Arrange
        ArgumentCaptor<CreateBookingRequest> bookingCaptor = ArgumentCaptor.forClass(CreateBookingRequest.class);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        Date checkInDate = sdf.parse("2025-03-10T14:00:00");
        Date checkOutDate = sdf.parse("2025-03-15T12:00:00");

        CreateBookingRequest request = new CreateBookingRequest();
        request.setHotelId("hotelId");
        request.setHotelRoomId("hotelRoomId");
        request.setGuests(3);
        request.setCheckInDate(checkInDate);
        request.setCheckOutDate(checkOutDate);

        when(bookingService.createBooking(any(), any()))
                .thenReturn(ResponseEntity.ok(singletonMap("message", "Booking created successfully")));

        // Act & Assert
        mockMvc.perform(post("/private/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "hotelId": "hotelId",
                                "hotelRoomId": "hotelRoomId",
                                "guests": 3,
                                "checkInDate": "2025-03-10T14:00:00",
                                "checkOutDate": "2025-03-15T12:00:00"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Booking created successfully"));

        verify(bookingService).createBooking(any(), bookingCaptor.capture());
        assertThat(bookingCaptor.getValue().getHotelId()).isEqualTo("hotelId");
        assertThat(bookingCaptor.getValue().getHotelRoomId()).isEqualTo("hotelRoomId");
        assertThat(bookingCaptor.getValue().getGuests()).isEqualTo(3);
        assertThat(bookingCaptor.getValue().getCheckInDate()).isEqualTo("2025-03-10T15:00:00");
        assertThat(bookingCaptor.getValue().getCheckOutDate()).isEqualTo("2025-03-15T13:00:00");
    }

    @Test
    void shouldReturnBooking() throws Exception {
        // Arrange
        String bookingId = "bookingId";

        Booking booking = Booking.builder()
                .id("bookingId")
                .build();

        GetBookingResponse response = GetBookingResponse.builder()
                .booking(booking)
                .build();

        when(bookingService.getBooking(any(), any()))
                .thenReturn(ResponseEntity.ok(singletonMap("informations", response)));

        // Act & Assert
        mockMvc.perform(get("/private/booking/{bookingId}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.informations.booking.id").value(bookingId));
    }

    @Test
    void shouldUpdateBooking() throws Exception {
        // Arrange
        ArgumentCaptor<UpdateBookingRequest> bookingCaptor = ArgumentCaptor.forClass(UpdateBookingRequest.class);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        Date checkInDate = sdf.parse("2025-04-01T14:00:00");
        Date checkOutDate = sdf.parse("2025-04-05T12:00:00");

        UpdateBookingRequest request = new UpdateBookingRequest();
        request.setBookingId("bookingId");
        request.setGuests(2);
        request.setCheckInDate(checkInDate);
        request.setCheckOutDate(checkOutDate);

        when(bookingService.updateBooking(any(), any()))
                .thenReturn(ResponseEntity.ok(singletonMap("message", "Booking updated successfully")));

        // Act & Assert
        mockMvc.perform(put("/private/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "bookingId": "bookingId",
                            "guests": 2,
                            "checkInDate": "2025-04-01T14:00:00",
                            "checkOutDate": "2025-04-05T12:00:00"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Booking updated successfully"));

        verify(bookingService).updateBooking(any(), bookingCaptor.capture());
        assertThat(bookingCaptor.getValue().getBookingId()).isEqualTo("bookingId");
        assertThat(bookingCaptor.getValue().getGuests()).isEqualTo(2);
        assertThat(bookingCaptor.getValue().getCheckInDate()).isEqualTo("2025-04-01T16:00:00");
        assertThat(bookingCaptor.getValue().getCheckOutDate()).isEqualTo("2025-04-05T14:00:00");
    }

    @Test
    void shouldDeleteBooking() throws Exception {
        // Arrange
        String bookingId = "bookingId123";

        when(bookingService.deleteBooking(any(), eq(bookingId)))
                .thenReturn(ResponseEntity.ok(singletonMap("message", "Booking deleted successfully")));

        // Act & Assert
        mockMvc.perform(delete("/private/booking/{bookingId}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Booking deleted successfully"));

        verify(bookingService).deleteBooking(any(), eq(bookingId));
    }

}