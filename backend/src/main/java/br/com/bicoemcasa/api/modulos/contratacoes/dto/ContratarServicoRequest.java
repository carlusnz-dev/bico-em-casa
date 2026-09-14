package br.com.bicoemcasa.api.modulos.contratacoes.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ContratarServicoRequest(
        @NotNull(message = "Informe o serviço que deseja contratar") UUID servicoId,
        @Size(max = 1000) String observacao
) {
}
