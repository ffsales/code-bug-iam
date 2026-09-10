package br.com.sales.code.bug.iam.domain.exception;

import br.com.sales.code.bug.iam.domain.UserStatus;

/**
 * Decidi pelo uso de uma exception para informar se um usuário está bloqueado ou pendente por que trata do mesmo campo
 * e posso informar qual é o caso pelo "message".
 * Ela é unchecked pois trata de um usuário válido, portanto, esperado pelas regras de negócio.
 * Adicionei o UserStatus para que seja possível verificar programaticamente qual o status não ativo do usuário.
 */
public class UserNotActiveException extends DomainException {

    private final UserStatus status;

    public UserNotActiveException(String message, UserStatus status) {
        super(message);
        this.status = status;
    }

    public UserStatus getStatus() {
        return this.status;
    }
}
