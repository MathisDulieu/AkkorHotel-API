package com.akkorhotel.hotel.controller;

import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.UserRole;
import com.akkorhotel.hotel.model.response.GetAuthenticatedUserResponse;
import com.akkorhotel.hotel.service.JwtAuthenticationService;
import com.akkorhotel.hotel.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private JwtAuthenticationService jwtAuthenticationService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void shouldReturnAuthenticatedUserInformations() throws Exception {
        // Arrange
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        GetAuthenticatedUserResponse authenticatedUserResponse = GetAuthenticatedUserResponse.builder()
                .username("username")
                .email("email")
                .userRole("USER")
                .build();

        User authenticatedUser = User.builder()
                .id("id")
                .username("username")
                .email("email")
                .password("password")
                .role(UserRole.USER)
                .isValidEmail(true)
                .build();

        when(userService.getAuthenticatedUser(any(User.class))).thenReturn(ResponseEntity.ok(singletonMap("informations", authenticatedUserResponse)));
        when(jwtAuthenticationService.getAuthenticatedUser()).thenReturn(authenticatedUser);

        // Act
        mockMvc.perform(get("/private/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.informations.username").value("username"))
                .andExpect(jsonPath("$.informations.email").value("email"))
                .andExpect(jsonPath("$.informations.userRole").value("USER"));

        // Assert
        verify(userService).getAuthenticatedUser(captor.capture());

        User capturedRegisterRequest = captor.getValue();
        assertThat(capturedRegisterRequest.getId()).isEqualTo("id");
        assertThat(capturedRegisterRequest.getUsername()).isEqualTo("username");
        assertThat(capturedRegisterRequest.getEmail()).isEqualTo("email");
        assertThat(capturedRegisterRequest.getPassword()).isEqualTo("password");
        assertThat(capturedRegisterRequest.getRole()).isEqualTo(UserRole.USER);
        assertThat(capturedRegisterRequest.getIsValidEmail()).isTrue();
    }

}
