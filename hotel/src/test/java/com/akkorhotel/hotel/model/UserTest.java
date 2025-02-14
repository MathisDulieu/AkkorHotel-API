package com.akkorhotel.hotel.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void shouldBuildUserWithDefaultValues() {
        // Arrange
        User user = User.builder()
                .username("username")
                .email("email")
                .password("password")
                .build();

        // Assert
        assertThat(user.getIsValidEmail()).isFalse();
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(user.getId()).isNull();
        assertThat(user.getUsername()).isEqualTo("username");
        assertThat(user.getEmail()).isEqualTo("email");
        assertThat(user.getPassword()).isEqualTo("password");
    }

    @Test
    void shouldOverrideDefaultValuesWhenSpecified() {
        // Arrange
        User user = User.builder()
                .id("id")
                .isValidEmail(true)
                .role(UserRole.ADMIN)
                .username("username")
                .email("email")
                .password("password")
                .build();

        // Assert
        assertThat(user.getIsValidEmail()).isTrue();
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(user.getId()).isEqualTo("id");
        assertThat(user.getUsername()).isEqualTo("username");
        assertThat(user.getEmail()).isEqualTo("email");
        assertThat(user.getPassword()).isEqualTo("password");
    }
}
