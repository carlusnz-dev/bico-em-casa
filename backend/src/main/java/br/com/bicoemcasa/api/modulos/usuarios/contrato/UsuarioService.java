package br.com.bicoemcasa.api.modulos.usuarios.contrato;

import br.com.bicoemcasa.api.modulos.usuarios.dto.UsuarioRequest;
import br.com.bicoemcasa.api.modulos.usuarios.dto.UsuarioResponse;

public interface UsuarioService {
    UsuarioResponse buscarPorId(Long id);
    UsuarioResponse criar(UsuarioRequest request);
}
