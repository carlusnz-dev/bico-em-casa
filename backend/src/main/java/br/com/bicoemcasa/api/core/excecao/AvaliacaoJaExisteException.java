package br.com.bicoemcasa.api.core.excecao;

public class AvaliacaoJaExisteException extends RuntimeException {
    public AvaliacaoJaExisteException(String message) {
        super(message);
    }
}
