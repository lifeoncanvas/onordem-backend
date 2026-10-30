package com.honouryourmentor.security;

import com.honouryourmentor.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    // Separate buckets for Auth endpoints (strict) vs General endpoints
    private final Map<String, RequestCounter> authLimits = new ConcurrentHashMap<>();
    private final Map<String, RequestCounter> generalLimits = new ConcurrentHashMap<>();

    private static final int AUTH_MAX_REQUESTS_PER_MINUTE = 15;
    private static final int GENERAL_MAX_REQUESTS_PER_MINUTE = 150;
    private static final long ONE_MINUTE_MS = 60_000L;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String clientIp = getClientIP(request);
        String path = request.getRequestURI();

        boolean isAuthEndpoint = path.startsWith("/api/v1/auth/login") ||
                path.startsWith("/api/v1/auth/register") ||
                path.startsWith("/api/v1/auth/verify-otp") ||
                path.startsWith("/api/v1/auth/forgot-password") ||
                path.startsWith("/api/v1/auth/reset-password");

        if (isAuthEndpoint) {
            if (!isAllowed(authLimits, clientIp, AUTH_MAX_REQUESTS_PER_MINUTE)) {
                sendRateLimitResponse(response, "Too many authentication requests. Please wait a minute before trying again.");
                return;
            }
        } else if (path.startsWith("/api/v1/")) {
            if (!isAllowed(generalLimits, clientIp, GENERAL_MAX_REQUESTS_PER_MINUTE)) {
                sendRateLimitResponse(response, "Rate limit exceeded. Please slow down your requests.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowed(Map<String, RequestCounter> limitMap, String clientIp, int maxRequests) {
        long now = System.currentTimeMillis();
        cleanupOldEntries(limitMap, now);

        RequestCounter counter = limitMap.compute(clientIp, (key, existing) -> {
            if (existing == null || (now - existing.windowStart) > ONE_MINUTE_MS) {
                return new RequestCounter(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        return counter.count.get() <= maxRequests;
    }

    private void cleanupOldEntries(Map<String, RequestCounter> limitMap, long now) {
        if (limitMap.size() > 5000) {
            limitMap.entrySet().removeIf(entry -> (now - entry.getValue().windowStart) > ONE_MINUTE_MS);
        }
    }

    private void sendRateLimitResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", "60");
        ApiResponse<Void> apiResponse = ApiResponse.error(message);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isBlank() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private static class RequestCounter {
        final long windowStart;
        final AtomicInteger count;

        RequestCounter(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}

