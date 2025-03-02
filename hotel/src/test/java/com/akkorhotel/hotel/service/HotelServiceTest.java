package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.HotelDao;
import com.akkorhotel.hotel.model.*;
import com.akkorhotel.hotel.model.response.GetAllHotelsResponse;
import com.akkorhotel.hotel.model.response.GetHotelResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
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

    @Test
    void shouldReturnAllHotelsWithMatchingPrefix() {
        // Arrange
        String keyword = "name";
        int page = 0;
        int pageSize = 2;

        Hotel hotel1 = Hotel.builder()
                .id("hotelId1")
                .name("name1")
                .picture_list(List.of("picture1", "picture2"))
                .amenities(List.of(HotelAmenities.PARKING, HotelAmenities.GYM))
                .rooms(emptyList())
                .location(HotelLocation.builder()
                        .id("locationId1")
                        .address("address1")
                        .city("city1")
                        .state("state1")
                        .country("country1")
                        .postalCode("postalCode1")
                        .googleMapsUrl("googleMapsUrl1")
                        .build())
                .build();

        Hotel hotel2 = Hotel.builder()
                .id("hotelId2")
                .name("name2")
                .picture_list(List.of("picture3", "picture4"))
                .amenities(List.of(HotelAmenities.RESTAURANT, HotelAmenities.WIFI))
                .rooms(emptyList())
                .location(HotelLocation.builder()
                        .id("locationId2")
                        .address("address2")
                        .city("city2")
                        .state("state2")
                        .country("country2")
                        .postalCode("postalCode2")
                        .googleMapsUrl("googleMapsUrl2")
                        .build())
                .build();

        when(hotelDao.countHotelsByNamePrefix(anyString())).thenReturn(3L);
        when(hotelDao.searchHotelsByNamePrefix(anyString(), anyInt(), anyInt())).thenReturn(List.of(hotel1, hotel2));

        // Act
        ResponseEntity<Map<String, GetAllHotelsResponse>> response = hotelService.getHotels(keyword, page, pageSize);

        // Assert
        InOrder inOrder = inOrder(hotelDao);
        inOrder.verify(hotelDao).countHotelsByNamePrefix("name");
        inOrder.verify(hotelDao).searchHotelsByNamePrefix("name", 0, 2);
        inOrder.verifyNoMoreInteractions();

        GetAllHotelsResponse expectedResponse = GetAllHotelsResponse.builder()
                .hotels(List.of(hotel1, hotel2))
                .totalPages(2)
                .build();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("informations", expectedResponse));
    }

    @Test
    void shouldReturnBadRequest_whenPageSizeIsNegativeNumber() {
        // Arrange
        String keyword = "any";
        int page = 0;
        int pageSize = -2;

        // Act
        ResponseEntity<Map<String, GetAllHotelsResponse>> response = hotelService.getHotels(keyword, page, pageSize);

        // Assert
        verifyNoInteractions(hotelDao);

        GetAllHotelsResponse expectedResponse = GetAllHotelsResponse.builder()
                .error("Page size must be greater than or equal to zero")
                .totalPages(0)
                .build();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", expectedResponse));
    }

    @Test
    void shouldReturnBadRequest_whenPageIsNegativeNumber() {
        // Arrange
        String keyword = "any";
        int page = -5;
        int pageSize = 2;

        // Act
        ResponseEntity<Map<String, GetAllHotelsResponse>> response = hotelService.getHotels(keyword, page, pageSize);

        // Assert
        verifyNoInteractions(hotelDao);

        GetAllHotelsResponse expectedResponse = GetAllHotelsResponse.builder()
                .error("Page number must be greater than or equal to zero")
                .totalPages(0)
                .build();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", expectedResponse));
    }

    @Test
    void shouldReturnBadRequest_whenKeywordContainsSpaces() {
        // Arrange
        String keyword = " spaces ";
        int page = 5;
        int pageSize = 2;

        // Act
        ResponseEntity<Map<String, GetAllHotelsResponse>> response = hotelService.getHotels(keyword, page, pageSize);

        // Assert
        verifyNoInteractions(hotelDao);

        GetAllHotelsResponse expectedResponse = GetAllHotelsResponse.builder()
                .error("Search keyword cannot contain spaces")
                .totalPages(0)
                .build();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", expectedResponse));
    }

    @Test
    void shouldReturnOkWithErrorMessage_whenNoHotelFoundWithMatchingPrefix() {
        // Arrange
        String keyword = "notFound";
        int page = 1;
        int pageSize = 2;

        when(hotelDao.countHotelsByNamePrefix(anyString())).thenReturn(0L);

        // Act
        ResponseEntity<Map<String, GetAllHotelsResponse>> response = hotelService.getHotels(keyword, page, pageSize);

        // Assert
        verify(hotelDao).countHotelsByNamePrefix("notFound");
        verifyNoMoreInteractions(hotelDao);

        GetAllHotelsResponse expectedResponse = GetAllHotelsResponse.builder()
                .error("No hotel found")
                .totalPages(0)
                .build();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("informations", expectedResponse));
    }

    @Test
    void shouldReturnOkWithWarningMessage_whenRequestedPageExceedsTotalPages() {
        // Arrange
        String keyword = "any";
        int page = 5;
        int pageSize = 3;

        when(hotelDao.countHotelsByNamePrefix(anyString())).thenReturn(9L);

        // Act
        ResponseEntity<Map<String, GetAllHotelsResponse>> response = hotelService.getHotels(keyword, page, pageSize);

        // Assert
        verify(hotelDao).countHotelsByNamePrefix("any");
        verifyNoMoreInteractions(hotelDao);

        GetAllHotelsResponse expectedResponse = GetAllHotelsResponse.builder()
                .error("Requested page exceeds the total number of available pages")
                .totalPages(3)
                .build();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("warning", expectedResponse));
    }

}