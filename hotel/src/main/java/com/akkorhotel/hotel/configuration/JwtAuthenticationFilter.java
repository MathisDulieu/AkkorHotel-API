package com.akkorhotel.hotel.configuration;

import com.akkorhotel.hotel.dao.UserDao;
import com.akkorhotel.hotel.model.UserRole;
import com.akkorhotel.hotel.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserDao userDao;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        if (isNotPrivateRoute(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication userInformations = getUserInformations(request);
        if (isNull(userInformations)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You must be authenticated to perform this action.");
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(userInformations);
        filterChain.doFilter(request, response);
    }

    private Authentication getUserInformations(HttpServletRequest request) {
        String userId = jwtTokenService.resolveUserIdFromRequest(request);
        if (isNull(userId)) {
            return null;
        }

        String userRole = getUserRole(userId);
        if (isNull(userRole)) {
            return null;
        }

        return buildAuthentication(userId, userRole);
    }

    private Authentication buildAuthentication(String userId, String userRole) {
        List<SimpleGrantedAuthority> authorityUserRole = Collections.singletonList(new SimpleGrantedAuthority(userRole));
        return new UsernamePasswordAuthenticationToken(userId, null, authorityUserRole);
    }

    private String getUserRole(String userId) {
        UserRole userRole = userDao.getUserRole(userId);
        return isNull(userRole) ? null : userRole.toString();
    }

    private boolean isNotPrivateRoute(String uri) {
        return !uri.startsWith("/api/private");
    }
}


