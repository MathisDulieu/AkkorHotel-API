package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.UserDao;
import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.request.LoginRequest;
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

import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private UuidProvider uuidProvider;

    @Mock
    private UserDao userDao;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private UserUtils userUtils;

    @Test
    void shouldReturnOkAndRegisterNewUser() {
        // Arrange
        User user = User.builder()
                .username("alice123")
                .email("alice@example.com")
                .password("AliceStrongP@ss1!")
                .build();

        when(userUtils.isInvalidEmail(anyString())).thenReturn(false);
        when(userUtils.isInvalidUsername(anyString())).thenReturn(false);
        when(userUtils.isInvalidPassword(anyString())).thenReturn(false);
        when(userDao.isUserInDatabase(anyString(), anyString())).thenReturn(false);
        when(uuidProvider.generateUuid()).thenReturn("anyId");

        // Act
        ResponseEntity<Map<String, String>> response = authenticationService.register(user);

        // Assert
        InOrder inOrder = inOrder(userUtils, userDao, uuidProvider, passwordEncoder);
        inOrder.verify(userUtils).isInvalidEmail("alice@example.com");
        inOrder.verify(userUtils).isInvalidUsername("alice123");
        inOrder.verify(userUtils).isInvalidPassword("AliceStrongP@ss1!");
        inOrder.verify(userDao).isUserInDatabase("alice123", "alice@example.com");
        inOrder.verify(uuidProvider).generateUuid();
        inOrder.verify(passwordEncoder).encode("AliceStrongP@ss1!");
        inOrder.verify(userDao).save(user);
        inOrder.verify(userUtils, times(1)).sendRegisterConfirmationEmail(user);
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("message", "User successfully registered!"));
    }

    @Test
    void shouldNotRegisterNewUserAndSendBadRequest_whenUserEmailIsInvalid() {
        // Arrange
        User user = User.builder()
                .username("alice123")
                .password("AliceStrongP@ss1!")
                .email("invalidEmail")
                .build();

        when(userUtils.isInvalidEmail(any())).thenReturn(true);

        // Act
        ResponseEntity<Map<String, String>> response = authenticationService.register(user);

        // Assert
        verify(userUtils).isInvalidEmail("invalidEmail");
        verifyNoMoreInteractions(userUtils);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "The provided email is not valid."));
        verifyNoInteractions(userDao, uuidProvider, passwordEncoder);
    }

    @Test
    void shouldNotRegisterNewUserAndSendBadRequest_whenUsernameIsInvalid() {
        // Arrange
        User user = User.builder()
                .username("invalidUsername")
                .email("alice@example.com")
                .password("AliceStrongP@ss1!")
                .build();

        when(userUtils.isInvalidEmail(any())).thenReturn(false);
        when(userUtils.isInvalidUsername(any())).thenReturn(true);

        // Act
        ResponseEntity<Map<String, String>> response = authenticationService.register(user);

        // Assert
        InOrder inOrder = inOrder(userUtils);
        inOrder.verify(userUtils).isInvalidEmail("alice@example.com");
        inOrder.verify(userUtils).isInvalidUsername("invalidUsername");
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "The username must be between 3 and 11 characters long and must not contain spaces."));
        verifyNoInteractions(userDao, uuidProvider, passwordEncoder);
    }

    @Test
    void shouldNotRegisterNewUserAndSendBadRequest_whenPasswordIsInvalid() {
        // Arrange
        User user = User.builder()
                .username("alice123")
                .email("alice@example.com")
                .password("invalidPassword")
                .build();

        when(userUtils.isInvalidEmail(any())).thenReturn(false);
        when(userUtils.isInvalidUsername(any())).thenReturn(false);
        when(userUtils.isInvalidPassword(any())).thenReturn(true);

        // Act
        ResponseEntity<Map<String, String>> response = authenticationService.register(user);

        // Assert
        InOrder inOrder = inOrder(userUtils);
        inOrder.verify(userUtils).isInvalidEmail("alice@example.com");
        inOrder.verify(userUtils).isInvalidUsername("alice123");
        inOrder.verify(userUtils).isInvalidPassword("invalidPassword");
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "The password does not meet the required criteria."));
        verifyNoInteractions(userDao, uuidProvider, passwordEncoder);
    }

    @Test
    void shouldNotRegisterNewUserAndSendBadRequest_whenEmailIsAlreadyUsed() {
        // Arrange
        User user = User.builder()
                .username("alice123")
                .email("alreadyUsed@example.com")
                .password("AliceStrongP@ss1!")
                .build();

        when(userUtils.isInvalidEmail(any())).thenReturn(false);
        when(userUtils.isInvalidUsername(any())).thenReturn(false);
        when(userUtils.isInvalidPassword(any())).thenReturn(false);
        when(userDao.isUserInDatabase(anyString(), eq("alreadyUsed@example.com"))).thenReturn(true);

        // Act
        ResponseEntity<Map<String, String>> response = authenticationService.register(user);

        // Assert
        InOrder inOrder = inOrder(userUtils, userDao);
        inOrder.verify(userUtils).isInvalidEmail("alreadyUsed@example.com");
        inOrder.verify(userUtils).isInvalidUsername("alice123");
        inOrder.verify(userUtils).isInvalidPassword("AliceStrongP@ss1!");
        inOrder.verify(userDao).isUserInDatabase(anyString(), eq("alreadyUsed@example.com"));
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(uuidProvider, passwordEncoder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "A user with this email or username already exists."));
    }

    @Test
    void shouldNotRegisterNewUserAndSendBadRequest_whenUsernameIsAlreadyUsed() {
        // Arrange
        User user = User.builder()
                .username("alreadyUsed")
                .email("alice@example.com")
                .password("AliceStrongP@ss1!")
                .build();

        when(userUtils.isInvalidEmail(any())).thenReturn(false);
        when(userUtils.isInvalidUsername(any())).thenReturn(false);
        when(userUtils.isInvalidPassword(any())).thenReturn(false);
        when(userDao.isUserInDatabase(eq("alreadyUsed"), anyString())).thenReturn(true);

        // Act
        ResponseEntity<Map<String, String>> response = authenticationService.register(user);

        // Assert
        InOrder inOrder = inOrder(userUtils, userDao);
        inOrder.verify(userUtils).isInvalidEmail("alice@example.com");
        inOrder.verify(userUtils).isInvalidUsername("alreadyUsed");
        inOrder.verify(userUtils).isInvalidPassword("AliceStrongP@ss1!");
        inOrder.verify(userDao).isUserInDatabase(eq("alreadyUsed"), any());
        inOrder.verifyNoMoreInteractions();

        verifyNoInteractions(uuidProvider, passwordEncoder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "A user with this email or username already exists."));
    }

    @Test
    void shouldSaveUserButReturnInternalServerError_whenExceptionOccursDuringEmailSending() {
        // Arrange
        User user = User.builder()
                .username("alice123")
                .email("alice@example.com")
                .password("AliceStrongP@ss1!")
                .build();

        when(userUtils.isInvalidEmail(any())).thenReturn(false);
        when(userUtils.isInvalidUsername(any())).thenReturn(false);
        when(userUtils.isInvalidPassword(any())).thenReturn(false);
        when(userDao.isUserInDatabase(anyString(), anyString())).thenReturn(false);
        when(uuidProvider.generateUuid()).thenReturn("anyId");
        when(userUtils.sendRegisterConfirmationEmail(any())).thenReturn("Failed to send the registration confirmation email. Please try again later.");

        // Act
        ResponseEntity<Map<String, String>> response = authenticationService.register(user);

        // Assert
        InOrder inOrder = inOrder(userUtils, userDao, uuidProvider, jwtTokenService, passwordEncoder);
        inOrder.verify(userUtils).isInvalidEmail("alice@example.com");
        inOrder.verify(userUtils).isInvalidUsername("alice123");
        inOrder.verify(userUtils).isInvalidPassword("AliceStrongP@ss1!");
        inOrder.verify(userDao).isUserInDatabase("alice123", "alice@example.com");
        inOrder.verify(uuidProvider).generateUuid();
        inOrder.verify(passwordEncoder).encode("AliceStrongP@ss1!");
        inOrder.verify(userDao).save(user);
        inOrder.verify(userUtils).sendRegisterConfirmationEmail(user);
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo(singletonMap("warning", "Failed to send the registration confirmation email. Please try again later."));
    }

    @Test
    void shouldLoginUserAndReturnOkWithToken() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("userEmail");
        loginRequest.setPassword("userPassword");

        when(userDao.findByEmail(anyString())).thenReturn(Optional.of(User.builder()
                        .id("userId")
                        .email("userEmail")
                        .password("encodedUserPassword")
                        .isValidEmail(true)
                .build()));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenService.generateToken(anyString())).thenReturn("anyToken");

        // Act
        ResponseEntity<Map<String, String>> response = authenticationService.login(loginRequest);

        // Assert
        InOrder inOrder = inOrder(userDao, passwordEncoder, jwtTokenService);
        inOrder.verify(userDao).findByEmail("userEmail");
        inOrder.verify(passwordEncoder).matches("userPassword", "encodedUserPassword");
        inOrder.verify(jwtTokenService).generateToken("userId");
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("token", "anyToken"));
    }

    @Test
    void shouldReturnNotFound_whenUserIsNotFoundInDatabase() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("userEmail");
        loginRequest.setPassword("userPassword");

        when(userDao.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, String>> response = authenticationService.login(loginRequest);

        // Assert
        verify(userDao).findByEmail("userEmail");

        verifyNoMoreInteractions(userDao);
        verifyNoInteractions(passwordEncoder, jwtTokenService);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "User not found"));
    }

    @Test
    void shouldReturnConflit_whenUserEmailIsNotValidateYet() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("userEmail");
        loginRequest.setPassword("userPassword");

        when(userDao.findByEmail(anyString())).thenReturn(Optional.of(User.builder()
                .email("userEmail")
                .isValidEmail(false)
                .build()));

        // Act
        ResponseEntity<Map<String, String>> response = authenticationService.login(loginRequest);

        // Assert
        verify(userDao).findByEmail("userEmail");

        verifyNoMoreInteractions(userDao);
        verifyNoInteractions(passwordEncoder, jwtTokenService);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "Email is not verified"));
    }

    @Test
    void shouldReturnBadRequest_whenPasswordIsIncorrect() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("userEmail");
        loginRequest.setPassword("incorrectPassword");

        when(userDao.findByEmail(anyString())).thenReturn(Optional.of(User.builder()
                .email("userEmail")
                .isValidEmail(true)
                .password("encodedUserPassword")
                .build()));

        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act
        ResponseEntity<Map<String, String>> response = authenticationService.login(loginRequest);

        // Assert
        InOrder inOrder = inOrder(userDao, passwordEncoder);
        inOrder.verify(userDao).findByEmail("userEmail");
        inOrder.verify(passwordEncoder).matches("incorrectPassword", "encodedUserPassword");
        inOrder.verifyNoMoreInteractions();

        verifyNoMoreInteractions(userDao, passwordEncoder);
        verifyNoInteractions(jwtTokenService);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "Invalid password"));
    }

    @Test
    void shouldConfirmUserEmail() {
        // Arrange
        when(jwtTokenService.isEmailTokenValid(anyString())).thenReturn(true);
        when(jwtTokenService.resolveUserIdFromToken(anyString())).thenReturn("userId");
        when(userDao.findById(anyString())).thenReturn(Optional.of(User.builder()
                .id("userId")
                .email("userEmail")
                .isValidEmail(false)
                .build()));

        // Act
        ResponseEntity<Map<String, String>> response = authenticationService.confirmEmail("emailToken");

        // Assert
        InOrder inOrder = inOrder(jwtTokenService, userDao);
        inOrder.verify(jwtTokenService).isEmailTokenValid("emailToken");
        inOrder.verify(jwtTokenService).resolveUserIdFromToken("emailToken");
        inOrder.verify(userDao).findById("userId");
        inOrder.verify(userDao).save(eq(User.builder().id("userId").email("userEmail").isValidEmail(true).build()));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("message", "Email successfully validated"));
    }

    @Test
    void shouldReturnBadRequest_whenEmailTokenIsNotValid() {
        // Arrange
        when(jwtTokenService.isEmailTokenValid(anyString())).thenReturn(false);

        // Act
        ResponseEntity<Map<String, String>> response = authenticationService.confirmEmail("emailToken");

        // Assert
        verify(jwtTokenService).isEmailTokenValid("emailToken");

        verifyNoMoreInteractions(jwtTokenService);
        verifyNoInteractions(userDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "Invalid or expired token"));
    }

    @Test
    void shouldReturnNotFound_whenEmailConfirmationTokenBelongsToNonExistentUser() {
        // Arrange
        when(jwtTokenService.isEmailTokenValid(anyString())).thenReturn(true);
        when(jwtTokenService.resolveUserIdFromToken(anyString())).thenReturn("nonExistentUserId");
        when(userDao.findById(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, String>> response = authenticationService.confirmEmail("emailToken");

        // Assert
        InOrder inOrder = inOrder(jwtTokenService, userDao);
        inOrder.verify(jwtTokenService).isEmailTokenValid("emailToken");
        inOrder.verify(jwtTokenService).resolveUserIdFromToken("emailToken");
        inOrder.verify(userDao).findById("nonExistentUserId");
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "User not found"));
    }

    @Test
    void shouldReturnBadRequest_whenUserEmailIsAlreadyValidated() {
        // Arrange
        when(jwtTokenService.isEmailTokenValid(anyString())).thenReturn(true);
        when(jwtTokenService.resolveUserIdFromToken(anyString())).thenReturn("userId");
        when(userDao.findById(anyString())).thenReturn(Optional.of(User.builder()
                .id("userId")
                .email("userEmail")
                .isValidEmail(true)
                .build()));

        // Act
        ResponseEntity<Map<String, String>> response = authenticationService.confirmEmail("emailToken");

        // Assert
        InOrder inOrder = inOrder(jwtTokenService, userDao);
        inOrder.verify(jwtTokenService).isEmailTokenValid("emailToken");
        inOrder.verify(jwtTokenService).resolveUserIdFromToken("emailToken");
        inOrder.verify(userDao).findById("userId");
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "Email already validated"));
    }

    @Test
    void shouldSendRegisterConfirmationEmail() {
        // Arrange
        User user = User.builder()
                .id("userId")
                .email("userEmail")
                .isValidEmail(false)
                .build();

        when(userDao.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(userUtils.sendRegisterConfirmationEmail(any())).thenReturn(null);

        // Act
        ResponseEntity<Map<String, String>> response = authenticationService.resendConfirmationEmail("userEmail");

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils);
        inOrder.verify(userDao).findByEmail("userEmail");
        inOrder.verify(userUtils).sendRegisterConfirmationEmail(user);
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(singletonMap("message", "Confirmation email successfully sent"));
    }

    @Test
    void shouldReturnNotFound_whenEmailBelongsToNonExistentUser() {
        // Arrange
        when(userDao.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, String>> response = authenticationService.resendConfirmationEmail("userEmail");

        // Assert
        verify(userDao).findByEmail("userEmail");

        verifyNoMoreInteractions(userDao);
        verifyNoInteractions(userUtils);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "User not found"));
    }

    @Test
    void shouldReturnBadRequest_whenEmailIsAlreadyValidated() {
        // Arrange
        when(userDao.findByEmail(anyString())).thenReturn(Optional.of(User.builder()
                .id("userId")
                .email("userEmail")
                .isValidEmail(true)
                .build()));

        // Act
        ResponseEntity<Map<String, String>> response = authenticationService.resendConfirmationEmail("userEmail");

        // Assert
        verify(userDao).findByEmail("userEmail");

        verifyNoMoreInteractions(userDao);
        verifyNoInteractions(userUtils);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "Email already validated"));
    }

    @Test
    void shouldReturnInternalServerError_whenExceptionOccursDuringEmailSending() {
        // Arrange
        User user = User.builder()
                .id("userId")
                .email("userEmail")
                .isValidEmail(false)
                .build();

        when(userDao.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(userUtils.sendRegisterConfirmationEmail(any())).thenReturn("Failed to send the registration confirmation email. Please try again later.");

        // Act
        ResponseEntity<Map<String, String>> response = authenticationService.resendConfirmationEmail("userEmail");

        // Assert
        InOrder inOrder = inOrder(userDao, userUtils);
        inOrder.verify(userDao).findByEmail("userEmail");
        inOrder.verify(userUtils).sendRegisterConfirmationEmail(user);
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo(singletonMap("error", "Failed to send the registration confirmation email. Please try again later."));
    }

}