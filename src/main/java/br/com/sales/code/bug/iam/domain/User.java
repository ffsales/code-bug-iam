package br.com.sales.code.bug.iam.domain;

import br.com.sales.code.bug.iam.service.PasswordHasher;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class User {

    private static final String ALGORITHM_256= "SHA-256";

    public User(UUID id, String username, String email, String password, UserStatus status, Set<Role> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = PasswordHasher.hash(password, ALGORITHM_256);
        this.status = status;
        this.roles = roles;
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
        return status;
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    public boolean hasPermission(String permissionName) {

        for (Role role : this.roles) {
            if (role.hasPermission(permissionName))
                return true;
        }

        return false;
    }
}
