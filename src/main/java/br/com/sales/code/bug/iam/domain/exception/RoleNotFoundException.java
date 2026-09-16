package br.com.sales.code.bug.iam.domain.exception;

public class RoleNotFoundException extends EntityNotFoundException {

    public RoleNotFoundException(String message) {
        super(message);
    }
}
