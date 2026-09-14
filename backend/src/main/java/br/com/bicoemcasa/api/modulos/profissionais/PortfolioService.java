package br.com.bicoemcasa.api.modulos.profissionais;

import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.modulos.profissionais.dto.ConfirmarFotoCapaRequest;
import br.com.bicoemcasa.api.modulos.profissionais.dto.PortfolioRequest;
import br.com.bicoemcasa.api.modulos.profissionais.dto.PortfolioResponse;
import br.com.bicoemcasa.api.modulos.profissionais.dto.SolicitarUploadFotoCapaRequest;
import br.com.bicoemcasa.api.modulos.profissionais.dto.UrlUploadResponse;

public interface PortfolioService {
    PortfolioResponse criar(PortfolioRequest request, Long usuarioId);
    PortfolioResponse buscarPorId(Long id);
    PortfolioResponse buscarMeu(Long usuarioId);
    PaginaResponse<PortfolioResponse> listarTodos(int pagina, int tamanho);
    PortfolioResponse editar(Long id, PortfolioRequest request, Long usuarioId);
    UrlUploadResponse solicitarUploadFotoCapa(Long id, SolicitarUploadFotoCapaRequest request, Long usuarioId);
    PortfolioResponse confirmarFotoCapa(Long id, ConfirmarFotoCapaRequest request, Long usuarioId);
}
