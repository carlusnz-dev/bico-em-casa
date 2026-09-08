package br.com.bicoemcasa.api.modulos.usuarios.contrato;

import br.com.bicoemcasa.api.modulos.usuarios.dto.*;

import java.util.Optional;

public interface UsuarioService {
    UsuarioResponse criar(UsuarioRequest request);
    UsuarioResponse buscarPorId(Long id);
    UsuarioResponse buscarPorEmail(String email);
    Optional<CredenciaisUsuario> buscarCredenciaisPorEmail(String email);
    UsuarioExiste usuarioExiste(Long id);
}
