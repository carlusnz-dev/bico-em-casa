package br.com.bicoemcasa.api.modulos.autenticacao.contrato;

import br.com.bicoemcasa.api.modulos.autenticacao.dto.LoginRequest;
import br.com.bicoemcasa.api.modulos.autenticacao.dto.LoginResponse;

public interface AutenticacaoService {
    LoginResponse entrar(LoginRequest request);
    LoginResponse sair();
}
