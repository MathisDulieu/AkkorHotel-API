package com.akkorhotel.hotel.configuration;

import com.akkorhotel.hotel.service.RateLimitingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Mockito.*;

class RateLimitingFilterTest {

    private RateLimitingFilter rateLimitingFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() throws IOException {
        RateLimitingService rateLimitingService = new RateLimitingService();
        rateLimitingFilter = new RateLimitingFilter(rateLimitingService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
        PrintWriter writer = mock(PrintWriter.class);

        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void shouldAllowRequestUnderLimit() throws ServletException, IOException {
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    void shouldBlockRequestsOverLimit() throws ServletException, IOException {
        for (int i = 0; i <= 60; i++) {
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        verify(response, atLeastOnce()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(filterChain, times(60)).doFilter(request, response);
    }

    @Test
    void shouldReturnTooManyRequestsWhenBlocked() throws ServletException, IOException {
        for (int i = 0; i < 60; i++) {
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

}
