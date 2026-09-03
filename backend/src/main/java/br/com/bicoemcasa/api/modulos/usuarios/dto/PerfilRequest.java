package br.com.bicoemcasa.api.modulos.usuarios.dto;

import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PerfilRequest(
        @NotNull PerfilTipo tipo,
        @NotBlank @Size(max = 50) String nomeUsuario,
        @NotBlank @Size(max = 100) String nomeExibicao,
        String telefone,
        String bio
) {}
