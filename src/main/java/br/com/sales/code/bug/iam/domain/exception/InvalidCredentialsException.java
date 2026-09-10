package br.com.sales.code.bug.iam.domain.exception;

/**
 * Decidi criar essa exception como unchecked pois ela trata de uma regra onde o usuário é válido, mas a senha passada para validar
 * não é compatível com a cadastrada.
 */
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
