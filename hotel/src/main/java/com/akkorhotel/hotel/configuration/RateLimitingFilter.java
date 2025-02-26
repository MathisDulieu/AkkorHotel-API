package com.akkorhotel.hotel.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> blockExpirations = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS = 60;
    private static final long BLOCK_DURATION = 600000;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        String ipAddress = getClientIP(request);

        if (isIpBlocked(ipAddress)) {
            rejectRequest(response);
            return;
        }

        if (incrementAndCheckLimit(ipAddress)) {
            blockIpAddress(ipAddress);
            rejectRequest(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isIpBlocked(String ipAddress) {
        return blockExpirations.containsKey(ipAddress) &&
                blockExpirations.get(ipAddress) > System.currentTimeMillis();
    }

    private boolean incrementAndCheckLimit(String ipAddress) {
        int count = requestCounts.getOrDefault(ipAddress, 0) + 1;
        requestCounts.put(ipAddress, count);
        return count > MAX_REQUESTS;
    }

    private void blockIpAddress(String ipAddress) {
        blockExpirations.put(ipAddress, System.currentTimeMillis() + BLOCK_DURATION);
    }

    private void rejectRequest(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.getWriter().write("Too many requests. Please try again later.");
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    @Scheduled(fixedRate = 60000)
    public void resetRequestCounts() {
        requestCounts.clear();
    }
}