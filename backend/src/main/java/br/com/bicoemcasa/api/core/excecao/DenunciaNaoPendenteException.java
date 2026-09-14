package br.com.bicoemcasa.api.core.excecao;

public class DenunciaNaoPendenteException extends RuntimeException {
    public DenunciaNaoPendenteException(String mensagem) {
        super(mensagem);
    }
}
