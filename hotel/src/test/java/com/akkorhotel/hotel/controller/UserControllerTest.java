package com.akkorhotel.hotel.controller;

import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.UserRole;
import com.akkorhotel.hotel.model.request.UpdateUserRequest;
import com.akkorhotel.hotel.model.response.GetAuthenticatedUserResponse;
import com.akkorhotel.hotel.service.JwtAuthenticationService;
import com.akkorhotel.hotel.service.UserService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
                .profileImageUrl("profileImageUrl")
                .build();

        User authenticatedUser = User.builder()
                .id("id")
                .username("username")
                .email("email")
                .password("password")
                .role(UserRole.USER)
                .isValidEmail(true)
                .profileImageUrl("profileImageUrl")
                .build();

        when(userService.getAuthenticatedUser(any(User.class))).thenReturn(ResponseEntity.ok(singletonMap("informations", authenticatedUserResponse)));
        when(jwtAuthenticationService.getAuthenticatedUser()).thenReturn(authenticatedUser);

        // Act
        mockMvc.perform(get("/private/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.informations.username").value("username"))
                .andExpect(jsonPath("$.informations.email").value("email"))
                .andExpect(jsonPath("$.informations.userRole").value("USER"))
                .andExpect(jsonPath("$.informations.profileImageUrl").value("profileImageUrl"));

        // Assert
        verify(userService).getAuthenticatedUser(captor.capture());

        User capturedRegisterRequest = captor.getValue();
        assertThat(capturedRegisterRequest.getId()).isEqualTo("id");
        assertThat(capturedRegisterRequest.getUsername()).isEqualTo("username");
        assertThat(capturedRegisterRequest.getEmail()).isEqualTo("email");
        assertThat(capturedRegisterRequest.getPassword()).isEqualTo("password");
        assertThat(capturedRegisterRequest.getRole()).isEqualTo(UserRole.USER);
        assertThat(capturedRegisterRequest.getIsValidEmail()).isTrue();
        assertThat(capturedRegisterRequest.getProfileImageUrl()).isEqualTo("profileImageUrl");
    }

    @Test
    void shouldUpdateExistingUser() throws Exception {
        // Arrange
        ArgumentCaptor<UpdateUserRequest> captor = ArgumentCaptor.forClass(UpdateUserRequest.class);

        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setUsername("newUsername");
        updateRequest.setEmail("new.email@example.com");
        updateRequest.setOldPassword("oldPass123#!");
        updateRequest.setNewPassword("newPass456#!");

        when(userService.updateUser(any(UpdateUserRequest.class), any(User.class)))
                .thenReturn(ResponseEntity.ok(singletonMap("message", "User details updated successfully")));

        // Act
        mockMvc.perform(patch("/private/user")
                        .content(new ObjectMapper().writeValueAsString(updateRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> "authenticatedUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User details updated successfully"));

        // Assert
        verify(userService).updateUser(captor.capture(), any(User.class));
        UpdateUserRequest capturedUpdateRequest = captor.getValue();

        assertThat(capturedUpdateRequest.getUsername()).isEqualTo("newUsername");
        assertThat(capturedUpdateRequest.getEmail()).isEqualTo("new.email@example.com");
        assertThat(capturedUpdateRequest.getOldPassword()).isEqualTo("oldPass123#!");
        assertThat(capturedUpdateRequest.getNewPassword()).isEqualTo("newPass456#!");
    }

    @Test
    void shouldDeleteAuthenticatedUser() throws Exception {
        // Arrange
        when(userService.deleteUser(null))
                .thenReturn(ResponseEntity.ok(singletonMap("message", "User deleted successfully")));

        // Act
        mockMvc.perform(delete("/private/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));

        // Assert
        verify(userService).deleteUser(null);
    }

    @Test
    void shouldUploadUserProfileImageSuccessfully() throws Exception {
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile("file", "test-image.png", MediaType.IMAGE_PNG_VALUE, "test image content".getBytes());

        when(userService.uploadUserProfileImage(any(User.class), any(MultipartFile.class)))
                .thenReturn(ResponseEntity.ok(singletonMap("message", "Profile image uploaded successfully")));

        // Act
        mockMvc.perform(multipart("/private/user/profile-image")
                        .file(mockFile)
                        .principal(() -> "authenticatedUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile image uploaded successfully"));

        // Assert
        ArgumentCaptor<MultipartFile> fileCaptor = ArgumentCaptor.forClass(MultipartFile.class);

        verify(userService).uploadUserProfileImage(any(User.class), fileCaptor.capture());

        MultipartFile capturedFile = fileCaptor.getValue();
        assertThat(capturedFile.getOriginalFilename()).isEqualTo("test-image.png");
        assertThat(capturedFile.getContentType()).isEqualTo(MediaType.IMAGE_PNG_VALUE);
        assertThat(capturedFile.getBytes()).isEqualTo("test image content".getBytes());
        assertThat(capturedFile.getName()).isEqualTo("file");
    }

}
