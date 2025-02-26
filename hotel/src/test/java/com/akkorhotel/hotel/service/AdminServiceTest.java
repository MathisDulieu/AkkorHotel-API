package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.UserDao;
import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.UserRole;
import com.akkorhotel.hotel.model.response.GetAllUsersResponse;
import com.akkorhotel.hotel.model.response.GetUserByIdResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private UserDao userDao;

    @Test
    void shouldReturnAllUsersWithMatchingPrefix() {
        // Arrange
        String keyword = "any";
        int page = 0;
        int pageSize = 2;

        when(userDao.countUsersByUsernamePrefix(anyString())).thenReturn(3L);
        when(userDao.searchUsersByUsernamePrefix(anyString(), anyInt(), anyInt())).thenReturn(List.of(
                User.builder().id("id1").username("anyUsername1").email("email1").build(),
                User.builder().id("id2").username("anyUsername2").email("email2").build()
        ));

        // Act
        ResponseEntity<Map<String, GetAllUsersResponse>> response = adminService.getAllUsers(keyword, page, pageSize);

        // Assert
        InOrder inOrder = inOrder(userDao);
        inOrder.verify(userDao).countUsersByUsernamePrefix("any");
        inOrder.verify(userDao).searchUsersByUsernamePrefix("any", 0, 2);
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, GetAllUsersResponse> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.containsKey("users")).isTrue();

        GetAllUsersResponse usersResponse = responseBody.get("users");
        assertThat(usersResponse).isNotNull();
        assertThat(usersResponse.getUsers()).isEqualTo(
                List.of(
                        User.builder().id("id1").username("anyUsername1").email("email1").build(),
                        User.builder().id("id2").username("anyUsername2").email("email2").build()
                )
        );
        assertThat(usersResponse.getTotalPages()).isEqualTo(2);
        assertThat(usersResponse.getError()).isNull();
    }

    @Test
    void shouldReturnBadRequest_whenPageSizeIsNegativeNumber() {
        // Arrange
        String keyword = "any";
        int page = 0;
        int pageSize = -2;

        // Act
        ResponseEntity<Map<String, GetAllUsersResponse>> response = adminService.getAllUsers(keyword, page, pageSize);

        // Assert
        verifyNoInteractions(userDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Map<String, GetAllUsersResponse> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.containsKey("error")).isTrue();

        GetAllUsersResponse usersResponse = responseBody.get("error");
        assertThat(usersResponse).isNotNull();
        assertThat(usersResponse.getUsers()).isEqualTo(emptyList());
        assertThat(usersResponse.getTotalPages()).isEqualTo(0);
        assertThat(usersResponse.getError()).isEqualTo("Page size must be greater than or equal to zero");
    }

    @Test
    void shouldReturnBadRequest_whenPageIsNegativeNumber() {
        // Arrange
        String keyword = "any";
        int page = -5;
        int pageSize = 2;

        // Act
        ResponseEntity<Map<String, GetAllUsersResponse>> response = adminService.getAllUsers(keyword, page, pageSize);

        // Assert
        verifyNoInteractions(userDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Map<String, GetAllUsersResponse> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.containsKey("error")).isTrue();

        GetAllUsersResponse usersResponse = responseBody.get("error");
        assertThat(usersResponse).isNotNull();
        assertThat(usersResponse.getUsers()).isEqualTo(emptyList());
        assertThat(usersResponse.getTotalPages()).isEqualTo(0);
        assertThat(usersResponse.getError()).isEqualTo("Page number must be greater than or equal to zero");
    }

    @Test
    void shouldReturnBadRequest_whenKeywordContainsSpaces() {
        // Arrange
        String keyword = " spaces ";
        int page = 5;
        int pageSize = 2;

        // Act
        ResponseEntity<Map<String, GetAllUsersResponse>> response = adminService.getAllUsers(keyword, page, pageSize);

        // Assert
        verifyNoInteractions(userDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Map<String, GetAllUsersResponse> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.containsKey("error")).isTrue();

        GetAllUsersResponse usersResponse = responseBody.get("error");
        assertThat(usersResponse).isNotNull();
        assertThat(usersResponse.getUsers()).isEqualTo(emptyList());
        assertThat(usersResponse.getTotalPages()).isEqualTo(0);
        assertThat(usersResponse.getError()).isEqualTo("Search keyword cannot contain spaces");
    }

    @Test
    void shouldReturnOkWithErrorMessage_whenNoUsersFoundWithMatchingPrefix() {
        // Arrange
        String keyword = "notFound";
        int page = 1;
        int pageSize = 2;

        when(userDao.countUsersByUsernamePrefix(anyString())).thenReturn(0L);

        // Act
        ResponseEntity<Map<String, GetAllUsersResponse>> response = adminService.getAllUsers(keyword, page, pageSize);

        // Assert
        verify(userDao).countUsersByUsernamePrefix("notFound");
        verifyNoMoreInteractions(userDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, GetAllUsersResponse> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.containsKey("users")).isTrue();

        GetAllUsersResponse usersResponse = responseBody.get("users");
        assertThat(usersResponse).isNotNull();
        assertThat(usersResponse.getUsers()).isEqualTo(emptyList());
        assertThat(usersResponse.getTotalPages()).isEqualTo(0);
        assertThat(usersResponse.getError()).isEqualTo("No users found");
    }

    @Test
    void shouldReturnOkWithWarningMessage_whenRequestedPageExceedsTotalPages() {
        // Arrange
        String keyword = "any";
        int page = 5;
        int pageSize = 3;

        when(userDao.countUsersByUsernamePrefix(anyString())).thenReturn(9L);

        // Act
        ResponseEntity<Map<String, GetAllUsersResponse>> response = adminService.getAllUsers(keyword, page, pageSize);

        // Assert
        verify(userDao).countUsersByUsernamePrefix("any");
        verifyNoMoreInteractions(userDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, GetAllUsersResponse> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.containsKey("warning")).isTrue();

        GetAllUsersResponse usersResponse = responseBody.get("warning");
        assertThat(usersResponse).isNotNull();
        assertThat(usersResponse.getUsers()).isEqualTo(emptyList());
        assertThat(usersResponse.getTotalPages()).isEqualTo(0);
        assertThat(usersResponse.getError()).isEqualTo("Requested page exceeds the total number of available pages");
    }

    @Test
    void shouldReturnUserWithMatchingId() {
        // Arrange
        String userId = "anyId";

        User user = User.builder()
                .id("anyId")
                .username("username")
                .email("email")
                .role(UserRole.USER)
                .isValidEmail(true)
                .build();

        when(userDao.findById(anyString())).thenReturn(Optional.of(user));

        // Act
        ResponseEntity<Map<String, GetUserByIdResponse>> response = adminService.getUserById(userId);

        // Assert
        verify(userDao).findById("anyId");
        verifyNoMoreInteractions(userDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, GetUserByIdResponse> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.containsKey("user")).isTrue();

        GetUserByIdResponse userResponse = responseBody.get("user");
        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getUser()).isEqualTo(
                User.builder()
                        .id("anyId")
                        .username("username")
                        .email("email")
                        .role(UserRole.USER)
                        .isValidEmail(true)
                        .password(null)
                        .build()
        );
        assertThat(userResponse.getError()).isNull();
    }

    @Test
    void shouldReturnNotFound_whenUserDoesNotExist() {
        // Arrange
        String userId = "nonExistentId";

        when(userDao.findById(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, GetUserByIdResponse>> response = adminService.getUserById(userId);

        // Assert
        verify(userDao).findById("nonExistentId");
        verifyNoMoreInteractions(userDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        Map<String, GetUserByIdResponse> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.containsKey("error")).isTrue();

        GetUserByIdResponse userResponse = responseBody.get("error");
        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getError()).isEqualTo("User not found");
        assertThat(userResponse.getUser()).isNull();
    }

    @Test
    void shouldReturnForbidden_whenUserIsAdmin() {
        // Arrange
        String userId = "adminId";

        User adminUser = User.builder()
                .id("adminId")
                .username("adminUser")
                .email("admin@email.com")
                .role(UserRole.ADMIN)
                .isValidEmail(true)
                .build();

        when(userDao.findById(anyString())).thenReturn(Optional.of(adminUser));

        // Act
        ResponseEntity<Map<String, GetUserByIdResponse>> response = adminService.getUserById(userId);

        // Assert
        verify(userDao).findById("adminId");
        verifyNoMoreInteractions(userDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        Map<String, GetUserByIdResponse> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.containsKey("error")).isTrue();

        GetUserByIdResponse userResponse = responseBody.get("error");
        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getError()).isEqualTo("Admin users cannot be retrieved");
        assertThat(userResponse.getUser()).isNull();
    }

}