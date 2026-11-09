package br.com.bicoemcasa.api.modulos.contratacoes.dto;

import br.com.bicoemcasa.api.modulos.contratacoes.models.StatusContratacao;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusRequest(
        @NotNull(message = "O status é obrigatório")
        StatusContratacao status
) {
}
