package br.com.sales.code.bug.iam.service;

import br.com.sales.code.bug.iam.domain.*;

import java.util.Objects;

public class ConsumerLogin {

    public String describe(LoginResult result) {

        Objects.requireNonNull(result, "O resultado login não pode ser nulo");

        return switch (result) {
            case Success(User user) -> "O usuário %s está autenticado com sucesso".formatted(user.getUsername());
            case UserBlocked blocked -> "O usuário %s está bloqueado".formatted(blocked.username());
            case UserPending pending -> "O usuário %s está pendente".formatted(pending.username());
            case InvalidCredentials invalidCredentials -> "Usuário ou senha inválida";
            case UserNotFound notFound -> "Username %s não encontrado".formatted(notFound.username());
        };
    }
}
