package br.com.bicoemcasa.api.modulos.servicos.services;

import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.core.excecao.ServicoNaoPertenceAoPerfilException;
import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.modulos.servicos.contrato.ServicoService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ServicoServiceImpl implements ServicoService {
    private final ServicoRepository servicoRepository;
    private final TagRepository tagRepository;
    private final PerfilService perfilService;

    public ServicoServiceImpl(
            ServicoRepository servicoRepository,
            TagRepository tagRepository,
            PerfilService perfilService
    ) {
        this.servicoRepository = servicoRepository;
        this.tagRepository = tagRepository;
        this.perfilService = perfilService;
    }

    @Override
    public ServicoResponse criar(ServicoRequest request, Long usuarioId) {
        PerfilResponse perfil = perfilService.buscarPorUsuarioIdETipo(usuarioId, PerfilTipo.PROFISSIONAL);
        Set<Tag> tags = buscarTags(request.tagIds());

        Servico servico = new Servico();
        servico.setPerfilId(perfil.id());
        servico.setTitulo(request.titulo());
        servico.setDescricao(request.descricao());
        servico.setPrecoPrevio(request.precoPrevio());
        servico.setUnidadePreco(request.unidadePreco());
        servico.setAtivo(true);
        servico.setTags(tags);

        servicoRepository.save(servico);

        return paraResponse(servico);
    }

    @Override
    @Transactional(readOnly = true)
    public ServicoResponse buscarPorId(UUID id) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Serviço não encontrado"));
        return paraResponse(servico);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<ServicoResponse> listarAtivos(int pagina, int tamanho) {
        var paginacao = PageRequest.of(pagina, tamanho, Sort.by("criadoEm").descending());
        return PaginaResponse.de(servicoRepository.findByAtivoTrue(paginacao), this::paraResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<ServicoResponse> listarMeus(Long usuarioId, int pagina, int tamanho) {
        PerfilResponse perfil = perfilService.buscarPorUsuarioIdETipo(usuarioId, PerfilTipo.PROFISSIONAL);
        var paginacao = PageRequest.of(pagina, tamanho, Sort.by("criadoEm").descending());
        return PaginaResponse.de(servicoRepository.findByPerfilId(perfil.id(), paginacao), this::paraResponse);
    }

    @Override
    public ServicoResponse editar(UUID id, ServicoRequest request, Long usuarioId) {
        Servico servico = buscarServicoDoPerfil(id, usuarioId);
        Set<Tag> tags = buscarTags(request.tagIds());

        servico.setTitulo(request.titulo());
        servico.setDescricao(request.descricao());
        servico.setPrecoPrevio(request.precoPrevio());
        servico.setUnidadePreco(request.unidadePreco());
        servico.setTags(tags);

        servicoRepository.save(servico);

        return paraResponse(servico);
    }

    @Override
    @Transactional
    public ServicoResponse ativar(UUID id, Long usuarioId) {
        Servico servico = buscarServicoDoPerfil(id, usuarioId);
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
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Serviço não encontrado"));

        PerfilResponse perfil = perfilService.buscarPorUsuarioIdETipo(usuarioId, PerfilTipo.PROFISSIONAL);
        if (!servico.getPerfilId().equals(perfil.id())) {
            throw new ServicoNaoPertenceAoPerfilException("Este serviço não pertence ao seu perfil");
        }

        return servico;
    }

    private Set<Tag> buscarTags(Set<UUID> tagIds) {
        List<Tag> tags = tagRepository.findAllById(tagIds);
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
}
