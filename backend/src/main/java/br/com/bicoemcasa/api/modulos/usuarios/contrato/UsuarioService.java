package br.com.bicoemcasa.api.modulos.usuarios.contrato;

import br.com.bicoemcasa.api.modulos.usuarios.dto.CredenciaisUsuario;
import br.com.bicoemcasa.api.modulos.usuarios.dto.UsuarioRequest;
import br.com.bicoemcasa.api.modulos.usuarios.dto.UsuarioResponse;

import java.util.Optional;

public interface UsuarioService {
    UsuarioResponse criar(UsuarioRequest request);
    UsuarioResponse buscarPorId(Long id);
    UsuarioResponse buscarPorEmail(String email);
    Optional<CredenciaisUsuario> buscarCredenciaisPorEmail(String email);
}
