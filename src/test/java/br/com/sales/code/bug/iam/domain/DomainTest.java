package br.com.sales.code.bug.iam.domain;

import br.com.sales.code.bug.iam.domain.exception.UserFieldInvalidException;
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

        assertEquals(1, roles.size());
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenGiveInvalidData() {
        var idUserNullPointerException = assertThrows(NullPointerException.class, () -> {
            User.newUser(null, "Name", "Email", "Pass", UserStatus.ATIVO, Set.of(), passwordHasher);
        });
        assertEquals("Id é obrigatório.", idUserNullPointerException.getMessage());

        var usernameNullPointerException = assertThrows(NullPointerException.class, () -> {
           User.newUser(UUID.randomUUID(), null, "Email", "Pass", UserStatus.ATIVO, Set.of(), passwordHasher);
        });
        assertEquals("Username é obrigatório.", usernameNullPointerException.getMessage());

        var emailNullPointerException = assertThrows(NullPointerException.class, () -> {
            User.newUser(UUID.randomUUID(), "Name", null, "Pass", UserStatus.ATIVO, Set.of(), passwordHasher);
        });
        assertEquals("Email é obrigatório.", emailNullPointerException.getMessage());

        var passNullPointerException = assertThrows(NullPointerException.class, () -> {
            User.newUser(UUID.randomUUID(), "Name", "Email", null, UserStatus.ATIVO, Set.of(), passwordHasher);
        });
        assertEquals("Password é obrigatório.", passNullPointerException.getMessage());

        var statusNullPointerException = assertThrows(NullPointerException.class, () -> {
            User.newUser(UUID.randomUUID(), "Name", "Email", "Pass", null, Set.of(), passwordHasher);
        });
        assertEquals("Status é obrigatório.", statusNullPointerException.getMessage());

        var rolesNullPointerException = assertThrows(NullPointerException.class, () -> {
            User.newUser(UUID.randomUUID(), "Name", "Email", "Pass", UserStatus.ATIVO, null, passwordHasher);
        });
        assertEquals("Role é obrigatório.", rolesNullPointerException.getMessage());

        var hasherNullPointerException = assertThrows(NullPointerException.class, () -> {
            User.newUser(UUID.randomUUID(), "NAME", "EMAIL", "PASS", UserStatus.ATIVO, Set.of(new Role("name", Set.of())), null);
        });
        assertEquals("PasswordHasher é obrigatório.", hasherNullPointerException.getMessage());

        var withoutRoleException = assertThrows(UserWithoutRoleException.class, () -> {
            User.newUser(UUID.randomUUID(), "NAME", "EMAIL", "PASS", UserStatus.ATIVO, Set.of(), passwordHasher);
        });
        assertEquals("É obrigatório ao menos uma Role.", withoutRoleException.getMessage());
    }



    @Test
    public void shouldThrowUserFieldInvalidExceptionWithInvalidUUIDOnReconstructUser() {
        var idNullUserFieldInvalidException = assertThrows(UserFieldInvalidException.class, () ->
                User.reconstructUser(null, "123", "teste@teste.com", "123", "ATIVO", Set.of()));
        assertEquals("Id é obrigatório.", idNullUserFieldInvalidException.getMessage());

        var idUserFieldInvalidException = assertThrows(UserFieldInvalidException.class, () ->
                User.reconstructUser("123", "123", "teste@teste.com", "123", "ATIVO", Set.of()));
        assertEquals("Id é inválido.", idUserFieldInvalidException.getMessage());

        var usernameUserFieldInvalidException = assertThrows(UserFieldInvalidException.class, () ->
                User.reconstructUser(UUID.randomUUID().toString(), null, "teste@teste.com", "123", "ATIVO", Set.of()));
        assertEquals("Username é obrigatório.", usernameUserFieldInvalidException.getMessage());

        var emailUserFieldInvalidException = assertThrows(UserFieldInvalidException.class, () ->
                User.reconstructUser(UUID.randomUUID().toString(), "user", null, "123", "ATIVO", Set.of()));
        assertEquals("Email é obrigatório.", emailUserFieldInvalidException.getMessage());

        var passUserFieldInvalidException = assertThrows(UserFieldInvalidException.class, () ->
                User.reconstructUser(UUID.randomUUID().toString(), "user", "teste@teste.com", null, "ATIVO", Set.of()));
        assertEquals("Password é obrigatório.", passUserFieldInvalidException.getMessage());

        var statusNullUserFieldInvalidException = assertThrows(UserFieldInvalidException.class, () ->
                User.reconstructUser(UUID.randomUUID().toString(), "user", "teste@teste.com", "123", null, Set.of()));
        assertEquals("Status é obrigatório.", statusNullUserFieldInvalidException.getMessage());

        var statusUserFieldInvalidException = assertThrows(UserFieldInvalidException.class, () ->
                User.reconstructUser(UUID.randomUUID().toString(), "user", "teste@teste.com", "123", "TESTE", Set.of()));
        assertEquals("Status é inválido.", statusUserFieldInvalidException.getMessage());

        var rolesUserWithoutRoleException = assertThrows(UserWithoutRoleException.class, () ->
                User.reconstructUser(UUID.randomUUID().toString(), "user", "teste@teste.com", "123", "ATIVO", Set.of()));
        assertEquals("É obrigatório ao menos uma Role.", rolesUserWithoutRoleException.getMessage());

    }

    private User createMockedUser(Set<Role> setRoles) {

        var id = UUID.randomUUID();
        var username = "username";
        var email = "username@teste.com";
        var pass = MOCKED_PASS;
        var status = UserStatus.ATIVO;
        var roles = setRoles;

        return User.newUser(id, username, email, pass, status, roles, passwordHasher);
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
