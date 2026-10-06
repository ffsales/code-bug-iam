package br.com.sales.code.bug.iam.domain;

import br.com.sales.code.bug.iam.domain.exception.UserFieldInvalidException;
import br.com.sales.code.bug.iam.domain.exception.UserWithoutRoleException;
import br.com.sales.code.bug.iam.service.PasswordHasher;
import br.com.sales.code.bug.iam.utils.DomainUtil;

import java.util.*;
import java.util.regex.Pattern;

/**
 * nenhum campo de User é opcional por design
 */
public class User {

    //A decisão de tornar o PasswordHasher como uma interface foi para tornar a implementação tornar essa dependência menos acoplada,
    //assim, tornando a criação de novas formas de criptografia mais fácil de ser implementada
    private PasswordHasher passwordHasher;

    private User(UUID id, String username, String email, String password, UserStatus status, Set<Role> roles, PasswordHasher passwordHasher) {

        this.passwordHasher = passwordHasher;
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHasher.hash(password);
        this.status = status;
        this.roles = Set.copyOf(roles);
    }

    // Construtor para ser usado apenas na recuperação do usuário do repositório
    private User(String id, String username, String email, String password, UserStatus status, Set<Role> roles) {

        this.id = UUID.fromString(id);
        this.username = username;
        this.email = email;
        this.passwordHash = password;
        this.status = status;
        this.roles = Set.copyOf(roles);
    }

    private final UUID id;
    private final String username;
    private final String email;
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

    public static User newUser(UUID id, String username, String email, String password, UserStatus status, Set<Role> roles, PasswordHasher passwordHasher) {

        validateNewUser(id, username, email, password, status, roles, passwordHasher);

        return new User(id, username, email, password, status, roles, passwordHasher);
    }

    // Existem limitações na reconstrução de usuários a partir de fontes externas.
    // Não é esperado nenhum tipo de caracter de escape ou aspas
    public static User reconstructUser(String id, String username, String email, String password, String status, Set<Role> roles) {

        validateReconstructUser(id, username, email, password, status, roles);

        return new User(id, username, email, password, UserStatus.valueOf(status), roles);
    }

    // Objects.requireNonNull está sendo usado por que não quebram regra de negócio, mas trazem dados inválidos
    private static void validateNewUser(UUID id, String username, String email, String password, UserStatus status, Set<Role> roles, PasswordHasher passwordHasher) {
        Objects.requireNonNull(id, "Id é obrigatório.");
        Objects.requireNonNull(passwordHasher, "PasswordHasher é obrigatório.");
        Objects.requireNonNull(status, "Status é obrigatório.");
        Objects.requireNonNull(username, "Username é obrigatório.");
        Objects.requireNonNull(email, "Email é obrigatório.");
        Objects.requireNonNull(password, "Password é obrigatório.");
        Objects.requireNonNull(roles, "Role é obrigatório.");
        if (roles.isEmpty())
            throw new UserWithoutRoleException("É obrigatório ao menos uma Role.");
    }

    // Aqui a validação lança UserFieldInvalidException pois a informação vem de uma fonte externa e
    // dessa forma comunicamos de forma correta o dado inválido ou ausente
    private static void validateReconstructUser(String id, String username, String email, String password, String status, Set<Role> roles) {

        if (Objects.isNull(id))
            throw new UserFieldInvalidException("Id é obrigatório.");

        if (!DomainUtil.isValidUuid(id))
            throw new UserFieldInvalidException("Id é inválido.");

        if (Objects.isNull(status))
            throw new UserFieldInvalidException( "Status é obrigatório.");

        Arrays.stream(UserStatus.values())
                .filter(userStatus -> userStatus.name().equals(status))
                .findFirst()
                .orElseThrow(() -> new UserFieldInvalidException( "Status é inválido."));

        if (Objects.isNull(username))
            throw new UserFieldInvalidException("Username é obrigatório.");

        if (Objects.isNull(email))
            throw new UserFieldInvalidException("Email é obrigatório.");

        if (Objects.isNull(password))
            throw new UserFieldInvalidException( "Password é obrigatório.");

        //Usei a exception de domínio para tratar de um objeto válido, mas incompleto que é necessário para a aplicação
        //das regras de negócio
        if (Objects.isNull(roles) || roles.isEmpty()) {
            throw new UserWithoutRoleException("É obrigatório ao menos uma Role.");
        }
    }
}
