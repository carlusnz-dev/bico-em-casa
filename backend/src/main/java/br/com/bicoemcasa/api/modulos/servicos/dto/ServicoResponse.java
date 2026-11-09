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
        OffsetDateTime criadoEm,
        String motivoModeracao,
        String statusRevisao,
        String motivoReprovacao
) {
    public ServicoResponse(UUID id, UUID perfilId, String titulo, String descricao, BigDecimal precoPrevio,
        UnidadePreco unidadePreco, boolean ativo, Set<UUID> tagIds, OffsetDateTime criadoEm) {
        this(id, perfilId, titulo, descricao, precoPrevio, unidadePreco, ativo, tagIds, criadoEm, null, null, null);
    }
    public ServicoResponse comModeracao(String motivo, String status, String reprovacao) {
        return new ServicoResponse(id, perfilId, titulo, descricao, precoPrevio, unidadePreco, ativo, tagIds,
                criadoEm, motivo, status, reprovacao);
    }
}
