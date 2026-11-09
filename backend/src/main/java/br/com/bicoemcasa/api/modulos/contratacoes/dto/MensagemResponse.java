package br.com.bicoemcasa.api.modulos.contratacoes.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MensagemResponse(
        UUID id,
        UUID contratacaoId,
        UUID autorPerfilId,
        String autorNome,
        String conteudo,
        OffsetDateTime criadoEm
) {
}
