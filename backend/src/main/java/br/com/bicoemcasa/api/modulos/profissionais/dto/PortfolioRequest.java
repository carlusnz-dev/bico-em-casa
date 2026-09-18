package br.com.bicoemcasa.api.modulos.profissionais.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PortfolioRequest(
        @NotBlank(message = "O título não pode estar em branco") @Size(max = 120) String titulo,
        @Size(max = 1000) String descricao,
        @NotBlank(message = "O endereço não pode estar em branco") @Size(max = 120) String slugUrl
) {
}
