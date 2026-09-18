package br.com.bicoemcasa.api.modulos.servicos.dto;

import java.util.UUID;

public record TagResponse(
        UUID id,
        String nome,
        String slug
) {
}
