package br.com.bicoemcasa.api.modulos.profissionais.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmarFotoCapaRequest(
        @NotBlank(message = "Informe a chave do arquivo enviado") String chave
) {
}
