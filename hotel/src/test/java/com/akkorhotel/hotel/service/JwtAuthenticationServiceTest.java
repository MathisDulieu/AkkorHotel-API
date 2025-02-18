package com.akkorhotel.hotel.service;

import com.akkorhotel.hotel.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationServiceTest {

    @InjectMocks
    private JwtAuthenticationService jwtAuthenticationService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Test
    void shouldReturnAuthenticatedUser() {
        // Arrange
        User authenticatedUser = User.builder().id("123").build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(authenticatedUser);

        SecurityContextHolder.setContext(securityContext);

        // Act
        User user = jwtAuthenticationService.getAuthenticatedUser();

        // Assert
        assertThat(user).isEqualTo(authenticatedUser);
    }
}
