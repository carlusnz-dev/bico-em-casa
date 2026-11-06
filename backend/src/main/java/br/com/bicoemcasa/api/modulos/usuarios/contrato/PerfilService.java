package br.com.bicoemcasa.api.modulos.usuarios.contrato;

import br.com.bicoemcasa.api.modulos.usuarios.dto.PerfilRequest;
import br.com.bicoemcasa.api.modulos.usuarios.dto.PerfilResponse;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;

import java.util.UUID;

public interface PerfilService {
    PerfilResponse criar(Long usuarioId, PerfilRequest request);
    PerfilResponse buscarPorId(UUID id);
    PerfilResponse buscarPorUsuarioId(Long usuarioId);
    PerfilResponse buscarPorUsuarioIdETipo(Long usuarioId, PerfilTipo tipo);
    PerfilResponse buscarPorSlug(String slugUrl);
}
