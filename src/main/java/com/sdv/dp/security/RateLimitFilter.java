package com.sdv.dp.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.slf4j.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // run before security filter
public class RateLimitFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static class TokenBucket {
        private final int capacity;
        private final double refillPerMillis;
        private double tokens;
        private long lastRefill;

        TokenBucket(int capacity, int refillPerSecond) {
            this.capacity = capacity;
            this.refillPerMillis = refillPerSecond / 1000.0;
            this.tokens = capacity;
            this.lastRefill = System.currentTimeMillis();
        }
        synchronized boolean allow() {
            long now = System.currentTimeMillis();
            double toAdd = (now - lastRefill) * refillPerMillis;
            if (toAdd > 0) {
                tokens = Math.min(capacity, tokens + toAdd);
                lastRefill = now;
            }
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }
    }

    private final Map<String, TokenBucket> generalBuckets = new ConcurrentHashMap<>();
    private final Map<String, TokenBucket> authBuckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String ip = request.getRemoteAddr();
        boolean isAuth = request.getRequestURI().startsWith("/api/auth/");
        TokenBucket bucket = (isAuth
                ? authBuckets.computeIfAbsent(ip, key -> new TokenBucket(10, 5))
                : generalBuckets.computeIfAbsent(ip, key -> new TokenBucket(100, 50)));

        if (!bucket.allow()) {
            log.warn("Rate limit exceeded from IP {}", ip);
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too Many Requests\"}");
            return;
        }
        chain.doFilter(req, res);
    }
}