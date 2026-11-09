package br.com.bicoemcasa.api.modulos.profissionais.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SolicitarUploadMidiaRequest(
        @NotBlank @Pattern(regexp = SolicitarUploadMidiaRequest.FORMATOS,
                message = "Formato de imagem ou vídeo inválido") String contentType
) {
    public static final String FORMATOS = "image/(jpeg|png|webp)|video/(mp4|webm)";
}
