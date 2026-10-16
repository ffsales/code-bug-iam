package br.com.sales.code.bug.iam.domain;

public enum UserStatus {
    ATIVO,
    // O status BLOQUEADO será usado para representar tanto um bloqueio administrativo quanto um bloqueio por
    // tentativas de login com senha incorreta
    BLOQUEADO,
    PENDENTE
}
