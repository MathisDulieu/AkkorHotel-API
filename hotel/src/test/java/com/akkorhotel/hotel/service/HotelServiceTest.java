package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.HotelDao;
import com.akkorhotel.hotel.model.*;
import com.akkorhotel.hotel.model.response.GetHotelResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @InjectMocks
    private HotelService hotelService;

    @Mock
    private HotelDao hotelDao;

    @Test
    void shouldReturnHotelInformations() {
        // Arrange
        String hotelId = "hotelId";

        HotelLocation hotelLocation = HotelLocation.builder()
                .id("hotelLocationId")
                .address("address")
                .city("city")
                .state("state")
                .country("country")
                .postalCode("postalCode")
                .googleMapsUrl("https://googleMapsUrl")
                .build();

        HotelRoom hotelRoom = HotelRoom.builder()
                .id("hotelRoomId")
                .type(HotelRoomType.DELUXE)
                .maxOccupancy(8)
                .features(List.of(HotelRoomFeatures.FLAT_SCREEN_TV, HotelRoomFeatures.SAFE))
                .price(150.00)
                .build();

        Hotel hotel = Hotel.builder()
                .id("hotelId")
                .name("name")
                .description("description")
                .picture_list(List.of("https://picture1.jpg", "https://picture2.png"))
                .amenities(List.of(HotelAmenities.WIFI, HotelAmenities.BAR))
                .location(hotelLocation)
                .rooms(List.of(hotelRoom))
                .build();

        when(hotelDao.findById(hotelId)).thenReturn(Optional.of(hotel));

        // Act
        ResponseEntity<Map<String, GetHotelResponse>> response = hotelService.getHotel(hotelId);

        // Assert
        GetHotelResponse expectedResponse = GetHotelResponse.builder()
                .hotel(hotel)
                .build();


        verify(hotelDao).findById(hotelId);
        verifyNoMoreInteractions(hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("informations", expectedResponse));
    }

    @Test
    void shouldReturnNotFoundError_whenHotelDoesNotExist() {
        // Arrange
        String hotelId = "hotelId";

        when(hotelDao.findById(hotelId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, GetHotelResponse>> response = hotelService.getHotel(hotelId);

        // Assert
        GetHotelResponse expectedResponse = GetHotelResponse.builder()
                .error("Hotel not found")
                .build();

        verify(hotelDao).findById(hotelId);
        verifyNoMoreInteractions(hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", expectedResponse));
    }

}