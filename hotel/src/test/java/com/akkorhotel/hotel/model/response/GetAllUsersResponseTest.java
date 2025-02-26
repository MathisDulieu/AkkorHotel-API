package com.akkorhotel.hotel.model.response;

import com.akkorhotel.hotel.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class GetAllUsersResponseTest {

    @Test
    void shouldBuildGetAllUsersResponseWithDefaultValues() {
        // Arrange
        GetAllUsersResponse getAllUsersResponse = GetAllUsersResponse.builder().build();

        // Assert
        assertThat(getAllUsersResponse.getError()).isNull();
        assertThat(getAllUsersResponse.getUsers()).isEqualTo(emptyList());
        assertThat(getAllUsersResponse.getTotalPages()).isEqualTo(0);
    }

    @Test
    void shouldOverrideGetAllUsersResponseDefaultValuesWhenSpecified() {
        // Arrange
        GetAllUsersResponse getAllUsersResponse = GetAllUsersResponse.builder()
                .users(List.of(
                        User.builder().id("id1").build(),
                        User.builder().id("id2").build()
                ))
                .totalPages(10)
                .error("ErrorMessage")
                .build();

        // Assert
        assertThat(getAllUsersResponse.getError()).isEqualTo("ErrorMessage");
        assertThat(getAllUsersResponse.getUsers()).isEqualTo(List.of(
                User.builder().id("id1").build(),
                User.builder().id("id2").build()
        ));
        assertThat(getAllUsersResponse.getTotalPages()).isEqualTo(10);
    }

}