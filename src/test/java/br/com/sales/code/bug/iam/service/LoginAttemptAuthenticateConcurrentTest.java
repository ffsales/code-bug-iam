package br.com.sales.code.bug.iam.service;

import br.com.sales.code.bug.iam.domain.*;
import br.com.sales.code.bug.iam.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class LoginAttemptAuthenticateConcurrentTest {

    @Test
    public void shouldExecuteAuthenticateInThreads() throws InterruptedException, RuntimeException {
        ExecutorService executor =
                Executors.newFixedThreadPool(10);

        var userRepository = new UserRepository();
        var passwordHasher = new PasswordHasherDigest();
        var loginAttemptTracker = new LoginAttemptTracker();
        var authenticateService = new AuthenticationService(userRepository, passwordHasher, loginAttemptTracker);

        var id = UUID.randomUUID();
        var role = new Role("role", Set.of());
        var user = User.newUser(id, "name", "email", "pass", UserStatus.ATIVO, Set.of(role), passwordHasher);
        userRepository.save(user);

        List<Callable<LoginResult>> callablesLoginResult = List.of(
                () -> authenticateService.tryAuthenticate("name", "error1"),
                () -> authenticateService.tryAuthenticate("name", "error2"),
                () -> authenticateService.tryAuthenticate("name", "error3"),
                () -> authenticateService.tryAuthenticate("name", "error4"),
                () -> authenticateService.tryAuthenticate("name", "error5"),
                () -> authenticateService.tryAuthenticate("name", "error6"),
                () -> authenticateService.tryAuthenticate("name", "error7"),
                () -> authenticateService.tryAuthenticate("name", "error8"),
                () -> authenticateService.tryAuthenticate("name", "error9"),
                () -> authenticateService.tryAuthenticate("name", "error10")
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
