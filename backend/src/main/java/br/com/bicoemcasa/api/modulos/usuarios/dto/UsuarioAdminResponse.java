package br.com.bicoemcasa.api.modulos.usuarios.dto;

import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;

import java.time.OffsetDateTime;
import java.util.List;

public record UsuarioAdminResponse(
        Long id,
        String nome,
        String email,
        boolean ativo,
        List<PerfilTipo> papeis,
        OffsetDateTime criadoEm
) {}
