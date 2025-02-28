package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.UserDao;
import com.akkorhotel.hotel.model.ImageCategory;
import com.akkorhotel.hotel.model.ImageExtension;
import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.UserRole;
import com.akkorhotel.hotel.model.request.UpdateUserRequest;
import com.akkorhotel.hotel.model.response.GetAuthenticatedUserResponse;
import com.akkorhotel.hotel.utils.ImageUtils;
import com.akkorhotel.hotel.utils.UserUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserDao userDao;

    @Mock
    private UserUtils userUtils;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private ImageService imageService;

    @Mock
    private ImageUtils imageUtils;

    @Test
    void shouldReturnAuthenticatedUserInformations() {
        // Arrange
        User authenticatedUser = User.builder()
                .id("id")
                .username("username")
                .email("email")
                .password("password")
                .role(UserRole.USER)
                .isValidEmail(true)
                .profileImageUrl("profileImageUrl")
                .build();

        // Act
        ResponseEntity<Map<String, GetAuthenticatedUserResponse>> response = userService.getAuthenticatedUser(authenticatedUser);

        // Assert
        GetAuthenticatedUserResponse expectedResponse = GetAuthenticatedUserResponse.builder()
                .username("username")
                .email("email")
                .userRole("USER")
                .profileImageUrl("profileImageUrl")
                .build();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("informations", expectedResponse));
    }

    @Test
    void shouldUpdateUser() {
        // Arrange
        UpdateUserRequest userRequest = new UpdateUserRequest();
        userRequest.setEmail("new.email@gmail.com");
        userRequest.setUsername("newUsername");
        userRequest.setOldPassword("oldPassword123#!");
        userRequest.setNewPassword("newPassword123#!");

        User authenticatedUser = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("oldPassword123#!")
                .role(UserRole.USER)
                .isValidEmail(true)
                .profileImageUrl("profileImageUrl")
                .build();

        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidEmail(anyString())).thenReturn(false);
        when(userDao.isEmailAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidPassword(anyString())).thenReturn(false);
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false)
                .thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userUtils.sendRegisterConfirmationEmail(any())).thenReturn(null);

        // Act
        ResponseEntity<Map<String, String>> response = userService.updateUser(userRequest, authenticatedUser);

        // Assert
        User expectedUser = User.builder()
                .id("id")
                .username("newUsername")
                .email("new.email@gmail.com")
                .password("encodedPassword")
                .isValidEmail(false)
                .role(UserRole.USER)
                .profileImageUrl("profileImageUrl")
                .build();

        InOrder inOrder = inOrder(userUtils, userDao, passwordEncoder);
        inOrder.verify(userUtils).isInvalidUsername("newUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("newUsername");
        inOrder.verify(userUtils).isInvalidEmail("new.email@gmail.com");
        inOrder.verify(userDao).isEmailAlreadyUsed("new.email@gmail.com");
        inOrder.verify(userUtils).isInvalidPassword("newPassword123#!");
        inOrder.verify(passwordEncoder).matches("newPassword123#!","oldPassword123#!");
        inOrder.verify(passwordEncoder).matches("oldPassword123#!","oldPassword123#!");
        inOrder.verify(passwordEncoder).encode("newPassword123#!");
        inOrder.verify(userDao).save(expectedUser);
        inOrder.verify(userUtils).sendRegisterConfirmationEmail(expectedUser);
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("message", "User details updated successfully"));
    }

    @Test
    void shouldReturnBadRequest_whenNoValuesProvidedForUpdate() {
        // Arrange
        UpdateUserRequest userRequest = new UpdateUserRequest();

        User authenticatedUser = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("oldPassword123#!")
                .role(UserRole.USER)
                .isValidEmail(true)
                .profileImageUrl("profileImageUrl")
                .build();

        when(userUtils.getErrorsAsString(anyList())).thenReturn("No values provided for update. Please specify at least one field (email, username, or new password).");

        // Act
        ResponseEntity<Map<String, String>> response = userService.updateUser(userRequest, authenticatedUser);

        // Assert
        verifyNoMoreInteractions(userUtils);
        verifyNoInteractions(userDao, passwordEncoder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "No values provided for update. Please specify at least one field (email, username, or new password)."));
    }

    @Test
    void shouldReturnBadRequest_whenUsernameIsInvalid() {
        // Arrange
        UpdateUserRequest userRequest = new UpdateUserRequest();
        userRequest.setUsername("invalidUsername");

        User authenticatedUser = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("oldPassword123#!")
                .role(UserRole.USER)
                .isValidEmail(true)
                .profileImageUrl("profileImageUrl")
                .build();

        when(userUtils.isInvalidUsername(anyString())).thenReturn(true);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("Invalid username: Must be 3-11 characters and cannot contain spaces.");

        // Act
        ResponseEntity<Map<String, String>> response = userService.updateUser(userRequest, authenticatedUser);

        // Assert
        InOrder inOrder = inOrder(userUtils, userDao);
        inOrder.verify(userUtils).isInvalidUsername("invalidUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("invalidUsername");
        inOrder.verify(userUtils).getErrorsAsString(List.of("Invalid username: Must be 3-11 characters and cannot contain spaces."));
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(passwordEncoder);

        assertThat(authenticatedUser.getUsername()).isEqualTo("invalidUsername");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Invalid username: Must be 3-11 characters and cannot contain spaces."));
    }

    @Test
    void shouldReturnBadRequest_whenUsernameIsAlreadyUsed() {
        // Arrange
        UpdateUserRequest userRequest = new UpdateUserRequest();
        userRequest.setUsername("alreadyUsedUsername");

        User authenticatedUser = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("oldPassword123#!")
                .role(UserRole.USER)
                .isValidEmail(true)
                .profileImageUrl("profileImageUrl")
                .build();

        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(true);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("Username already taken: Please choose a different one.");

        // Act
        ResponseEntity<Map<String, String>> response = userService.updateUser(userRequest, authenticatedUser);

        // Assert
        InOrder inOrder = inOrder(userUtils, userDao);
        inOrder.verify(userUtils).isInvalidUsername("alreadyUsedUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("alreadyUsedUsername");
        inOrder.verify(userUtils).getErrorsAsString(List.of("Username already taken: Please choose a different one."));
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(passwordEncoder);

        assertThat(authenticatedUser.getUsername()).isEqualTo("alreadyUsedUsername");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Username already taken: Please choose a different one."));
    }

    @Test
    void shouldReturnBadRequest_whenUsernameIsSameAsCurrent() {
        // Arrange
        UpdateUserRequest userRequest = new UpdateUserRequest();
        userRequest.setUsername("oldUsername");

        User authenticatedUser = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("oldPassword123#!")
                .role(UserRole.USER)
                .isValidEmail(true)
                .profileImageUrl("profileImageUrl")
                .build();

        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("Username cannot be the same as the current one.");

        // Act
        ResponseEntity<Map<String, String>> response = userService.updateUser(userRequest, authenticatedUser);

        // Assert
        InOrder inOrder = inOrder(userUtils, userDao);
        inOrder.verify(userUtils).isInvalidUsername("oldUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("oldUsername");
        inOrder.verify(userUtils).getErrorsAsString(List.of("Username cannot be the same as the current one."));
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(passwordEncoder);

        assertThat(authenticatedUser.getUsername()).isEqualTo("oldUsername");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Username cannot be the same as the current one."));
    }

    @Test
    void shouldReturnBadRequest_whenEmailIsInvalid() {
        // Arrange
        UpdateUserRequest userRequest = new UpdateUserRequest();
        userRequest.setEmail("invalidEmail");

        User authenticatedUser = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("oldPassword123#!")
                .role(UserRole.USER)
                .isValidEmail(true)
                .profileImageUrl("profileImageUrl")
                .build();

        when(userUtils.isInvalidEmail(anyString())).thenReturn(true);
        when(userDao.isEmailAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("Invalid email format.");

        // Act
        ResponseEntity<Map<String, String>> response = userService.updateUser(userRequest, authenticatedUser);

        // Assert
        InOrder inOrder = inOrder(userUtils, userDao);
        inOrder.verify(userUtils).isInvalidEmail("invalidEmail");
        inOrder.verify(userDao).isEmailAlreadyUsed("invalidEmail");
        inOrder.verify(userUtils).getErrorsAsString(List.of("Invalid email format."));
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(passwordEncoder);

        assertThat(authenticatedUser.getIsValidEmail()).isFalse();
        assertThat(authenticatedUser.getEmail()).isEqualTo("invalidEmail");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Invalid email format."));
    }

    @Test
    void shouldReturnBadRequest_whenEmailIsAlreadyUsed() {
        // Arrange
        UpdateUserRequest userRequest = new UpdateUserRequest();
        userRequest.setEmail("alreadyUsedEmail");

        User authenticatedUser = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("oldPassword123#!")
                .role(UserRole.USER)
                .isValidEmail(true)
                .profileImageUrl("profileImageUrl")
                .build();

        when(userUtils.isInvalidEmail(anyString())).thenReturn(false);
        when(userDao.isEmailAlreadyUsed(anyString())).thenReturn(true);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("This email is already used.");

        // Act
        ResponseEntity<Map<String, String>> response = userService.updateUser(userRequest, authenticatedUser);

        // Assert
        InOrder inOrder = inOrder(userUtils, userDao);
        inOrder.verify(userUtils).isInvalidEmail("alreadyUsedEmail");
        inOrder.verify(userDao).isEmailAlreadyUsed("alreadyUsedEmail");
        inOrder.verify(userUtils).getErrorsAsString(List.of("This email is already used."));
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(passwordEncoder);

        assertThat(authenticatedUser.getIsValidEmail()).isFalse();
        assertThat(authenticatedUser.getEmail()).isEqualTo("alreadyUsedEmail");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "This email is already used."));
    }

    @Test
    void shouldReturnBadRequest_whenEmailIsSameAsCurrent() {
        // Arrange
        UpdateUserRequest userRequest = new UpdateUserRequest();
        userRequest.setEmail("old.email@gmail.com");

        User authenticatedUser = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("oldPassword123#!")
                .role(UserRole.USER)
                .isValidEmail(true)
                .profileImageUrl("profileImageUrl")
                .build();

        when(userUtils.isInvalidEmail(anyString())).thenReturn(false);
        when(userDao.isEmailAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The new email address must be different from the current one.");

        // Act
        ResponseEntity<Map<String, String>> response = userService.updateUser(userRequest, authenticatedUser);

        // Assert
        InOrder inOrder = inOrder(userUtils, userDao);
        inOrder.verify(userUtils).isInvalidEmail("old.email@gmail.com");
        inOrder.verify(userDao).isEmailAlreadyUsed("old.email@gmail.com");
        inOrder.verify(userUtils).getErrorsAsString(List.of("The new email address must be different from the current one."));
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(passwordEncoder);

        assertThat(authenticatedUser.getIsValidEmail()).isFalse();
        assertThat(authenticatedUser.getEmail()).isEqualTo("old.email@gmail.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The new email address must be different from the current one."));
    }

    @Test
    void shouldReturnBadRequest_whenNewPasswordIsInvalid() {
        // Arrange
        UpdateUserRequest userRequest = new UpdateUserRequest();
        userRequest.setOldPassword("oldPassword123#!");
        userRequest.setNewPassword("invalidPassword");

        User authenticatedUser = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("oldPassword123#!")
                .role(UserRole.USER)
                .isValidEmail(true)
                .profileImageUrl("profileImageUrl")
                .build();

        when(userUtils.isInvalidPassword(anyString())).thenReturn(true);
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false)
                .thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userUtils.getErrorsAsString(anyList())).thenReturn("The new password does not meet the required criteria.");

        // Act
        ResponseEntity<Map<String, String>> response = userService.updateUser(userRequest, authenticatedUser);

        // Assert
        InOrder inOrder = inOrder(userUtils, passwordEncoder);
        inOrder.verify(userUtils).isInvalidPassword("invalidPassword");
        inOrder.verify(passwordEncoder).matches("invalidPassword","oldPassword123#!");
        inOrder.verify(passwordEncoder).matches("oldPassword123#!","oldPassword123#!");
        inOrder.verify(passwordEncoder).encode("invalidPassword");
        inOrder.verify(userUtils).getErrorsAsString(List.of("The new password does not meet the required criteria."));
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(userDao);

        assertThat(authenticatedUser.getPassword()).isEqualTo("encodedPassword");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "The new password does not meet the required criteria."));
    }

    @Test
    void shouldReturnBadRequest_whenNewPasswordIsSameAsOldPassword() {
        // Arrange
        UpdateUserRequest userRequest = new UpdateUserRequest();
        userRequest.setOldPassword("oldPassword123#!");
        userRequest.setNewPassword("oldPassword123#!");

        User authenticatedUser = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("oldPassword123#!")
                .role(UserRole.USER)
                .isValidEmail(true)
                .profileImageUrl("profileImageUrl")
                .build();

        when(userUtils.isInvalidPassword(anyString())).thenReturn(false);
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true)
                .thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userUtils.getErrorsAsString(anyList())).thenReturn("New password must be different from the old password.");

        // Act
        ResponseEntity<Map<String, String>> response = userService.updateUser(userRequest, authenticatedUser);

        // Assert
        InOrder inOrder = inOrder(userUtils, passwordEncoder);
        inOrder.verify(userUtils).isInvalidPassword("oldPassword123#!");
        inOrder.verify(passwordEncoder, times(2)).matches("oldPassword123#!","oldPassword123#!");
        inOrder.verify(passwordEncoder).encode("oldPassword123#!");
        inOrder.verify(userUtils).getErrorsAsString(List.of("New password must be different from the old password."));
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(userDao);

        assertThat(authenticatedUser.getPassword()).isEqualTo("encodedPassword");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "New password must be different from the old password."));
    }

    @Test
    void shouldReturnBadRequest_whenOldPasswordIsIncorrect() {
        // Arrange
        UpdateUserRequest userRequest = new UpdateUserRequest();
        userRequest.setOldPassword("incorrectPassword");
        userRequest.setNewPassword("newPassword123#!");

        User authenticatedUser = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("oldPassword123#!")
                .role(UserRole.USER)
                .isValidEmail(true)
                .profileImageUrl("profileImageUrl")
                .build();

        when(userUtils.isInvalidPassword(anyString())).thenReturn(false);
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false)
                .thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userUtils.getErrorsAsString(anyList())).thenReturn("Old password is incorrect.");

        // Act
        ResponseEntity<Map<String, String>> response = userService.updateUser(userRequest, authenticatedUser);

        // Assert
        InOrder inOrder = inOrder(userUtils, passwordEncoder);
        inOrder.verify(userUtils).isInvalidPassword("newPassword123#!");
        inOrder.verify(passwordEncoder).matches("newPassword123#!","oldPassword123#!");
        inOrder.verify(passwordEncoder).matches("incorrectPassword","oldPassword123#!");
        inOrder.verify(passwordEncoder).encode("newPassword123#!");
        inOrder.verify(userUtils).getErrorsAsString(List.of("Old password is incorrect."));
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(userDao);

        assertThat(authenticatedUser.getPassword()).isEqualTo("encodedPassword");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Old password is incorrect."));
    }

    @Test
    void shouldReturnOkWithWarning_whenEmailConfirmationFails() {
        // Arrange
        UpdateUserRequest userRequest = new UpdateUserRequest();
        userRequest.setEmail("new.email@gmail.com");
        userRequest.setUsername("newUsername");
        userRequest.setOldPassword("oldPassword123#!");
        userRequest.setNewPassword("newPassword123#!");

        User authenticatedUser = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("oldPassword123#!")
                .role(UserRole.USER)
                .isValidEmail(true)
                .profileImageUrl("profileImageUrl")
                .build();

        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidEmail(anyString())).thenReturn(false);
        when(userDao.isEmailAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidPassword(anyString())).thenReturn(false);
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false)
                .thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userUtils.sendRegisterConfirmationEmail(any())).thenReturn("Error while sending register confirmation email");

        // Act
        ResponseEntity<Map<String, String>> response = userService.updateUser(userRequest, authenticatedUser);

        // Assert
        User expectedUser = User.builder()
                .id("id")
                .username("newUsername")
                .email("new.email@gmail.com")
                .password("encodedPassword")
                .isValidEmail(false)
                .role(UserRole.USER)
                .profileImageUrl("profileImageUrl")
                .build();

        InOrder inOrder = inOrder(userUtils, userDao, passwordEncoder);
        inOrder.verify(userUtils).isInvalidUsername("newUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("newUsername");
        inOrder.verify(userUtils).isInvalidEmail("new.email@gmail.com");
        inOrder.verify(userDao).isEmailAlreadyUsed("new.email@gmail.com");
        inOrder.verify(userUtils).isInvalidPassword("newPassword123#!");
        inOrder.verify(passwordEncoder).matches("newPassword123#!","oldPassword123#!");
        inOrder.verify(passwordEncoder).matches("oldPassword123#!","oldPassword123#!");
        inOrder.verify(passwordEncoder).encode("newPassword123#!");
        inOrder.verify(userDao).save(expectedUser);
        inOrder.verify(userUtils).sendRegisterConfirmationEmail(expectedUser);
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("warning", "Error while sending register confirmation email"));
    }

    @Test
    void shouldReturnMultipleErrors_whenMultipleValidationFailuresOccur() {
        // Arrange
        UpdateUserRequest userRequest = new UpdateUserRequest();
        userRequest.setEmail("not_valid@gmail.com");
        userRequest.setUsername("alreadyUsedUsername");
        userRequest.setOldPassword("oldPassword123#!");
        userRequest.setNewPassword("notValidPassword");

        User authenticatedUser = User.builder()
                .id("id")
                .username("oldUsername")
                .email("old.email@gmail.com")
                .password("oldPassword123#!")
                .role(UserRole.USER)
                .isValidEmail(true)
                .profileImageUrl("profileImageUrl")
                .build();

        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userDao.isUsernameAlreadyUsed(anyString())).thenReturn(true);
        when(userUtils.isInvalidEmail(anyString())).thenReturn(true);
        when(userDao.isEmailAlreadyUsed(anyString())).thenReturn(false);
        when(userUtils.isInvalidPassword(anyString())).thenReturn(true);
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false)
                .thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userUtils.getErrorsAsString(anyList())).thenReturn("Username already taken: Please choose a different one. | Invalid email format. | The new password does not meet the required criteria.");

        // Act
        ResponseEntity<Map<String, String>> response = userService.updateUser(userRequest, authenticatedUser);

        // Assert
        InOrder inOrder = inOrder(userUtils, userDao, passwordEncoder);
        inOrder.verify(userUtils).isInvalidUsername("alreadyUsedUsername");
        inOrder.verify(userDao).isUsernameAlreadyUsed("alreadyUsedUsername");
        inOrder.verify(userUtils).isInvalidEmail("not_valid@gmail.com");
        inOrder.verify(userDao).isEmailAlreadyUsed("not_valid@gmail.com");
        inOrder.verify(userUtils).isInvalidPassword("notValidPassword");
        inOrder.verify(passwordEncoder).matches("notValidPassword","oldPassword123#!");
        inOrder.verify(passwordEncoder).matches("oldPassword123#!","oldPassword123#!");
        inOrder.verify(passwordEncoder).encode("notValidPassword");
        inOrder.verify(userUtils).getErrorsAsString(List.of("Username already taken: Please choose a different one.", "Invalid email format.", "The new password does not meet the required criteria."));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("errors", "Username already taken: Please choose a different one. | Invalid email format. | The new password does not meet the required criteria."));
    }

    @Test
    void shouldDeleteAuthenticatedUser() {
        // Act
        ResponseEntity<Map<String, String>> response = userService.deleteUser("authenticatedUserId");

        // Assert
        verify(userDao).delete("authenticatedUserId");
        verifyNoMoreInteractions(userDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("message", "User deleted successfully"));
    }

    @Test
    void shouldUploadUserProfileImage_whenValidFileProvided() throws IOException {
        // Arrange
        User authenticatedUser = User.builder()
                .id("userId")
                .username("username")
                .email("email@gmail.com")
                .password("Password123#!")
                .role(UserRole.USER)
                .isValidEmail(true)
                .profileImageUrl("https://any.png")
                .build();

        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("profile.jpg");
        when(imageService.getImageExtension(anyString())).thenReturn(ImageExtension.jpg);
        when(imageUtils.uploadImage(any())).thenReturn("https://example.com/images/username-userId.jpg");

        // Act
        ResponseEntity<Map<String, String>> response = userService.uploadUserProfileImage(authenticatedUser, file);

        // Assert
        InOrder inOrder = inOrder(file, imageService, imageUtils);
        inOrder.verify(file).isEmpty();
        inOrder.verify(file).getOriginalFilename();
        inOrder.verify(imageService).getImageExtension("profile.jpg");
        inOrder.verify(imageUtils).uploadImage(file);
        inOrder.verify(imageService).saveNewImage(
                ImageCategory.USER,
                "username-userId.jpg",
                "https://example.com/images/username-userId.jpg",
                ImageExtension.jpg,
                "userId"
        );
        inOrder.verifyNoMoreInteractions();

        assertThat(authenticatedUser.getProfileImageUrl()).isEqualTo("https://example.com/images/username-userId.jpg");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("message", "Profile image uploaded successfully"));
    }

    @Test
    void shouldReturnBadRequest_whenFileIsEmpty() throws IOException {
        // Arrange
        User authenticatedUser = User.builder()
                .id("userId")
                .username("username")
                .email("email@gmail.com")
                .password("Password123#!")
                .role(UserRole.USER)
                .isValidEmail(true)
                .profileImageUrl("https://any.png")
                .build();

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        // Act
        ResponseEntity<Map<String, String>> response = userService.uploadUserProfileImage(authenticatedUser, file);

        // Assert
        verify(file).isEmpty();
        verifyNoMoreInteractions(file);
        verifyNoInteractions(imageService, imageUtils);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "No file uploaded"));
    }

    @Test
    void shouldReturnBadRequest_whenFileFormatIsUnsupported() throws IOException {
        // Arrange
        User authenticatedUser = User.builder()
                .id("userId")
                .username("username")
                .email("email@gmail.com")
                .password("Password123#!")
                .role(UserRole.USER)
                .isValidEmail(true)
                .profileImageUrl("https://any.png")
                .build();

        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("document.pdf");
        when(imageService.getImageExtension(anyString())).thenReturn(null);

        // Act
        ResponseEntity<Map<String, String>> response = userService.uploadUserProfileImage(authenticatedUser, file);

        // Assert
        InOrder inOrder = inOrder(file, imageService);
        inOrder.verify(file).isEmpty();
        inOrder.verify(file).getOriginalFilename();
        inOrder.verify(imageService).getImageExtension("document.pdf");
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(imageUtils);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "Unsupported image format"));
    }

    @Test
    void shouldReturnBadRequest_whenUploadFails() throws IOException {
        // Arrange
        User authenticatedUser = User.builder()
                .id("userId")
                .username("username")
                .email("email@gmail.com")
                .password("Password123#!")
                .role(UserRole.USER)
                .isValidEmail(true)
                .profileImageUrl("https://any.png")
                .build();

        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("profile.png");
        when(imageService.getImageExtension(anyString())).thenReturn(ImageExtension.png);
        when(imageUtils.uploadImage(any())).thenReturn(null);

        // Act
        ResponseEntity<Map<String, String>> response = userService.uploadUserProfileImage(authenticatedUser, file);

        // Assert
        InOrder inOrder = inOrder(file, imageService, imageUtils);
        inOrder.verify(file).isEmpty();
        inOrder.verify(file).getOriginalFilename();
        inOrder.verify(imageService).getImageExtension("profile.png");
        inOrder.verify(imageUtils).uploadImage(file);
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "Failed to upload the image"));
    }

}