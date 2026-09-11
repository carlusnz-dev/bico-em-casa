package br.com.bicoemcasa.api.modulos.autenticacao.dto;

import java.time.OffsetDateTime;

public record LogoutResponse(
        Long usuarioId,
        OffsetDateTime dataSaida
) {
}
