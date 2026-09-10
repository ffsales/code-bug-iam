package br.com.sales.code.bug.iam.domain;

import java.util.*;

public class Role {

    public Role(String name, Set<Permission> permissions) {
        this.name = name;
        var newPermissions = new HashSet<Permission>(permissions);
        this.permissions = Collections.unmodifiableSet(newPermissions);
    }

    private String name;
    private Set<Permission> permissions;

    public String getName() {
        return this.name;
    }

    public boolean hasPermission(String permissionName) {
        if (permissionName == null)
            return false;

        return this.permissions.contains(new Permission(permissionName));
    }

     //A lista de permissions deve ser imutável para garantir que o usuário que o conjunto seja único, caso seja
     //necessário adicionar outra permission deve ser criado uma nova Role para que a mudança seja sempre
     //intencional e não por acidente
    public Set<Permission> getPermissions() {
        return this.permissions;
    }


    //A decisão por comparar o equals apenas com o name é porque neste momento o name funciona como um ID, mas deve ser
    //refatorado conforme o projeto evoluir
    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || this.getClass() != other.getClass())
            return false;

        var otherRole = (Role)other;
        return otherRole.name.equals(this.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
