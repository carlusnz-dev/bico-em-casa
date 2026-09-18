package br.com.bicoemcasa.api.modulos.autenticacao.dto;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RefreshTokenRequest(
        @NotNull Long usuarioId,
        UUID familiaId,
        String hashToken,
        OffsetDateTime expiraEm
) {}
