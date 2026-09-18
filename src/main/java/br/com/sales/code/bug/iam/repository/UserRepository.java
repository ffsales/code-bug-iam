package br.com.sales.code.bug.iam.repository;

import br.com.sales.code.bug.iam.domain.Role;
import br.com.sales.code.bug.iam.domain.User;
import br.com.sales.code.bug.iam.domain.UserStatus;
import br.com.sales.code.bug.iam.domain.exception.DuplicateUsernameException;
import br.com.sales.code.bug.iam.domain.exception.UserNotFoundException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UserRepository implements Repository<User, UUID> {

    //A decisão por usar um Map permite acelerar a busca e substituir um user caso seja inserido outro com o mesmo id
    private final Map<UUID, User> mapUsersById;

    //A decisão de manter dois índices, na implementação atual, gera risco de falha na manutenção já que cria dois
    //pontos de falha, o uso de espaço de armazenamento fica duplicado, mas temos ganho na consulta por nome, já que
    //não é necessário percorrer o primeiro mapa.
    //Porém, com o uso de um banco de dados real, podemos criar mais de um índice dentro da mesma estrutura.
    private final Map<String, UUID> mapUsersByName;

    public UserRepository() {
        this.mapUsersById = new HashMap<>();
        this.mapUsersByName = new HashMap<>();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(this.mapUsersById.get(id));
    }

    @Override
    public User getById(UUID id) {
        return this.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário %s não encontrado".formatted(id)));
    }

    @Override
    public List<User> findAll() {
        return List.copyOf(this.mapUsersById.values());
    }

    @Override
    public void save(User newUser) {
        //Validação trocadda para um Objects.requireNonNull porque o parâmetro informada é inválido e não por quebra
        //de regra de negócio
        Objects.requireNonNull(newUser, "Entidade inválida");

        if (this.mapUsersByName.containsKey(newUser.getUsername())
            && !this.mapUsersByName.get(newUser.getUsername()).equals(newUser.getId())) {
            throw new DuplicateUsernameException("Já existe um usuário com esse username");
        }

        this.findById(newUser.getId()).ifPresent(u -> mapUsersByName.remove(u.getUsername()));

        this.mapUsersById.put(newUser.getId(), newUser);
        this.mapUsersByName.put(newUser.getUsername(), newUser.getId());
    }

    public List<User> findByStatus(UserStatus status) {
        return this.mapUsersById.values().stream()
                .filter(user -> status == user.getStatus())
                .collect(Collectors.toUnmodifiableList());
    }

    public List<User> findByRoleName(String roleName) {
        return this.mapUsersById.values().stream()
                .filter(user -> user.getRoles().stream()
                                        .anyMatch(role -> role.getName().equals(roleName)))
                .collect(Collectors.toUnmodifiableList());
    }

    public Map<UserStatus, List<User>> groupByStatus() {
        return this.mapUsersById.values().stream()
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
        return this.mapUsersById.values().stream()
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
        return this.mapUsersById.values().stream()
                .flatMap(user -> user.getRoles().stream())
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.value())
                .distinct()
                .sorted()
                .collect(Collectors.toUnmodifiableList());
    }

    public boolean anyUserHasPermission(String permissionName) {
        return this.mapUsersById.values().stream()
                .filter(user -> user.getStatus() == UserStatus.ATIVO)
                .anyMatch(user -> user.hasPermission(permissionName));
    }

    /**
     * Nome alterado para manter o mesmo padrão de getById/FindById
     */
    public User getByUsername(String username) {
        return Optional.ofNullable(this.mapUsersByName.get(username))
                .flatMap(this::findById)
                .orElseThrow(() -> new UserNotFoundException("Username %s não encontrado".formatted(username)));
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(this.mapUsersByName.get(username))
                .flatMap(this::findById);
    }

    // Ajustei o método para validar duplicatas na lista de saveAll e reutilizei a exception que já existei para
    // esse propósito
    @Override
    public void saveAll(Collection<? extends User> entities) {
        Objects.requireNonNull(entities, "A lista de usuários não pode ser nula");

        entities.stream()
                .collect(Collectors.toMap(
                        User::getUsername,
                        Function.identity(),
                        (primeiro, segundo) -> {
                            if (!primeiro.equals(segundo))
                                throw new DuplicateUsernameException("O username %s está duplicado na lista".formatted(primeiro.getUsername()));

                            return null;
                        }
                ));
        entities.forEach(this::save);
    }
}
