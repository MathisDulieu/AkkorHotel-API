package com.akkorhotel.hotel.controller;

import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.response.GetAllUsersResponse;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                User.builder().id("id1").username("username1").email("email1").build(),
                User.builder().id("id2").username("username2").email("email2").build()
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
                .andExpect(jsonPath("$.users.users[1].id").value("id2"))
                .andExpect(jsonPath("$.users.users[1].username").value("username2"))
                .andExpect(jsonPath("$.users.users[1].email").value("email2"))
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
                User.builder().id("id1").username("username1").email("email1").build(),
                User.builder().id("id2").username("username2").email("email2").build()
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
                .andExpect(jsonPath("$.users.users[1].id").value("id2"))
                .andExpect(jsonPath("$.users.users[1].username").value("username2"))
                .andExpect(jsonPath("$.users.users[1].email").value("email2"))
                .andExpect(jsonPath("$.users.totalPages").value(1))
                .andExpect(jsonPath("$.users.error").doesNotExist());;

        verify(adminService).getAllUsers(keywordCaptor.capture(), pageCaptor.capture(), pageSizeCaptor.capture());

        assertThat(keywordCaptor.getValue()).isEqualTo("");
        assertThat(pageCaptor.getValue()).isEqualTo(0);
        assertThat(pageSizeCaptor.getValue()).isEqualTo(10);
    }

}