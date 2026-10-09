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

    public boolean isReachedMaxAttempts(UUID uuid) {
        int MAX_ATTEMPTS = 5;
        return counterAttempts.get(uuid).get() == MAX_ATTEMPTS;
    }

    public void restartAttempts(UUID userId) {
        if (counterAttempts.isEmpty())
            return;
        if (counterAttempts.get(userId) == null)
            return;
        counterAttempts.get(userId).getAndDecrement();
    }
}
