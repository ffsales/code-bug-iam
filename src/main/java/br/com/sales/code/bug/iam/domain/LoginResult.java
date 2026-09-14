package br.com.sales.code.bug.iam.domain;

/**
 * A escolha pelo uso de permits explícito se dá por tornar mais legível a hierarquia das classes.
 */
public sealed interface LoginResult
        permits InvalidCredentials, Success, UserBlocked, UserNotFound, UserPending {
}
