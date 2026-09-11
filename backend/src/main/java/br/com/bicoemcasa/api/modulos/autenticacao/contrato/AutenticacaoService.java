package br.com.bicoemcasa.api.modulos.autenticacao.contrato;

import br.com.bicoemcasa.api.modulos.autenticacao.dto.LoginRequest;
import br.com.bicoemcasa.api.modulos.autenticacao.dto.LoginResponse;
import br.com.bicoemcasa.api.modulos.autenticacao.dto.LogoutResponse;

public interface AutenticacaoService {
    LoginResponse entrar(LoginRequest request);
    LogoutResponse sair(String hashBruto);
}
