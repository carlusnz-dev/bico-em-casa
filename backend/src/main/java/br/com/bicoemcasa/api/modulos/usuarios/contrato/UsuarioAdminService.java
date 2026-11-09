package br.com.bicoemcasa.api.modulos.usuarios.contrato;

import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.modulos.usuarios.dto.AdministradorRequest;
import br.com.bicoemcasa.api.modulos.usuarios.dto.UsuarioAdminResponse;

public interface UsuarioAdminService {
    PaginaResponse<UsuarioAdminResponse> listar(Long autor, int pagina, int tamanho, Boolean ativo, String busca);
    UsuarioAdminResponse criarAdministrador(Long autor, AdministradorRequest dados, String ip);
    void alterarSituacao(Long autor, Long id, boolean ativo, String motivo, String ip);
}
