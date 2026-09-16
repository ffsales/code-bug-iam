package br.com.sales.code.bug.iam.repository;

import br.com.sales.code.bug.iam.domain.Permission;
import br.com.sales.code.bug.iam.domain.Role;
import br.com.sales.code.bug.iam.domain.User;
import br.com.sales.code.bug.iam.domain.UserStatus;
import br.com.sales.code.bug.iam.domain.exception.DuplicateUsernameException;
import br.com.sales.code.bug.iam.domain.exception.UserNotFoundException;
import br.com.sales.code.bug.iam.service.PasswordHasher;
import br.com.sales.code.bug.iam.service.PasswordHasherDigest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


public class UserRepositoryTest {

    public static final String ADMIN = "ADMIN";
    public static final String COMMOM_WRITE = "COMMOM_WRITE";
    public static final String COMMOM_READ = "COMMOM_READ";
    private final PasswordHasher passwordHasher = new PasswordHasherDigest();

    private static final String MOCKED_ADMIN_PERMISSION_ADMIN = "admin";
    private static final String MOCKED_ADMIN_PERMISSION_USER_WRITE = "user:write";
    private static final String MOCKED_ADMIN_PERMISSION_USER_READ = "user:read";

    @Test
    public void shouldSaveUsersInRepository() {
        var repository = new UserRepository();

        var adminUser = createUserAdminActive();
        var commomUser = createUserCommomActive();

        repository.save(adminUser);
        repository.save(commomUser);

        assertEquals(2, repository.findAll().size());

        var blockedUser = createUserCommomBlocked();
        repository.save(blockedUser);

        assertEquals(3, repository.findAll().size());

        var newCommomUser = new User(
                commomUser.getId(),
                "NOVO_COMMOM",
                commomUser.getEmail(),
                commomUser.getPasswordHash(),
                commomUser.getStatus(),
                commomUser.getRoles(),
                passwordHasher);
        repository.save(newCommomUser);
        assertEquals(3, repository.findAll().size());

        repository.save(new User(
                UUID.randomUUID(),
                commomUser.getUsername(),
                commomUser.getEmail(),
                commomUser.getPasswordHash(),
                commomUser.getStatus(),
                commomUser.getRoles(),
                passwordHasher));
        assertEquals(4, repository.findAll().size());
    }

    @Test
    public void shouldThrowExceptionWithNewUserWithSameUsername() {
        var repository = new UserRepository();

        var adminUser = createUserAdminActive();
        var commomUser = createUserCommomActive();

        repository.save(adminUser);
        repository.save(commomUser);

        var newUser = new User(
                UUID.randomUUID(),
                commomUser.getUsername(),
                "newEmail",
                "newuser123",
                UserStatus.ATIVO,
                commomUser.getRoles(),
                passwordHasher
        );

        assertThrows(DuplicateUsernameException.class, () -> {
            repository.save(newUser);
        });
    }

    @Test
    public void shouldDoNotModifyUserList() {
        var repository = new UserRepository();

        var adminUser = createUserAdminActive();
        var commomUser = createUserCommomActive();

        repository.save(adminUser);
        repository.save(commomUser);

        var allUsers = repository.findAll();

        var blockedUser = createUserCommomBlocked();

        assertThrows(UnsupportedOperationException.class,() -> {
            allUsers.add(blockedUser);
        });
        assertEquals(2, allUsers.size());
    }

    @Test
    public void shouldGetUsersByStatus()  {
        var repository = new UserRepository();

        var adminUser = createUserAdminActive();
        var commomUser = createUserCommomActive();
        var blockedUser = createUserCommomBlocked();

        repository.save(adminUser);
        repository.save(commomUser);
        repository.save(blockedUser);

        var users = repository.findByStatus(UserStatus.ATIVO);
        assertEquals(2, users.size());
    }

    @Test
    public void shouldGetUsersByRoleName()  {
        var repository = new UserRepository();

        var adminUser = createUserAdminActive();
        var commomUser = createUserCommomActive();
        var blockedUser = createUserCommomBlocked();

        repository.save(adminUser);
        repository.save(commomUser);
        repository.save(blockedUser);

        var users = repository.findByRoleName(new String("COMMOM_WRITE"));

        assertEquals(2, users.size());
    }

    @Test
    public void shouldGetMapUsersByStatus() {
        var repository = new UserRepository();

        var adminUser = createUserAdminActive();
        var commomUser = createUserCommomActive();
        var blockedUser = createUserCommomBlocked();

        repository.save(adminUser);
        repository.save(commomUser);
        repository.save(blockedUser);

        var usersByStatus = repository.groupByStatus();
        assertEquals(2, usersByStatus.get(UserStatus.ATIVO).size());
    }

    @Test
    public void shouldGetUserById() {
        var repository = new UserRepository();

        var adminUser = createUserAdminActive();
        var commomUser = createUserCommomActive();

        repository.save(adminUser);
        repository.save(commomUser);

        var foundUser = repository.findById(commomUser.getId());

        assertTrue(foundUser.isPresent());
        assertEquals(commomUser.getId(), foundUser.get().getId());

        var newCommomUser = new User(
                commomUser.getId(),
                "NOVO USER",
                "NOVO EMAIL",
                commomUser.getPasswordHash(),
                commomUser.getStatus(),
                commomUser.getRoles(),
                passwordHasher);

        repository.save(newCommomUser);

        var newFoundUser = repository.findById(commomUser.getId());
        assertTrue(newFoundUser.isPresent());
        assertEquals(commomUser.getId(), newFoundUser.get().getId());
        assertEquals("NOVO USER", newFoundUser.get().getUsername());
        assertEquals("NOVO EMAIL", newFoundUser.get().getEmail());
    }

    @Test
    public void shouldGetCountUsersByRolename() {
        var repository = new UserRepository();

        var adminUser = createUserAdminActive();
        var commomUser = createUserCommomActive();
        var blockedUser = createUserCommomBlocked();

        repository.save(adminUser);
        repository.save(commomUser);
        repository.save(blockedUser);

        var mapCount = repository.countUsersByRoleName();

        assertEquals(2, mapCount.get("COMMOM_WRITE"));
        assertEquals(1, mapCount.get("ADMIN"));
    }

    @Test
    public void shouldGetAllDistinctPermissionNames() {
        var repository = new UserRepository();

        var adminUser = createUserAdminActive();
        var commomUser = createUserCommomActive();
        var blockedUser = createUserCommomBlocked();
        var otherBlockedUser = createOtherUserCommomBlocked();

        repository.save(adminUser);
        repository.save(commomUser);
        repository.save(blockedUser);
        repository.save(otherBlockedUser);

        var distinctPermissionNames = repository.allDistinctPermissionNames();
        assertEquals(3, distinctPermissionNames.size());
        assertEquals(List.of("admin", "user:read", "user:write"), distinctPermissionNames);

    }

    @Test
    public void shouldValidateAnyUserHasPermission() {
        var repository = new UserRepository();

        var adminUser = createUserAdminActive();
        var commomUser = createUserCommomActive();
        var blockedUser = createUserCommomBlocked();
        var otherBlockedUser = createOtherUserCommomBlocked();

        repository.save(adminUser);
        repository.save(commomUser);
        repository.save(blockedUser);
        repository.save(otherBlockedUser);

        assertTrue(repository.anyUserHasPermission("user:write"));
        assertFalse(repository.anyUserHasPermission("user:read"));
    }

    @Test
    public void shouldThrowExceptionInGetByIdWithNonExistsId() {
        var repository = new UserRepository();

        var adminUser = createUserAdminActive();
        var commomUser = createUserCommomActive();
        var blockedUser = createUserCommomBlocked();
        var otherBlockedUser = createOtherUserCommomBlocked();

        repository.save(adminUser);
        repository.save(commomUser);
        repository.save(blockedUser);
        repository.save(otherBlockedUser);

        var foundedUser = repository.getById(commomUser.getId());
        assertNotNull(foundedUser);
        assertEquals(commomUser.getUsername(), foundedUser.getUsername());

        assertThrows(UserNotFoundException.class, () -> {
            repository.getById(UUID.randomUUID());
        });
    }

    @Test
    public void shouldSaveListOfUsers() {
        var repository = new UserRepository();

        var adminUser = createUserAdminActive();
        var commomUser = createUserCommomActive();

        repository.saveAll(List.of(adminUser, commomUser));

        var listUsers = repository.findAll();

        assertNotNull(listUsers);
        assertEquals(2, listUsers.size());
        var foundUserAdmin = listUsers.stream().filter(user -> user.getUsername().equals("user_admin_1")).findFirst().orElse(null);
        var foundUserCommom = listUsers.stream().filter(user -> user.getUsername().equals("user_commom_1")).findFirst().orElse(null);
        assertEquals("user_admin_1", foundUserAdmin.getUsername());
        assertEquals("user_commom_1", foundUserCommom.getUsername());
    }

    @Test
    public void shouldThrowDuplicateUsernameExceptionWhenSaveListOfUsersWithSameUsername() {
        var repository = new UserRepository();

        var adminUser = createUserAdminActive();

        var admin_v2 = new User(
                adminUser.getId(),
                "user_admin_2",
                adminUser.getEmail(),
                adminUser.getPasswordHash(),
                adminUser.getStatus(),
                adminUser.getRoles(),
                passwordHasher
                );

        var admin_v3 = new User(
                UUID.randomUUID(),
                "user_admin_2",
                adminUser.getEmail(),
                adminUser.getPasswordHash(),
                adminUser.getStatus(),
                adminUser.getRoles(),
                passwordHasher
        );

        var exception = assertThrows(DuplicateUsernameException.class, () -> {
            repository.saveAll(List.of(admin_v2, admin_v3));
        });

        assertTrue(exception instanceof DuplicateUsernameException);
    }

    @Test
    public void shouldUpdateUserWhenSaveAllListOfUsersWithSameUsernameAndID() {
        var repository = new UserRepository();

        var adminUser = createUserAdminActive();

        var admin_v2 = new User(
                adminUser.getId(),
                "user_admin_2",
                adminUser.getEmail(),
                adminUser.getPasswordHash(),
                adminUser.getStatus(),
                adminUser.getRoles(),
                passwordHasher
        );

        var admin_v3 = new User(
                adminUser.getId(),
                "user_admin_2",
                adminUser.getEmail(),
                adminUser.getPasswordHash(),
                adminUser.getStatus(),
                adminUser.getRoles(),
                passwordHasher
        );

        repository.saveAll(List.of(admin_v2, admin_v3));

        var foundUser = repository.getById(adminUser.getId());

        assertNotNull(foundUser);
        assertEquals("user_admin_2", foundUser.getUsername());
    }

    private User createUserAdminActive() {

        var setPermissions = new HashSet<Permission>();
        setPermissions.add(new Permission(MOCKED_ADMIN_PERMISSION_ADMIN));
        setPermissions.add(new Permission(MOCKED_ADMIN_PERMISSION_USER_WRITE));

        var role = new Role(ADMIN, setPermissions);
        var roles = new HashSet<Role>();
        roles.add(role);

        return createMockedUser(
                "user_admin_1",
                "user.admin_1@teste.com",
                "admin123",
                UserStatus.ATIVO,
                roles
        );
    }

    private User createUserCommomActive() {

        var setPermissions = new HashSet<Permission>();
        setPermissions.add(new Permission(MOCKED_ADMIN_PERMISSION_USER_WRITE));

        var role = new Role(COMMOM_WRITE, setPermissions);
        var roles = new HashSet<Role>();
        roles.add(role);

        return createMockedUser(
                "user_commom_1",
                "user.commom_1@teste.com",
                "commom123",
                UserStatus.ATIVO,
                roles
        );
    }

    private User createUserCommomBlocked() {
        var setPermissions = new HashSet<Permission>();
        setPermissions.add(new Permission(MOCKED_ADMIN_PERMISSION_USER_WRITE));

        var role = new Role(COMMOM_WRITE, setPermissions);
        var roles = new HashSet<Role>();
        roles.add(role);

        return createMockedUser(
                "user_commom_2",
                "user.commom_2@teste.com",
                "commom123",
                UserStatus.BLOQUEADO,
                roles
        );
    }

    private User createOtherUserCommomBlocked() {
        var setPermissions = new HashSet<Permission>();
        setPermissions.add(new Permission(MOCKED_ADMIN_PERMISSION_USER_READ));

        var role = new Role(COMMOM_READ, setPermissions);
        var roles = new HashSet<Role>();
        roles.add(role);

        return createMockedUser(
                "user_commom_3",
                "user.commom_3@teste.com",
                "commom123",
                UserStatus.BLOQUEADO,
                roles
        );
    }

    private User createMockedUser(String username, String email, String pass, UserStatus status, Set<Role> roles) {
        var id = UUID.randomUUID();
        return new User(id, username, email, pass, status, roles, passwordHasher);
    }
}
