package br.com.bicoemcasa.api.modulos.servicos.contrato;

import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.modulos.servicos.dto.PrecoMedioResponse;
import br.com.bicoemcasa.api.modulos.servicos.dto.ServicoRequest;
import br.com.bicoemcasa.api.modulos.servicos.dto.ServicoResponse;
import br.com.bicoemcasa.api.modulos.servicos.dto.TopListResponse;

import java.util.List;
import java.math.BigDecimal;
import java.util.UUID;

public interface ServicoService {
    ServicoResponse criar(ServicoRequest request, Long usuarioId);
    ServicoResponse buscarPorId(UUID id);
    ServicoResponse buscarPublicoPorId(UUID id);
    ServicoResponse buscarMeu(UUID id, Long usuarioId);
    default PaginaResponse<ServicoResponse> listarAtivos(int pagina, int tamanho, String cidade) {
        return listarAtivos(pagina, tamanho, cidade, null, null, null, null);
    }
    PaginaResponse<ServicoResponse> listarAtivos(int pagina, int tamanho, String cidade, String categoria,
                                                BigDecimal precoMinimo, BigDecimal precoMaximo, String busca);
    PaginaResponse<ServicoResponse> listarMeus(Long usuarioId, int pagina, int tamanho);
    List<PrecoMedioResponse> calcularPrecosMedios(String categoria);
    List<PrecoMedioResponse> calcularOrcamentoMedio(Long usuarioId);
    ServicoResponse editar(UUID id, ServicoRequest request, Long usuarioId);
    ServicoResponse reenviar(UUID id, ServicoRequest request, Long usuarioId, String ip);
    ServicoResponse ativar(UUID id, Long usuarioId);
    ServicoResponse desativar(UUID id, Long usuarioId);
    void deletar(UUID id, Long usuarioId);
    List<TopListResponse> listarTop5PorPerfilId(UUID perfilId);
}
