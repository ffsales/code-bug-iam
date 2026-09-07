package br.com.sales.code.bug.iam.repository;

import br.com.sales.code.bug.iam.config.exception.UserInvalidException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EntityRepository<T> {

    void save(T entity);

    Optional<T> findById(UUID id);

    List<T> findAll();
}
