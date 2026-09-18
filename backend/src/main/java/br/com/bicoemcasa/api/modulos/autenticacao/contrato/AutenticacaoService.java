package br.com.bicoemcasa.api.modulos.autenticacao.contrato;

import br.com.bicoemcasa.api.modulos.autenticacao.dto.*;

public interface AutenticacaoService {
    LoginResponse entrar(LoginRequest request);

    LogoutResponse sair(String hashBruto);

    CadastroResponse criar(CadastroRequest request);

    RenovarResponse renovar(String tokenBruto);
}
