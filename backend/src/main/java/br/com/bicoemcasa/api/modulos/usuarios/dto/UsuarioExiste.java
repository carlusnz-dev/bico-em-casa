package br.com.bicoemcasa.api.modulos.usuarios.dto;

public record UsuarioExiste(
        boolean existe,
        boolean ativo
) {
}
