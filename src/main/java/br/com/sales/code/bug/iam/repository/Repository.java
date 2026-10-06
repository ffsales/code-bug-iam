package br.com.sales.code.bug.iam.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * ID não está sendo usado para comparar neste momento então removi a extenção de Comparable
 */
public interface Repository<T, ID> {

    void save(T entity);

    Optional<T> findById(ID id);

    // getById foi mantido como método comum de interface para que a cada método implemente a sua lógica
    // e continue enviando a sua própria exception, para comunicar melhor a exceção em caso de ausência
    T getById(ID id);

    List<T> findAll();

    // O formato genérico do parâmetro permite criarmos um limite para os tipos aceito pelo método e a possibilidade de
    // usar a hierarquia da entidade
    void saveAll(Collection<? extends T> entities);
}
