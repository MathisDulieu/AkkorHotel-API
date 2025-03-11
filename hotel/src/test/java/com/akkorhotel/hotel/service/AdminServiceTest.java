package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.BookingDao;
import com.akkorhotel.hotel.dao.HotelDao;
import com.akkorhotel.hotel.dao.HotelRoomDao;
import com.akkorhotel.hotel.dao.UserDao;
import com.akkorhotel.hotel.model.*;
import com.akkorhotel.hotel.model.request.*;
import com.akkorhotel.hotel.model.response.AdminGetBookingsResponse;
import com.akkorhotel.hotel.model.response.GetAllUsersResponse;
import com.akkorhotel.hotel.model.response.GetUserByIdResponse;
import com.akkorhotel.hotel.utils.ImageUtils;
import com.akkorhotel.hotel.utils.UserUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
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

    @Mock
    private ImageService imageService;

    @Mock
    private ImageUtils imageUtils;

    @Mock
    private UuidProvider uuidProvider;

    @Mock
    private HotelDao hotelDao;

    @Mock
    private HotelRoomDao hotelRoomDao;

    @Mock
    private BookingDao bookingDao;

    @Test
    void shouldReturnAllUsersWithMatchingPrefix() {
        // Arrange
        String keyword = "any";
        int page = 0;
        int pageSize = 2;

        when(userDao.countUsersByUsernamePrefix(anyString())).thenReturn(3L);
        when(userDao.searchUsersByUsernamePrefix(anyString(), anyInt(), anyInt())).thenReturn(List.of(
                User.builder().id("id1").username("anyUsername1").email("email1").profileImageUrl("profileImageUrl1").build(),
                User.builder().id("id2").username("anyUsername2").email("email2").profileImageUrl("profileImageUrl2").build()
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
                        User.builder().id("id1").username("anyUsername1").email("email1").profileImageUrl("profileImageUrl1").build(),
                        User.builder().id("id2").username("anyUsername2").email("email2").profileImageUrl("profileImageUrl2").build()
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

        User user = buildUser("anyId", "username", "email", "password",
                true, UserRole.USER, "profileImageUrl");

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

        User expectedUser = buildUser("anyId", "username", "email", null,
                true, UserRole.USER, "profileImageUrl");

        assertThat(userResponse.getUser()).isEqualTo(expectedUser);
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

        User adminUser = buildUser("adminId", "adminUser", "admin@email.com", "password",
                true, UserRole.ADMIN, "profileImageUrl");

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
        AdminUpdateUserRequest request = buildAdminUpdateUserRequest("new.email@gmail.com",
                "newUsername", false, "ADMIN", "https://newProfileImageUrl.jpg");

        User userToUpdate = buildUser("id", "oldUsername", "old.email@gmail.com", "password",
                true, UserRole.USER, "https://oldProfileImageUrl.png");

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidEmail(anyString())).thenReturn(false);
        when(userDao.isEmailAlreadyUsed(anyString())).thenReturn(false);
        when(imageService.getImageExtension(anyString())).thenReturn(ImageExtension.jpg);

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        User expectedUser = buildUser("id", "newUsername", "new.email@gmail.com", "password",
                false, UserRole.ADMIN, "https://newProfileImageUrl.jpg");

        InOrder inOrder = inOrder(userDao, userUtils, imageService);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("newUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("newUsername");
        inOrder.verify(userUtils).isInvalidEmail("new.email@gmail.com");
        inOrder.verify(userDao).isEmailAlreadyUsed("new.email@gmail.com");
        inOrder.verify(imageService).getImageExtension("https://newProfileImageUrl.jpg");
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
        verifyNoInteractions(userUtils, imageService);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "User not found"));
    }

    @Test
    void shouldReturnBadRequest_whenNoValuesProvidedForUpdate() {
        // Arrange
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();

        User userToUpdate = buildUser("id", "oldUsername", "old.email@gmail.com", "password",
                true, UserRole.USER, "https://oldProfileImageUrl.png");

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.getErrorsAsString(anyList())).thenReturn("No values provided for update. Please specify at least one field (email, username, isValidEmail, profileImageUrl or role)");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).getErrorsAsString(List.of("No values provided for update. Please specify at least one field (email, username, isValidEmail, profileImageUrl or role)"));
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(imageService);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "No values provided for update. Please specify at least one field (email, username, isValidEmail, profileImageUrl or role)"));
    }

    @Test
    void shouldReturnBadRequest_whenUsernameIsInvalid() {
        // Arrange
        AdminUpdateUserRequest request = buildAdminUpdateUserRequest("new.email@gmail.com",
                "invalidUsername", false, "ADMIN", "https://newProfileImageUrl.jpg");

        User userToUpdate = buildUser("id", "oldUsername", "old.email@gmail.com", "password",
                true, UserRole.USER, "https://oldProfileImageUrl.png");

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(true);
        when(imageService.getImageExtension(anyString())).thenReturn(ImageExtension.png);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The provided username is invalid. It must be between 3 and 11 characters long and cannot contain spaces");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils, imageService);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("invalidUsername");
        inOrder.verify(imageService).getImageExtension("https://newProfileImageUrl.jpg");
        inOrder.verify(userUtils).getErrorsAsString(List.of("The provided username is invalid. It must be between 3 and 11 characters long and cannot contain spaces"));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The provided username is invalid. It must be between 3 and 11 characters long and cannot contain spaces"));
    }

    @Test
    void shouldReturnBadRequest_whenUsernameIsAlreadyUsed() {
        // Arrange
        AdminUpdateUserRequest request = buildAdminUpdateUserRequest("new.email@gmail.com",
                "alreadyUsed", false, "ADMIN", "https://newProfileImageUrl.jpg");

        User userToUpdate = buildUser("id", "oldUsername", "old.email@gmail.com", "password",
                true, UserRole.USER, "https://oldProfileImageUrl.png");

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(true);
        when(imageService.getImageExtension(anyString())).thenReturn(ImageExtension.jpg);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The username 'alreadyUsed' is already in use by another account. Please choose a different one");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils, imageService);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("alreadyUsed");
        inOrder.verify(userDao).isUsernameAlreadyUsed("alreadyUsed");
        inOrder.verify(imageService).getImageExtension("https://newProfileImageUrl.jpg");
        inOrder.verify(userUtils).getErrorsAsString(List.of("The username 'alreadyUsed' is already in use by another account. Please choose a different one"));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The username 'alreadyUsed' is already in use by another account. Please choose a different one"));
    }

    @Test
    void shouldReturnBadRequest_whenUsernameIsSameAsCurrent() {
        // Arrange
        AdminUpdateUserRequest request = buildAdminUpdateUserRequest("new.email@gmail.com",
                "oldUsername", false, "ADMIN", "https://newProfileImageUrl.jpg");

        User userToUpdate = buildUser("id", "oldUsername", "old.email@gmail.com", "password",
                true, UserRole.USER, "https://oldProfileImageUrl.png");

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(imageService.getImageExtension(anyString())).thenReturn(ImageExtension.jpg);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The new username must be different from the current one");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils, imageService);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("oldUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("oldUsername");
        inOrder.verify(imageService).getImageExtension("https://newProfileImageUrl.jpg");
        inOrder.verify(userUtils).getErrorsAsString(List.of("The new username must be different from the current one"));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The new username must be different from the current one"));
    }

    @Test
    void shouldReturnBadRequest_whenEmailIsInvalid() {
        // Arrange
        AdminUpdateUserRequest request = buildAdminUpdateUserRequest("invalidEmail",
                "newUsername", false, "ADMIN", "https://newProfileImageUrl.jpg");

        User userToUpdate = buildUser("id", "oldUsername", "old.email@gmail.com", "password",
                true, UserRole.USER, "https://oldProfileImageUrl.png");

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidEmail(anyString())).thenReturn(true);
        when(imageService.getImageExtension(anyString())).thenReturn(ImageExtension.jpg);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The provided email format is invalid. Please enter a valid email address");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils, imageService);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("newUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("newUsername");
        inOrder.verify(userUtils).isInvalidEmail("invalidEmail");
        inOrder.verify(imageService).getImageExtension("https://newProfileImageUrl.jpg");
        inOrder.verify(userUtils).getErrorsAsString(List.of("The provided email format is invalid. Please enter a valid email address"));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The provided email format is invalid. Please enter a valid email address"));
    }

    @Test
    void shouldReturnBadRequest_whenEmailIsAlreadyUsed() {
        // Arrange
        AdminUpdateUserRequest request = buildAdminUpdateUserRequest("alreadyUsed",
                "newUsername", false, "ADMIN", "https://newProfileImageUrl.jpg");

        User userToUpdate = buildUser("id", "oldUsername", "old.email@gmail.com", "password",
                true, UserRole.USER, "https://oldProfileImageUrl.png");

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidEmail(anyString())).thenReturn(false);
        when(userDao.isEmailAlreadyUsed(anyString())).thenReturn(true);
        when(imageService.getImageExtension(anyString())).thenReturn(ImageExtension.jpg);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The email address 'alreadyUsed' is already associated with another account");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils, imageService);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("newUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("newUsername");
        inOrder.verify(userUtils).isInvalidEmail("alreadyUsed");
        inOrder.verify(userDao).isEmailAlreadyUsed("alreadyUsed");
        inOrder.verify(imageService).getImageExtension("https://newProfileImageUrl.jpg");
        inOrder.verify(userUtils).getErrorsAsString(List.of("The email address 'alreadyUsed' is already associated with another account"));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The email address 'alreadyUsed' is already associated with another account"));
    }

    @Test
    void shouldReturnBadRequest_whenEmailIsSameAsCurrent() {
        // Arrange
        AdminUpdateUserRequest request = buildAdminUpdateUserRequest("old.email@gmail.com",
                "newUsername", false, "ADMIN", "https://newProfileImageUrl.jpg");

        User userToUpdate = buildUser("id", "oldUsername", "old.email@gmail.com", "password",
                true, UserRole.USER, "https://oldProfileImageUrl.png");

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidEmail(anyString())).thenReturn(false);
        when(userDao.isEmailAlreadyUsed(anyString())).thenReturn(false);
        when(imageService.getImageExtension(anyString())).thenReturn(ImageExtension.jpg);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The new email address must be different from the current one");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils, imageService);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("newUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("newUsername");
        inOrder.verify(userUtils).isInvalidEmail("old.email@gmail.com");
        inOrder.verify(userDao).isEmailAlreadyUsed("old.email@gmail.com");
        inOrder.verify(imageService).getImageExtension("https://newProfileImageUrl.jpg");
        inOrder.verify(userUtils).getErrorsAsString(List.of("The new email address must be different from the current one"));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The new email address must be different from the current one"));
    }

    @Test
    void shouldReturnBadRequest_whenRoleIsSameAsCurrent() {
        // Arrange
        AdminUpdateUserRequest request = buildAdminUpdateUserRequest("new.email@gmail.com",
                "newUsername", false, "USER", "https://newProfileImageUrl.jpg");

        User userToUpdate = buildUser("id", "oldUsername", "old.email@gmail.com", "password",
                true, UserRole.USER, "https://oldProfileImageUrl.png");

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidEmail(anyString())).thenReturn(false);
        when(userDao.isEmailAlreadyUsed(anyString())).thenReturn(false);
        when(imageService.getImageExtension(anyString())).thenReturn(ImageExtension.jpg);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The new role must be different from the current one");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils, imageService);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("newUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("newUsername");
        inOrder.verify(userUtils).isInvalidEmail("new.email@gmail.com");
        inOrder.verify(userDao).isEmailAlreadyUsed("new.email@gmail.com");
        inOrder.verify(imageService).getImageExtension("https://newProfileImageUrl.jpg");
        inOrder.verify(userUtils).getErrorsAsString(List.of("The new role must be different from the current one"));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The new role must be different from the current one"));
    }

    @Test
    void shouldReturnBadRequest_whenRoleIsNotValid() {
        // Arrange
        AdminUpdateUserRequest request = buildAdminUpdateUserRequest("new.email@gmail.com",
                "newUsername", false, "NOT_VALID", "https://newProfileImageUrl.jpg");

        User userToUpdate = buildUser("id", "oldUsername", "old.email@gmail.com", "password",
                true, UserRole.USER, "https://oldProfileImageUrl.png");

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidEmail(anyString())).thenReturn(false);
        when(userDao.isEmailAlreadyUsed(anyString())).thenReturn(false);
        when(imageService.getImageExtension(anyString())).thenReturn(ImageExtension.jpg);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("Invalid role: NOT_VALID. Allowed values are: [USER, ADMIN]");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils, imageService);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("newUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("newUsername");
        inOrder.verify(userUtils).isInvalidEmail("new.email@gmail.com");
        inOrder.verify(userDao).isEmailAlreadyUsed("new.email@gmail.com");
        inOrder.verify(imageService).getImageExtension("https://newProfileImageUrl.jpg");
        inOrder.verify(userUtils).getErrorsAsString(List.of("Invalid role: NOT_VALID. Allowed values are: [USER, ADMIN]"));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Invalid role: NOT_VALID. Allowed values are: [USER, ADMIN]"));
    }

    @Test
    void shouldReturnBadRequest_whenIsValidEmailValueIsSameAsCurrent() {
        // Arrange
        AdminUpdateUserRequest request = buildAdminUpdateUserRequest("new.email@gmail.com",
                "newUsername", true, "ADMIN", "https://newProfileImageUrl.jpg");

        User userToUpdate = buildUser("id", "oldUsername", "old.email@gmail.com", "password",
                true, UserRole.USER, "https://any.jpg");

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidEmail(anyString())).thenReturn(false);
        when(userDao.isEmailAlreadyUsed(anyString())).thenReturn(false);
        when(imageService.getImageExtension(anyString())).thenReturn(ImageExtension.jpg);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The email verification status is already set to the provided value. No changes were made.");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils, imageService);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("newUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("newUsername");
        inOrder.verify(userUtils).isInvalidEmail("new.email@gmail.com");
        inOrder.verify(userDao).isEmailAlreadyUsed("new.email@gmail.com");
        inOrder.verify(imageService).getImageExtension("https://newProfileImageUrl.jpg");
        inOrder.verify(userUtils).getErrorsAsString(List.of("The email verification status is already set to the provided value. No changes were made."));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The email verification status is already set to the provided value. No changes were made."));
    }

    @Test
    void shouldReturnBadRequest_whenProfileImageUrlDoesNotStartWithHttps() {
        // Arrange
        AdminUpdateUserRequest request = buildAdminUpdateUserRequest("new.email@gmail.com",
                "newUsername", false, "ADMIN", "notValid.png");

        User userToUpdate = buildUser("id", "oldUsername", "old.email@gmail.com", "password",
                true, UserRole.USER, "https://any.jpg");

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidEmail(anyString())).thenReturn(false);
        when(userDao.isEmailAlreadyUsed(anyString())).thenReturn(false);
        when(imageService.getImageExtension(anyString())).thenReturn(ImageExtension.png);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The provided URL must start with 'https://'");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils, imageService);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("newUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("newUsername");
        inOrder.verify(userUtils).isInvalidEmail("new.email@gmail.com");
        inOrder.verify(userDao).isEmailAlreadyUsed("new.email@gmail.com");
        inOrder.verify(imageService).getImageExtension("notValid.png");
        inOrder.verify(userUtils).getErrorsAsString(List.of("The provided URL must start with 'https://'"));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The provided URL must start with 'https://'"));
    }

    @Test
    void shouldReturnBadRequest_whenProfileImageExtensionIsNotValid() {
        // Arrange
        AdminUpdateUserRequest request = buildAdminUpdateUserRequest("new.email@gmail.com",
                "newUsername", false, "ADMIN", "https://notValid");

        User userToUpdate = buildUser("id", "oldUsername", "old.email@gmail.com", "password",
                true, UserRole.USER, "https://any.jpg");

        when(userDao.findById(anyString())).thenReturn(Optional.of(userToUpdate));
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidEmail(anyString())).thenReturn(false);
        when(userDao.isEmailAlreadyUsed(anyString())).thenReturn(false);
        when(imageService.getImageExtension(anyString())).thenReturn(null);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The provided URL does not have a valid image format. Please provide a valid image URL");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.updateUser("id", request);

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils, imageService);
        inOrder.verify(userDao).findById("id");
        inOrder.verify(userUtils).isInvalidUsername("newUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("newUsername");
        inOrder.verify(userUtils).isInvalidEmail("new.email@gmail.com");
        inOrder.verify(userDao).isEmailAlreadyUsed("new.email@gmail.com");
        inOrder.verify(imageService).getImageExtension("https://notValid");
        inOrder.verify(userUtils).getErrorsAsString(List.of("The provided URL does not have a valid image format. Please provide a valid image URL"));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The provided URL does not have a valid image format. Please provide a valid image URL"));
    }

    @Test
    void shouldCreateHotel() throws IOException {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "address",
                "city", "state", "country", "postalCode", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = buildUser("id", "username", null, null, true, UserRole.ADMIN, null);

        when(imageService.getImageExtension("filename1.jpg")).thenReturn(ImageExtension.jpg);
        when(imageUtils.uploadImage(file1)).thenReturn("https://picture1.jpg");
        when(imageService.getImageExtension("filename2.png")).thenReturn(ImageExtension.png);
        when(imageUtils.uploadImage(file2)).thenReturn("https://picture2.png");
        when(uuidProvider.generateUuid())
                .thenReturn("hotelId")
                .thenReturn("hotelLocationId");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        HotelLocation hotelLocation = HotelLocation.builder()
                .id("hotelLocationId")
                .address("address")
                .city("city")
                .state("state")
                .country("country")
                .postalCode("postalCode")
                .googleMapsUrl("https://googleMapsUrl")
                .build();

        Hotel expectedHotel = Hotel.builder()
                .id("hotelId")
                .name("name")
                .description("description")
                .picture_list(List.of("https://picture1.jpg", "https://picture2.png"))
                .amenities(List.of(HotelAmenities.WIFI, HotelAmenities.BAR))
                .location(hotelLocation)
                .rooms(emptyList())
                .build();

        InOrder inOrder = inOrder(imageService, imageUtils, uuidProvider, hotelDao);
        inOrder.verify(imageService).getImageExtension("filename1.jpg");
        inOrder.verify(imageUtils).uploadImage(picture_list.get(0));
        inOrder.verify(imageService).saveNewImage(ImageCategory.HOTEL, "hotel-image-name.jpg", "https://picture1.jpg", ImageExtension.jpg, "id");
        inOrder.verify(imageService).getImageExtension("filename2.png");
        inOrder.verify(imageUtils).uploadImage(picture_list.get(1));
        inOrder.verify(imageService).saveNewImage(ImageCategory.HOTEL, "hotel-image-name.png", "https://picture2.png", ImageExtension.png, "id");
        inOrder.verify(uuidProvider, times(2)).generateUuid();
        inOrder.verify(hotelDao).save(expectedHotel);
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(userUtils);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("message", "Hotel created successfully"));
    }

    @Test
    void shouldReturnBadRequest_whenHotelNameIsNull() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest(null, "description", "address",
                "city", "state", "country", "postalCode", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The hotel name cannot be null or empty");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The hotel name cannot be null or empty"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The hotel name cannot be null or empty"));
    }

    @Test
    void shouldReturnBadRequest_whenHotelNameIsEmpty() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("", "description", "address",
                "city", "state", "country", "postalCode", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The hotel name cannot be null or empty");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The hotel name cannot be null or empty"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The hotel name cannot be null or empty"));
    }

    @Test
    void shouldReturnBadRequest_whenHotelNameIsTooShort() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("ab", "description", "address",
                "city", "state", "country", "postalCode", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The hotel name must be between 3 and 25 characters long");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The hotel name must be between 3 and 25 characters long"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The hotel name must be between 3 and 25 characters long"));
    }

    @Test
    void shouldReturnBadRequest_whenHotelNameIsTooLong() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("ThisHotelNameIsTooLongForExample", "description", "address",
                "city", "state", "country", "postalCode", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The hotel name must be between 3 and 25 characters long");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The hotel name must be between 3 and 25 characters long"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The hotel name must be between 3 and 25 characters long"));
    }

    @Test
    void shouldReturnBadRequest_whenHotelNameContainsSpaces() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest(" Spaces ", "description", "address",
                "city", "state", "country", "postalCode", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The hotel name cannot contain spaces");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The hotel name cannot contain spaces"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The hotel name cannot contain spaces"));
    }

    @Test
    void shouldReturnBadRequest_whenDescriptionIsTooLong() {
        // Arrange
        String description = "This description exceeds the allowed 500-character limit, which violates the established" +
                " rule that text should not exceed this length. Exceeding the limit may lead to errors or cause the" +
                " text to be rejected during the saving or validation process, potentially preventing it from being " +
                "properly stored or processed. It is important to respect this limit to ensure proper system " +
                "functioning, avoid potential issues, and ensure a smooth user experience during submission. Adhering " +
                "to such rules helps maintain consistency and efficiency.";

        CreateHotelRequest request = buildCreateHotelRequest("name", description, "address",
                "city", "state", "country", "postalCode", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The hotel description must be less than or equal to 500 characters long");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The hotel description must be less than or equal to 500 characters long"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The hotel description must be less than or equal to 500 characters long"));
    }

    @Test
    void shouldReturnBadRequest_whenCityIsNull() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "address",
                null, "state", "country", "postalCode", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The city cannot be null or empty");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The city cannot be null or empty"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The city cannot be null or empty"));
    }

    @Test
    void shouldReturnBadRequest_whenCityIsEmpty() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "address",
                "", "state", "country", "postalCode", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The city cannot be null or empty");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The city cannot be null or empty"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The city cannot be null or empty"));
    }

    @Test
    void shouldReturnBadRequest_whenAddressIsNull() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("name", "description", null,
                "city", "state", "country", "postalCode", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The address cannot be null or empty");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The address cannot be null or empty"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The address cannot be null or empty"));
    }

    @Test
    void shouldReturnBadRequest_whenAddressIsEmpty() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "",
                "city", "state", "country", "postalCode", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The address cannot be null or empty");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The address cannot be null or empty"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The address cannot be null or empty"));
    }

    @Test
    void shouldReturnBadRequest_whenAddressIsTooLong() {
        // Arrange
        String address = "The address provided is too long and exceeds the allowed character limit. Please shorten " +
                "the address to fit within the specified length to ensure proper submission.";

        CreateHotelRequest request = buildCreateHotelRequest("name", "description", address,
                "city", "state", "country", "postalCode", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The address must be less than or equal to 100 characters long");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The address must be less than or equal to 100 characters long"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The address must be less than or equal to 100 characters long"));
    }

    @Test
    void shouldReturnBadRequest_whenCountryIsNull() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "address",
                "city", "state", null, "postalCode", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The country cannot be null or empty");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The country cannot be null or empty"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The country cannot be null or empty"));
    }

    @Test
    void shouldReturnBadRequest_whenCountryIsEmpty() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "address",
                "city", "state", "", "postalCode", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The country cannot be null or empty");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The country cannot be null or empty"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The country cannot be null or empty"));
    }

    @Test
    void shouldReturnBadRequest_whenGoogleMapsUrlIsNull() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "address",
                "city", "state", "country", "postalCode", null,
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The Google Maps URL cannot be null or empty");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The Google Maps URL cannot be null or empty"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The Google Maps URL cannot be null or empty"));
    }

    @Test
    void shouldReturnBadRequest_whenGoogleMapsUrlIsEmpty() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "address",
                "city", "state", "country", "postalCode", "",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The Google Maps URL cannot be null or empty");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The Google Maps URL cannot be null or empty"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The Google Maps URL cannot be null or empty"));
    }

    @Test
    void shouldReturnBadRequest_whenGoogleMapsUrlIsInvalid() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "address",
                "city", "state", "country", "postalCode", "invalidUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("Google Maps url must start with 'https://'");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("Google Maps url must start with 'https://'"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Google Maps url must start with 'https://'"));
    }

    @Test
    void shouldReturnBadRequest_whenStateIsNull() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "address",
                "city", null, "country", "postalCode", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The state cannot be null or empty");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The state cannot be null or empty"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The state cannot be null or empty"));
    }

    @Test
    void shouldReturnBadRequest_whenStateIsEmpty() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "address",
                "city", "", "country", "postalCode", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The state cannot be null or empty");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The state cannot be null or empty"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The state cannot be null or empty"));
    }

    @Test
    void shouldReturnBadRequest_whenStateIsTooLong() {
        // Arrange
        String state = "This is an example of a too long state. State must be less than 50 characters :)";

        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "address",
                "city", state, "country", "postalCode", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The state cannot be longer than 50 characters");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The state cannot be longer than 50 characters"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The state cannot be longer than 50 characters"));
    }

    @Test
    void shouldReturnBadRequest_whenPostalCodeIsNull() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "address",
                "city", "state", "country", null, "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The postal code cannot be null or empty");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The postal code cannot be null or empty"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The postal code cannot be null or empty"));
    }

    @Test
    void shouldReturnBadRequest_whenPostalCodeIsEmpty() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "address",
                "city", "state", "country", "", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The postal code cannot be null or empty");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The postal code cannot be null or empty"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The postal code cannot be null or empty"));
    }

    @Test
    void shouldReturnBadRequest_whenPostalCodeIsTooLong() {
        // Arrange
        String postalCode = "10 characters maximum ! :)";

        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "address",
                "city", "state", "country", postalCode, "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The postal code cannot be longer than 10 characters");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The postal code cannot be longer than 10 characters"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The postal code cannot be longer than 10 characters"));
    }

    @Test
    void shouldReturnBadRequest_whenAmenitiesListIsNull() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "address",
                "city", "state", "country", "postalCode", "https://googleMapsUrl",
                null);

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The amenities list cannot be null or empty");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The amenities list cannot be null or empty"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The amenities list cannot be null or empty"));
    }

    @Test
    void shouldReturnBadRequest_whenAmenitiesListIsEmpty() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "address",
                "city", "state", "country", "postalCode", "https://googleMapsUrl",
                emptyList());

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("The amenities list cannot be null or empty");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("The amenities list cannot be null or empty"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The amenities list cannot be null or empty"));
    }

    @Test
    void shouldReturnBadRequest_whenInvalidAmenityInAmenitiesList() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "address",
                "city", "state", "country", "postalCode", "https://googleMapsUrl",
                List.of("INVALID", "BAR"));

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        User authenticatedUser = User.builder().build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("Invalid amenity: INVALID");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("Invalid amenity: INVALID"));
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Invalid amenity: INVALID"));
    }

    @Test
    void shouldReturnBadRequest_whenPictureListIsEmpty() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "address",
                "city", "state", "country", "postalCode", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        List<MultipartFile> picture_list = emptyList();

        User authenticatedUser = User.builder().build();

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao, userUtils);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "At least one valid picture is required"));
    }

    @Test
    void shouldReturnBadRequest_whenPictureListIsNull() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "address",
                "city", "state", "country", "postalCode", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        User authenticatedUser = User.builder().build();

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, null);

        // Assert
        verifyNoInteractions(imageService, imageUtils, uuidProvider, hotelDao, userUtils);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "At least one valid picture is required"));
    }

    @Test
    void shouldReturnBadRequest_whenNoValidPictureInPictureList() {
        // Arrange
        CreateHotelRequest request = buildCreateHotelRequest("name", "description", "address",
                "city", "state", "country", "postalCode", "https://googleMapsUrl",
                List.of("WIFI", "BAR"));

        User authenticatedUser = User.builder().build();

        MockMultipartFile file1 = new MockMultipartFile("file1", "filename1", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});
        MockMultipartFile file2 = new MockMultipartFile("file2", "filename2", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3, 4, 5});
        List<MultipartFile> picture_list = List.of(file1, file2);

        when(imageService.getImageExtension("filename1")).thenReturn(null);
        when(imageService.getImageExtension("filename2")).thenReturn(null);

        // Act
        ResponseEntity<Map<String, String>> response = adminService.createHotel(authenticatedUser, request, picture_list);

        // Assert
        verify(imageService).getImageExtension("filename1");
        verify(imageService).getImageExtension("filename2");
        verifyNoMoreInteractions(imageService);
        verifyNoInteractions(imageUtils, uuidProvider, hotelDao, userUtils);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "At least one valid picture is required"));
    }

    @Test
    void shouldAddNewRoomToHotel() {
        // Arrange
        CreateHotelRoomRequest request = buildCreateHotelRoomRequest("hotelId", "SUITE",
                List.of("SMOKE_DETECTED", "COFFEE_MACHINE"), 152.0, 5);

        HotelLocation hotelLocation = HotelLocation.builder()
                .id("hotelLocationId")
                .address("address")
                .city("city")
                .state("state")
                .country("country")
                .postalCode("postalCode")
                .googleMapsUrl("https://googleMapsUrl")
                .build();

        Hotel hotel = Hotel.builder()
                .id("hotelId")
                .name("name")
                .description("description")
                .picture_list(List.of("https://picture1.jpg", "https://picture2.png"))
                .amenities(List.of(HotelAmenities.WIFI, HotelAmenities.BAR))
                .location(hotelLocation)
                .rooms(emptyList())
                .build();

        when(hotelDao.findById(anyString())).thenReturn(Optional.of(hotel));
        when(uuidProvider.generateUuid()).thenReturn("hotelRoomId");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.addRoomToHotel(request);

        // Assert
        HotelRoom expectedHotelRoom = HotelRoom.builder()
                .id("hotelRoomId")
                .type(HotelRoomType.SUITE)
                .price(152.0)
                .features(List.of(HotelRoomFeatures.SMOKE_DETECTED, HotelRoomFeatures.COFFEE_MACHINE))
                .maxOccupancy(5)
                .build();

        Hotel expectedHotel = Hotel.builder()
                .id("hotelId")
                .name("name")
                .description("description")
                .picture_list(List.of("https://picture1.jpg", "https://picture2.png"))
                .amenities(List.of(HotelAmenities.WIFI, HotelAmenities.BAR))
                .location(hotelLocation)
                .rooms(List.of(expectedHotelRoom))
                .build();

        InOrder inOrder = inOrder(hotelDao, uuidProvider, hotelRoomDao);
        inOrder.verify(hotelDao).findById("hotelId");
        inOrder.verify(uuidProvider).generateUuid();
        inOrder.verify(hotelRoomDao).save(expectedHotelRoom);
        inOrder.verify(hotelDao).save(expectedHotel);
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(userUtils);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("message", "HotelRoom added successfully"));
    }

    @Test
    void shouldReturnBadRequest_whenHotelIdIsNull() {
        // Arrange
        CreateHotelRoomRequest request = buildCreateHotelRoomRequest(null, "SUITE",
                List.of("SMOKE_DETECTED", "COFFEE_MACHINE"), 152.0, 5);

        when(userUtils.getErrorsAsString(anyList())).thenReturn("Hotel ID cannot be null");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.addRoomToHotel(request);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("Hotel ID cannot be null"));

        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(hotelDao, uuidProvider, hotelRoomDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Hotel ID cannot be null"));
    }

    @Test
    void shouldReturnBadRequest_whenRoomTypeIsNull() {
        // Arrange
        CreateHotelRoomRequest request = buildCreateHotelRoomRequest("hotelId", null,
                List.of("SMOKE_DETECTED", "COFFEE_MACHINE"), 152.0, 5);

        when(userUtils.getErrorsAsString(anyList())).thenReturn("Room type cannot be null");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.addRoomToHotel(request);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("Room type cannot be null"));

        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(hotelDao, uuidProvider, hotelRoomDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Room type cannot be null"));
    }

    @Test
    void shouldReturnBadRequest_whenRoomTypeIsInvalid() {
        // Arrange
        CreateHotelRoomRequest request = buildCreateHotelRoomRequest("hotelId", "NOT_VALID",
                List.of("SMOKE_DETECTED", "COFFEE_MACHINE"), 152.0, 5);

        when(userUtils.getErrorsAsString(anyList())).thenReturn("Invalid type: NOT_VALID");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.addRoomToHotel(request);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("Invalid type: NOT_VALID"));

        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(hotelDao, uuidProvider, hotelRoomDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Invalid type: NOT_VALID"));
    }

    @Test
    void shouldReturnBadRequest_whenRoomFeaturesIsNull() {
        // Arrange
        CreateHotelRoomRequest request = buildCreateHotelRoomRequest("hotelId", "SUITE",
                null, 152.0, 5);

        when(userUtils.getErrorsAsString(anyList())).thenReturn("Room features cannot be null or empty");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.addRoomToHotel(request);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("Room features cannot be null or empty"));

        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(hotelDao, uuidProvider, hotelRoomDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Room features cannot be null or empty"));
    }

    @Test
    void shouldReturnBadRequest_whenRoomFeaturesIsEmpty() {
        // Arrange
        CreateHotelRoomRequest request = buildCreateHotelRoomRequest("hotelId", "SUITE",
                emptyList(), 152.0, 5);

        when(userUtils.getErrorsAsString(anyList())).thenReturn("Room features cannot be null or empty");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.addRoomToHotel(request);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("Room features cannot be null or empty"));

        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(hotelDao, uuidProvider, hotelRoomDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Room features cannot be null or empty"));
    }

    @Test
    void shouldReturnBadRequest_whenRoomFeaturesContainInvalidFeature() {
        // Arrange
        CreateHotelRoomRequest request = buildCreateHotelRoomRequest("hotelId", "SUITE",
                List.of("INVALID_FEATURE", "COFFEE_MACHINE"), 152.0, 5);

        when(userUtils.getErrorsAsString(anyList())).thenReturn("Invalid feature: INVALID_FEATURE");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.addRoomToHotel(request);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("Invalid feature: INVALID_FEATURE"));

        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(hotelDao, uuidProvider, hotelRoomDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Invalid feature: INVALID_FEATURE"));
    }

    @Test
    void shouldReturnBadRequest_whenMaxOccupancyIsZeroOrNegative() {
        // Arrange
        CreateHotelRoomRequest request = buildCreateHotelRoomRequest("hotelId", "SUITE",
                List.of("SMOKE_DETECTED", "COFFEE_MACHINE"), 152.0, 0);

        when(userUtils.getErrorsAsString(anyList())).thenReturn("Room capacity must be greater than 0");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.addRoomToHotel(request);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("Room capacity must be greater than 0"));

        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(hotelDao, uuidProvider, hotelRoomDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Room capacity must be greater than 0"));
    }

    @Test
    void shouldReturnBadRequest_whenPriceIsZeroOrNegative() {
        // Arrange
        CreateHotelRoomRequest request = buildCreateHotelRoomRequest("hotelId", "SUITE",
                List.of("SMOKE_DETECTED", "COFFEE_MACHINE"), 0, 5);

        when(userUtils.getErrorsAsString(anyList())).thenReturn("Room price must be greater than 0");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.addRoomToHotel(request);

        // Assert
        verify(userUtils).getErrorsAsString(List.of("Room price must be greater than 0"));

        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(hotelDao, uuidProvider, hotelRoomDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Room price must be greater than 0"));
    }

    @Test
    void shouldReturnNotFound_whenHotelDoesNotExist() {
        // Arrange
        CreateHotelRoomRequest request = buildCreateHotelRoomRequest("hotelId", "SUITE",
                List.of("SMOKE_DETECTED", "COFFEE_MACHINE"), 152.0, 5);

        when(hotelDao.findById(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, String>> response = adminService.addRoomToHotel(request);

        // Assert
        verify(hotelDao).findById("hotelId");

        verifyNoMoreInteractions(hotelDao);
        verifyNoInteractions(userUtils, uuidProvider, hotelRoomDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "Hotel not found"));
    }

    @Test
    void shouldDeleteRoomFromHotel() {
        // Arrange
        DeleteHotelRoomRequest request = new DeleteHotelRoomRequest("hotelRoomId", "hotelId");

        HotelLocation hotelLocation = HotelLocation.builder()
                .id("hotelLocationId")
                .address("address")
                .city("city")
                .state("state")
                .country("country")
                .postalCode("postalCode")
                .googleMapsUrl("https://googleMapsUrl")
                .build();

        HotelRoom hotelRoom = HotelRoom.builder()
                .id("hotelRoomId")
                .type(HotelRoomType.DELUXE)
                .maxOccupancy(8)
                .features(List.of(HotelRoomFeatures.FLAT_SCREEN_TV, HotelRoomFeatures.SAFE))
                .price(150.00)
                .build();

        Hotel hotel = Hotel.builder()
                .id("hotelId")
                .name("name")
                .description("description")
                .picture_list(List.of("https://picture1.jpg", "https://picture2.png"))
                .amenities(List.of(HotelAmenities.WIFI, HotelAmenities.BAR))
                .location(hotelLocation)
                .rooms(List.of(hotelRoom))
                .build();

        when(hotelDao.findById(anyString())).thenReturn(Optional.of(hotel));
        when(hotelRoomDao.findById(anyString())).thenReturn(Optional.of(hotelRoom));

        // Act
        ResponseEntity<Map<String, String>> response = adminService.deleteRoomFromHotel(request);

        // Assert
        Hotel expectedHotel = Hotel.builder()
                .id("hotelId")
                .name("name")
                .description("description")
                .picture_list(List.of("https://picture1.jpg", "https://picture2.png"))
                .amenities(List.of(HotelAmenities.WIFI, HotelAmenities.BAR))
                .location(hotelLocation)
                .rooms(emptyList())
                .build();

        InOrder inOrder = inOrder(hotelDao, hotelRoomDao);
        inOrder.verify(hotelDao).findById("hotelId");
        inOrder.verify(hotelRoomDao).findById("hotelRoomId");
        inOrder.verify(hotelDao).save(expectedHotel);
        inOrder.verify(hotelRoomDao).delete("hotelRoomId");
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("message", "HotelRoom removed successfully"));
    }

    @Test
    void shouldReturnNotFound_whenHotelIdDoesNotExist() {
        // Arrange
        DeleteHotelRoomRequest request = new DeleteHotelRoomRequest("hotelRoomId", "nonExistentId");

        when(hotelDao.findById("nonExistentId")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, String>> response = adminService.deleteRoomFromHotel(request);

        // Assert
        verify(hotelDao).findById("nonExistentId");
        verifyNoMoreInteractions(hotelDao);
        verifyNoInteractions(hotelRoomDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "Hotel not found"));
    }

    @Test
    void shouldReturnNotFound_whenHotelRoomDoesNotExist() {
        // Arrange
        DeleteHotelRoomRequest request = new DeleteHotelRoomRequest("nonExistentId", "hotelId");

        Hotel hotel = Hotel.builder().build();

        when(hotelDao.findById(anyString())).thenReturn(Optional.of(hotel));
        when(hotelRoomDao.findById(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, String>> response = adminService.deleteRoomFromHotel(request);

        // Assert
        InOrder inOrder = inOrder(hotelDao, hotelRoomDao);
        inOrder.verify(hotelDao).findById("hotelId");
        inOrder.verify(hotelRoomDao).findById("nonExistentId");
        inOrder.verifyNoMoreInteractions();

        verifyNoMoreInteractions(hotelDao, hotelRoomDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "HotelRoom not found"));
    }

    @Test
    void shouldReturnBadRequest_whenHotelRoomIsNotInHotel() {
        // Arrange
        DeleteHotelRoomRequest request = new DeleteHotelRoomRequest("hotelRoomId", "hotelId");

        HotelLocation hotelLocation = HotelLocation.builder()
                .id("hotelLocationId")
                .address("address")
                .city("city")
                .state("state")
                .country("country")
                .postalCode("postalCode")
                .googleMapsUrl("https://googleMapsUrl")
                .build();

        HotelRoom hotelRoom = HotelRoom.builder()
                .id("hotelRoomId")
                .type(HotelRoomType.DELUXE)
                .maxOccupancy(8)
                .features(List.of(HotelRoomFeatures.FLAT_SCREEN_TV, HotelRoomFeatures.SAFE))
                .price(150.00)
                .build();

        Hotel hotel = Hotel.builder()
                .id("hotelId")
                .name("name")
                .description("description")
                .picture_list(List.of("https://picture1.jpg", "https://picture2.png"))
                .amenities(List.of(HotelAmenities.WIFI, HotelAmenities.BAR))
                .location(hotelLocation)
                .rooms(emptyList())
                .build();

        when(hotelDao.findById(anyString())).thenReturn(Optional.of(hotel));
        when(hotelRoomDao.findById(anyString())).thenReturn(Optional.of(hotelRoom));

        // Act
        ResponseEntity<Map<String, String>> response = adminService.deleteRoomFromHotel(request);

        // Assert
        InOrder inOrder = inOrder(hotelDao, hotelRoomDao);
        inOrder.verify(hotelDao).findById("hotelId");
        inOrder.verify(hotelRoomDao).findById("hotelRoomId");
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "The room is not in the hotel's list of rooms"));
    }

    @Test
    void shouldDeleteHotel() {
        // Arrange
        HotelLocation hotelLocation = HotelLocation.builder()
                .id("hotelLocationId")
                .address("address")
                .city("city")
                .state("state")
                .country("country")
                .postalCode("postalCode")
                .googleMapsUrl("https://googleMapsUrl")
                .build();

        HotelRoom hotelRoom = HotelRoom.builder()
                .id("hotelRoomId")
                .type(HotelRoomType.DELUXE)
                .maxOccupancy(8)
                .features(List.of(HotelRoomFeatures.FLAT_SCREEN_TV, HotelRoomFeatures.SAFE))
                .price(150.00)
                .build();

        Hotel hotel = Hotel.builder()
                .id("hotelId")
                .name("name")
                .description("description")
                .picture_list(List.of("https://picture1.jpg", "https://picture2.png"))
                .amenities(List.of(HotelAmenities.WIFI, HotelAmenities.BAR))
                .location(hotelLocation)
                .rooms(List.of(hotelRoom))
                .build();

        when(hotelDao.findById(anyString())).thenReturn(Optional.of(hotel));

        // Act
        ResponseEntity<Map<String, String>> response = adminService.deleteHotel("hotelId");

        // Assert
        InOrder inOrder = inOrder(hotelDao, hotelRoomDao);
        inOrder.verify(hotelDao).findById("hotelId");
        inOrder.verify(hotelRoomDao).delete("hotelRoomId");
        inOrder.verify(hotelDao).delete("hotelId");
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("message", "Hotel deleted successfully"));
    }

    @Test
    void shouldReturnNotFound_whenHotelIsNotFound() {
        // Arrange
        when(hotelDao.findById(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, String>> response = adminService.deleteHotel("hotelId");

        // Assert
        verify(hotelDao).findById("hotelId");
        verifyNoMoreInteractions(hotelDao);
        verifyNoInteractions(hotelRoomDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "Hotel not found"));
    }

    @Test
    void shouldAddPictureToHotel() throws IOException {
        // Arrange
        HotelLocation hotelLocation = HotelLocation.builder()
                .id("hotelLocationId")
                .address("address")
                .city("city")
                .state("state")
                .country("country")
                .postalCode("postalCode")
                .googleMapsUrl("https://googleMapsUrl")
                .build();

        HotelRoom hotelRoom = HotelRoom.builder()
                .id("hotelRoomId")
                .type(HotelRoomType.DELUXE)
                .maxOccupancy(8)
                .features(List.of(HotelRoomFeatures.FLAT_SCREEN_TV, HotelRoomFeatures.SAFE))
                .price(150.00)
                .build();

        Hotel hotel = Hotel.builder()
                .id("hotelId")
                .name("name")
                .description("description")
                .picture_list(List.of("https://picture1.jpg", "https://picture2.png"))
                .amenities(List.of(HotelAmenities.WIFI, HotelAmenities.BAR))
                .location(hotelLocation)
                .rooms(List.of(hotelRoom))
                .build();

        User authenticatedUser = User.builder()
                .id("authenticatedUserId")
                .build();

        MockMultipartFile picture = new MockMultipartFile("picture", "picture.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});

        when(hotelDao.findById(anyString())).thenReturn(Optional.of(hotel));
        when(imageService.getImageExtension(anyString())).thenReturn(ImageExtension.jpg);
        when(imageUtils.uploadImage(any())).thenReturn("imageUrl");

        // Act
        ResponseEntity<Map<String, String>> response = adminService.addHotelPicture(authenticatedUser, "hotelId", picture);

        // Assert
        Hotel expectedHotel = Hotel.builder()
                .id("hotelId")
                .name("name")
                .description("description")
                .picture_list(List.of("https://picture1.jpg", "https://picture2.png", "imageUrl"))
                .amenities(List.of(HotelAmenities.WIFI, HotelAmenities.BAR))
                .location(hotelLocation)
                .rooms(List.of(hotelRoom))
                .build();

        InOrder inOrder = inOrder(hotelDao, imageService, imageUtils, hotelDao);
        inOrder.verify(hotelDao).findById("hotelId");
        inOrder.verify(imageService).getImageExtension("picture.jpg");
        inOrder.verify(imageUtils).uploadImage(picture);
        inOrder.verify(imageService).saveNewImage(ImageCategory.HOTEL, "hotel-image-name.jpg", "imageUrl", ImageExtension.jpg, "authenticatedUserId");
        inOrder.verify(hotelDao).save(expectedHotel);
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("message", "Picture added successfully"));
    }

    @Test
    void shouldReturnNotFound_whenHotelDoesNotExistInDatabase() {
        // Arrange
        User authenticatedUser = User.builder()
                .id("authenticatedUserId")
                .build();

        MockMultipartFile picture = new MockMultipartFile("picture", "picture.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});

        when(hotelDao.findById(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, String>> response = adminService.addHotelPicture(authenticatedUser, "hotelId", picture);

        // Assert
        verify(hotelDao).findById("hotelId");
        verifyNoMoreInteractions(hotelDao);
        verifyNoInteractions(imageService, imageUtils);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "Hotel not found"));
    }

    @Test
    void shouldReturnBadRequest_whenPictureIsInvalid() {
        // Arrange
        HotelLocation hotelLocation = HotelLocation.builder()
                .id("hotelLocationId")
                .address("address")
                .city("city")
                .state("state")
                .country("country")
                .postalCode("postalCode")
                .googleMapsUrl("https://googleMapsUrl")
                .build();

        HotelRoom hotelRoom = HotelRoom.builder()
                .id("hotelRoomId")
                .type(HotelRoomType.DELUXE)
                .maxOccupancy(8)
                .features(List.of(HotelRoomFeatures.FLAT_SCREEN_TV, HotelRoomFeatures.SAFE))
                .price(150.00)
                .build();

        Hotel hotel = Hotel.builder()
                .id("hotelId")
                .name("name")
                .description("description")
                .picture_list(List.of("https://picture1.jpg", "https://picture2.png"))
                .amenities(List.of(HotelAmenities.WIFI, HotelAmenities.BAR))
                .location(hotelLocation)
                .rooms(List.of(hotelRoom))
                .build();

        User authenticatedUser = User.builder()
                .id("authenticatedUserId")
                .build();

        MockMultipartFile picture = new MockMultipartFile("picture", "invalid", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3, 4, 5});

        when(hotelDao.findById(anyString())).thenReturn(Optional.of(hotel));
        when(imageService.getImageExtension(anyString())).thenReturn(null);

        // Act
        ResponseEntity<Map<String, String>> response = adminService.addHotelPicture(authenticatedUser, "hotelId", picture);

        // Assert
        InOrder inOrder = inOrder(hotelDao, imageService);
        inOrder.verify(hotelDao).findById("hotelId");
        inOrder.verify(imageService).getImageExtension("invalid");
        inOrder.verifyNoMoreInteractions();

        verifyNoMoreInteractions(hotelDao, imageService);
        verifyNoInteractions(imageUtils);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "The provided picture is invalid or missing"));
    }

    @Test
    void shouldDeletePictureFromHotel() {
        // Arrange
        RemovePictureFromHotelRequest request = new RemovePictureFromHotelRequest();
        request.setPictureLink("https://picture1.jpg");

        HotelLocation hotelLocation = HotelLocation.builder()
                .id("hotelLocationId")
                .address("address")
                .city("city")
                .state("state")
                .country("country")
                .postalCode("postalCode")
                .googleMapsUrl("https://googleMapsUrl")
                .build();

        Hotel hotel = Hotel.builder()
                .id("hotelId")
                .name("name")
                .description("description")
                .picture_list(List.of("https://picture1.jpg", "https://picture2.png"))
                .amenities(List.of(HotelAmenities.WIFI, HotelAmenities.BAR))
                .location(hotelLocation)
                .rooms(emptyList())
                .build();

        when(hotelDao.findById(anyString())).thenReturn(Optional.of(hotel));

        // Act
        ResponseEntity<Map<String, String>> response = adminService.deleteHotelPicture("hotelId", request);

        // Assert
        Hotel expectedHotel = Hotel.builder()
                .id("hotelId")
                .name("name")
                .description("description")
                .picture_list(List.of("https://picture2.png"))
                .amenities(List.of(HotelAmenities.WIFI, HotelAmenities.BAR))
                .location(hotelLocation)
                .rooms(emptyList())
                .build();

        InOrder inOrder = inOrder(hotelDao);
        inOrder.verify(hotelDao).findById("hotelId");
        inOrder.verify(hotelDao).save(expectedHotel);
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("message", "Picture removed successfully"));
    }

    @Test
    void shouldReturnNotFound_whenHotelIsNotFoundInDatabase() {
        // Arrange
        RemovePictureFromHotelRequest request = new RemovePictureFromHotelRequest();
        request.setPictureLink("https://picture1.jpg");

        when(hotelDao.findById(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, String>> response = adminService.deleteHotelPicture("hotelId", request);

        // Assert
        verify(hotelDao).findById("hotelId");
        verifyNoMoreInteractions(hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "Hotel not found"));
    }

    @Test
    void shouldReturnBadRequest_whenPictureIsNotInHotelList() {
        // Arrange
        RemovePictureFromHotelRequest request = new RemovePictureFromHotelRequest();
        request.setPictureLink("anyLink");

        HotelLocation hotelLocation = HotelLocation.builder()
                .id("hotelLocationId")
                .address("address")
                .city("city")
                .state("state")
                .country("country")
                .postalCode("postalCode")
                .googleMapsUrl("https://googleMapsUrl")
                .build();

        Hotel hotel = Hotel.builder()
                .id("hotelId")
                .name("name")
                .description("description")
                .picture_list(List.of("https://picture1.jpg", "https://picture2.png"))
                .amenities(List.of(HotelAmenities.WIFI, HotelAmenities.BAR))
                .location(hotelLocation)
                .rooms(emptyList())
                .build();

        when(hotelDao.findById(anyString())).thenReturn(Optional.of(hotel));

        // Act
        ResponseEntity<Map<String, String>> response = adminService.deleteHotelPicture("hotelId", request);

        // Assert
        verify(hotelDao).findById("hotelId");
        verifyNoMoreInteractions(hotelDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "The picture is not in the hotel's list of pictures"));
    }

    @Test
    void shouldReturnNotFoundError_whenUserDoesNotExist() {
        // Arrange
        String userId = "userId";

        when(userDao.exists(anyString())).thenReturn(false);

        // Act
        ResponseEntity<Map<String, AdminGetBookingsResponse>> response = adminService.getAllUserBookings(userId);

        // Assert
        AdminGetBookingsResponse expectedResponse = AdminGetBookingsResponse.builder()
                .error("User not found")
                .bookings(null)
                .build();

        verify(userDao).exists("userId");
        verifyNoMoreInteractions(userDao);
        verifyNoInteractions(bookingDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", expectedResponse));
    }

    @Test
    void shouldReturnBookings() {
        // Arrange
        String userId = "userId";

        Booking booking1 = Booking.builder()
                .id("bookingId1")
                .checkInDate(new Date(1705276800000L))
                .checkOutDate(new Date(1705612800000L))
                .userId("userId")
                .hotelRoom(null)
                .status(BookingStatus.CONFIRMED)
                .isPaid(true)
                .totalPrice(200.0)
                .build();

        Booking booking2 = Booking.builder()
                .id("bookingId2")
                .checkInDate(new Date(1705276800000L))
                .checkOutDate(new Date(1705612800000L))
                .userId("userId")
                .hotelRoom(null)
                .status(BookingStatus.CONFIRMED)
                .isPaid(true)
                .totalPrice(200.0)
                .build();

        when(userDao.exists(anyString())).thenReturn(true);
        when(bookingDao.getBookings(anyString())).thenReturn(List.of(booking1, booking2));

        // Act
        ResponseEntity<Map<String, AdminGetBookingsResponse>> response = adminService.getAllUserBookings(userId);

        // Assert
        AdminGetBookingsResponse expectedResponse = AdminGetBookingsResponse.builder()
                .error(null)
                .bookings(List.of(booking1, booking2))
                .build();

        InOrder inOrder = inOrder(userDao, bookingDao);
        inOrder.verify(userDao).exists("userId");
        inOrder.verify(bookingDao).getBookings("userId");
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("informations", expectedResponse));
    }

    private CreateHotelRoomRequest buildCreateHotelRoomRequest(String hotelId, String type, List<String> features, double price, int maxOccupancy) {
        CreateHotelRoomRequest request = new CreateHotelRoomRequest();
        request.setHotelId(hotelId);
        request.setType(type);
        request.setFeatures(features);
        request.setPrice(price);
        request.setMaxOccupancy(maxOccupancy);

        return request;
    }

    private AdminUpdateUserRequest buildAdminUpdateUserRequest(String email, String username, boolean isValidEmail, String role, String profileImageUrl) {
        AdminUpdateUserRequest request = new AdminUpdateUserRequest();
        request.setEmail(email);
        request.setUsername(username);
        request.setIsValidEmail(isValidEmail);
        request.setRole(role);
        request.setProfileImageUrl(profileImageUrl);

        return request;
    }

    private CreateHotelRequest buildCreateHotelRequest(String name, String description, String address, String city,
                                                       String state, String country, String postalCode,
                                                       String googleMapsUrl, List<String> amenities) {
        CreateHotelRequest request = new CreateHotelRequest();
        request.setName(name);
        request.setDescription(description);
        request.setAddress(address);
        request.setCity(city);
        request.setState(state);
        request.setCountry(country);
        request.setPostalCode(postalCode);
        request.setGoogleMapsUrl(googleMapsUrl);
        request.setAmenities(amenities);

        return request;
    }

    private User buildUser(String id, String username, String email, String password, boolean isValidEmail, UserRole role, String profileImageUrl) {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .password(password)
                .isValidEmail(isValidEmail)
                .role(role)
                .profileImageUrl(profileImageUrl)
                .build();
    }
}