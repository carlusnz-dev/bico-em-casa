package br.com.bicoemcasa.api.modulos.denuncias.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import br.com.bicoemcasa.api.modulos.denuncias.models.StatusDenuncia;
import br.com.bicoemcasa.api.modulos.denuncias.models.TipoAlvoDenuncia;

public record DenunciaContraMimResponse(
    UUID id,
    TipoAlvoDenuncia alvoTipo,
    String alvoId,
    String motivo,
    String descricao,
    StatusDenuncia status,
    OffsetDateTime analisadoEm,
    String parecer,
    OffsetDateTime criadoEm
) {} 
 
