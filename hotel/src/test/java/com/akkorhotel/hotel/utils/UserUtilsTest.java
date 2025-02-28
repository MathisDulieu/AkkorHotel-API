package com.akkorhotel.hotel.utils;

import com.akkorhotel.hotel.configuration.EnvConfiguration;
import com.akkorhotel.hotel.model.User;
import com.akkorhotel.hotel.model.UserRole;
import com.akkorhotel.hotel.service.EmailService;
import com.akkorhotel.hotel.service.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUtilsTest {

    @InjectMocks
    private UserUtils userUtils;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private EnvConfiguration envConfiguration;


    @Test
    void shouldReturnTrue_whenEmailIsNull() {
        // Arrange
        String email = null;

        // Act
        boolean isInvalidEmail = userUtils.isInvalidEmail(email);

        // Assert
        assertThat(isInvalidEmail).isTrue();
    }

    @Test
    void shouldReturnTrue_whenEmailIsInvalid() {
        // Arrange
        String email = "invalidEmail";

        // Act
        boolean isInvalidEmail = userUtils.isInvalidEmail(email);

        // Assert
        assertThat(isInvalidEmail).isTrue();
    }

    @Test
    void shouldReturnFalse_whenEmailIsValid() {
        // Arrange
        String email = "valid.email@gmail.com";

        // Act
        boolean isInvalidEmail = userUtils.isInvalidEmail(email);

        // Assert
        assertThat(isInvalidEmail).isFalse();
    }

    @Test
    void shouldReturnTrue_whenUsernameIsNull() {
        // Arrange
        String username = null;

        // Act
        boolean isInvalidUsername = userUtils.isInvalidUsername(username);

        // Assert
        assertThat(isInvalidUsername).isTrue();
    }

    @Test
    void shouldReturnTrue_whenUsernameIsShorterThan3Characters() {
        // Arrange
        String username = "ex";

        // Act
        boolean isInvalidUsername = userUtils.isInvalidUsername(username);

        // Assert
        assertThat(isInvalidUsername).isTrue();
    }

    @Test
    void shouldReturnTrue_whenUsernameIsLongerThan11Characters() {
        // Arrange
        String username = "thisIsTooLong";

        // Act
        boolean isInvalidUsername = userUtils.isInvalidUsername(username);

        // Assert
        assertThat(isInvalidUsername).isTrue();
    }

    @Test
    void shouldReturnTrue_whenUsernameContainsSpaces() {
        // Arrange
        String username = " NotValid ";

        // Act
        boolean isInvalidUsername = userUtils.isInvalidUsername(username);

        // Assert
        assertThat(isInvalidUsername).isTrue();
    }

    @Test
    void shouldReturnFalse_whenUsernameIsValid() {
        // Arrange
        String username = "Valid";

        // Act
        boolean isInvalidUsername = userUtils.isInvalidUsername(username);

        // Assert
        assertThat(isInvalidUsername).isFalse();
    }

    @Test
    void shouldReturnTrue_whenPasswordIsNull() {
        // Arrange
        String password = null;

        // Act
        boolean isInvalidPassword = userUtils.isInvalidPassword(password);

        // Assert
        assertThat(isInvalidPassword).isTrue();
    }

    @Test
    void shouldReturnTrue_whenPasswordIsNotStrongEnough() {
        // Arrange
        String password = "notStrong";

        // Act
        boolean isInvalidPassword = userUtils.isInvalidPassword(password);

        // Assert
        assertThat(isInvalidPassword).isTrue();
    }

    @Test
    void shouldReturnFalse_whenPasswordIsValid() {
        // Arrange
        String password = "Valid@Pass123!";

        // Act
        boolean isInvalidPassword = userUtils.isInvalidPassword(password);

        // Assert
        assertThat(isInvalidPassword).isFalse();
    }

    @Test
    void shouldReturnNullAndSendRegisterConfirmationEmail() {
        // Arrange
        User user = User.builder()
                .id("id")
                .username("username")
                .email("email")
                .password("password")
                .isValidEmail(false)
                .role(UserRole.USER)
                .profileImageUrl("profileImageUrl")
                .build();

        when(jwtTokenService.generateEmailConfirmationToken("id")).thenReturn("emailToken");
        when(envConfiguration.getMailRegisterSubject()).thenReturn("Registration Confirmation");

        // Act
        String response = userUtils.sendRegisterConfirmationEmail(user);

        // Assert
        InOrder inOrder = inOrder(jwtTokenService, envConfiguration, emailService);
        inOrder.verify(jwtTokenService).generateEmailConfirmationToken("id");
        inOrder.verify(envConfiguration).getMailRegisterSubject();
        inOrder.verify(emailService, times(1)).sendEmail(eq("email"), any(), any());
        inOrder.verifyNoMoreInteractions();

        assertThat(response).isNull();
    }

    @Test
    void shouldNotSendRegisterConfirmationEmail_whenErrorOccursWhileSendingEmail() {
        // Arrange
        User user = User.builder()
                .id("id")
                .username("username")
                .email("email")
                .password("password")
                .isValidEmail(false)
                .role(UserRole.USER)
                .profileImageUrl("profileImageUrl")
                .build();

        when(jwtTokenService.generateEmailConfirmationToken("id")).thenReturn("emailToken");
        when(envConfiguration.getMailRegisterSubject()).thenReturn("Registration Confirmation");
        doThrow(new MailException("Email error") {}).when(emailService).sendEmail(anyString(), anyString(), anyString());

        // Act
        String response = userUtils.sendRegisterConfirmationEmail(user);

        // Assert
        InOrder inOrder = inOrder(jwtTokenService, envConfiguration, emailService);
        inOrder.verify(jwtTokenService).generateEmailConfirmationToken("id");
        inOrder.verify(envConfiguration).getMailRegisterSubject();
        inOrder.verify(emailService, times(1)).sendEmail(eq("email"), any(), any());
        inOrder.verifyNoMoreInteractions();

        assertThat(response).isEqualTo("Failed to send the registration confirmation email. Please try again later.");
    }

    @Test
    void shouldReturnErrorsAsString() {
        // Arrange
        List<String> errors = List.of(
                "error1",
                "error2",
                "error3"
        );

        // Act
        String stringOfErrors = userUtils.getErrorsAsString(errors);

        // Assert
        assertThat(stringOfErrors).isEqualTo("error1 | error2 | error3");
    }

}