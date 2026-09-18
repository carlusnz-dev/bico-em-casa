package br.com.bicoemcasa.api.modulos.autenticacao.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RefreshTokenResponse(
        UUID id,
        Long usuarioId,
        UUID familiaId,
        OffsetDateTime expiraEm,
        OffsetDateTime revogadoEm,
        OffsetDateTime criadoEm
) {}
