package br.com.bicoemcasa.api.modulos.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MotivoSituacaoUsuarioRequest(@NotBlank @Size(min = 10, max = 1000) String motivo) {
    public MotivoSituacaoUsuarioRequest {
        if (motivo != null) {
            motivo = motivo.strip();
        }
    }
}
