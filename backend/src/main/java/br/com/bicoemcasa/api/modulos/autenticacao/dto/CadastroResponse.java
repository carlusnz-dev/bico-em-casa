package br.com.bicoemcasa.api.modulos.autenticacao.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CadastroResponse(
        UUID perfilId,
        String nomeExibicao,
        String perfilTipo,
        OffsetDateTime criadoEm
) {
}
