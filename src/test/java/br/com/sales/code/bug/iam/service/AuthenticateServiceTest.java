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
        var loginAttemptTracker = new LoginAttemptTracker();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher, loginAttemptTracker);

        var notFoundException = assertThrows(UserNotFoundException.class, () -> {
           authenticateService.authenticate("user", "pass");
        });
        assertEquals("Username user não encontrado", notFoundException.getMessage());
    }

    @Test
    public void shouldThrowExceptionOfStatusIfUserIsNotActive() {
        var userRepository = new UserRepository();
        var passwordHasher = new PasswordHasherDigest();
        var loginAttemptTracker = new LoginAttemptTracker();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher, loginAttemptTracker);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = User.newUser(id, "name", "email", "pass", UserStatus.BLOQUEADO, Set.of(role), passwordHasher);
        userRepository.save(user);

        var blockedException = assertThrows(UserNotActiveException.class, () -> {
            authenticateService.authenticate("name", "pass");
        });
        assertEquals("O status do usuário é: BLOQUEADO.", blockedException.getMessage());
        assertEquals(UserStatus.BLOQUEADO, blockedException.getStatus());

        var otherId = UUID.randomUUID();
        var otherUser = User.newUser(otherId, "otherName", "email", "pass", UserStatus.PENDENTE, Set.of(role), passwordHasher);
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
        var loginAttemptTracker = new LoginAttemptTracker();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher, loginAttemptTracker);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = User.newUser(id, "name", "email", "pass", UserStatus.ATIVO, Set.of(role), passwordHasher);
        userRepository.save(user);

        assertThrows(InvalidCredentialsException.class, () -> {
            authenticateService.authenticate("name", "pass1");
        });
    }

    @Test
    public void shouldAuthenticateUserWithValidCredentials() {
        var userRepository = new UserRepository();
        var passwordHasher = new PasswordHasherDigest();
        var loginAttemptTracker = new LoginAttemptTracker();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher, loginAttemptTracker);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = User.newUser(id, "name", "email", "pass", UserStatus.ATIVO, Set.of(role), passwordHasher);
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
        var loginAttemptTracker = new LoginAttemptTracker();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher, loginAttemptTracker);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = User.newUser(id, "name", "email", "pass", UserStatus.BLOQUEADO, Set.of(role), passwordHasher);
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
        var loginAttemptTracker = new LoginAttemptTracker();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher, loginAttemptTracker);

        var result = authenticateService.tryAuthenticate("teste", "teste");
        assertNotNull(result);
        assertInstanceOf(UserNotFound.class, result);
    }

    @Test
    public void shouldReturnUserBlockedInTryAuthenticate() {
        var userRepository = new UserRepository();
        var passwordHasher = new PasswordHasherDigest();
        var loginAttemptTracker = new LoginAttemptTracker();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher, loginAttemptTracker);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = User.newUser(id, "name", "email", "pass", UserStatus.BLOQUEADO, Set.of(role), passwordHasher);
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
        var loginAttemptTracker = new LoginAttemptTracker();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher, loginAttemptTracker);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = User.newUser(id, "name", "email", "pass", UserStatus.PENDENTE, Set.of(role), passwordHasher);
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
        var loginAttemptTracker = new LoginAttemptTracker();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher, loginAttemptTracker);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = User.newUser(id, "name", "email", "pass", UserStatus.ATIVO, Set.of(role), passwordHasher);
        userRepository.save(user);

        var result = authenticateService.tryAuthenticate("name", "error");
        assertNotNull(result);
        assertInstanceOf(InvalidCredentials.class, result);
    }

    @Test
    public void shouldChangeUserStatusToBlock() {

        var userRepository = new UserRepository();
        var passwordHasher = new PasswordHasherDigest();
        var loginAttemptTracker = new LoginAttemptTracker();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher, loginAttemptTracker);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = User.newUser(id, "name", "email", "pass", UserStatus.ATIVO, Set.of(role), passwordHasher);
        userRepository.save(user);

        for (int i = 0; i < 5; i++) {
            assertThrows(InvalidCredentialsException.class,
                () -> authenticateService.authenticate("name", "pass1"));
        }
        var getUser = userRepository.getById(user.getId());

        assertNotNull(getUser);
        assertEquals(user.getId(), getUser.getId());
        assertEquals(user.getUsername(), getUser.getUsername());

        assertEquals(UserStatus.BLOQUEADO, getUser.getStatus());
    }

    @Test
    public void shouldRestartCounterAttempts() {
        var userRepository = new UserRepository();
        var passwordHasher = new PasswordHasherDigest();
        var loginAttemptTracker = new LoginAttemptTracker();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher, loginAttemptTracker);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = User.newUser(id, "name", "email", "pass", UserStatus.ATIVO, Set.of(role), passwordHasher);
        userRepository.save(user);

        for (int i = 0; i < 3; i++) {
            assertThrows(InvalidCredentialsException.class,
                    () -> authenticateService.authenticate("name", "pass1"));
        }

        authenticateService.authenticate("name", "pass");

        for (int i = 0; i < 3; i++) {
            assertThrows(InvalidCredentialsException.class,
                    () -> authenticateService.authenticate("name", "pass1"));
        }

        var getUser = userRepository.getById(user.getId());

        assertNotNull(getUser);
        assertEquals(user.getId(), getUser.getId());
        assertEquals(user.getUsername(), getUser.getUsername());

        assertEquals(UserStatus.ATIVO, getUser.getStatus());
    }

    @Test
    public void shouldReturnInvalidCredentialsInTryAuthenticateAndBlockUser() {
        var userRepository = new UserRepository();
        var passwordHasher = new PasswordHasherDigest();
        var loginAttemptTracker = new LoginAttemptTracker();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher, loginAttemptTracker);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = User.newUser(id, "name", "email", "pass", UserStatus.ATIVO, Set.of(role), passwordHasher);
        userRepository.save(user);

        for (int i = 0; i < 5; i++) {
            var result = authenticateService.tryAuthenticate("name", "error");
            assertNotNull(result);
            if (i < 4)
                assertInstanceOf(InvalidCredentials.class, result);
            if (i == 4)
                assertInstanceOf(UserBlocked.class, result);
        }

        var resultBlock = authenticateService.tryAuthenticate("name", "pass");
        assertNotNull(resultBlock);
        assertInstanceOf(UserBlocked.class, resultBlock);
    }

    @Test
    public void shouldRestartCounterAttemptsOnTryAuthenticate() {
        var userRepository = new UserRepository();
        var passwordHasher = new PasswordHasherDigest();
        var loginAttemptTracker = new LoginAttemptTracker();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher, loginAttemptTracker);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = User.newUser(id, "name", "email", "pass", UserStatus.ATIVO, Set.of(role), passwordHasher);
        userRepository.save(user);

        for (int i = 0; i < 3; i++) {
            var result = authenticateService.tryAuthenticate("name", "error");
            assertNotNull(result);
            assertInstanceOf(InvalidCredentials.class, result);
        }

        var resultSuccess = authenticateService.tryAuthenticate("name", "pass");
        assertNotNull(resultSuccess);
        assertInstanceOf(Success.class, resultSuccess);

        for (int i = 0; i < 3; i++) {
            var result = authenticateService.tryAuthenticate("name", "error");
            assertNotNull(result);
            assertInstanceOf(InvalidCredentials.class, result);
        }

        var statusResult = userRepository.getById(user.getId());
        assertEquals(UserStatus.ATIVO, statusResult.getStatus());
    }
}
