package br.com.bicoemcasa.api.modulos.denuncias.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DenunciaAtualizacaoRequest(
        @NotBlank(message = "O motivo é obrigatório")
        @Size(max = 80, message = "O motivo deve ter no máximo 80 caracteres")
        String motivo,

        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
        String descricao
) {
}
