package br.com.bicoemcasa.api.modulos.servicos.contrato;

import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.modulos.servicos.dto.ServicoRequest;
import br.com.bicoemcasa.api.modulos.servicos.dto.ServicoResponse;
import br.com.bicoemcasa.api.modulos.servicos.dto.TopListResponse;

import java.util.List;
import java.util.UUID;

public interface ServicoService {
    ServicoResponse criar(ServicoRequest request, Long usuarioId);
    ServicoResponse buscarPorId(UUID id);
    PaginaResponse<ServicoResponse> listarAtivos(int pagina, int tamanho);
    PaginaResponse<ServicoResponse> listarMeus(Long usuarioId, int pagina, int tamanho);
    ServicoResponse editar(UUID id, ServicoRequest request, Long usuarioId);
    ServicoResponse ativar(UUID id, Long usuarioId);
    ServicoResponse desativar(UUID id, Long usuarioId);
    List<TopListResponse> listarTop5PorPerfilId(UUID perfilId);
}
