package br.com.bicoemcasa.api.modulos.usuarios.dto;

import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;

import java.util.UUID;

public record PerfilResponse(
        UUID id,
        Long usuarioId,
        PerfilTipo tipo,
        String nomeUsuario,
        String nomeExibicao,
        String fotoUrl
) {}
