package br.com.sales.code.bug.iam.service;

import br.com.sales.code.bug.iam.domain.Role;
import br.com.sales.code.bug.iam.domain.User;
import br.com.sales.code.bug.iam.domain.UserStatus;
import br.com.sales.code.bug.iam.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

public class ConsumerLoginTest {

    @Test
    public void shouldGetSucessResultOfTryAuthenticate() {
        var userRepository = new UserRepository();
        var passwordHasher = new PasswordHasherDigest();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = new User(id, "name", "email", "pass", UserStatus.ATIVO, Set.of(role), passwordHasher);
        userRepository.save(user);

        var result = authenticateService.tryAuthenticate("name", "pass");

        var consumerLogin = new ConsumerLogin();
        var message = consumerLogin.describe(result);

        Assertions.assertNotNull(message);
        Assertions.assertEquals("O usuário name está autenticado com sucesso", message);
    }

    @Test
    public void shouldGetUserBlockedResultOfTryAuthenticate() {
        var userRepository = new UserRepository();
        var passwordHasher = new PasswordHasherDigest();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = new User(id, "name", "email", "pass", UserStatus.BLOQUEADO, Set.of(role), passwordHasher);
        userRepository.save(user);

        var result = authenticateService.tryAuthenticate("name", "wrong");

        var consumerLogin = new ConsumerLogin();
        var message = consumerLogin.describe(result);

        Assertions.assertNotNull(message);
        Assertions.assertEquals("O usuário name está bloqueado", message);
    }
}
