package br.com.sales.code.bug.iam.domain.exception;

/**
 * Exceção usada para informar que o usuário informado é válido mas já existe na base de dados da aplicação, violando a
 * regra de que usernames devem ser únicos
 */
public class DuplicateUsernameException extends DomainException{

    public DuplicateUsernameException(String message) {
        super(message);
    }
}
