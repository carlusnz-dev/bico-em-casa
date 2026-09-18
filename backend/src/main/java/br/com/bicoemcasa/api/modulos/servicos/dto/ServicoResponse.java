package br.com.bicoemcasa.api.modulos.servicos.dto;

import br.com.bicoemcasa.api.modulos.servicos.models.UnidadePreco;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record ServicoResponse(
        UUID id,
        UUID perfilId,
        String titulo,
        String descricao,
        BigDecimal precoPrevio,
        UnidadePreco unidadePreco,
        boolean ativo,
        Set<UUID> tagIds,
        OffsetDateTime criadoEm
) {
}
