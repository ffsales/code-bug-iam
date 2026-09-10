package br.com.sales.code.bug.iam.domain.exception;

/**
 * Criando uma Exception base que extende RuntimeException, ou seja, uma exception unchecked, ela será usadda para
 * tratar, principalmente, erros de regra de negócio, pois o cliente deve saber lidar com as minhas regras, e a
 * exception unchecked é uma forma de comunicar quando ele viola essas regras.
 * Ela está no pacote domain.exception pois domain é onde estão as classes com as regras de negócio.
 */
public class DomainException extends RuntimeException{

     // O construtor apenas o parâmetro de mensagem deve ser usado para Exceptions exclusivas de regras de negócio, onde
     // os dados usados são programaticamente válidos, mas o valor viola regras definidas para a aplicação
    public DomainException(String message) {
        super(message);
    }
}
