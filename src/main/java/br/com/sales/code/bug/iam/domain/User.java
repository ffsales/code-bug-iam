package br.com.sales.code.bug.iam.domain;

import br.com.sales.code.bug.iam.service.PasswordHasher;
import br.com.sales.code.bug.iam.service.PasswordHasherDigest;

import java.util.*;

public class User {

    /**
     * A decisão de tornar o PasswordHasher como uma interface foi para tornar a implementação tornar essa dependência menos acoplada,
     * assim, tornando a criação de novas formas de criptografia mais fácil de ser implementada
     */
    private PasswordHasher passwordHasher;

    public User(UUID id, String username, String email, String password, UserStatus status, Set<Role> roles, PasswordHasher passwordHasher) {
        this.passwordHasher = passwordHasher;
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHasher.hash(password);
        this.status = status;
        var newRoles = new HashSet<Role>();
        newRoles.addAll(roles);
        this.roles = Collections.unmodifiableSet(newRoles);
    }

    private UUID id;
    private String username;
    private String email;
    private String passwordHash;
    private UserStatus status;
    private Set<Role> roles;

    public UUID getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPasswordHash() {
        return this.passwordHash;
    }

    public UserStatus getStatus() {
        return this.status;
    }

    public Set<Role> getRoles() {
        return this.roles;
    }

    public void addRole(Role newRole) {

        var newSetRoles = new HashSet<Role>();
        this.roles.forEach(role -> newSetRoles.add(role));
        newSetRoles.add(newRole);

        this.roles = Collections.unmodifiableSet(newSetRoles);
    }

    public void removeRole(Role removeRole) {

        var newSetRoles = new HashSet<Role>();
        this.roles.forEach(role -> {
            if (!role.equals(removeRole))
                newSetRoles.add(role);
        });

        this.roles = Collections.unmodifiableSet(newSetRoles);
    }

    public boolean hasPermission(String permissionName) {

        for (Role role : this.roles) {
            if (role.hasPermission(permissionName))
                return true;
        }

        return false;
    }
}
