package br.com.bicoemcasa.api.modulos.contratacoes.dto;

import br.com.bicoemcasa.api.modulos.contratacoes.models.StatusContratacao;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ContratacaoResponse(
        UUID id,
        UUID servicoId,
        UUID clienteId,
        UUID profissionalId,
        String tituloServico,
        BigDecimal precoServico,
        String observacao,
        StatusContratacao status,
        OffsetDateTime criadoEm
) {
}
