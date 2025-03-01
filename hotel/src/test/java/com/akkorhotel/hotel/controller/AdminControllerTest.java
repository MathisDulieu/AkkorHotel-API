package com.akkorhotel.hotel.controller;

import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.UserRole;
import com.akkorhotel.hotel.model.request.AdminUpdateUserRequest;
import com.akkorhotel.hotel.model.request.CreateHotelRequest;
import com.akkorhotel.hotel.model.request.CreateHotelRoomRequest;
import com.akkorhotel.hotel.model.response.GetAllUsersResponse;
import com.akkorhotel.hotel.model.response.GetUserByIdResponse;
import com.akkorhotel.hotel.service.AdminService;
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

import java.util.List;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Test
    void shouldCreateHotel() throws Exception {
        // Arrange
        MockMultipartFile mockFile1 = new MockMultipartFile("pictures", "hotel-image1.png", MediaType.IMAGE_PNG_VALUE, "image-content-1".getBytes());
        MockMultipartFile mockFile2 = new MockMultipartFile("pictures", "hotel-image2.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content-2".getBytes());

        CreateHotelRequest hotelRequest = new CreateHotelRequest();
        hotelRequest.setName("MyHotel");
        hotelRequest.setDescription("A great hotel.");
        hotelRequest.setCity("Paris");
        hotelRequest.setAddress("123 Rue de la Paix");
        hotelRequest.setCountry("France");
        hotelRequest.setGoogleMapsUrl("https://maps.google.com/?q=MyHotel");
        hotelRequest.setState("Île-de-France");
        hotelRequest.setPostalCode("75001");
        hotelRequest.setAmenities(List.of("POOL", "WIFI"));

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "filename.jpg",
                MediaType.APPLICATION_JSON_VALUE,
                new ObjectMapper().writeValueAsBytes(hotelRequest)
        );

        when(adminService.createHotel(any(User.class), any(CreateHotelRequest.class), anyList()))
                .thenReturn(ResponseEntity.ok(singletonMap("message", "Hotel created successfully")));

        // Act
        mockMvc.perform(multipart("/private/admin/hotel")
                        .file(requestPart)
                        .file(mockFile1)
                        .file(mockFile2)
                        .with(request -> {
                            request.setMethod("POST");
                            return request;
                        })
                        .principal(() -> "authenticatedUser")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hotel created successfully"));

        // Assert
        ArgumentCaptor<CreateHotelRequest> requestCaptor = ArgumentCaptor.forClass(CreateHotelRequest.class);
        ArgumentCaptor<List<MultipartFile>> fileCaptor = ArgumentCaptor.forClass(List.class);

        verify(adminService).createHotel(any(User.class), requestCaptor.capture(), fileCaptor.capture());

        CreateHotelRequest capturedRequest = requestCaptor.getValue();
        List<MultipartFile> capturedFiles = fileCaptor.getValue();

        assertThat(capturedRequest.getName()).isEqualTo("MyHotel");
        assertThat(capturedRequest.getDescription()).isEqualTo("A great hotel.");
        assertThat(capturedRequest.getCity()).isEqualTo("Paris");
        assertThat(capturedRequest.getCountry()).isEqualTo("France");
        assertThat(capturedRequest.getAddress()).isEqualTo("123 Rue de la Paix");
        assertThat(capturedRequest.getState()).isEqualTo("Île-de-France");
        assertThat(capturedRequest.getPostalCode()).isEqualTo("75001");
        assertThat(capturedRequest.getGoogleMapsUrl()).isEqualTo("https://maps.google.com/?q=MyHotel");
        assertThat(capturedRequest.getAmenities()).isEqualTo(List.of("POOL", "WIFI"));

        assertThat(capturedFiles).hasSize(2);
        assertThat(capturedFiles.get(0).getOriginalFilename()).isEqualTo("hotel-image1.png");
        assertThat(capturedFiles.get(1).getOriginalFilename()).isEqualTo("hotel-image2.jpg");
    }

    @Test
    void shouldAddNewRoomToHotel() throws Exception {
        // Arrange
        String requestBody = """
        {
            "hotelId": "hotelId",
            "type": "type",
            "features": ["feature1", "feature2"],
            "maxOccupancy": 1,
            "price": 10.00
        }
        """;

        ArgumentCaptor<CreateHotelRoomRequest> requestCaptor = ArgumentCaptor.forClass(CreateHotelRoomRequest.class);

        when(adminService.addRoomToHotel(any(CreateHotelRoomRequest.class)))
                .thenReturn(ResponseEntity.ok(singletonMap("message", "HotelRoom added successfully")));

        // Act & Assert
        mockMvc.perform(post("/private/admin/hotel/room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("HotelRoom added successfully"));

        verify(adminService).addRoomToHotel(requestCaptor.capture());

        CreateHotelRoomRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getHotelId()).isEqualTo("hotelId");
        assertThat(capturedRequest.getType()).isEqualTo("type");
        assertThat(capturedRequest.getFeatures()).containsExactly("feature1", "feature2");
        assertThat(capturedRequest.getMaxOccupancy()).isEqualTo(1);
        assertThat(capturedRequest.getPrice()).isEqualTo(10.00);
    }

}