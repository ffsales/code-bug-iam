package br.com.sales.code.bug.iam.service;

public interface PasswordHasher {
    String hash(String pass);
}
