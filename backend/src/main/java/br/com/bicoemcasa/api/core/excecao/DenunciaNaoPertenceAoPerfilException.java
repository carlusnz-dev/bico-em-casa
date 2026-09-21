package br.com.bicoemcasa.api.core.excecao;

public class DenunciaNaoPertenceAoPerfilException extends RuntimeException {
    public DenunciaNaoPertenceAoPerfilException(String mensagem) {
        super(mensagem);
    }
}
