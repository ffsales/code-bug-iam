package br.com.sales.code.bug.iam.repository;

import br.com.sales.code.bug.iam.domain.Role;
import br.com.sales.code.bug.iam.domain.User;
import br.com.sales.code.bug.iam.domain.UserStatus;

import java.util.*;
import java.util.stream.Collectors;

public class UserRepository implements EntityRepository<User> {

    /**
     * A decisão por usar um Map permite acelerar a busca e substituir um user caso seja inserido outro com o mesmo id
     */
    private final Map<UUID, User> mapUsers;

    public UserRepository() {
        this.mapUsers = new HashMap<>();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(this.mapUsers.get(id));
    }

    @Override
    public List<User> findAll() {
        return List.copyOf(this.mapUsers.values());
    }

    @Override
    public void save(User newUser) {
        Objects.requireNonNull(newUser, "Entidade inválida");
        this.mapUsers.put(newUser.getId(), newUser);
    }

    public List<User> findByStatus(UserStatus status) {
        return this.mapUsers.values().stream()
                .filter(user -> status == user.getStatus())
                .collect(Collectors.toUnmodifiableList());
    }

    public List<User> findByRoleName(String roleName) {
        return this.mapUsers.values().stream()
                .filter(user -> user.getRoles().stream()
                                        .anyMatch(role -> role.getName().equals(roleName)))
                .collect(Collectors.toUnmodifiableList());
    }

    public Map<UserStatus, List<User>> groupByStatus() {
        return this.mapUsers.values().stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(
                            User::getStatus,
                            Collectors.collectingAndThen(
                                    Collectors.toList(),
                                    List::copyOf
                            )),
                        Map::copyOf
                        ));
    }

    public Map<String, Long> countUsersByRoleName() {
        return this.mapUsers.values().stream()
                .flatMap(user -> user.getRoles().stream())
                .collect(
                        Collectors.collectingAndThen(
                        Collectors.groupingBy(
                            Role::getName,
                            Collectors.counting()),
                        Map::copyOf
                ));
    }

    public List<String> allDistinctPermissionNames() {
        return this.mapUsers.values().stream()
                .flatMap(user -> user.getRoles().stream())
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.value())
                .distinct()
                .sorted()
                .collect(Collectors.toUnmodifiableList());
    }

    public boolean anyUserHasPermission(String permissionName) {
        return this.mapUsers.values().stream()
                .filter(user -> user.getStatus() == UserStatus.ATIVO)
                .anyMatch(user -> user.hasPermission(permissionName));
    }
}
