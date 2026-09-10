package br.com.sales.code.bug.iam.domain.exception;

/**
 * Exceção usada para informar que o objeto role é válido mas não é um conjunto vazio, quebrando a regra de obriatoriede de ter ao menos uma role
 */
public class UserWithoutRoleException extends DomainException {

    public UserWithoutRoleException(String message) {
        super(message);
    }
}
