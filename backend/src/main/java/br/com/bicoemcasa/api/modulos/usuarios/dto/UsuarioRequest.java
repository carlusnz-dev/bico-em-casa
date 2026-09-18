package br.com.bicoemcasa.api.modulos.usuarios.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank @Size(max = 255) String nome,
        @NotBlank @Size(max = 255) String email,
        @NotBlank @Size(max = 11) String cpf,
        @NotBlank @Size(min = 8) String senha,
        @NotNull @Valid PerfilRequest perfil
) {
}
