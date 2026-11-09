package br.com.bicoemcasa.api.modulos.autenticacao.contrato;

import br.com.bicoemcasa.api.modulos.autenticacao.dto.*;

public interface AutenticacaoService {
    String CLAIM_VERSAO_SESSAO = "versao_sessao";

    LoginResponse entrar(LoginRequest request);

    LogoutResponse sair(String hashBruto);

    CadastroResponse criar(CadastroRequest request);

    RenovarResponse renovar(String tokenBruto);
}
