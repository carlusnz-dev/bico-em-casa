package br.com.bicoemcasa.api.modulos.contratacoes.dto;

import jakarta.validation.constraints.Size;

public record EditarContratacaoRequest(
        @Size(max = 1000) String observacao
) {
}
