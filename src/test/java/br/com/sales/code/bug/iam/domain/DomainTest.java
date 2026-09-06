package br.com.sales.code.bug.iam.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class DomainTest {

    private static final String MOCKED_PASS = "minhaSenha123";
    private static final String MOCKED_ADMIN_PERMISSION_ADMIN = "admin";
    private static final String MOCKED_ADMIN_PERMISSION_USER_WRITE = "user:write";
    private static final String MOCKED_ADMIN_PERMISSION_USER_READ = "user:read";

    @Test
    public void sholdNotRetrievePasswordUserInPlainText() {

        var user = createMockedUser();

        Assertions.assertNotEquals(MOCKED_PASS, user.getPasswordHash());
    }

    private User createMockedUser() {

        var id = UUID.randomUUID();
        var username = "username";
        var email = "username@teste.com";
        var pass = MOCKED_PASS;
        var status = UserStatus.ATIVO;
        var roles = createMockedSetRoles();

        return new User(id, username, email, pass, status, roles);
    }

    @Test
    public void shouldHasPermissionInUserRole() {
        var user = createMockedUser();

        Assertions.assertTrue(user.hasPermission(MOCKED_ADMIN_PERMISSION_USER_WRITE));
        Assertions.assertFalse(user.hasPermission(MOCKED_ADMIN_PERMISSION_USER_READ));
    }

    @Test
    public void shouldNotModifiedUserRole() {
        var user = createMockedUser();

        Assertions.assertThrows(RuntimeException.class, () -> {
            user.getRoles().add(new Role("User", Arrays.asList(new Permission(MOCKED_ADMIN_PERMISSION_USER_READ))));
        });
    }

    private Set<Role> createMockedSetRoles() {
        var role = new Role("ADMIN", Arrays.asList(
                new Permission(MOCKED_ADMIN_PERMISSION_ADMIN),
                new Permission(MOCKED_ADMIN_PERMISSION_USER_WRITE)));
        return Set.of(role);
    }
}
