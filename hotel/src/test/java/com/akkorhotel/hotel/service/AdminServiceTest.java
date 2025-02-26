package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.UserDao;
import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.UserRole;
import com.akkorhotel.hotel.model.request.AdminUpdateUserRequest;
import com.akkorhotel.hotel.model.response.GetAllUsersResponse;
import com.akkorhotel.hotel.model.response.GetUserByIdResponse;
import com.akkorhotel.hotel.utils.UserUtils;
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
import static java.util.Collections.singletonMap;
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

    @Mock
    private UserUtils userUtils;

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

    @Test
    void shouldUpdateUser() {
        // Arrange
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();
        request.setEmail("new.email@gmail.com");
        request.setUsername("newUsername");
        request.setIsValidEmail(false);
        request.setRole("ADMIN");

        User userToUpdate = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("password")
                .role(UserRole.USER)
                .isValidEmail(true)
                .build();

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidEmail(anyString())).thenReturn(false);
        when(userDao.isEmailAlreadyUsed(anyString())).thenReturn(false);

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        User expectedUser = User.builder()
                .id("id")
                .username("newUsername")
                .email("new.email@gmail.com")
                .password("password")
                .isValidEmail(false)
                .role(UserRole.ADMIN)
                .build();

        InOrder inOrder = inOrder(userDao, userUtils);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("newUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("newUsername");
        inOrder.verify(userUtils).isInvalidEmail("new.email@gmail.com");
        inOrder.verify(userDao).isEmailAlreadyUsed("new.email@gmail.com");
        inOrder.verify(userDao).save(expectedUser);
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("message", "User with id: id updated successfully"));
    }

    @Test
    void shouldReturnNotFound_whenUserIdDoesNotExist() {
        // Arrange
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();

        when(userDao.findById(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("notFoundId", request);

        // Assert
        verify(userDao).findById("notFoundId");
        verifyNoMoreInteractions(userDao);
        verifyNoInteractions(userUtils);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "User not found"));
    }

    @Test
    void shouldReturnBadRequest_whenNoValuesProvidedForUpdate() {
        // Arrange
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();

        User userToUpdate = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("password")
                .role(UserRole.USER)
                .isValidEmail(true)
                .build();

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.getErrorsAsString(anyList())).thenReturn("No values provided for update. Please specify at least one field (email, username, isValidEmail or role)");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).getErrorsAsString(List.of("No values provided for update. Please specify at least one field (email, username, isValidEmail or role)"));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "No values provided for update. Please specify at least one field (email, username, isValidEmail or role)"));
    }

    @Test
    void shouldReturnBadRequest_whenUsernameIsInvalid() {
        // Arrange
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();
        request.setEmail("new.email@gmail.com");
        request.setUsername("invalidUsername");
        request.setIsValidEmail(false);
        request.setRole("ADMIN");

        User userToUpdate = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("password")
                .role(UserRole.USER)
                .isValidEmail(true)
                .build();

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(true);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The provided username is invalid. It must be between 3 and 11 characters long and cannot contain spaces");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("invalidUsername");
        inOrder.verify(userUtils).getErrorsAsString(List.of("The provided username is invalid. It must be between 3 and 11 characters long and cannot contain spaces"));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The provided username is invalid. It must be between 3 and 11 characters long and cannot contain spaces"));
    }

    @Test
    void shouldReturnBadRequest_whenUsernameIsAlreadyUsed() {
        // Arrange
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();
        request.setEmail("new.email@gmail.com");
        request.setUsername("alreadyUsed");
        request.setIsValidEmail(false);
        request.setRole("ADMIN");

        User userToUpdate = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("password")
                .role(UserRole.USER)
                .isValidEmail(true)
                .build();

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(true);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The username 'alreadyUsed' is already in use by another account. Please choose a different one");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("alreadyUsed");
        inOrder.verify(userDao).isUsernameAlreadyUsed("alreadyUsed");
        inOrder.verify(userUtils).getErrorsAsString(List.of("The username 'alreadyUsed' is already in use by another account. Please choose a different one"));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The username 'alreadyUsed' is already in use by another account. Please choose a different one"));
    }

    @Test
    void shouldReturnBadRequest_whenUsernameIsSameAsCurrent() {
        // Arrange
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();
        request.setEmail("new.email@gmail.com");
        request.setUsername("oldUsername");
        request.setIsValidEmail(false);
        request.setRole("ADMIN");

        User userToUpdate = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("password")
                .role(UserRole.USER)
                .isValidEmail(true)
                .build();

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The new username must be different from the current one");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("oldUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("oldUsername");
        inOrder.verify(userUtils).getErrorsAsString(List.of("The new username must be different from the current one"));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The new username must be different from the current one"));
    }

    @Test
    void shouldReturnBadRequest_whenEmailIsInvalid() {
        // Arrange
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();
        request.setEmail("invalidEmail");
        request.setUsername("newUsername");
        request.setIsValidEmail(false);
        request.setRole("ADMIN");

        User userToUpdate = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("password")
                .role(UserRole.USER)
                .isValidEmail(true)
                .build();

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidEmail(anyString())).thenReturn(true);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The provided email format is invalid. Please enter a valid email address");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("newUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("newUsername");
        inOrder.verify(userUtils).isInvalidEmail("invalidEmail");
        inOrder.verify(userUtils).getErrorsAsString(List.of("The provided email format is invalid. Please enter a valid email address"));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The provided email format is invalid. Please enter a valid email address"));
    }

    @Test
    void shouldReturnBadRequest_whenEmailIsAlreadyUsed() {
        // Arrange
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();
        request.setEmail("alreadyUsed");
        request.setUsername("newUsername");
        request.setIsValidEmail(false);
        request.setRole("ADMIN");

        User userToUpdate = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("password")
                .role(UserRole.USER)
                .isValidEmail(true)
                .build();

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidEmail(anyString())).thenReturn(false);
        when(userDao.isEmailAlreadyUsed(anyString())).thenReturn(true);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The email address 'alreadyUsed' is already associated with another account");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("newUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("newUsername");
        inOrder.verify(userUtils).isInvalidEmail("alreadyUsed");
        inOrder.verify(userDao).isEmailAlreadyUsed("alreadyUsed");
        inOrder.verify(userUtils).getErrorsAsString(List.of("The email address 'alreadyUsed' is already associated with another account"));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The email address 'alreadyUsed' is already associated with another account"));
    }

    @Test
    void shouldReturnBadRequest_whenEmailIsSameAsCurrent() {
        // Arrange
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();
        request.setEmail("old.email@gmail.com");
        request.setUsername("newUsername");
        request.setIsValidEmail(false);
        request.setRole("ADMIN");

        User userToUpdate = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("password")
                .role(UserRole.USER)
                .isValidEmail(true)
                .build();

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidEmail(anyString())).thenReturn(false);
        when(userDao.isEmailAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The new email address must be different from the current one");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("newUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("newUsername");
        inOrder.verify(userUtils).isInvalidEmail("old.email@gmail.com");
        inOrder.verify(userDao).isEmailAlreadyUsed("old.email@gmail.com");
        inOrder.verify(userUtils).getErrorsAsString(List.of("The new email address must be different from the current one"));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The new email address must be different from the current one"));
    }

    @Test
    void shouldReturnBadRequest_whenRoleIsSameAsCurrent() {
        // Arrange
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();
        request.setEmail("new.email@gmail.com");
        request.setUsername("newUsername");
        request.setIsValidEmail(false);
        request.setRole("USER");

        User userToUpdate = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("password")
                .role(UserRole.USER)
                .isValidEmail(true)
                .build();

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidEmail(anyString())).thenReturn(false);
        when(userDao.isEmailAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The new role must be different from the current one");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("newUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("newUsername");
        inOrder.verify(userUtils).isInvalidEmail("new.email@gmail.com");
        inOrder.verify(userDao).isEmailAlreadyUsed("new.email@gmail.com");
        inOrder.verify(userUtils).getErrorsAsString(List.of("The new role must be different from the current one"));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The new role must be different from the current one"));
    }

    @Test
    void shouldReturnBadRequest_whenRoleIsNotValid() {
        // Arrange
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();
        request.setEmail("new.email@gmail.com");
        request.setUsername("newUsername");
        request.setIsValidEmail(false);
        request.setRole("NOT_VALID");

        User userToUpdate = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("password")
                .role(UserRole.USER)
                .isValidEmail(true)
                .build();

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidEmail(anyString())).thenReturn(false);
        when(userDao.isEmailAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("Invalid role: NOT_VALID. Allowed values are: [USER, ADMIN]");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("newUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("newUsername");
        inOrder.verify(userUtils).isInvalidEmail("new.email@gmail.com");
        inOrder.verify(userDao).isEmailAlreadyUsed("new.email@gmail.com");
        inOrder.verify(userUtils).getErrorsAsString(List.of("Invalid role: NOT_VALID. Allowed values are: [USER, ADMIN]"));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Invalid role: NOT_VALID. Allowed values are: [USER, ADMIN]"));
    }

    @Test
    void shouldReturnBadRequest_whenIsValidEmailValueIsSameAsCurrent() {
        // Arrange
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();
        request.setEmail("new.email@gmail.com");
        request.setUsername("newUsername");
        request.setIsValidEmail(true);
        request.setRole("ADMIN");

        User userToUpdate = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("password")
                .role(UserRole.USER)
                .isValidEmail(true)
                .build();

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidEmail(anyString())).thenReturn(false);
        when(userDao.isEmailAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The email verification status is already set to the provided value. No changes were made.");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("newUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("newUsername");
        inOrder.verify(userUtils).isInvalidEmail("new.email@gmail.com");
        inOrder.verify(userDao).isEmailAlreadyUsed("new.email@gmail.com");
        inOrder.verify(userUtils).getErrorsAsString(List.of("The email verification status is already set to the provided value. No changes were made."));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The email verification status is already set to the provided value. No changes were made."));
    }

}