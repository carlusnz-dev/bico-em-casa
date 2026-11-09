package br.com.bicoemcasa.api.modulos.servicos.services;

import br.com.bicoemcasa.api.core.RegistroAuditoria;
import br.com.bicoemcasa.api.core.excecao.ModeracaoConflitoException;
import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.core.excecao.ServicoNaoPertenceAoPerfilException;
import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.modulos.servicos.contrato.ServicoService;
import br.com.bicoemcasa.api.modulos.servicos.dto.PrecoMedioResponse;
import br.com.bicoemcasa.api.modulos.servicos.dto.ServicoRequest;
import br.com.bicoemcasa.api.modulos.servicos.dto.ServicoResponse;
import br.com.bicoemcasa.api.modulos.servicos.dto.TopListResponse;
import br.com.bicoemcasa.api.modulos.servicos.models.Servico;
import br.com.bicoemcasa.api.modulos.servicos.models.Tag;
import br.com.bicoemcasa.api.modulos.servicos.repository.ServicoRepository;
import br.com.bicoemcasa.api.modulos.servicos.repository.TagRepository;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.PerfilResponse;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServicoServiceImpl implements ServicoService {
    private final ServicoRepository servicoRepository;
    private final TagRepository tagRepository;
    private final PerfilService perfilService;
    private final RegistroAuditoria auditoria;

    public ServicoServiceImpl(
            ServicoRepository servicoRepository,
            TagRepository tagRepository,
            PerfilService perfilService,
            RegistroAuditoria auditoria
    ) {
        this.servicoRepository = servicoRepository;
        this.tagRepository = tagRepository;
        this.perfilService = perfilService;
        this.auditoria = auditoria;
    }

    @Override
    public ServicoResponse criar(ServicoRequest request, Long usuarioId) {
        PerfilResponse perfil = perfilService.buscarPorUsuarioIdETipo(usuarioId, PerfilTipo.PROFISSIONAL);
        Servico servico = new Servico();
        servico.setPerfilId(perfil.id());
        servico.setAtivo(true);
        preencherDados(servico, request);
        servicoRepository.save(servico);

        return paraResponse(servico).comModeracao(null, "PENDENTE", null);
    }

    @Override
    @Transactional(readOnly = true)
    public ServicoResponse buscarPorId(UUID id) {
        return paraResponse(buscarServico(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ServicoResponse buscarPublicoPorId(UUID id) {
        Servico servico = buscarServico(id);
        if (!servico.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado");
        }
        return paraResponse(servico);
    }

    @Override
    @Transactional(readOnly = true)
    public ServicoResponse buscarMeu(UUID id, Long usuarioId) {
        Servico servico = buscarServico(id);
        verificarDono(servico, usuarioId);
        return comModeracao(servico);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<ServicoResponse> listarAtivos(int pagina, int tamanho, String cidade, String categoria,
                                                       BigDecimal precoMinimo, BigDecimal precoMaximo, String busca) {
        if (precoMinimo != null && precoMaximo != null && precoMinimo.compareTo(precoMaximo) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O preço mínimo deve ser menor ou igual ao máximo");
        }
        boolean filtrarCidade = cidade != null && !cidade.isBlank();
        List<UUID> perfilIds = filtrarCidade ? perfilService.buscarIdsProfissionaisPorCidade(cidade.strip()) : List.of();
        String termo = busca == null ? "" : normalizarBusca(busca);
        String padrao = "%" + termo.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";

        var paginacao = PageRequest.of(pagina, tamanho, Sort.by("criadoEm", "id").descending());
        Specification<Servico> filtro = (raiz, consulta, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            condicoes.add(cb.isTrue(raiz.get("ativo")));
            if (filtrarCidade) {
                condicoes.add(perfilIds.isEmpty() ? cb.disjunction() : raiz.get("perfilId").in(perfilIds));
            }
            if (categoria != null && !categoria.isBlank()) {
                condicoes.add(cb.equal(raiz.join("tags").get("slug"), categoria.strip()));
            }
            if (precoMinimo != null) {
                condicoes.add(cb.greaterThanOrEqualTo(raiz.<BigDecimal>get("precoPrevio"), precoMinimo));
            }
            if (precoMaximo != null) {
                condicoes.add(cb.lessThanOrEqualTo(raiz.<BigDecimal>get("precoPrevio"), precoMaximo));
            }
            if (!termo.isEmpty()) {
                var categorias = consulta.subquery(UUID.class);
                var tag = categorias.correlate(raiz).join("tags");
                categorias.select(tag.get("id")).where(cb.like(textoNormalizado(cb, tag.get("nome")), padrao, '!'));
                condicoes.add(cb.or(cb.like(textoNormalizado(cb, raiz.get("titulo")), padrao, '!'),
                        cb.like(textoNormalizado(cb, raiz.get("descricao")), padrao, '!'), cb.exists(categorias)));
            }
            return cb.and(condicoes.toArray(Predicate[]::new));
        };
        return PaginaResponse.de(servicoRepository.findAll(filtro, paginacao), this::paraResponse);
    }

    private String normalizarBusca(String busca) {
        return Normalizer.normalize(busca.strip(), Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private Expression<String> textoNormalizado(CriteriaBuilder cb, Expression<String> campo) {
        return cb.function("translate", String.class, cb.lower(campo),
                cb.literal("áàâãäéèêëíìîïóòôõöúùûüç"), cb.literal("aaaaaeeeeiiiiooooouuuuc"));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<ServicoResponse> listarMeus(Long usuarioId, int pagina, int tamanho) {
        PerfilResponse perfil = perfilService.buscarPorUsuarioIdETipo(usuarioId, PerfilTipo.PROFISSIONAL);
        var paginacao = PageRequest.of(pagina, tamanho, Sort.by("criadoEm").descending());
        return PaginaResponse.de(servicoRepository.findByPerfilId(perfil.id(), paginacao), this::comModeracao);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrecoMedioResponse> calcularPrecosMedios(String categoria) {
        return servicoRepository.calcularPrecosMediosPorCategoria(categoria.strip());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrecoMedioResponse> calcularOrcamentoMedio(Long usuarioId) {
        PerfilResponse perfil = perfilService.buscarPorUsuarioIdETipo(usuarioId, PerfilTipo.PROFISSIONAL);
        return servicoRepository.calcularOrcamentoMedioPorPerfil(perfil.id());
    }

    @Override
    public ServicoResponse editar(UUID id, ServicoRequest request, Long usuarioId) {
        Servico servico = buscarServicoDoPerfil(id, usuarioId);
        preencherDados(servico, request);
        return comModeracao(servico);
    }

    @Override
    public ServicoResponse reenviar(UUID id, ServicoRequest request, Long usuarioId, String ip) {
        Servico servico = buscarServicoDoPerfil(id, usuarioId);
        if (!"REPROVADO".equals(servicoRepository.buscarStatusRevisao(id))) {
            throw new ModeracaoConflitoException("Apenas serviços reprovados podem ser reenviados");
        }
        preencherDados(servico, request);
        servico.setAtivo(false);
        auditoria.registrar(usuarioId, "REENVIAR_SERVICO", "servico", id.toString(), ip,
                "Reenvio após correção", Map.of(), Map.of("ativo", false));
        return comModeracao(servico);
    }

    private void preencherDados(Servico servico, ServicoRequest request) {
        servico.setTitulo(request.titulo());
        servico.setDescricao(request.descricao());
        servico.setPrecoPrevio(request.precoPrevio());
        servico.setUnidadePreco(request.unidadePreco());
        servico.setTags(buscarTags(request.tagIds()));
    }

    @Override
    @Transactional
    public ServicoResponse ativar(UUID id, Long usuarioId) {
        Servico servico = buscarServicoDoPerfil(id, usuarioId);
        if (servico.getMotivoReprovacao() != null) {
            throw new ModeracaoConflitoException("Serviço aguarda aprovação da moderação");
        }
        if (auditoria.motivoDesativacao(id.toString()) != null)
            throw new ModeracaoConflitoException(
                "Serviço desativado pela moderação; somente o administrador pode reativá-lo");
        servico.setAtivo(true);
        servicoRepository.save(servico);
        return paraResponse(servico);
    }

    @Override
    @Transactional
    public ServicoResponse desativar(UUID id, Long usuarioId) {
        Servico servico = buscarServicoDoPerfil(id, usuarioId);
        servico.setAtivo(false);
        servicoRepository.save(servico);
        return paraResponse(servico);
    }

    @Override
    public List<TopListResponse> listarTop5PorPerfilId(UUID perfilId) {
        return servicoRepository.findTop5ByPerfilId(perfilId)
                .stream().map(TopListResponse::new).toList();
    }

    private Servico buscarServicoDoPerfil(UUID id, Long usuarioId) {
        Servico servico = servicoRepository.buscarParaAtualizar(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Serviço não encontrado"));
        verificarDono(servico, usuarioId);
        return servico;
    }

    private void verificarDono(Servico servico, Long usuarioId) {
        PerfilResponse perfil = perfilService.buscarPorUsuarioIdETipo(usuarioId, PerfilTipo.PROFISSIONAL);
        if (!servico.getPerfilId().equals(perfil.id())) {
            throw new ServicoNaoPertenceAoPerfilException("Este serviço não pertence ao seu perfil");
        }

    }

    private Servico buscarServico(UUID id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Serviço não encontrado"));
    }

    private Set<Tag> buscarTags(Set<UUID> tagIds) {
        List<Tag> tags = tagRepository.buscarParaVincular(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new EntidadeNaoEncontradaException("Uma ou mais categorias informadas não existem");
        }
        return new HashSet<>(tags);
    }

    private ServicoResponse paraResponse(Servico servico) {
        Set<UUID> tagIds = servico.getTags().stream()
                .map(Tag::getId)
                .collect(Collectors.toSet());

        return new ServicoResponse(
                servico.getId(),
                servico.getPerfilId(),
                servico.getTitulo(),
                servico.getDescricao(),
                servico.getPrecoPrevio(),
                servico.getUnidadePreco(),
                servico.isAtivo(),
                tagIds,
                servico.getCriadoEm()
        );
    }

    @Override
    public void deletar(UUID id, Long usuarioId) {
        Servico servico = buscarServicoDoPerfil(id, usuarioId);
        servicoRepository.delete(servico);
    }

    private ServicoResponse comModeracao(Servico servico) {
        UUID id = servico.getId();
        return paraResponse(servico).comModeracao(auditoria.motivoDesativacao(id.toString()),
                servicoRepository.buscarStatusRevisao(id), servico.getMotivoReprovacao());
    }
}
