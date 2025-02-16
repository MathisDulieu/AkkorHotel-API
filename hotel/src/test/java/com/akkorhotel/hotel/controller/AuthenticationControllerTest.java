package com.akkorhotel.hotel.controller;

import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.UserRole;
import com.akkorhotel.hotel.model.request.CreateUserRequest;
import com.akkorhotel.hotel.model.request.LoginRequest;
import com.akkorhotel.hotel.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Mock
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController).build();
    }

    @Test
    void shouldCreateNewUser() throws Exception {
        // Arrange
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("TestUsername");
        createUserRequest.setEmail("TestEmail");
        createUserRequest.setPassword("TestPassword");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(authenticationService.register(any(User.class))).thenReturn(ResponseEntity.ok("User created successfully"));

        // Act
        mockMvc.perform(post("/auth/register")
                        .content(new ObjectMapper().writeValueAsString(createUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User created successfully"));

        // Assert
        verify(authenticationService).register(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername()).isEqualTo("TestUsername");
        assertThat(capturedUser.getEmail()).isEqualTo("TestEmail");
        assertThat(capturedUser.getPassword()).isEqualTo("TestPassword");
        assertThat(capturedUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(capturedUser.getId()).isNull();
    }

    @Test
    void shouldLoginUser() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("userEmail");
        loginRequest.setPassword("userPassword");

        ArgumentCaptor<LoginRequest> userCaptor = ArgumentCaptor.forClass(LoginRequest.class);

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(ResponseEntity.ok("jwtToken"));

        // Act
        mockMvc.perform(post("/auth/login")
                        .content(new ObjectMapper().writeValueAsString(loginRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("jwtToken"));

        // Assert
        verify(authenticationService).login(userCaptor.capture());

        LoginRequest capturedLoginRequest = userCaptor.getValue();
        assertThat(capturedLoginRequest.getEmail()).isEqualTo("userEmail");
        assertThat(capturedLoginRequest.getPassword()).isEqualTo("userPassword");
    }

    @Test
    void shouldConfirmUserEmail() throws Exception {
        // Arrange
        String token = "token";

        ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);

        when(authenticationService.confirmEmail(any(String.class))).thenReturn(ResponseEntity.ok("Email successfully validated"));

        // Act
        mockMvc.perform(post("/auth/confirm-email")
                        .content(token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Email successfully validated"));

        // Assert
        verify(authenticationService).confirmEmail(userCaptor.capture());

        String capturedLoginRequest = userCaptor.getValue();
        assertThat(capturedLoginRequest).isEqualTo("token");
    }

}