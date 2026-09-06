package br.com.sales.code.bug.iam.domain;

import java.util.List;
import java.util.stream.Collectors;

public class Role {

    public Role(String name, List<Permission> permissions) {
        this.name = name;
        this.permissions = permissions;
    }

    private String name;
    private List<Permission> permissions;

    public String getName() {
        return this.name;
    }

    public boolean hasPermission(String permissionName) {
        if (permissionName == null)
            return false;

        return this.permissions.contains(new Permission(permissionName));
    }

    public List<Permission> getPermissions() {
        return permissions.stream()
                .collect(Collectors.toUnmodifiableList());
    }
    /**
     * A lista de permissions deve ser imutável para garantir que o usuário que o conjunto seja único, caso seja
     * necessário adicionar outra permission deve ser criado uma nova Role para que a mudança seja sempre
     * intencional e não por acidente
     */
}
