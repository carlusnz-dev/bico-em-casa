package br.com.bicoemcasa.api.modulos.avaliacoes.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AvaliacaoResponse(
        UUID id,
        UUID contratacaoId,
        Long autorId,
        Long avaliadoId,
        Integer nota,
        String comentario,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
