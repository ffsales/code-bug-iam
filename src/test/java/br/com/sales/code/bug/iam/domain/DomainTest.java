package br.com.sales.code.bug.iam.domain;

import br.com.sales.code.bug.iam.domain.exception.UserWithoutRoleException;
import br.com.sales.code.bug.iam.service.PasswordHasher;
import br.com.sales.code.bug.iam.service.PasswordHasherDigest;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DomainTest {

    private static final String MOCKED_PASS = "minhaSenha123";
    private static final String MOCKED_ADMIN_PERMISSION_ADMIN = "admin";
    private static final String MOCKED_ADMIN_PERMISSION_USER_WRITE = "user:write";
    private static final String MOCKED_ADMIN_PERMISSION_USER_READ = "user:read";

    private final PasswordHasher passwordHasher = new PasswordHasherDigest();

    @Test
    public void shouldNotRetrievePasswordUserInPlainText() {

        var roles = createMockedSetRoles();
        var user = createMockedUser(roles);

        assertNotEquals(MOCKED_PASS, user.getPasswordHash());
    }

    @Test
    public void shouldHasPermissionInUserRole() {

        var roles = createMockedSetRoles();
        var user = createMockedUser(roles);

        assertTrue(user.hasPermission(MOCKED_ADMIN_PERMISSION_USER_WRITE));
        assertFalse(user.hasPermission(MOCKED_ADMIN_PERMISSION_USER_READ));
    }

    @Test
    public void shouldNotModifiedUserRole() {

        var roles = createMockedSetRoles();
        var user = createMockedUser(roles);

        var newSetPermissions = new HashSet<Permission>();
        newSetPermissions.add(new Permission(MOCKED_ADMIN_PERMISSION_USER_READ));

        assertThrows(RuntimeException.class, () -> {
            user.getRoles().add(new Role("User", newSetPermissions));
        });

        roles.add(new Role("TESTE", newSetPermissions));

        assertFalse(user.hasPermission(MOCKED_ADMIN_PERMISSION_USER_READ));
    }

    @Test
    public void shouldAddAndRemoveANewRoleToSetRoles() {
        var setRoles = createMockedSetRoles();
        var user = createMockedUser(setRoles);

        var newSetPermissions = new HashSet<Permission>();
        newSetPermissions.add(new Permission(MOCKED_ADMIN_PERMISSION_USER_READ));

        var newRole = new Role("NEW_ROLE", newSetPermissions);

        user.addRole(newRole);

        assertTrue(user.hasPermission(MOCKED_ADMIN_PERMISSION_USER_READ));

        var otherNewRole = new Role("NEW_ROLE", newSetPermissions);
        user.removeRole(otherNewRole    );

        assertFalse(user.hasPermission(MOCKED_ADMIN_PERMISSION_USER_READ));
    }

    @Test
    public void shouldNotAddRole() {
        var roles = new HashSet<Role>();

        var setPermissions1 = new HashSet<Permission>();
        setPermissions1.add(new Permission(MOCKED_ADMIN_PERMISSION_ADMIN));
        setPermissions1.add(new Permission(MOCKED_ADMIN_PERMISSION_USER_WRITE));
        var role1 = new Role("ROLE", setPermissions1);

        var setPermissions2 = new HashSet<Permission>();
        setPermissions2.add(new Permission(MOCKED_ADMIN_PERMISSION_ADMIN));
        setPermissions2.add(new Permission(MOCKED_ADMIN_PERMISSION_USER_WRITE));
        var role2 = new Role("ROLE", setPermissions2);

        roles.add(role1);
        roles.add(role2);

//        Assertions.assertEquals(2, roles.size());
        assertEquals(1, roles.size());
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenGiveInvalidData() {
        var idNullException = assertThrows(NullPointerException.class, () -> {
            new User(null, "Name", "Email", "Pass", UserStatus.ATIVO, Set.of(), passwordHasher);
        });
        assertEquals("Id é obrigatório.", idNullException.getMessage());

        var usernameNullException = assertThrows(NullPointerException.class, () -> {
           new User(UUID.randomUUID(), null, "Email", "Pass", UserStatus.ATIVO, Set.of(), passwordHasher);
        });
        assertEquals("Username é obrigatório.", usernameNullException.getMessage());

        var emailNullException = assertThrows(NullPointerException.class, () -> {
            new User(UUID.randomUUID(), "Name", null, "Pass", UserStatus.ATIVO, Set.of(), passwordHasher);
        });
        assertEquals("Email é obrigatório.", emailNullException.getMessage());

        var passNullException = assertThrows(NullPointerException.class, () -> {
            new User(UUID.randomUUID(), "Name", "Email", null, UserStatus.ATIVO, Set.of(), passwordHasher);
        });
        assertEquals("Password é obrigatório.", passNullException.getMessage());

        var statusNullException = assertThrows(NullPointerException.class, () -> {
            new User(UUID.randomUUID(), "Name", "Email", "Pass", null, Set.of(), passwordHasher);
        });
        assertEquals("Status é obrigatório.", statusNullException.getMessage());

        var rolesNullException = assertThrows(NullPointerException.class, () -> {
            new User(UUID.randomUUID(), "Name", "Email", "Pass", UserStatus.ATIVO, null, passwordHasher);
        });
        assertEquals("Roles é obrigatório.", rolesNullException.getMessage());

        var hasherNullException = assertThrows(NullPointerException.class, () -> {
            new User(UUID.randomUUID(), "NAME", "EMAIL", "PASS", UserStatus.ATIVO, Set.of(new Role("name", Set.of())), null);
        });
        assertEquals("Hasher é obrigatório.", hasherNullException.getMessage());

        var withoutRoleException = assertThrows(UserWithoutRoleException.class, () -> {
            new User(UUID.randomUUID(), "NAME", "EMAIL", "PASS", UserStatus.ATIVO, Set.of(), passwordHasher);
        });
        assertEquals("É obrigatório ao menos uma Role.", withoutRoleException.getMessage());
    }

    private User createMockedUser(Set<Role> setRoles) {

        var id = UUID.randomUUID();
        var username = "username";
        var email = "username@teste.com";
        var pass = MOCKED_PASS;
        var status = UserStatus.ATIVO;
        var roles = setRoles;

        return new User(id, username, email, pass, status, roles, passwordHasher);
    }

    private Set<Role> createMockedSetRoles() {

        var setPermissions = new HashSet<Permission>();
        setPermissions.add(new Permission(MOCKED_ADMIN_PERMISSION_ADMIN));
        setPermissions.add(new Permission(MOCKED_ADMIN_PERMISSION_USER_WRITE));

        var role = new Role("ADMIN", setPermissions);
        var setRoles = new HashSet<Role>();
        setRoles.add(role);

        return setRoles;
    }
}
