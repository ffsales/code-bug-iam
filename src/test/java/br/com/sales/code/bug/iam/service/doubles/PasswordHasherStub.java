package br.com.sales.code.bug.iam.service.doubles;

import br.com.sales.code.bug.iam.service.PasswordHasher;

import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Usei o stub para o double test do PasswordHasher por que a função dessa classe é aplicar uma regra de criptografia.
 * Mas para esse teste não há valor em testar a criptografia, mas apenas o resultado da autenticação.
 */
public class PasswordHasherStub implements PasswordHasher {
    @Override
    public String hash(String pass) {
        return pass;
    }
}
