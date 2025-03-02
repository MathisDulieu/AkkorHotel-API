package com.akkorhotel.hotel.controller;

import com.akkorhotel.hotel.model.*;
import com.akkorhotel.hotel.model.response.GetHotelResponse;
import com.akkorhotel.hotel.service.HotelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    void shouldReturnHotelInformation_whenHotelExists() throws Exception {
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

}