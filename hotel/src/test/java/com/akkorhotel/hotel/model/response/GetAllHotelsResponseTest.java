package com.akkorhotel.hotel.model.response;

import com.akkorhotel.hotel.model.Hotel;
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
    }

    @Test
    void shouldOverrideGetAllHotelsResponseDefaultValuesWhenSpecified() {
        // Arrange
        GetAllHotelsResponse getAllHotelsResponse = GetAllHotelsResponse.builder()
                .hotels(List.of(
                        Hotel.builder().id("id1").build(),
                        Hotel.builder().id("id2").build()
                ))
                .totalPages(10)
                .error("ErrorMessage")
                .build();

        // Assert
        assertThat(getAllHotelsResponse.getError()).isEqualTo("ErrorMessage");
        assertThat(getAllHotelsResponse.getHotels()).isEqualTo(List.of(
                Hotel.builder().id("id1").build(),
                Hotel.builder().id("id2").build()
        ));
        assertThat(getAllHotelsResponse.getTotalPages()).isEqualTo(10);
    }

}