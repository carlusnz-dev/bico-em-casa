package br.com.bicoemcasa.api.core.excecao;

public class TransicaoStatusInvalidaException extends RuntimeException {
    public TransicaoStatusInvalidaException(String message) {
        super(message);
    }
}
