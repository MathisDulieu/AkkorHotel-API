package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.HotelDao;
import com.akkorhotel.hotel.model.*;
import com.akkorhotel.hotel.model.request.GetHotelsFilters;
import com.akkorhotel.hotel.model.request.GetHotelsRequest;
import com.akkorhotel.hotel.model.response.GetAllHotelsHotelResponse;
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
    void shouldReturnHotels() {
        // Arrange
        GetHotelsRequest request = new GetHotelsRequest();
        request.setPageSize(1);
        request.setPage(0);
        request.setFilter("PRICE_LOW_TO_HIGH");
        request.setFilters(GetHotelsFilters.builder()
                .bedrooms(1)
                .guests(2)
                .city("city")
                .hotelAmenities(List.of("WIFI"))
                .fiveStars(false)
                .fourStars(true)
                .threeStars(false)
                .twoStars(true)
                .oneStar(false)
                .maxPrice(2000)
                .minPrice(150)
                .build());

        HotelRoom hotelRoom1 = HotelRoom.builder()
                .id("hotelRoomId")
                .type(HotelRoomType.DELUXE)
                .maxOccupancy(8)
                .features(List.of(HotelRoomFeatures.FLAT_SCREEN_TV, HotelRoomFeatures.SAFE))
                .price(210.00)
                .build();

        HotelRoom hotelRoom2 = HotelRoom.builder()
                .id("hotelRoomId")
                .type(HotelRoomType.DELUXE)
                .maxOccupancy(8)
                .features(List.of(HotelRoomFeatures.FLAT_SCREEN_TV, HotelRoomFeatures.SAFE))
                .price(700.00)
                .build();

        Hotel hotel1 = Hotel.builder()
                .id("hotelId1")
                .name("name1")
                .description("description1")
                .picture_list(List.of("https://picture1.jpg", "https://picture2.png"))
                .amenities(List.of(HotelAmenities.WIFI, HotelAmenities.BAR))
                .location(HotelLocation.builder().city("city").build())
                .rooms(List.of(hotelRoom1, hotelRoom2))
                .stars(4)
                .build();

        when(hotelDao.countHotelsWithRequest(any())).thenReturn(2L);
        when(hotelDao.searchHotelsByRequest(any())).thenReturn(List.of(hotel1));

        // Act
        ResponseEntity<Map<String, GetAllHotelsResponse>> response = hotelService.getHotels(request);

        // Assert
        GetAllHotelsResponse expectedResponse = GetAllHotelsResponse.builder()
                .totalPages(2)
                .hotelsFound(2)
                .error(null)
                .hotels(List.of(GetAllHotelsHotelResponse.builder()
                        .hotelId("hotelId1")
                        .stars(4)
                        .name("name1")
                        .firstPicture("https://picture1.jpg")
                        .googleMapUrl(null)
                        .price(210.00)
                        .address(null)
                        .description("description1")
                        .build()))
                .build();

        InOrder inOrder = inOrder(hotelDao);
        inOrder.verify(hotelDao).countHotelsWithRequest(request.getFilters());
        inOrder.verify(hotelDao).searchHotelsByRequest(request);
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("informations", expectedResponse));
    }

    @Test
    void shouldReturnBadRequest_whenPageNumberIsNegative() {
        // Arrange
        GetHotelsRequest request = new GetHotelsRequest();
        request.setPageSize(1);
        request.setPage(-5);
        request.setFilter("PRICE_LOW_TO_HIGH");
        request.setFilters(GetHotelsFilters.builder()
                .bedrooms(1)
                .guests(2)
                .city("city")
                .hotelAmenities(List.of("WIFI"))
                .fiveStars(false)
                .fourStars(true)
                .threeStars(false)
                .twoStars(true)
                .oneStar(false)
                .maxPrice(2000)
                .minPrice(150)
                .build());

        // Act
        ResponseEntity<Map<String, GetAllHotelsResponse>> response = hotelService.getHotels(request);

        // Assert
        GetAllHotelsResponse expectedResponse = GetAllHotelsResponse.builder()
                .totalPages(0)
                .hotelsFound(0)
                .error("Page number must be greater than or equal to zero")
                .hotels(emptyList())
                .build();

        verifyNoInteractions(hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", expectedResponse));
    }

    @Test
    void shouldReturnBadRequest_whenInvalidFilterIsProvided() {
        // Arrange
        GetHotelsRequest request = new GetHotelsRequest();
        request.setPageSize(1);
        request.setPage(0);
        request.setFilter("INVALID_FILTER");
        request.setFilters(GetHotelsFilters.builder()
                .bedrooms(1)
                .guests(2)
                .city("city")
                .hotelAmenities(List.of("WIFI"))
                .fiveStars(false)
                .fourStars(true)
                .threeStars(false)
                .twoStars(true)
                .oneStar(false)
                .maxPrice(2000)
                .minPrice(150)
                .build());

        // Act
        ResponseEntity<Map<String, GetAllHotelsResponse>> response = hotelService.getHotels(request);

        // Assert
        GetAllHotelsResponse expectedResponse = GetAllHotelsResponse.builder()
                .totalPages(0)
                .hotelsFound(0)
                .error("Invalid filter provided")
                .hotels(emptyList())
                .build();

        verifyNoInteractions(hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", expectedResponse));
    }

    @Test
    void shouldReturnBadRequest_whenPageSizeIsNegative() {
        // Arrange
        GetHotelsRequest request = new GetHotelsRequest();
        request.setPageSize(-2);
        request.setPage(0);
        request.setFilter("PRICE_LOW_TO_HIGH");
        request.setFilters(GetHotelsFilters.builder()
                .bedrooms(1)
                .guests(2)
                .city("city")
                .hotelAmenities(List.of("WIFI"))
                .fiveStars(false)
                .fourStars(true)
                .threeStars(false)
                .twoStars(true)
                .oneStar(false)
                .maxPrice(2000)
                .minPrice(150)
                .build());

        // Act
        ResponseEntity<Map<String, GetAllHotelsResponse>> response = hotelService.getHotels(request);

        // Assert
        GetAllHotelsResponse expectedResponse = GetAllHotelsResponse.builder()
                .totalPages(0)
                .hotelsFound(0)
                .error("Page size must be greater than or equal to zero")
                .hotels(emptyList())
                .build();

        verifyNoInteractions(hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", expectedResponse));
    }

    @Test
    void shouldReturnBadRequest_whenMinPriceIsNegative() {
        // Arrange
        GetHotelsRequest request = new GetHotelsRequest();
        request.setPageSize(1);
        request.setPage(0);
        request.setFilter("PRICE_LOW_TO_HIGH");
        request.setFilters(GetHotelsFilters.builder()
                .bedrooms(1)
                .guests(2)
                .city("city")
                .hotelAmenities(List.of("WIFI"))
                .fiveStars(false)
                .fourStars(true)
                .threeStars(false)
                .twoStars(true)
                .oneStar(false)
                .maxPrice(2000)
                .minPrice(-150)
                .build());

        // Act
        ResponseEntity<Map<String, GetAllHotelsResponse>> response = hotelService.getHotels(request);

        // Assert
        GetAllHotelsResponse expectedResponse = GetAllHotelsResponse.builder()
                .totalPages(0)
                .hotelsFound(0)
                .error("Minimum price must be greater than or equal to zero")
                .hotels(emptyList())
                .build();

        verifyNoInteractions(hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", expectedResponse));
    }

    @Test
    void shouldReturnBadRequest_whenMinPriceIsGreaterThanOrEqualToMaxPrice() {
        // Arrange
        GetHotelsRequest request = new GetHotelsRequest();
        request.setPageSize(1);
        request.setPage(0);
        request.setFilter("PRICE_LOW_TO_HIGH");
        request.setFilters(GetHotelsFilters.builder()
                .bedrooms(1)
                .guests(2)
                .city("city")
                .hotelAmenities(List.of("WIFI"))
                .fiveStars(false)
                .fourStars(true)
                .threeStars(false)
                .twoStars(true)
                .oneStar(false)
                .maxPrice(2000)
                .minPrice(2001)
                .build());

        // Act
        ResponseEntity<Map<String, GetAllHotelsResponse>> response = hotelService.getHotels(request);

        // Assert
        GetAllHotelsResponse expectedResponse = GetAllHotelsResponse.builder()
                .totalPages(0)
                .hotelsFound(0)
                .error("Minimum price must be less than maximum price")
                .hotels(emptyList())
                .build();

        verifyNoInteractions(hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", expectedResponse));
    }

    @Test
    void shouldReturnBadRequest_whenNumberOfGuestsIsLessThanBedrooms() {
        // Arrange
        GetHotelsRequest request = new GetHotelsRequest();
        request.setPageSize(1);
        request.setPage(0);
        request.setFilter("PRICE_LOW_TO_HIGH");
        request.setFilters(GetHotelsFilters.builder()
                .bedrooms(3)
                .guests(2)
                .city("city")
                .hotelAmenities(List.of("WIFI"))
                .fiveStars(false)
                .fourStars(true)
                .threeStars(false)
                .twoStars(true)
                .oneStar(false)
                .maxPrice(2000)
                .minPrice(150)
                .build());

        // Act
        ResponseEntity<Map<String, GetAllHotelsResponse>> response = hotelService.getHotels(request);

        // Assert
        GetAllHotelsResponse expectedResponse = GetAllHotelsResponse.builder()
                .totalPages(0)
                .hotelsFound(0)
                .error("Number of guests must be greater than or equal to the number of bedrooms")
                .hotels(emptyList())
                .build();

        verifyNoInteractions(hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", expectedResponse));
    }

    @Test
    void shouldReturnWarning_whenRequestedPageExceedsTotalPages() {
        // Arrange
        GetHotelsRequest request = new GetHotelsRequest();
        request.setPageSize(1);
        request.setPage(5);
        request.setFilter("PRICE_LOW_TO_HIGH");
        request.setFilters(GetHotelsFilters.builder()
                .bedrooms(1)
                .guests(2)
                .city("city")
                .hotelAmenities(List.of("WIFI"))
                .fiveStars(false)
                .fourStars(true)
                .threeStars(false)
                .twoStars(true)
                .oneStar(false)
                .maxPrice(2000)
                .minPrice(150)
                .build());

        when(hotelDao.countHotelsWithRequest(any())).thenReturn(3L);

        // Act
        ResponseEntity<Map<String, GetAllHotelsResponse>> response = hotelService.getHotels(request);

        // Assert
        GetAllHotelsResponse expectedResponse = GetAllHotelsResponse.builder()
                .totalPages(3)
                .hotelsFound(3)
                .error("Requested page exceeds the total number of available pages")
                .hotels(emptyList())
                .build();

        verify(hotelDao).countHotelsWithRequest(request.getFilters());
        verifyNoMoreInteractions(hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("warning", expectedResponse));
    }

}