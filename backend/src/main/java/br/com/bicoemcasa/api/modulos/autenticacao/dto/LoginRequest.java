package br.com.bicoemcasa.api.modulos.autenticacao.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String email,
        @NotBlank String senha
) {
}
