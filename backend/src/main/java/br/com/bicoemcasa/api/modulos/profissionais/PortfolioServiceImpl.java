package br.com.bicoemcasa.api.modulos.profissionais;

import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.core.excecao.PortfolioConflitoException;
import br.com.bicoemcasa.api.core.excecao.PortfolioNaoPertenceAoPerfilException;
import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.lib.armazenamento.ClienteArmazenamentoS3;
import br.com.bicoemcasa.api.modulos.profissionais.dto.ConfirmarFotoCapaRequest;
import br.com.bicoemcasa.api.modulos.profissionais.dto.ConfirmarMidiaRequest;
import br.com.bicoemcasa.api.modulos.profissionais.dto.PortfolioMidiaResponse;
import br.com.bicoemcasa.api.modulos.profissionais.dto.PortfolioRequest;
import br.com.bicoemcasa.api.modulos.profissionais.dto.PortfolioResponse;
import br.com.bicoemcasa.api.modulos.profissionais.dto.SolicitarUploadFotoCapaRequest;
import br.com.bicoemcasa.api.modulos.profissionais.dto.SolicitarUploadMidiaRequest;
import br.com.bicoemcasa.api.modulos.profissionais.dto.UrlUploadResponse;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.PerfilResponse;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URL;
import java.util.List;
import java.util.UUID;

@Service
public class PortfolioServiceImpl implements PortfolioService {
    private static final String BUCKET_PORTFOLIOS = "portfolios";
    private static final int MAX_MIDIAS = 12;
    private static final long LIMITE_FOTO = 5L * 1024 * 1024;
    private static final long LIMITE_VIDEO = 25L * 1024 * 1024;

    private final PortfolioRepository portfolioRepository;
    private final PortfolioMidiaRepository midias;
    private final PerfilService perfilService;
    private final ClienteArmazenamentoS3 clienteArmazenamento;

    public PortfolioServiceImpl(
            PortfolioRepository portfolioRepository,
            PortfolioMidiaRepository midias,
            PerfilService perfilService,
            ClienteArmazenamentoS3 clienteArmazenamento
    ) {
        this.portfolioRepository = portfolioRepository;
        this.midias = midias;
        this.perfilService = perfilService;
        this.clienteArmazenamento = clienteArmazenamento;
    }

    @Override
    public PortfolioResponse criar(PortfolioRequest request, Long usuarioId) {
        PerfilResponse perfil = perfilService.buscarPorUsuarioIdETipo(usuarioId, PerfilTipo.PROFISSIONAL);

        if (portfolioRepository.findByPerfilId(perfil.id()).isPresent()) {
            throw new PortfolioConflitoException("Este perfil já tem um portfólio");
        }
        garantirSlugDisponivel(request.slugUrl());

        Portfolio portfolio = new Portfolio();
        portfolio.setPerfilId(perfil.id());
        portfolio.setTitulo(request.titulo());
        portfolio.setDescricao(request.descricao());
        portfolio.setSlugUrl(request.slugUrl());

        portfolioRepository.save(portfolio);

        return paraResponse(portfolio);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioResponse buscarPorId(Long id) {
        return paraResponse(buscarOuFalhar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioResponse buscarPorPerfilId(UUID perfilId) {
        Portfolio portfolio = portfolioRepository.findByPerfilId(perfilId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Este profissional não tem portfólio"));
        return paraResponse(portfolio);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioResponse buscarMeu(Long usuarioId) {
        PerfilResponse perfil = perfilService.buscarPorUsuarioIdETipo(usuarioId, PerfilTipo.PROFISSIONAL);
        Portfolio portfolio = portfolioRepository.findByPerfilId(perfil.id())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Você ainda não tem um portfólio"));
        return paraResponse(portfolio);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<PortfolioResponse> listarTodos(int pagina, int tamanho, String cidade) {
        var paginacao = PageRequest.of(pagina, tamanho, Sort.by("criadoEm", "id").descending());
        if (cidade == null || cidade.isBlank()) {
            return PaginaResponse.de(portfolioRepository.findAll(paginacao), this::paraResponse);
        }
        List<UUID> perfis = perfilService.buscarIdsProfissionaisPorCidade(cidade.strip());
        Page<Portfolio> resultado = perfis.isEmpty() ? Page.empty(paginacao)
                : portfolioRepository.findByPerfilIdIn(perfis, paginacao);
        return PaginaResponse.de(resultado, this::paraResponse);
    }

    @Override
    public PortfolioResponse editar(Long id, PortfolioRequest request, Long usuarioId) {
        Portfolio portfolio = buscarPortfolioDoPerfil(id, usuarioId);

        if (!portfolio.getSlugUrl().equals(request.slugUrl())) {
            garantirSlugDisponivel(request.slugUrl());
        }

        portfolio.setTitulo(request.titulo());
        portfolio.setDescricao(request.descricao());
        portfolio.setSlugUrl(request.slugUrl());

        portfolioRepository.save(portfolio);

        return paraResponse(portfolio);
    }

    @Override
    public UrlUploadResponse solicitarUploadFotoCapa(Long id, SolicitarUploadFotoCapaRequest request, Long usuarioId) {
        Portfolio portfolio = buscarPortfolioDoPerfil(id, usuarioId);

        String chave = "capas/%d/%s".formatted(portfolio.getId(), UUID.randomUUID());
        URL url = clienteArmazenamento.gerarUrlUpload(BUCKET_PORTFOLIOS, chave, request.contentType());

        return new UrlUploadResponse(url.toString(), chave);
    }

    @Override
    public PortfolioResponse confirmarFotoCapa(Long id, ConfirmarFotoCapaRequest request, Long usuarioId) {
        Portfolio portfolio = buscarPortfolioDoPerfil(id, usuarioId);

        portfolio.setFotoCapaUrl(clienteArmazenamento.construirUrlPublica(BUCKET_PORTFOLIOS, request.chave()));
        portfolioRepository.save(portfolio);

        return paraResponse(portfolio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioMidiaResponse> listarMidias(Long id) {
        buscarOuFalhar(id);
        return midias.findByPortfolioIdOrderByCriadoEmAscIdAsc(id).stream().map(this::paraMidiaResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UrlUploadResponse solicitarUploadMidia(Long id, SolicitarUploadMidiaRequest request, Long usuarioId) {
        buscarPortfolioDoPerfil(id, usuarioId);
        verificarLimiteMidias(id);
        String chave = prefixoGaleria(id) + UUID.randomUUID();
        URL url = clienteArmazenamento.gerarUrlUpload(BUCKET_PORTFOLIOS, chave, request.contentType());
        return new UrlUploadResponse(url.toString(), chave);
    }

    @Override
    @Transactional
    public PortfolioMidiaResponse confirmarMidia(Long id, ConfirmarMidiaRequest request, Long usuarioId) {
        bloquearPortfolioDoPerfil(id, usuarioId);
        String chave = request.chave();
        if (!chave.matches(prefixoGaleria(id) + "[0-9a-f-]{36}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chave de upload inválida para este portfólio");
        }
        var confirmada = midias.findByPortfolioIdAndChave(id, chave);
        if (confirmada.isPresent()) {
            return paraMidiaResponse(confirmada.get());
        }
        verificarLimiteMidias(id);
        long limiteBytes = request.contentType().startsWith("video/") ? LIMITE_VIDEO : LIMITE_FOTO;
        if (!clienteArmazenamento.verificarObjeto(BUCKET_PORTFOLIOS, chave, request.contentType(), limiteBytes)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Arquivo ausente, com formato diferente ou acima do limite permitido");
        }
        var midia = new PortfolioMidia();
        midia.setPortfolioId(id);
        midia.setChave(chave);
        midia.setContentType(request.contentType());
        return paraMidiaResponse(midias.save(midia));
    }

    @Override
    @Transactional
    public void removerMidia(Long id, UUID midiaId, Long usuarioId) {
        bloquearPortfolioDoPerfil(id, usuarioId);
        PortfolioMidia midia = midias.findByIdAndPortfolioId(midiaId, id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Mídia não encontrada"));
        midias.delete(midia);
        midias.flush();
        clienteArmazenamento.removerObjeto(BUCKET_PORTFOLIOS, midia.getChave());
    }

    private String prefixoGaleria(Long id) {
        return "galeria/" + id + "/";
    }

    private void verificarLimiteMidias(Long id) {
        if (midias.countByPortfolioId(id) >= MAX_MIDIAS) {
            throw new PortfolioConflitoException("O portfólio já tem 12 arquivos na galeria");
        }
    }

    private PortfolioMidiaResponse paraMidiaResponse(PortfolioMidia midia) {
        return new PortfolioMidiaResponse(midia.getId(),
                clienteArmazenamento.construirUrlPublica(BUCKET_PORTFOLIOS, midia.getChave()),
                midia.getContentType());
    }

    private void garantirSlugDisponivel(String slugUrl) {
        if (portfolioRepository.existsBySlugUrl(slugUrl)) {
            throw new PortfolioConflitoException("Esse endereço já está em uso, escolha outro");
        }
    }

    private Portfolio buscarOuFalhar(Long id) {
        return portfolioRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Portfólio não encontrado"));
    }

    private Portfolio buscarPortfolioDoPerfil(Long id, Long usuarioId) {
        Portfolio portfolio = buscarOuFalhar(id);
        verificarDono(portfolio, usuarioId);
        return portfolio;
    }

    private void bloquearPortfolioDoPerfil(Long id, Long usuarioId) {
        Portfolio portfolio = portfolioRepository.buscarParaAtualizar(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Portfólio não encontrado"));
        verificarDono(portfolio, usuarioId);
    }

    private void verificarDono(Portfolio portfolio, Long usuarioId) {
        PerfilResponse perfil = perfilService.buscarPorUsuarioIdETipo(usuarioId, PerfilTipo.PROFISSIONAL);
        if (!portfolio.getPerfilId().equals(perfil.id())) {
            throw new PortfolioNaoPertenceAoPerfilException("Este portfólio não pertence ao seu perfil");
        }

    }

    private PortfolioResponse paraResponse(Portfolio portfolio) {
        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getPerfilId(),
                portfolio.getTitulo(),
                portfolio.getDescricao(),
                portfolio.getSlugUrl(),
                portfolio.getFotoCapaUrl(),
                portfolio.getCriadoEm()
        );
    }
}
