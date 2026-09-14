package br.com.bicoemcasa.api.modulos.avaliacoes.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvaliacaoRequest(
        @NotNull(message = "É necessário informar uma nota")
        @Min(value = 1, message = "A nota deve ser no mínimo 1")
        @Max(value = 5, message = "A nota deve ser no máximo 5")
        Integer nota,

        @Size(max = 1000, message = "O comentário deve ter no máximo 1000 caracteres")
        String comentario
) {
}
