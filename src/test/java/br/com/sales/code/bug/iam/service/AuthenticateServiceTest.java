package br.com.sales.code.bug.iam.service;

import br.com.sales.code.bug.iam.domain.*;
import br.com.sales.code.bug.iam.domain.exception.InvalidCredentialsException;
import br.com.sales.code.bug.iam.domain.exception.UserNotActiveException;
import br.com.sales.code.bug.iam.domain.exception.UserNotFoundException;
import br.com.sales.code.bug.iam.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticateServiceTest {

    @Test
    public void shouldThrowUserNotFoundExceptionIfNotFoundUser() {
        var userRepository = new UserRepository();
        var passwordHasher = new PasswordHasherDigest();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher);

        var notFoundException = assertThrows(UserNotFoundException.class, () -> {
           authenticateService.authenticate("user", "pass");
        });
        assertEquals("Username user não encontrado", notFoundException.getMessage());
    }

    @Test
    public void shouldThrowExceptionOfStatusIfUserIsNotActive() {
        var userRepository = new UserRepository();
        var passwordHasher = new PasswordHasherDigest();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = new User(id, "name", "email", "pass", UserStatus.BLOQUEADO, Set.of(role), passwordHasher);
        userRepository.save(user);

        var blockedException = assertThrows(UserNotActiveException.class, () -> {
            authenticateService.authenticate("name", "pass");
        });
        assertEquals("O status do usuário é: BLOQUEADO.", blockedException.getMessage());
        assertEquals(UserStatus.BLOQUEADO, blockedException.getStatus());

        var otherId = UUID.randomUUID();
        var otherUser = new User(otherId, "otherName", "email", "pass", UserStatus.PENDENTE, Set.of(role), passwordHasher);
        userRepository.save(otherUser);

        var pendenteException = assertThrows(UserNotActiveException.class, () -> {
            authenticateService.authenticate("otherName", "pass");
        });
        assertEquals("O status do usuário é: PENDENTE.", pendenteException.getMessage());
        assertEquals(UserStatus.PENDENTE, pendenteException.getStatus());
    }

    @Test
    public void shouldThrowsInvalidCredentialsExceptionWithWrongPass() {
        var userRepository = new UserRepository();
        var passwordHasher = new PasswordHasherDigest();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = new User(id, "name", "email", "pass", UserStatus.ATIVO, Set.of(role), passwordHasher);
        userRepository.save(user);

        assertThrows(InvalidCredentialsException.class, () -> {
            authenticateService.authenticate("name", "pass1");
        });
    }

    @Test
    public void shouldAuthenticateUserWithValidCredentials() {
        var userRepository = new UserRepository();
        var passwordHasher = new PasswordHasherDigest();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = new User(id, "name", "email", "pass", UserStatus.ATIVO, Set.of(role), passwordHasher);
        userRepository.save(user);

        var userAuthenticated = authenticateService.authenticate("name", "pass");
        assertNotNull(userAuthenticated);
        assertEquals(user.getId(), userAuthenticated.getId());
        assertEquals(user.getUsername(), userAuthenticated.getUsername());
    }

    @Test
    public void shouldThrowsUserNotActiveExceptionWithBlockedUserAndInvalidCredentials() {
        var userRepository = new UserRepository();
        var passwordHasher = new PasswordHasherDigest();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = new User(id, "name", "email", "pass", UserStatus.BLOQUEADO, Set.of(role), passwordHasher);
        userRepository.save(user);

        var blockedException = assertThrows(UserNotActiveException.class, () -> {
            authenticateService.authenticate("name", "wrong");
        });
        assertEquals("O status do usuário é: BLOQUEADO.", blockedException.getMessage());
    }

    @Test
    public void shouldReturnUserNotFoundInTryAuthenticate() {
        var userRepository = new UserRepository();
        var passwordHasher = new PasswordHasherDigest();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher);

        var result = authenticateService.tryAuthenticate("teste", "teste");
        assertNotNull(result);
        assertInstanceOf(UserNotFound.class, result);
    }

    @Test
    public void shouldReturnUserBlockedInTryAuthenticate() {
        var userRepository = new UserRepository();
        var passwordHasher = new PasswordHasherDigest();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = new User(id, "name", "email", "pass", UserStatus.BLOQUEADO, Set.of(role), passwordHasher);
        userRepository.save(user);

        var result = authenticateService.tryAuthenticate("name", "teste");
        assertNotNull(result);
        assertInstanceOf(UserBlocked.class, result);
        assertEquals("name", ((UserBlocked)result).username());
    }

    @Test
    public void shouldReturnUserPendingInTryAuthenticate() {
        var userRepository = new UserRepository();
        var passwordHasher = new PasswordHasherDigest();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = new User(id, "name", "email", "pass", UserStatus.PENDENTE, Set.of(role), passwordHasher);
        userRepository.save(user);

        var result = authenticateService.tryAuthenticate("name", "teste");
        assertNotNull(result);
        assertInstanceOf(UserPending.class, result);
        assertEquals("name", ((UserPending)result).username());
    }

    @Test
    public void shouldReturnInvalidCredentialsInTryAuthenticate() {
        var userRepository = new UserRepository();
        var passwordHasher = new PasswordHasherDigest();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = new User(id, "name", "email", "pass", UserStatus.ATIVO, Set.of(role), passwordHasher);
        userRepository.save(user);

        var result = authenticateService.tryAuthenticate("name", "error");
        assertNotNull(result);
        assertInstanceOf(InvalidCredentials.class, result);
    }
}
