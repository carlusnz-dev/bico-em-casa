package br.com.bicoemcasa.api.modulos.usuarios.dto;

public record UsuarioResponse(
        Long id,
        String nome,
        String email
) {
}
