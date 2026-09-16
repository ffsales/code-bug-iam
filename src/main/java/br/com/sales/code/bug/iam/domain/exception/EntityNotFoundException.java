package br.com.sales.code.bug.iam.domain.exception;

/**
 * A descisão por criar uma hiararquia sob EntityNotFoundException me permite criar exceptions específicas para cada entidade
 * sem perder o contexto de domínio e guardar a intenção da exceção
 */
public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
