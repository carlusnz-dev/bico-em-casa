package br.com.bicoemcasa.api.modulos.profissionais;

import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.core.excecao.PortfolioConflitoException;
import br.com.bicoemcasa.api.core.excecao.PortfolioNaoPertenceAoPerfilException;
import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.lib.armazenamento.ClienteArmazenamentoS3;
import br.com.bicoemcasa.api.modulos.profissionais.dto.ConfirmarFotoCapaRequest;
import br.com.bicoemcasa.api.modulos.profissionais.dto.PortfolioRequest;
import br.com.bicoemcasa.api.modulos.profissionais.dto.PortfolioResponse;
import br.com.bicoemcasa.api.modulos.profissionais.dto.SolicitarUploadFotoCapaRequest;
import br.com.bicoemcasa.api.modulos.profissionais.dto.UrlUploadResponse;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.PerfilResponse;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.UUID;

@Service
public class PortfolioServiceImpl implements PortfolioService {
    private static final String BUCKET_PORTFOLIOS = "portfolios";

    private final PortfolioRepository portfolioRepository;
    private final PerfilService perfilService;
    private final ClienteArmazenamentoS3 clienteArmazenamento;

    public PortfolioServiceImpl(
            PortfolioRepository portfolioRepository,
            PerfilService perfilService,
            ClienteArmazenamentoS3 clienteArmazenamento
    ) {
        this.portfolioRepository = portfolioRepository;
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
    public PortfolioResponse buscarMeu(Long usuarioId) {
        PerfilResponse perfil = perfilService.buscarPorUsuarioIdETipo(usuarioId, PerfilTipo.PROFISSIONAL);
        Portfolio portfolio = portfolioRepository.findByPerfilId(perfil.id())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Você ainda não tem um portfólio"));
        return paraResponse(portfolio);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<PortfolioResponse> listarTodos(int pagina, int tamanho) {
        var paginacao = PageRequest.of(pagina, tamanho, Sort.by("criadoEm").descending());
        return PaginaResponse.de(portfolioRepository.findAll(paginacao), this::paraResponse);
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

        PerfilResponse perfil = perfilService.buscarPorUsuarioIdETipo(usuarioId, PerfilTipo.PROFISSIONAL);
        if (!portfolio.getPerfilId().equals(perfil.id())) {
            throw new PortfolioNaoPertenceAoPerfilException("Este portfólio não pertence ao seu perfil");
        }

        return portfolio;
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
