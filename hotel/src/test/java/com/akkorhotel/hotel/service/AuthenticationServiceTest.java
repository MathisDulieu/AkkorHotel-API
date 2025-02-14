package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.dao.UserDao;
import com.akkorhotel.hotel.model.User;
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

}