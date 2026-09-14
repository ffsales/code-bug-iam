package br.com.sales.code.bug.iam.domain;

/**
 * O tipo escolhido foi um record por que a necessidade é de criar um pojo com valores imutáveis.
 * Não é um singleton por que ele não representa um recurso compartilhado, mas sim o estado de um objeto.
 * Ao utilizar a representação da situação de "usuário bloqueado" a partir de uma sealed class um record único se torna
 * mais legível e não há mais necessidade de carregar o objeto UserStatus dentro dele.
 */
public record UserBlocked(String username) implements LoginResult{}
