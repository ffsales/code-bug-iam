package br.com.sales.code.bug.iam.service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LoginAttemptTrackerImpl implements LoginAttemptTracker {

    private final ConcurrentHashMap<UUID, AtomicInteger> counterAttempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;

    public int addAttemptFail(UUID userId) {
        return counterAttempts.computeIfAbsent(
                userId,
                key -> new AtomicInteger()
        ).incrementAndGet();
    }

    public boolean isReachedMaxAttempts(UUID userId) {
        AtomicInteger counter = counterAttempts.get(userId);

        return counter != null
                && counter.get() >= MAX_ATTEMPTS;
    }

    public void restartAttempts(UUID userId) {
        counterAttempts.computeIfPresent(
                userId,
                (key, counter) -> {
                    counter.set(0);
                    return counter;
                }
        );
    }
}
