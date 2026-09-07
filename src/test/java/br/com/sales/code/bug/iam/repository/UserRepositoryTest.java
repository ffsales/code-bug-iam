package br.com.sales.code.bug.iam.repository;

import br.com.sales.code.bug.iam.config.exception.UserInvalidException;
import br.com.sales.code.bug.iam.domain.Permission;
import br.com.sales.code.bug.iam.domain.Role;
import br.com.sales.code.bug.iam.domain.User;
import br.com.sales.code.bug.iam.domain.UserStatus;
import br.com.sales.code.bug.iam.service.PasswordHasher;
import br.com.sales.code.bug.iam.service.PasswordHasherDigest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class UserRepositoryTest {

    public static final String ADMIN = "ADMIN";
    public static final String COMMOM_WRITE = "COMMOM_WRITE";
    public static final String COMMOM_READ = "COMMOM_READ";
    private final PasswordHasher passwordHasher = new PasswordHasherDigest();

    private static final String MOCKED_ADMIN_PERMISSION_ADMIN = "admin";
    private static final String MOCKED_ADMIN_PERMISSION_USER_WRITE = "user:write";
    private static final String MOCKED_ADMIN_PERMISSION_USER_READ = "user:read";

    @Test
    public void shouldSaveUsersInRepository() throws UserInvalidException {

        var repository = new UserRepository();

        var adminUser = createUserAdminActive();
        var commomUser = createUserCommomActive();

        repository.save(adminUser);
        repository.save(commomUser);

        Assertions.assertEquals(2, repository.findAll().size());

        var blockedUser = createUserCommomBlocked();
        repository.save(blockedUser);

        Assertions.assertEquals(3, repository.findAll().size());

        var newCommomUser = new User(
                commomUser.getId(),
                commomUser.getUsername(),
                commomUser.getEmail(),
                commomUser.getPasswordHash(),
                commomUser.getStatus(),
                commomUser.getRoles(),
                passwordHasher);
        repository.save(newCommomUser);
        Assertions.assertEquals(3, repository.findAll().size());
    }

    @Test
    public void shouldDoNotModifyUserList() throws UserInvalidException {
        var repository = new UserRepository();

        var adminUser = createUserAdminActive();
        var commomUser = createUserCommomActive();

        repository.save(adminUser);
        repository.save(commomUser);

        var allUsers = repository.findAll();

        var blockedUser = createUserCommomBlocked();

        Assertions.assertThrows(UnsupportedOperationException.class,() -> {
            allUsers.add(blockedUser);
        });
        Assertions.assertEquals(2, allUsers.size());
    }

    @Test
    public void shouldGetUsersByStatus() throws UserInvalidException {
        var repository = new UserRepository();

        var adminUser = createUserAdminActive();
        var commomUser = createUserCommomActive();
        var blockedUser = createUserCommomBlocked();

        repository.save(adminUser);
        repository.save(commomUser);
        repository.save(blockedUser);

        var users = repository.findByStatus(UserStatus.ATIVO);
        Assertions.assertEquals(2, users.size());
    }

    @Test
    public void shouldGetUsersByRoleName() throws UserInvalidException {
        var repository = new UserRepository();

        var adminUser = createUserAdminActive();
        var commomUser = createUserCommomActive();
        var blockedUser = createUserCommomBlocked();

        repository.save(adminUser);
        repository.save(commomUser);
        repository.save(blockedUser);

        var users = repository.findByRoleName(new String("COMMOM_WRITE"));

        Assertions.assertEquals(2, users.size());
    }

    @Test
    public void shouldGetMapUsersByStatus() throws UserInvalidException {
        var repository = new UserRepository();

        var adminUser = createUserAdminActive();
        var commomUser = createUserCommomActive();
        var blockedUser = createUserCommomBlocked();

        repository.save(adminUser);
        repository.save(commomUser);
        repository.save(blockedUser);

        var usersByStatus = repository.groupByStatus();
        Assertions.assertEquals(2, usersByStatus.get(UserStatus.ATIVO).size());
    }

    @Test
    public void shouldGetUserById() {
        var repository = new UserRepository();

        var adminUser = createUserAdminActive();
        var commomUser = createUserCommomActive();

        repository.save(adminUser);
        repository.save(commomUser);

        var foundUser = repository.findById(commomUser.getId());

        Assertions.assertTrue(foundUser.isPresent());
        Assertions.assertEquals(commomUser.getId(), foundUser.get().getId());

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
        Assertions.assertTrue(newFoundUser.isPresent());
        Assertions.assertEquals(commomUser.getId(), newFoundUser.get().getId());
        Assertions.assertEquals("NOVO USER", newFoundUser.get().getUsername());
        Assertions.assertEquals("NOVO EMAIL", newFoundUser.get().getEmail());
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

        Assertions.assertEquals(2, mapCount.get("COMMOM_WRITE"));
        Assertions.assertEquals(1, mapCount.get("ADMIN"));
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
        Assertions.assertEquals(3, distinctPermissionNames.size());
        Assertions.assertEquals(List.of("admin", "user:read", "user:write"), distinctPermissionNames);

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

        Assertions.assertTrue(repository.anyUserHasPermission("user:write"));
        Assertions.assertFalse(repository.anyUserHasPermission("user:read"));
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
