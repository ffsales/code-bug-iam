package br.com.sales.code.bug.iam.domain;

import br.com.sales.code.bug.iam.domain.exception.UserWithoutRoleException;
import br.com.sales.code.bug.iam.service.PasswordHasher;

import java.util.*;

/**
 * nenhum campo de User é opcional por design
 */
public class User {

    //A decisão de tornar o PasswordHasher como uma interface foi para tornar a implementação tornar essa dependência menos acoplada,
    //assim, tornando a criação de novas formas de criptografia mais fácil de ser implementada
    private PasswordHasher passwordHasher;

    public User(UUID id, String username, String email, String password, UserStatus status, Set<Role> roles, PasswordHasher passwordHasher) {

        //Objects.requireNonNull está sendo usado por que não quebram regra de negócio, mas trazem dados inválidos
        Objects.requireNonNull(passwordHasher, "Hasher é obrigatório.");
        Objects.requireNonNull(id, "Id é obrigatório.");
        Objects.requireNonNull(username, "Username é obrigatório.");
        Objects.requireNonNull(email, "Email é obrigatório.");
        Objects.requireNonNull(password, "Password é obrigatório.");
        Objects.requireNonNull(status, "Status é obrigatório.");
        Objects.requireNonNull(roles, "Roles é obrigatório.");

        //Usei a exception de domínio para tratar de um objeto válido, mas incompleto que é necessário para a aplicação
        //das regras de negócio
        if (roles.isEmpty()) {
            throw new UserWithoutRoleException("É obrigatório ao menos uma Role.");
        }


        this.passwordHasher = passwordHasher;
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHasher.hash(password);
        this.status = status;
        this.roles = Set.copyOf(roles);
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

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || this.getClass() != other.getClass())
            return false;

        var otherUser = (User)other;

        return this.id.equals(otherUser.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
