package com.akkorhotel.hotel.controller;

import com.akkorhotel.hotel.model.*;
import com.akkorhotel.hotel.model.request.GetHotelsFilters;
import com.akkorhotel.hotel.model.request.GetHotelsRequest;
import com.akkorhotel.hotel.model.response.GetAllHotelsHotelResponse;
import com.akkorhotel.hotel.model.response.GetAllHotelsResponse;
import com.akkorhotel.hotel.model.response.GetHotelResponse;
import com.akkorhotel.hotel.service.HotelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class HotelControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private HotelController hotelController;

    @Mock
    private HotelService hotelService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(hotelController).build();
    }

    @Test
    void shouldReturnHotelInformation() throws Exception {
        // Arrange
        String hotelId = "f2cccd2f-5711-4356-a13a-f687dc983ce1";

        HotelLocation hotelLocation = HotelLocation.builder()
                .id("f2cccd2f-5711-4356-a13a-f687dc983ce3")
                .address("123 Rue de la Paix")
                .city("Paris")
                .state("Île-de-France")
                .country("France")
                .postalCode("75001")
                .googleMapsUrl("https://maps.google.com/?q=LuxuryHotel")
                .build();

        HotelRoom hotelRoom = HotelRoom.builder()
                .id("f2cccd2f-5711-4356-a13a-f687dc983ce2")
                .type(HotelRoomType.SINGLE)
                .maxOccupancy(3)
                .features(List.of(HotelRoomFeatures.ROOM_SERVICE, HotelRoomFeatures.BALCONY))
                .price(120.00)
                .build();

        Hotel hotel = Hotel.builder()
                .id("f2cccd2f-5711-4356-a13a-f687dc983ce1")
                .name("LuxuryHotel")
                .description("A five-star experience.")
                .picture_list(List.of("https://picture1.jpg", "https://picture2.png"))
                .amenities(List.of(HotelAmenities.POOL, HotelAmenities.WIFI))
                .location(hotelLocation)
                .rooms(List.of(hotelRoom))
                .build();

        GetHotelResponse hotelResponse = GetHotelResponse.builder()
                .hotel(hotel)
                .build();

        when(hotelService.getHotel(anyString()))
                .thenReturn(ResponseEntity.ok(singletonMap("informations", hotelResponse)));

        // Act
        mockMvc.perform(get("/hotel/{hotelId}", hotelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.informations.error").doesNotExist())
                .andExpect(jsonPath("$.informations.hotel.id").value("f2cccd2f-5711-4356-a13a-f687dc983ce1"))
                .andExpect(jsonPath("$.informations.hotel.name").value("LuxuryHotel"))
                .andExpect(jsonPath("$.informations.hotel.description").value("A five-star experience."))
                .andExpect(jsonPath("$.informations.hotel.picture_list[0]").value("https://picture1.jpg"))
                .andExpect(jsonPath("$.informations.hotel.picture_list[1]").value("https://picture2.png"))
                .andExpect(jsonPath("$.informations.hotel.amenities[0]").value("POOL"))
                .andExpect(jsonPath("$.informations.hotel.amenities[1]").value("WIFI"))
                .andExpect(jsonPath("$.informations.hotel.rooms[0].id").value("f2cccd2f-5711-4356-a13a-f687dc983ce2"))
                .andExpect(jsonPath("$.informations.hotel.rooms[0].type").value("SINGLE"))
                .andExpect(jsonPath("$.informations.hotel.rooms[0].maxOccupancy").value(3))
                .andExpect(jsonPath("$.informations.hotel.rooms[0].features[0]").value("ROOM_SERVICE"))
                .andExpect(jsonPath("$.informations.hotel.rooms[0].features[1]").value("BALCONY"))
                .andExpect(jsonPath("$.informations.hotel.rooms[0].price").value(120.00))
                .andExpect(jsonPath("$.informations.hotel.location.id").value("f2cccd2f-5711-4356-a13a-f687dc983ce3"))
                .andExpect(jsonPath("$.informations.hotel.location.address").value("123 Rue de la Paix"))
                .andExpect(jsonPath("$.informations.hotel.location.city").value("Paris"))
                .andExpect(jsonPath("$.informations.hotel.location.state").value("Île-de-France"))
                .andExpect(jsonPath("$.informations.hotel.location.country").value("France"))
                .andExpect(jsonPath("$.informations.hotel.location.postalCode").value("75001"))
                .andExpect(jsonPath("$.informations.hotel.location.googleMapsUrl").value("https://maps.google.com/?q=LuxuryHotel"));

        // Assert
        verify(hotelService).getHotel(hotelId);
    }

    @Test
    void shouldReturnHotels() throws Exception {
        // Arrange
        GetHotelsRequest request = new GetHotelsRequest(0, 2, "Luxury",
                new GetHotelsFilters(false, false, false, false, false,
                        List.of("POOL", "WIFI"), 50, 200, 2, 1, "Paris"));

        GetAllHotelsHotelResponse hotel1 = GetAllHotelsHotelResponse.builder()
                .hotelId("hotelId1")
                .name("LuxuryHotel")
                .description("A five-star experience.")
                .firstPicture("https://mocked-image-url.com/hotel1.jpg")
                .price(150.0)
                .address("123 Rue de la Paix")
                .googleMapUrl("https://maps.google.com/?q=LuxuryHotel")
                .build();

        GetAllHotelsHotelResponse hotel2 = GetAllHotelsHotelResponse.builder()
                .hotelId("hotelId2")
                .name("LuxurySpa")
                .description("Relaxation at its finest.")
                .firstPicture("https://mocked-image-url.com/spa1.jpg")
                .price(180.0)
                .address("456 Avenue des Champs-Élysées")
                .googleMapUrl("https://maps.google.com/?q=LuxurySpa")
                .build();

        GetAllHotelsResponse hotelsResponse = GetAllHotelsResponse.builder()
                .hotelsFound(25)
                .totalPages(3)
                .hotels(List.of(hotel1, hotel2))
                .error(null)
                .build();

        when(hotelService.getHotels(any(GetHotelsRequest.class)))
                .thenReturn(ResponseEntity.ok(singletonMap("informations", hotelsResponse)));

        // Act
        mockMvc.perform(post("/hotel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.informations.hotelsFound").value(25))
                .andExpect(jsonPath("$.informations.totalPages").value(3))
                .andExpect(jsonPath("$.informations.error").doesNotExist())

                .andExpect(jsonPath("$.informations.hotels[0].hotelId").value("hotelId1"))
                .andExpect(jsonPath("$.informations.hotels[0].name").value("LuxuryHotel"))
                .andExpect(jsonPath("$.informations.hotels[0].description").value("A five-star experience."))
                .andExpect(jsonPath("$.informations.hotels[0].firstPicture").value("https://mocked-image-url.com/hotel1.jpg"))
                .andExpect(jsonPath("$.informations.hotels[0].price").value(150.0))
                .andExpect(jsonPath("$.informations.hotels[0].address").value("123 Rue de la Paix"))
                .andExpect(jsonPath("$.informations.hotels[0].googleMapUrl").value("https://maps.google.com/?q=LuxuryHotel"))

                .andExpect(jsonPath("$.informations.hotels[1].hotelId").value("hotelId2"))
                .andExpect(jsonPath("$.informations.hotels[1].name").value("LuxurySpa"))
                .andExpect(jsonPath("$.informations.hotels[1].description").value("Relaxation at its finest."))
                .andExpect(jsonPath("$.informations.hotels[1].firstPicture").value("https://mocked-image-url.com/spa1.jpg"))
                .andExpect(jsonPath("$.informations.hotels[1].price").value(180.0))
                .andExpect(jsonPath("$.informations.hotels[1].address").value("456 Avenue des Champs-Élysées"))
                .andExpect(jsonPath("$.informations.hotels[1].googleMapUrl").value("https://maps.google.com/?q=LuxurySpa"));

        // Assert
        verify(hotelService).getHotels(any(GetHotelsRequest.class));
    }

}