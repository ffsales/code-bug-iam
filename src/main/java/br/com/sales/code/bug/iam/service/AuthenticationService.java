package br.com.sales.code.bug.iam.service;

import br.com.sales.code.bug.iam.domain.*;
import br.com.sales.code.bug.iam.domain.exception.InvalidCredentialsException;
import br.com.sales.code.bug.iam.domain.exception.UserNotActiveException;
import br.com.sales.code.bug.iam.repository.UserRepository;

public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public AuthenticationService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public User authenticate(String username, String password) {

        var user = userRepository.getByUsername(username);
        if (!UserStatus.ATIVO.equals(user.getStatus())) {
            throw new UserNotActiveException("O status do usuário é: %s.".formatted(user.getStatus()), user.getStatus());
        }

        var passHash = this.passwordHasher.hash(password);

        if (!user.getPasswordHash().equals(passHash)) {
            throw new InvalidCredentialsException("Senha inválida");
        }

        return user;
    }

    public LoginResult tryAuthenticate(String username, String password) {
        var optUser = userRepository.findByUsername(username);

        if (optUser.isEmpty())
            return new UserNotFound(username);

        var user = optUser.get();

        if (UserStatus.BLOQUEADO.equals(user.getStatus()))
            return new UserBlocked(username);

        if (UserStatus.PENDENTE.equals(user.getStatus()))
            return new UserPending(username);

        var passHash = this.passwordHasher.hash(password);

        if (!user.getPasswordHash().equals(passHash))
            return new InvalidCredentials();

        return new Success(user);
    }
}

