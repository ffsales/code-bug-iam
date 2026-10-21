package br.com.sales.code.bug.iam.service;

import br.com.sales.code.bug.iam.domain.*;
import br.com.sales.code.bug.iam.domain.exception.InvalidCredentialsException;
import br.com.sales.code.bug.iam.domain.exception.UserNotActiveException;
import br.com.sales.code.bug.iam.domain.exception.UserNotFoundException;
import br.com.sales.code.bug.iam.repository.UserRepository;
import br.com.sales.code.bug.iam.service.doubles.LoginAttemptTrackerFake;
import br.com.sales.code.bug.iam.service.doubles.PasswordHasherStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticateServiceTest {

    private UserRepository userRepository;
    private PasswordHasher passwordHasher;
    private LoginAttemptTrackerImpl loginAttemptTrackerImpl;
    private AuthenticationService authenticationService;

    @BeforeEach
    public void setupAuthenticationTest() {
        userRepository = new UserRepository();
        passwordHasher = new PasswordHasherDigest();
        loginAttemptTrackerImpl = new LoginAttemptTrackerImpl();
        authenticationService = new AuthenticationService(userRepository, passwordHasher, loginAttemptTrackerImpl);
    }

    @Nested
    class AuthenticateServiceAutenticateTest {

        @Test
        public void shouldThrowUserNotFoundExceptionIfNotFoundUser() {
            var notFoundException = assertThrows(UserNotFoundException.class, () -> {
                authenticationService.authenticate("user", "pass");
            });
            assertEquals("Username user não encontrado", notFoundException.getMessage());
        }

        // Usei o proóprio argumento para validar a mensagem, mas entendo que isso não será possível em todas os cenários
        // EnumSource deixa mais claro que está sendo usado o um Enum como parâmetro do teste
        @ParameterizedTest
        @EnumSource(value = UserStatus.class, names = {"BLOQUEADO", "PENDENTE"})
        public void shouldThrowExceptionOfStatusIfUserIsNotActiveParameterized(UserStatus userStatus) {

            var id = UUID.randomUUID();
            var role = new Role("role", Set.of());
            var user = User.newUser(id, "name", "email", "pass", userStatus, Set.of(role), passwordHasher);
            userRepository.save(user);

            var userNotActiveException = assertThrows(UserNotActiveException.class, () -> {
                authenticationService.authenticate("name", "pass");
            });
            assertEquals("O status do usuário é: ".concat(userStatus.name()).concat("."), userNotActiveException.getMessage());
            assertEquals(userStatus, userNotActiveException.getStatus());
        }

        @Test
        public void shouldThrowsInvalidCredentialsExceptionWithWrongPass() {
            var id = UUID.randomUUID();
            var role = new Role("role", Set.of());
            var stubHasher = new PasswordHasherStub();
            var user = User.newUser(id, "name", "email", "pass", UserStatus.ATIVO, Set.of(role), stubHasher);
            userRepository.save(user);

            var otherAuthenticateService = new AuthenticationService(userRepository, stubHasher, loginAttemptTrackerImpl);

            assertThrows(InvalidCredentialsException.class, () -> {
                otherAuthenticateService.authenticate("name", "pass1");
            });
        }

        @Test
        public void shouldAuthenticateUserWithValidCredentials() {
            var id = UUID.randomUUID();
            var role = new Role("role", Set.of());
            var stubHasher = new PasswordHasherStub();
            var user = User.newUser(id, "name", "email", "pass", UserStatus.ATIVO, Set.of(role), stubHasher);
            userRepository.save(user);

            var otherAuthenticateService = new AuthenticationService(userRepository, stubHasher, loginAttemptTrackerImpl);
            var userAuthenticated = otherAuthenticateService.authenticate("name", "pass");
            assertNotNull(userAuthenticated);
            assertEquals(user.getId(), userAuthenticated.getId());
            assertEquals(user.getUsername(), userAuthenticated.getUsername());
        }

        @Test
        public void shouldThrowsUserNotActiveExceptionWithBlockedUserAndInvalidCredentials() {
            var id = UUID.randomUUID();
            var role = new Role("role", Set.of());
            var user = User.newUser(id, "name", "email", "pass", UserStatus.BLOQUEADO, Set.of(role), passwordHasher);
            userRepository.save(user);

            var blockedException = assertThrows(UserNotActiveException.class, () -> {
                authenticationService.authenticate("name", "wrong");
            });
            assertEquals("O status do usuário é: BLOQUEADO.", blockedException.getMessage());
        }

        @Test
        public void shouldChangeUserStatusToBlock() {
            var id = UUID.randomUUID();
            var role = new Role("role", Set.of());
            var user = User.newUser(id, "name", "email", "pass", UserStatus.ATIVO, Set.of(role), passwordHasher);
            userRepository.save(user);

            var loginAttemptTrackerFake = new LoginAttemptTrackerFake();
            var otherAuthenticateService = new AuthenticationService(userRepository, passwordHasher, loginAttemptTrackerFake);

            loginAttemptTrackerFake.goToLimitAttempts(user.getId());

            assertThrows(InvalidCredentialsException.class,
                () -> otherAuthenticateService.authenticate("name", "pass1"));

            var getUser = userRepository.getById(user.getId());

            assertNotNull(getUser);
            assertEquals(user.getId(), getUser.getId());
            assertEquals(user.getUsername(), getUser.getUsername());

            assertEquals(UserStatus.BLOQUEADO, getUser.getStatus());
        }

        @ParameterizedTest()
        @ValueSource(ints = {2, 4})
        public void shouldRestartCounterAttempts(int times) {
            var id = UUID.randomUUID();
            var role = new Role("role", Set.of());
            var user = User.newUser(id, "name", "email", "pass", UserStatus.ATIVO, Set.of(role), passwordHasher);
            userRepository.save(user);

            for (int i = 0; i < times; i++) {
                assertThrows(InvalidCredentialsException.class,
                        () -> authenticationService.authenticate("name", "pass1"));
            }

            authenticationService.authenticate("name", "pass");

            for (int i = 0; i < times; i++) {
                assertThrows(InvalidCredentialsException.class,
                        () -> authenticationService.authenticate("name", "pass1"));
            }

            var getUser = userRepository.getById(user.getId());

            assertNotNull(getUser);
            assertEquals(user.getId(), getUser.getId());
            assertEquals(user.getUsername(), getUser.getUsername());

            assertEquals(UserStatus.ATIVO, getUser.getStatus());
        }
    }

    @Nested
    class AuthenticateServiceTryAuthenticateTest {

        @Test
        public void shouldReturnUserNotFoundInTryAuthenticate() {
            var result = authenticationService.tryAuthenticate("teste", "teste");
            assertNotNull(result);
            assertInstanceOf(UserNotFound.class, result);
        }

        @Test
        public void shouldReturnUserBlockedInTryAuthenticate() {
            var id = UUID.randomUUID();
            var role = new Role("role", Set.of());
            var user = User.newUser(id, "name", "email", "pass", UserStatus.BLOQUEADO, Set.of(role), passwordHasher);
            userRepository.save(user);

            var result = authenticationService.tryAuthenticate("name", "teste");
            assertNotNull(result);
            assertInstanceOf(UserBlocked.class, result);
            assertEquals("name", ((UserBlocked)result).username());
        }

        @Test
        public void shouldReturnUserPendingInTryAuthenticate() {
            var id = UUID.randomUUID();
            var role = new Role("role", Set.of());
            var user = User.newUser(id, "name", "email", "pass", UserStatus.PENDENTE, Set.of(role), passwordHasher);
            userRepository.save(user);

            var result = authenticationService.tryAuthenticate("name", "teste");
            assertNotNull(result);
            assertInstanceOf(UserPending.class, result);
            assertEquals("name", ((UserPending)result).username());
        }

        @Test
        public void shouldReturnInvalidCredentialsInTryAuthenticate() {
            var id = UUID.randomUUID();
            var role = new Role("role", Set.of());
            var user = User.newUser(id, "name", "email", "pass", UserStatus.ATIVO, Set.of(role), passwordHasher);
            userRepository.save(user);

            var result = authenticationService.tryAuthenticate("name", "error");
            assertNotNull(result);
            assertInstanceOf(InvalidCredentials.class, result);
        }

        @Test
        public void shouldReturnInvalidCredentialsInTryAuthenticateAndBlockUser() {
            var id = UUID.randomUUID();
            var role = new Role("role", Set.of());
            var user = User.newUser(id, "name", "email", "pass", UserStatus.ATIVO, Set.of(role), passwordHasher);
            userRepository.save(user);

            for (int i = 0; i < 5; i++) {
                var result = authenticationService.tryAuthenticate("name", "error");
                assertNotNull(result);
                if (i < 4)
                    assertInstanceOf(InvalidCredentials.class, result);
                if (i == 4)
                    assertInstanceOf(UserBlocked.class, result);
            }

            var resultBlock = authenticationService.tryAuthenticate("name", "pass");
            assertNotNull(resultBlock);
            assertInstanceOf(UserBlocked.class, resultBlock);
        }

        @Test
        public void shouldRestartCounterAttemptsOnTryAuthenticate() {
            var id = UUID.randomUUID();
            var role = new Role("role", Set.of());
            var user = User.newUser(id, "name", "email", "pass", UserStatus.ATIVO, Set.of(role), passwordHasher);
            userRepository.save(user);

            for (int i = 0; i < 3; i++) {
                var result = authenticationService.tryAuthenticate("name", "error");
                assertNotNull(result);
                assertInstanceOf(InvalidCredentials.class, result);
            }

            var resultSuccess = authenticationService.tryAuthenticate("name", "pass");
            assertNotNull(resultSuccess);
            assertInstanceOf(Success.class, resultSuccess);

            for (int i = 0; i < 3; i++) {
                var result = authenticationService.tryAuthenticate("name", "error");
                assertNotNull(result);
                assertInstanceOf(InvalidCredentials.class, result);
            }

            var statusResult = userRepository.getById(user.getId());
            assertEquals(UserStatus.ATIVO, statusResult.getStatus());
        }
    }

    @Nested
    class LoginAttemptAuthenticateConcurrentTest {

        @Test
        public void shouldExecuteAuthenticateInThreads() throws InterruptedException, RuntimeException {
            ExecutorService executor =
                    Executors.newFixedThreadPool(10);

            var id = UUID.randomUUID();
            var role = new Role("role", Set.of());
            var user = User.newUser(id, "name", "email", "pass", UserStatus.ATIVO, Set.of(role), passwordHasher);
            userRepository.save(user);

            List<Callable<LoginResult>> callablesLoginResult = List.of(
                    () -> authenticationService.tryAuthenticate("name", "error1"),
                    () -> authenticationService.tryAuthenticate("name", "error2"),
                    () -> authenticationService.tryAuthenticate("name", "error3"),
                    () -> authenticationService.tryAuthenticate("name", "error4"),
                    () -> authenticationService.tryAuthenticate("name", "error5"),
                    () -> authenticationService.tryAuthenticate("name", "error6"),
                    () -> authenticationService.tryAuthenticate("name", "error7"),
                    () -> authenticationService.tryAuthenticate("name", "error8"),
                    () -> authenticationService.tryAuthenticate("name", "error9"),
                    () -> authenticationService.tryAuthenticate("name", "error10")
            );

            Map<Class<? extends LoginResult>, Integer> mapResult = new HashMap<>();

            for (Future<LoginResult> futureResult : executor.invokeAll(callablesLoginResult)) {

                try {
                    var result =  futureResult.get();
                    var qtde = mapResult.get(result.getClass());
                    mapResult.put(result.getClass(), qtde == null ? 1 : ++qtde);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }

            assertEquals(4, mapResult.get(InvalidCredentials.class));
            assertEquals(6, mapResult.get(UserBlocked.class));
        }
    }
}
