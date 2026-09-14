package br.com.bicoemcasa.api.modulos.avaliacoes.dto;

import br.com.bicoemcasa.api.modulos.avaliacoes.models.Denuncia;
import br.com.bicoemcasa.api.modulos.avaliacoes.models.StatusDenuncia;
import br.com.bicoemcasa.api.modulos.avaliacoes.models.TipoAlvoDenuncia;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DenunciaResponse(
    UUID id,
    UUID  autorPerfilId,
    UUID  denunciadoPerfilId,
    UUID contratacaoId,
    TipoAlvoDenuncia alvoTipo,
    String alvoId,
    String motivo,
    String descricao,
    StatusDenuncia status,
    UUID  analisadoPorPerfilId,
    OffsetDateTime analisadoEm,
    String parecer,
    OffsetDateTime criadoEm,
    OffsetDateTime atualizadoEm
) {
    public static  DenunciaResponse de(Denuncia denuncia){ 
        return  new DenunciaResponse(denuncia.getId(),
                denuncia.getAutorPerfilId(),
                denuncia.getDenunciadoPerfilId(),
                denuncia.getContratacaoId(),
                denuncia.getAlvoTipo(),
                denuncia.getAlvoId(),
                denuncia.getMotivo(),
                denuncia.getDescricao(),
                denuncia.getStatus(),
                denuncia.getAnalisadoPorPerfilId(),
                denuncia.getAnalisadoEm(),
                denuncia.getParecer(),
                denuncia.getCriadoEm(),
                denuncia.getAtualizadoEm()
            );
    }
} 
