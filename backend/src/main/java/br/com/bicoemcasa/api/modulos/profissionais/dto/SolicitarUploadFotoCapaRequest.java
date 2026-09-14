package br.com.bicoemcasa.api.modulos.profissionais.dto;

import jakarta.validation.constraints.NotBlank;

public record SolicitarUploadFotoCapaRequest(
        @NotBlank(message = "Informe o content-type do arquivo") String contentType
) {
}
