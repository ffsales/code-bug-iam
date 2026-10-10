package br.com.sales.code.bug.iam.service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LoginAttemptTracker {

    private final ConcurrentHashMap<UUID, AtomicInteger> counterAttempts = new ConcurrentHashMap<>();

    public void addAttemptFail(UUID userId) {
        counterAttempts.computeIfAbsent(
                userId,
                key -> new AtomicInteger()
        ).incrementAndGet();
    }

    public boolean isReachedMaxAttempts(UUID userId) {
        int MAX_ATTEMPTS = 5;
        if (counterAttempts.containsKey(userId))
            return counterAttempts.get(userId).get() == MAX_ATTEMPTS;
        return false;
    }

    public void restartAttempts(UUID userId) {
        if (counterAttempts.containsKey(userId))
            counterAttempts.replace(userId, new AtomicInteger());
    }
}
