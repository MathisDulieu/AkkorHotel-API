package com.akkorhotel.hotel.controller;

import com.akkorhotel.hotel.configuration.EnvConfiguration;
import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.UserRole;
import com.akkorhotel.hotel.model.request.ConfirmEmailRequest;
import com.akkorhotel.hotel.model.request.CreateUserRequest;
import com.akkorhotel.hotel.model.request.LoginRequest;
import com.akkorhotel.hotel.model.request.ResendConfirmationEmailRequest;
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

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Mock
    private AuthenticationService authenticationService;

    private final EnvConfiguration envConfiguration = new EnvConfiguration();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController).build();
    }

    @Test
    void shouldCreateNewUser() throws Exception {
        // Arrange
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("TestUsername");
        registerRequest.setEmail("TestEmail");
        registerRequest.setPassword("TestPassword");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        when(authenticationService.register(any(User.class))).thenReturn(ResponseEntity.ok(singletonMap("message", "User created successfully")));

        // Act
        mockMvc.perform(post("/auth/register")
                        .content(new ObjectMapper().writeValueAsString(registerRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User created successfully"));

        // Assert
        verify(authenticationService).register(captor.capture());

        User capturedRegisterRequest = captor.getValue();
        assertThat(capturedRegisterRequest.getUsername()).isEqualTo("TestUsername");
        assertThat(capturedRegisterRequest.getEmail()).isEqualTo("TestEmail");
        assertThat(capturedRegisterRequest.getPassword()).isEqualTo("TestPassword");
        assertThat(capturedRegisterRequest.getRole()).isEqualTo(UserRole.USER);
        assertThat(capturedRegisterRequest.getId()).isNull();
        assertThat(capturedRegisterRequest.getProfileImageUrl()).isEqualTo(envConfiguration.getDefaultUserProfileImage());
    }

    @Test
    void shouldLoginUser() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("userEmail");
        loginRequest.setPassword("userPassword");

        ArgumentCaptor<LoginRequest> captor = ArgumentCaptor.forClass(LoginRequest.class);

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(ResponseEntity.ok(singletonMap("token", "jwtToken")));

        // Act
        mockMvc.perform(post("/auth/login")
                        .content(new ObjectMapper().writeValueAsString(loginRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwtToken"));

        // Assert
        verify(authenticationService).login(captor.capture());

        LoginRequest capturedLoginRequest = captor.getValue();
        assertThat(capturedLoginRequest.getEmail()).isEqualTo("userEmail");
        assertThat(capturedLoginRequest.getPassword()).isEqualTo("userPassword");
    }

    @Test
    void shouldConfirmUserEmail() throws Exception {
        // Arrange
        ConfirmEmailRequest confirmEmailRequest = new ConfirmEmailRequest();
        confirmEmailRequest.setToken("token");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        when(authenticationService.confirmEmail(any(String.class))).thenReturn(ResponseEntity.ok(singletonMap("message", "Email successfully validated")));

        // Act
        mockMvc.perform(post("/auth/confirm-email")
                        .content(new ObjectMapper().writeValueAsString(confirmEmailRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email successfully validated"));

        // Assert
        verify(authenticationService).confirmEmail(captor.capture());

        String capturedConfirmationUserEmailRequest = captor.getValue();
        assertThat(capturedConfirmationUserEmailRequest).isEqualTo("token");
    }

    @Test
    void shouldSendConfirmationEmail() throws Exception {
        // Arrange
        ResendConfirmationEmailRequest resendConfirmationEmailRequest = new ResendConfirmationEmailRequest();
        resendConfirmationEmailRequest.setEmail("email");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        when(authenticationService.resendConfirmationEmail(any(String.class))).thenReturn(ResponseEntity.ok(singletonMap("message", "Confirmation email successfully sent")));

        // Act
        mockMvc.perform(post("/auth/resend-confirmation-email")
                        .content(new ObjectMapper().writeValueAsString(resendConfirmationEmailRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Confirmation email successfully sent"));

        // Assert
        verify(authenticationService).resendConfirmationEmail(captor.capture());

        String capturedConfirmationEmailRequest = captor.getValue();
        assertThat(capturedConfirmationEmailRequest).isEqualTo("email");
    }

}