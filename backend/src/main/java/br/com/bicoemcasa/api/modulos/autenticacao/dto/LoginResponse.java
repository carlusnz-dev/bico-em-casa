package br.com.bicoemcasa.api.modulos.autenticacao.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
