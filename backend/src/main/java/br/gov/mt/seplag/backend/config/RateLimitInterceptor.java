package br.gov.mt.seplag.backend.config;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interceptor para Rate Limiting usando Bucket4j
 * Limita: 10 requisições por minuto por usuário autenticado
 */
@Component
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    @Value("${rate-limit.requests-per-minute:10}")
    private int requestsPerMinute;

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) throws Exception {

        if (isPublicEndpoint(request)) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String identifier;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            identifier = "user:" + auth.getName();
        } else {
            identifier = "ip:" + getClientIP(request);
        }

        Bucket bucket = resolveBucket(identifier);

        if (bucket.tryConsume(1)) {
            addRateLimitHeaders(response, bucket);
            return true;
        }

        log.warn("Rate limit atingido para: {}", identifier);
        sendRateLimitError(response);
        return false;
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/v1/auth/") ||
                path.startsWith("/actuator/health") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/");
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private void addRateLimitHeaders(HttpServletResponse response, Bucket bucket) {
        response.addHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
        response.addHeader("X-RateLimit-Remaining", String.valueOf(bucket.getAvailableTokens()));
        response.addHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() / 1000 + 60));
    }

    private void sendRateLimitError(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // ✅ CORRIGIDO
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        long retryAfter = 60;
        response.addHeader("Retry-After", String.valueOf(retryAfter));

        response.getWriter().write(String.format("""
                {
                  "timestamp": "%s",
                  "status": %d,
                  "error": "%s",
                  "codigo": "RATE_LIMIT_EXCEEDED",
                  "mensagem": "Limite de requisições excedido. Máximo: %d por minuto.",
                  "retry_after_seconds": %d
                }
                """,
                java.time.Instant.now(),
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                requestsPerMinute,
                retryAfter
        ));
    }

    private Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createNewBucket());
    }

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(requestsPerMinute)
                        .refillGreedy(requestsPerMinute, Duration.ofMinutes(1)))
                .build();
    }
}