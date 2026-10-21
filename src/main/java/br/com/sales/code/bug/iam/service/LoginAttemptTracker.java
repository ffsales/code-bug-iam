package br.com.sales.code.bug.iam.service;

import java.util.UUID;

public interface LoginAttemptTracker {

    int addAttemptFail(UUID userId);

    boolean isReachedMaxAttempts(UUID userId);

    void restartAttempts(UUID userId);
}
