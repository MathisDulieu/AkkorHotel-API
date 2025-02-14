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
                .name("name")
                .description("description")
                .build();

        // Assert
        assertThat(hotel.getLocation()).isNotNull();
        assertThat(hotel.getLocation().getId()).isNull();
        assertThat(hotel.getLocation().getAddress()).isNull();
        assertThat(hotel.getLocation().getCity()).isNull();
        assertThat(hotel.getLocation().getState()).isNull();
        assertThat(hotel.getLocation().getCountry()).isNull();
        assertThat(hotel.getLocation().getPostalCode()).isNull();
        assertThat(hotel.getLocation().getGoogleMapsUrl()).isNull();

        assertThat(hotel.getId()).isNull();
        assertThat(hotel.getPicture_list()).isEqualTo(emptyList());
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
                .picture_list(List.of("picture1", "picture2"))
                .location(HotelLocation.builder()
                        .id("id")
                        .address("address")
                        .city("city")
                        .state("state")
                        .country("country")
                        .postalCode("postalCode")
                        .googleMapsUrl("googleMapUrl")
                        .build())
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
    }

}