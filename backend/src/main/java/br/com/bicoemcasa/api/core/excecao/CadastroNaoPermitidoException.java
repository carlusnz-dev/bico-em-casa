package br.com.bicoemcasa.api.core.excecao;

public class CadastroNaoPermitidoException extends RuntimeException {
    public CadastroNaoPermitidoException(String message) {
        super(message);
    }
}
