package br.com.sales.code.bug.iam.domain;

/**
 * Aqui a escolha por tornar o cenário de "usuário não encontrado" ser uma variante de LoginResult é por ser uma situação
 * ser esperada, bem como UserBlocked ou UserPending, e não um erro de uso da API.
 */
public record UserNotFound(String username) implements LoginResult {
}
