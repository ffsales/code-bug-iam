package br.com.sales.code.bug.iam.service;

import br.com.sales.code.bug.iam.domain.*;
import br.com.sales.code.bug.iam.domain.exception.InvalidCredentialsException;
import br.com.sales.code.bug.iam.domain.exception.UserNotActiveException;
import br.com.sales.code.bug.iam.repository.UserRepository;

public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final LoginAttemptTracker loginAttemptTracker;

    public AuthenticationService(UserRepository userRepository, PasswordHasher passwordHasher, LoginAttemptTracker loginAttemptTracker) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.loginAttemptTracker = loginAttemptTracker;
    }

    public User authenticate(String username, String password) {

        var user = this.userRepository.getByUsername(username);
        if (!UserStatus.ATIVO.equals(user.getStatus())) {
            throw new UserNotActiveException("O status do usuário é: %s.".formatted(user.getStatus()), user.getStatus());
        }

        var passHash = this.passwordHasher.hash(password);

        if (!user.getPasswordHash().equals(passHash)) {
            this.treatLoginFail(user);
            throw new InvalidCredentialsException("Senha inválida");
        }

        this.loginAttemptTracker.restartAttempts(user.getId());

        return user;
    }

    public LoginResult tryAuthenticate(String username, String password) {

        return userRepository.findByUsername(username)
                .map(user -> {
                    if (UserStatus.BLOQUEADO.equals(user.getStatus()))
                        return new UserBlocked(username);
                    if (UserStatus.PENDENTE.equals(user.getStatus()))
                        return new UserPending(username);
                    var passHash = this.passwordHasher.hash(password);
                    if (!user.getPasswordHash().equals(passHash)) {
                        return this.treatLoginFail(user);
                    }
                    this.loginAttemptTracker.restartAttempts(user.getId());
                    return new Success(user);
                })
                .orElse(new UserNotFound(username));
    }

    private LoginResult treatLoginFail(User user) {
        var attempts = this.loginAttemptTracker.addAttemptFail(user.getId());
        int MAX_ATTEMPTS = 5;
        if (attempts >= MAX_ATTEMPTS) {
            user.blockUser();
            this.userRepository.save(user);
            return new UserBlocked(user.getUsername());
        }
        return new InvalidCredentials();
    }
}

