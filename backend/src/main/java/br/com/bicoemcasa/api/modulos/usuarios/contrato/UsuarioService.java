package br.com.bicoemcasa.api.modulos.usuarios.contrato;

import br.com.bicoemcasa.api.modulos.usuarios.dto.*;

import java.util.Optional;

public interface UsuarioService {
    UsuarioResponse criar(UsuarioRequest request);
    UsuarioResponse criarAdministrador(Long autorId, UsuarioRequest request);
    UsuarioResponse buscarPorId(Long id);
    UsuarioResponse buscarPorEmail(String email);
    Optional<CredenciaisUsuario> buscarCredenciaisAtivasParaLogin(String email);
    Optional<Long> buscarVersaoAtivaParaRenovacao(Long id);
    boolean sessaoValida(Long id, long versao);
    void exigirAdministradorAtivo(Long id);
    UsuarioExiste usuarioExiste(Long id);
}
