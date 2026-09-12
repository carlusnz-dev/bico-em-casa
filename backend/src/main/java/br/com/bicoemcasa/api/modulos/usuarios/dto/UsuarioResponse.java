package br.com.bicoemcasa.api.modulos.usuarios.dto;

import java.util.UUID;

public record UsuarioResponse(
        Long id,
        UUID perfilId,
        String nome,
        String email
) {
}
