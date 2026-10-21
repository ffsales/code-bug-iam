package br.com.sales.code.bug.iam.service.doubles;

import br.com.sales.code.bug.iam.service.LoginAttemptTracker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A opção utilizada aqui foi um fake para simplificar o teste com uma classe mais leve, em vez de carregar classes
 * Thread-safe, usei classes comuns
 */
public class LoginAttemptTrackerFake implements LoginAttemptTracker {

    private final Map<UUID, Integer> counterAttempts = new HashMap<>();
    private static final int MAX_ATTEMPTS = 5;


    // Esse método permite levar o número de tentativas ao limite para agilizar o teste
    public void goToLimitAttempts(UUID userId) {
        counterAttempts.put(userId, MAX_ATTEMPTS);
    }

    @Override
    public int addAttemptFail(UUID userId) {
        var attempts = 1;
        if (counterAttempts.containsKey(userId)) {
            attempts += counterAttempts.get(userId);
        }
        counterAttempts.put(userId, attempts);

        return attempts;
    }

    @Override
    public boolean isReachedMaxAttempts(UUID userId) {
        return counterAttempts.containsKey(userId) && counterAttempts.get(userId) >= MAX_ATTEMPTS;
    }

    @Override
    public void restartAttempts(UUID userId) {
        counterAttempts.put(userId, 0);
    }
}
