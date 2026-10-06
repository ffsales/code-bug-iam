package br.com.sales.code.bug.iam.domain.exception;

/**
 * Decidi criar uma exeption para encapsular os casos de IOException, pois o esperado pela aplicação é ter um arquivo
 * válido para a persistência, logo isso deve ser tratado como um problema de uso pelo cliente
 * RepositoryPersistenceException não é uma exceção de domínio e por isso não estende DomainException
 */
public class RepositoryPersistenceException extends RuntimeException{

    public RepositoryPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryPersistenceException(String message) {
        super(message);
    }
}
