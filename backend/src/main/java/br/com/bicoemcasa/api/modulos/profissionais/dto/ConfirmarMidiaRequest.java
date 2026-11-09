package br.com.bicoemcasa.api.modulos.profissionais.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConfirmarMidiaRequest(
        @NotBlank @Size(max = 120) String chave,
        @NotBlank @Pattern(regexp = SolicitarUploadMidiaRequest.FORMATOS,
                message = "Formato de imagem ou vídeo inválido") String contentType
) {
}
