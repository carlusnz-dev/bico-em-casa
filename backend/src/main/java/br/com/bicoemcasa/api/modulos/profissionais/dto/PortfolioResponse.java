package br.com.bicoemcasa.api.modulos.profissionais.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PortfolioResponse(
        Long id,
        UUID perfilId,
        String titulo,
        String descricao,
        String slugUrl,
        String fotoCapaUrl,
        OffsetDateTime criadoEm
) {
}
