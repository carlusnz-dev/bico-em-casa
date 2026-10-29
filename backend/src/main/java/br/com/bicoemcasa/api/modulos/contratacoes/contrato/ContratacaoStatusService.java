package br.com.bicoemcasa.api.modulos.contratacoes.contrato;

import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.AlterarStatusRequest;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.ContratacaoResponse;

import java.util.UUID;

public interface ContratacaoStatusService {
    PaginaResponse<ContratacaoResponse> listarRecebidas(Long usuarioId, int pagina, int tamanho);
    ContratacaoResponse alterarStatus(UUID id, AlterarStatusRequest request, Long usuarioId);
}
