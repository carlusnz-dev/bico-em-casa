package br.com.bicoemcasa.api.modulos.servicos.dto;

import br.com.bicoemcasa.api.modulos.servicos.models.Servico;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TopListResponse(
        UUID id,
        UUID perfilId,
        String titulo,
        OffsetDateTime criadoEm
) {
    public TopListResponse(Servico servico) {
        this(servico.getId(), servico.getPerfilId(), servico.getTitulo(), servico.getCriadoEm());
    }
}
