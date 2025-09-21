package com.sdv.dp.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private static class Attempt { int count; Instant lockUntil; Instant lastFailure; }
    private final Map<String, Attempt> store = new ConcurrentHashMap<>();
    private final int maxFailures = 5;
    private final long lockMillis = 15 * 60_000;

    private String key(String email, String ip) { return email + "|" + ip; }

    public boolean isBlocked(String email, String ip) {
        Attempt attempt = store.get(key(email, ip));
        if (attempt == null) return false;
        if (attempt.lockUntil != null && Instant.now().isBefore(attempt.lockUntil)) return true;
        return false;
    }

    public void recordFailure(String email, String ip) {
        var key = key(email, ip);
        var attempt = store.computeIfAbsent(key, value -> new Attempt());
        attempt.count++;
        attempt.lastFailure = Instant.now();
        if (attempt.count >= maxFailures) {
            attempt.lockUntil = Instant.now().plusMillis(lockMillis);
        }
    }

    public void recordSuccess(String email, String ip) {
        store.remove(key(email, ip));
    }
}
