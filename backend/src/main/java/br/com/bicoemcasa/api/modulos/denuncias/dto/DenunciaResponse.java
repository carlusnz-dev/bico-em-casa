package br.com.bicoemcasa.api.modulos.denuncias.dto;

import br.com.bicoemcasa.api.modulos.denuncias.models.StatusDenuncia;
import br.com.bicoemcasa.api.modulos.denuncias.models.TipoAlvoDenuncia;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DenunciaResponse(
        UUID id,
        UUID autorPerfilId,
        UUID denunciadoPerfilId,
        UUID contratacaoId,
        TipoAlvoDenuncia alvoTipo,
        String alvoId,
        String motivo,
        String descricao,
        StatusDenuncia status,
        UUID analisadoPorPerfilId,
        OffsetDateTime analisadoEm,
        String parecer,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm
) {
}
