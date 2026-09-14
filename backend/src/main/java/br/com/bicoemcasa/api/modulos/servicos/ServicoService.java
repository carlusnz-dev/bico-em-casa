package br.com.bicoemcasa.api.modulos.servicos;

import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.modulos.servicos.dto.ServicoRequest;
import br.com.bicoemcasa.api.modulos.servicos.dto.ServicoResponse;

import java.util.UUID;

public interface ServicoService {
    ServicoResponse criar(ServicoRequest request, Long usuarioId);
    PaginaResponse<ServicoResponse> listarAtivos(int pagina, int tamanho);
    ServicoResponse editar(UUID id, ServicoRequest request, Long usuarioId);
    ServicoResponse ativar(UUID id, Long usuarioId);
    ServicoResponse desativar(UUID id, Long usuarioId);
}
