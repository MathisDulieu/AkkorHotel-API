package com.akkorhotel.hotel.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class HotelTest {

    @Test
    void shouldBuildHotelWithDefaultValues() {
        // Arrange
        Hotel hotel = Hotel.builder()
                .id("id")
                .name("name")
                .description("description")
                .location(HotelLocation.builder().id("id").build())
                .build();

        // Assert
        assertThat(hotel.getLocation()).isNotNull();
        assertThat(hotel.getLocation().getId()).isEqualTo("id");

        assertThat(hotel.getPicture_list()).isEqualTo(emptyList());
        assertThat(hotel.getAmenities()).isEqualTo(emptyList());
        assertThat(hotel.getRooms()).isEqualTo(emptyList());

        assertThat(hotel.getId()).isEqualTo("id");
        assertThat(hotel.getName()).isEqualTo("name");
        assertThat(hotel.getDescription()).isEqualTo("description");
    }

    @Test
    void shouldOverrideHotelDefaultValuesWhenSpecified() {
        // Arrange
        Hotel hotel = Hotel.builder()
                .id("id")
                .name("name")
                .description("description")
                .location(HotelLocation.builder()
                        .id("id")
                        .address("address")
                        .city("city")
                        .state("state")
                        .country("country")
                        .postalCode("postalCode")
                        .googleMapsUrl("googleMapUrl")
                        .build())
                .picture_list(List.of("picture1", "picture2"))
                .amenities(List.of(HotelAmenities.AIRPORT_SHUTTLE, HotelAmenities.BAR))
                .rooms(List.of(
                        HotelRoom.builder()
                                .id("id1")
                                .maxOccupancy(5)
                                .price(256)
                                .type(HotelRoomType.DELUXE)
                                .features(List.of(HotelRoomFeatures.FLAT_SCREEN_TV, HotelRoomFeatures.SAFE))
                                .build(),
                        HotelRoom.builder()
                                .id("id2")
                                .maxOccupancy(2)
                                .price(150)
                                .type(HotelRoomType.SINGLE)
                                .features(List.of(HotelRoomFeatures.ROOM_SERVICE, HotelRoomFeatures.BALCONY))
                                .build()
                        )
                )
                .build();

        // Assert
        assertThat(hotel.getLocation()).isNotNull();
        assertThat(hotel.getLocation().getId()).isEqualTo("id");
        assertThat(hotel.getLocation().getAddress()).isEqualTo("address");
        assertThat(hotel.getLocation().getCity()).isEqualTo("city");
        assertThat(hotel.getLocation().getState()).isEqualTo("state");
        assertThat(hotel.getLocation().getCountry()).isEqualTo("country");
        assertThat(hotel.getLocation().getPostalCode()).isEqualTo("postalCode");
        assertThat(hotel.getLocation().getGoogleMapsUrl()).isEqualTo("googleMapUrl");

        assertThat(hotel.getId()).isEqualTo("id");
        assertThat(hotel.getPicture_list()).isEqualTo(List.of("picture1", "picture2"));
        assertThat(hotel.getName()).isEqualTo("name");
        assertThat(hotel.getDescription()).isEqualTo("description");
        assertThat(hotel.getAmenities()).isEqualTo(List.of(HotelAmenities.AIRPORT_SHUTTLE, HotelAmenities.BAR));
        assertThat(hotel.getRooms()).isEqualTo(List.of(
                HotelRoom.builder()
                        .id("id1")
                        .maxOccupancy(5)
                        .price(256)
                        .type(HotelRoomType.DELUXE)
                        .features(List.of(HotelRoomFeatures.FLAT_SCREEN_TV, HotelRoomFeatures.SAFE))
                        .build(),
                HotelRoom.builder()
                        .id("id2")
                        .maxOccupancy(2)
                        .price(150)
                        .type(HotelRoomType.SINGLE)
                        .features(List.of(HotelRoomFeatures.ROOM_SERVICE, HotelRoomFeatures.BALCONY))
                        .build()
        ));
    }

}