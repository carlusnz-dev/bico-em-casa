package br.com.bicoemcasa.api.modulos.contratacoes.contrato;

import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.ContratacaoResponse;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.ContratarServicoRequest;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.EditarContratacaoRequest;

import java.util.UUID;

public interface ContratacaoService {
    ContratacaoResponse contratar(ContratarServicoRequest request, Long usuarioId);
    ContratacaoResponse buscarPorId(UUID id, Long usuarioId);
    PaginaResponse<ContratacaoResponse> listarMinhas(Long usuarioId, int pagina, int tamanho);
    ContratacaoResponse editar(UUID id, EditarContratacaoRequest request, Long usuarioId);
    ContratacaoResponse arquivar(UUID id, Long usuarioId);
}
