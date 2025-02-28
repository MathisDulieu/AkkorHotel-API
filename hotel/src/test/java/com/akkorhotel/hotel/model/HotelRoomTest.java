package com.akkorhotel.hotel.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HotelRoomTest {

    @Test
    void shouldBuildHotelRoomWithDefaultValues() {
        // Arrange
        HotelRoom hotelRoom = HotelRoom.builder()
                .id("id")
                .type(HotelRoomType.DOUBLE)
                .price(456)
                .maxOccupancy(2)
                .build();

        // Assert
        assertThat(hotelRoom.getId()).isEqualTo("id");
        assertThat(hotelRoom.getType()).isEqualTo(HotelRoomType.DOUBLE);
        assertThat(hotelRoom.getPrice()).isEqualTo(456);
        assertThat(hotelRoom.getMaxOccupancy()).isEqualTo(2);
        assertThat(hotelRoom.getFeatures()).isEmpty();
    }

    @Test
    void shouldOverrideHotelRoomDefaultValuesWhenSpecified() {
        // Arrange
        HotelRoom hotelRoom = HotelRoom.builder()
                .id("id")
                .type(HotelRoomType.DOUBLE)
                .price(456)
                .maxOccupancy(2)
                .features(List.of(HotelRoomFeatures.FLAT_SCREEN_TV, HotelRoomFeatures.MINI_FRIDGE))
                .build();

        // Assert
        assertThat(hotelRoom.getId()).isEqualTo("id");
        assertThat(hotelRoom.getType()).isEqualTo(HotelRoomType.DOUBLE);
        assertThat(hotelRoom.getPrice()).isEqualTo(456);
        assertThat(hotelRoom.getMaxOccupancy()).isEqualTo(2);
        assertThat(hotelRoom.getFeatures()).isEqualTo(List.of(HotelRoomFeatures.FLAT_SCREEN_TV, HotelRoomFeatures.MINI_FRIDGE));
    }

}