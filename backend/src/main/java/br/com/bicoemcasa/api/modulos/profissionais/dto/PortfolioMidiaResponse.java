package br.com.bicoemcasa.api.modulos.profissionais.dto;

import java.util.UUID;

public record PortfolioMidiaResponse(UUID id, String url, String contentType) {
}
