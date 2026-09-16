package br.com.sales.code.bug.iam.repository;

import br.com.sales.code.bug.iam.domain.Role;
import br.com.sales.code.bug.iam.domain.exception.RoleNotFoundException;

import java.util.*;

public class RoleRepository implements Repository<Role, String> {

    private final Map<String, Role> mapRolesByName;

    public RoleRepository() {
        this.mapRolesByName = new HashMap<>();
    }

    @Override
    public void save(Role newRole) {
        Objects.requireNonNull(newRole, "Entidade inválida");

        this.mapRolesByName.put(newRole.getName(), newRole);
    }

    @Override
    public Optional<Role> findById(String roleName) {
        return Optional.ofNullable(this.mapRolesByName.get(roleName));
    }

    @Override
    public Role getById(String roleName) {
        var role = this.mapRolesByName.get(roleName);
        if (Objects.isNull(role))
            throw new RoleNotFoundException("Role %s não encontrada".formatted(roleName));

        return role;
    }

    @Override
    public List<Role> findAll() {
        return List.copyOf(this.mapRolesByName.values());
    }

    @Override
    public void saveAll(Collection<? extends Role> entities) {
        Objects.requireNonNull(entities, "A lista de roles não pode ser nula");
        entities.forEach(this::save);
    }
}
