package com.akkorhotel.hotel.model.response;

import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class GetAllHotelsResponseTest {

    @Test
    void shouldBuildGetAllHotelsResponseWithDefaultValues() {
        // Arrange
        GetAllHotelsResponse getAllHotelsResponse = GetAllHotelsResponse.builder().build();

        // Assert
        assertThat(getAllHotelsResponse.getError()).isNull();
        assertThat(getAllHotelsResponse.getHotels()).isEqualTo(emptyList());
        assertThat(getAllHotelsResponse.getTotalPages()).isEqualTo(0);
        assertThat(getAllHotelsResponse.getHotelsFound()).isEqualTo(0);
    }

    @Test
    void shouldOverrideGetAllHotelsResponseDefaultValuesWhenSpecified() {
        // Arrange
        List<GetAllHotelsHotelResponse> hotelsResponse = List.of(
                GetAllHotelsHotelResponse.builder().hotelId("hotelId1").build(),
                GetAllHotelsHotelResponse.builder().hotelId("hotelId2").build()
        );

        GetAllHotelsResponse getAllHotelsResponse = GetAllHotelsResponse.builder()
                .totalPages(10)
                .error("ErrorMessage")
                .hotels(hotelsResponse)
                .hotelsFound(5L)
                .build();

        // Assert
        assertThat(getAllHotelsResponse.getError()).isEqualTo("ErrorMessage");
        assertThat(getAllHotelsResponse.getHotels()).isEqualTo(List.of(
                GetAllHotelsHotelResponse.builder().hotelId("hotelId1").build(),
                GetAllHotelsHotelResponse.builder().hotelId("hotelId2").build()
        ));
        assertThat(getAllHotelsResponse.getTotalPages()).isEqualTo(10);
        assertThat(getAllHotelsResponse.getHotelsFound()).isEqualTo(5L);
    }

}