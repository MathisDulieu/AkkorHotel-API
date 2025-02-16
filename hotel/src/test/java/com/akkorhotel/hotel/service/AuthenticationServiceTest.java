package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.UserDao;
import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.request.LoginRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

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
    private EmailService emailService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    void shouldReturnOkAndRegisterNewUser() {
        // Arrange
        User user = User.builder()
                .username("alice123")
                .email("alice@example.com")
                .password("AliceStrongP@ss1!")
                .build();

        when(userDao.isUserInDatabase(anyString(), anyString())).thenReturn(false);
        when(uuidProvider.generateUuid()).thenReturn("anyId");
        when(jwtTokenService.generateEmailConfirmationToken(anyString())).thenReturn("anyToken");

        // Act
        ResponseEntity<String> response = authenticationService.register(user);

        // Assert
        InOrder inOrder = inOrder(userDao, uuidProvider, jwtTokenService, emailService, passwordEncoder);
        inOrder.verify(userDao).isUserInDatabase("alice123", "alice@example.com");
        inOrder.verify(uuidProvider).generateUuid();
        inOrder.verify(passwordEncoder).encode("AliceStrongP@ss1!");
        inOrder.verify(userDao).save(user);
        inOrder.verify(jwtTokenService).generateEmailConfirmationToken("anyId");
        inOrder.verify(emailService, times(1)).sendEmail(eq("alice@example.com"), any(), any());
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldNotRegisterNewUserAndSendBadRequest_whenUserEmailIsNull() {
        // Arrange
        User user = User.builder()
                .username("alice123")
                .password("AliceStrongP@ss1!")
                .email(null)
                .build();

        // Act
        ResponseEntity<String> response = authenticationService.register(user);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("The provided email is not valid.");
        verifyNoInteractions(userDao, uuidProvider, passwordEncoder, emailService);
    }

    @Test
    void shouldNotRegisterNewUserAndSendBadRequest_whenUserEmailIsNotValid() {
        // Arrange
        User user = User.builder()
                .username("alice123")
                .email("invalid-email")
                .password("AliceStrongP@ss1!")
                .build();

        // Act
        ResponseEntity<String> response = authenticationService.register(user);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("The provided email is not valid.");
        verifyNoInteractions(userDao, uuidProvider, passwordEncoder, emailService);
    }

    @Test
    void shouldNotRegisterNewUserAndSendBadRequest_whenUsernameIsNull() {
        // Arrange
        User user = User.builder()
                .username(null)
                .email("alice@example.com")
                .password("AliceStrongP@ss1!")
                .build();

        // Act
        ResponseEntity<String> response = authenticationService.register(user);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("The username must be between 3 and 11 characters long and must not contain spaces.");
        verifyNoInteractions(userDao, uuidProvider, passwordEncoder, emailService);
    }

    @Test
    void shouldNotRegisterNewUserAndSendBadRequest_whenUsernameIsLessThan3Characters() {
        // Arrange
        User user = User.builder()
                .username("ab")
                .email("alice@example.com")
                .password("AliceStrongP@ss1!")
                .build();

        // Act
        ResponseEntity<String> response = authenticationService.register(user);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("The username must be between 3 and 11 characters long and must not contain spaces.");
        verifyNoInteractions(userDao, uuidProvider, passwordEncoder, emailService);
    }

    @Test
    void shouldNotRegisterNewUserAndSendBadRequest_whenUsernameIsMoreThan11Characters() {
        // Arrange
        User user = User.builder()
                .username("12Characters")
                .email("alice@example.com")
                .password("AliceStrongP@ss1!")
                .build();

        // Act
        ResponseEntity<String> response = authenticationService.register(user);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("The username must be between 3 and 11 characters long and must not contain spaces.");
        verifyNoInteractions(userDao, uuidProvider, passwordEncoder, emailService);
    }

    @Test
    void shouldNotRegisterNewUserAndSendBadRequest_whenUsernameContainsSpaces() {
        // Arrange
        User user = User.builder()
                .username(" Spaces ")
                .email("alice@example.com")
                .password("AliceStrongP@ss1!")
                .build();

        // Act
        ResponseEntity<String> response = authenticationService.register(user);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("The username must be between 3 and 11 characters long and must not contain spaces.");
        verifyNoInteractions(userDao, uuidProvider, passwordEncoder, emailService);
    }

    @Test
    void shouldNotRegisterNewUserAndSendBadRequest_whenPasswordIsNull() {
        // Arrange
        User user = User.builder()
                .username("alice123")
                .email("alice@example.com")
                .password(null)
                .build();

        // Act
        ResponseEntity<String> response = authenticationService.register(user);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("The password does not meet the required criteria.");
        verifyNoInteractions(userDao, uuidProvider, passwordEncoder, emailService);
    }

    @Test
    void shouldNotRegisterNewUserAndSendBadRequest_whenPasswordIsNotStrongEnough() {
        // Arrange
        User user = User.builder()
                .username("alice123")
                .email("alice@example.com")
                .password("NotStrongPassword")
                .build();

        // Act
        ResponseEntity<String> response = authenticationService.register(user);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("The password does not meet the required criteria.");
        verifyNoInteractions(userDao, uuidProvider, passwordEncoder, emailService);
    }

    @Test
    void shouldNotRegisterNewUserAndSendBadRequest_whenEmailIsAlreadyUsed() {
        // Arrange
        User user = User.builder()
                .username("alice123")
                .email("alreadyUsed@example.com")
                .password("AliceStrongP@ss1!")
                .build();

        when(userDao.isUserInDatabase(anyString(), eq("alreadyUsed@example.com"))).thenReturn(true);

        // Act
        ResponseEntity<String> response = authenticationService.register(user);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("A user with this email or username already exists.");
        verify(userDao, times(1)).isUserInDatabase(any(), eq("alreadyUsed@example.com"));
        verifyNoMoreInteractions(userDao);
        verifyNoInteractions(uuidProvider, passwordEncoder, emailService);
    }

    @Test
    void shouldNotRegisterNewUserAndSendBadRequest_whenUsernameIsAlreadyUsed() {
        // Arrange
        User user = User.builder()
                .username("alreadyUsed")
                .email("alice@example.com")
                .password("AliceStrongP@ss1!")
                .build();

        when(userDao.isUserInDatabase(eq("alreadyUsed"), anyString())).thenReturn(true);

        // Act
        ResponseEntity<String> response = authenticationService.register(user);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("A user with this email or username already exists.");
        verify(userDao, times(1)).isUserInDatabase(eq("alreadyUsed"), any());
        verifyNoMoreInteractions(userDao);
        verifyNoInteractions(uuidProvider, passwordEncoder, emailService);
    }

    @Test
    void shouldSaveUserButReturnInternalServerError_whenExceptionOccursDuringEmailSending() {
        // Arrange
        User user = User.builder()
                .username("alice123")
                .email("alice@example.com")
                .password("AliceStrongP@ss1!")
                .build();

        when(userDao.isUserInDatabase(anyString(), anyString())).thenReturn(false);
        when(uuidProvider.generateUuid()).thenReturn("anyId");
        when(jwtTokenService.generateEmailConfirmationToken(anyString())).thenReturn("anyToken");
        doThrow(new MailException("Email error") {}).when(emailService).sendEmail(anyString(), anyString(), anyString());

        // Act
        ResponseEntity<String> response = authenticationService.register(user);

        // Assert
        InOrder inOrder = inOrder(userDao, uuidProvider, jwtTokenService, emailService, passwordEncoder);
        inOrder.verify(userDao).isUserInDatabase("alice123", "alice@example.com");
        inOrder.verify(uuidProvider).generateUuid();
        inOrder.verify(passwordEncoder).encode("AliceStrongP@ss1!");
        inOrder.verify(userDao).save(user);
        inOrder.verify(jwtTokenService).generateEmailConfirmationToken("anyId");
        inOrder.verify(emailService, times(1)).sendEmail(eq("alice@example.com"), any(), any());
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Failed to send the registration confirmation email. Please try again later.");
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
        ResponseEntity<String> response = authenticationService.login(loginRequest);

        // Assert
        InOrder inOrder = inOrder(userDao, passwordEncoder, jwtTokenService);
        inOrder.verify(userDao).findByEmail("userEmail");
        inOrder.verify(passwordEncoder).matches("userPassword", "encodedUserPassword");
        inOrder.verify(jwtTokenService).generateToken("userId");
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnNotFound_whenUserIsNotFoundInDatabase() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("userEmail");
        loginRequest.setPassword("userPassword");

        when(userDao.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = authenticationService.login(loginRequest);

        // Assert
        verify(userDao).findByEmail("userEmail");

        verifyNoMoreInteractions(userDao);
        verifyNoInteractions(passwordEncoder, jwtTokenService);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("User not found");
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
        ResponseEntity<String> response = authenticationService.login(loginRequest);

        // Assert
        verify(userDao).findByEmail("userEmail");

        verifyNoMoreInteractions(userDao);
        verifyNoInteractions(passwordEncoder, jwtTokenService);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("Email is not verified");
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
        ResponseEntity<String> response = authenticationService.login(loginRequest);

        // Assert
        InOrder inOrder = inOrder(userDao, passwordEncoder);
        inOrder.verify(userDao).findByEmail("userEmail");
        inOrder.verify(passwordEncoder).matches("incorrectPassword", "encodedUserPassword");
        inOrder.verifyNoMoreInteractions();

        verifyNoMoreInteractions(userDao, passwordEncoder);
        verifyNoInteractions(jwtTokenService);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Invalid password");
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
        ResponseEntity<String> response = authenticationService.confirmEmail("emailToken");

        // Assert
        InOrder inOrder = inOrder(jwtTokenService, userDao);
        inOrder.verify(jwtTokenService).isEmailTokenValid("emailToken");
        inOrder.verify(jwtTokenService).resolveUserIdFromToken("emailToken");
        inOrder.verify(userDao).findById("userId");
        inOrder.verify(userDao).save(eq(User.builder().id("userId").email("userEmail").isValidEmail(true).build()));
        inOrder.verifyNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Email successfully validated");
    }

    @Test
    void shouldReturnBadRequest_whenEmailTokenIsNotValid() {
        // Arrange
        when(jwtTokenService.isEmailTokenValid(anyString())).thenReturn(false);

        // Act
        ResponseEntity<String> response = authenticationService.confirmEmail("emailToken");

        // Assert
        verify(jwtTokenService).isEmailTokenValid("emailToken");

        verifyNoMoreInteractions(jwtTokenService);
        verifyNoInteractions(userDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Invalid or expired token");
    }

    @Test
    void shouldReturnNotFound_whenUserIsNotFound() {
        // Arrange
        when(jwtTokenService.isEmailTokenValid(anyString())).thenReturn(true);
        when(jwtTokenService.resolveUserIdFromToken(anyString())).thenReturn("nonExistentUserId");
        when(userDao.findById(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = authenticationService.confirmEmail("emailToken");

        // Assert
        InOrder inOrder = inOrder(jwtTokenService, userDao);
        inOrder.verify(jwtTokenService).isEmailTokenValid("emailToken");
        inOrder.verify(jwtTokenService).resolveUserIdFromToken("emailToken");
        inOrder.verify(userDao).findById("nonExistentUserId");
        inOrder.verifyNoMoreInteractions();

        verifyNoMoreInteractions(jwtTokenService, userDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("User not found");
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
        ResponseEntity<String> response = authenticationService.confirmEmail("emailToken");

        // Assert
        InOrder inOrder = inOrder(jwtTokenService, userDao);
        inOrder.verify(jwtTokenService).isEmailTokenValid("emailToken");
        inOrder.verify(jwtTokenService).resolveUserIdFromToken("emailToken");
        inOrder.verify(userDao).findById("userId");
        inOrder.verifyNoMoreInteractions();

        verifyNoMoreInteractions(jwtTokenService, userDao);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Email already validated");
    }

}