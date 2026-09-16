package br.com.sales.code.bug.iam.domain.exception;

/**
 * Exceção usada para informar que o Id do usuário informado é válido mas que não existe na base de dados da aplicação.
 */
public class UserNotFoundException extends EntityNotFoundException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
