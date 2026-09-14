package br.com.bicoemcasa.api.modulos.autenticacao.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record RenovarResponse(
        String accessToken,
        @JsonIgnore String tokenBruto
) {
}
