package com.akkorhotel.hotel.controller;

import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.UserRole;
import com.akkorhotel.hotel.model.request.AdminUpdateUserRequest;
import com.akkorhotel.hotel.model.response.GetAllUsersResponse;
import com.akkorhotel.hotel.model.response.GetUserByIdResponse;
import com.akkorhotel.hotel.service.AdminService;
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

import java.util.List;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private AdminController adminController;

    @Mock
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    void shouldGetAllUsersWithSpecifiedParameters() throws Exception {
        // Arrange
        ArgumentCaptor<String> keywordCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> pageCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> pageSizeCaptor = ArgumentCaptor.forClass(Integer.class);

        List<User> users = List.of(
                User.builder().id("id1").username("username1").email("email1").profileImageUrl("profileImageUrl1").build(),
                User.builder().id("id2").username("username2").email("email2").profileImageUrl("profileImageUrl2").build()
        );

        GetAllUsersResponse response = GetAllUsersResponse.builder()
                .users(users)
                .totalPages(1)
                .build();

        when(adminService.getAllUsers(anyString(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(singletonMap("users", response)));

        // Act & Assert
        mockMvc.perform(get("/private/admin/users")
                        .param("keyword", "user")
                        .param("page", "2")
                        .param("pageSize", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.users[0].id").value("id1"))
                .andExpect(jsonPath("$.users.users[0].username").value("username1"))
                .andExpect(jsonPath("$.users.users[0].email").value("email1"))
                .andExpect(jsonPath("$.users.users[0].profileImageUrl").value("profileImageUrl1"))
                .andExpect(jsonPath("$.users.users[1].id").value("id2"))
                .andExpect(jsonPath("$.users.users[1].username").value("username2"))
                .andExpect(jsonPath("$.users.users[1].email").value("email2"))
                .andExpect(jsonPath("$.users.users[1].profileImageUrl").value("profileImageUrl2"))
                .andExpect(jsonPath("$.users.totalPages").value(1))
                .andExpect(jsonPath("$.users.error").doesNotExist());

        verify(adminService).getAllUsers(keywordCaptor.capture(), pageCaptor.capture(), pageSizeCaptor.capture());

        assertThat(keywordCaptor.getValue()).isEqualTo("user");
        assertThat(pageCaptor.getValue()).isEqualTo(2);
        assertThat(pageSizeCaptor.getValue()).isEqualTo(5);
    }

    @Test
    void shouldGetAllUsersWithDefaultParametersWhenNotSpecified() throws Exception {
        // Arrange
        ArgumentCaptor<String> keywordCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> pageCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> pageSizeCaptor = ArgumentCaptor.forClass(Integer.class);

        List<User> users = List.of(
                User.builder().id("id1").username("username1").email("email1").profileImageUrl("profileImageUrl1").build(),
                User.builder().id("id2").username("username2").email("email2").profileImageUrl("profileImageUrl2").build()
        );

        GetAllUsersResponse response = GetAllUsersResponse.builder()
                .users(users)
                .totalPages(1)
                .build();

        when(adminService.getAllUsers(anyString(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(singletonMap("users", response)));

        // Act & Assert
        mockMvc.perform(get("/private/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.users[0].id").value("id1"))
                .andExpect(jsonPath("$.users.users[0].username").value("username1"))
                .andExpect(jsonPath("$.users.users[0].email").value("email1"))
                .andExpect(jsonPath("$.users.users[0].profileImageUrl").value("profileImageUrl1"))
                .andExpect(jsonPath("$.users.users[1].id").value("id2"))
                .andExpect(jsonPath("$.users.users[1].username").value("username2"))
                .andExpect(jsonPath("$.users.users[1].email").value("email2"))
                .andExpect(jsonPath("$.users.users[1].profileImageUrl").value("profileImageUrl2"))
                .andExpect(jsonPath("$.users.totalPages").value(1))
                .andExpect(jsonPath("$.users.error").doesNotExist());;

        verify(adminService).getAllUsers(keywordCaptor.capture(), pageCaptor.capture(), pageSizeCaptor.capture());

        assertThat(keywordCaptor.getValue()).isEqualTo("");
        assertThat(pageCaptor.getValue()).isEqualTo(0);
        assertThat(pageSizeCaptor.getValue()).isEqualTo(10);
    }

    @Test
    void shouldGetUserById() throws Exception {
        // Arrange
        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);

        User user = User.builder()
                .id("id")
                .username("username")
                .email("email")
                .role(UserRole.USER)
                .isValidEmail(true)
                .password(null)
                .profileImageUrl("profileImageUrl")
                .build();

        GetUserByIdResponse response = GetUserByIdResponse.builder()
                .user(user)
                .build();

        when(adminService.getUserById(anyString()))
                .thenReturn(ResponseEntity.ok(singletonMap("user", response)));

        // Act & Assert
        mockMvc.perform(get("/private/admin/user/{userId}", "id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.user.id").value("id"))
                .andExpect(jsonPath("$.user.user.username").value("username"))
                .andExpect(jsonPath("$.user.user.email").value("email"))
                .andExpect(jsonPath("$.user.user.isValidEmail").value(true))
                .andExpect(jsonPath("$.user.user.role").value("USER"))
                .andExpect(jsonPath("$.user.user.password").doesNotExist())
                .andExpect(jsonPath("$.user.user.profileImageUrl").value("profileImageUrl"))
                .andExpect(jsonPath("$.user.error").doesNotExist());

        verify(adminService).getUserById(userIdCaptor.capture());
        assertThat(userIdCaptor.getValue()).isEqualTo("id");
    }

    @Test
    void shouldUpdateUser() throws Exception {
        // Arrange
        String userId = "id";
        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<AdminUpdateUserRequest> requestCaptor = ArgumentCaptor.forClass(AdminUpdateUserRequest.class);

        String requestBody = """
        {
            "username": "newUsername",
            "email": "new.email@example.com",
            "role": "ADMIN",
            "isValidEmail": false,
            "profileImageUrl": "https://any.jpg"
        }
        """;

        when(adminService.updateUser(anyString(), any(AdminUpdateUserRequest.class)))
                .thenReturn(ResponseEntity.ok(singletonMap("message", "User with id: " + userId + " updated successfully")));

        // Act
        mockMvc.perform(put("/private/admin/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User with id: id updated successfully"));

        // Assert
        verify(adminService).updateUser(userIdCaptor.capture(), requestCaptor.capture());

        assertThat(userIdCaptor.getValue()).isEqualTo(userId);

        AdminUpdateUserRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getUsername()).isEqualTo("newUsername");
        assertThat(capturedRequest.getEmail()).isEqualTo("new.email@example.com");
        assertThat(capturedRequest.getRole()).isEqualTo("ADMIN");
        assertThat(capturedRequest.getIsValidEmail()).isFalse();
        assertThat(capturedRequest.getProfileImageUrl()).isEqualTo("https://any.jpg");
    }

}