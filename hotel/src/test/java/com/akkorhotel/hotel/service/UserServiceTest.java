package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.UserDao;
import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.UserRole;
import com.akkorhotel.hotel.model.response.GetAuthenticatedUserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserDao userDao;

    @Test
    void shouldReturnAuthenticatedUserInformations() {
        // Arrange
        User user = User.builder()
                .id("id")
                .username("username")
                .email("email")
                .password("password")
                .role(UserRole.USER)
                .isValidEmail(true)
                .build();

        // Act
        ResponseEntity<Map<String, GetAuthenticatedUserResponse>> response = userService.getAuthenticatedUser(user);

        // Assert
        GetAuthenticatedUserResponse expectedResponse = GetAuthenticatedUserResponse.builder()
                .username("username")
                .email("email")
                .userRole("USER")
                .build();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("informations", expectedResponse));
    }
}