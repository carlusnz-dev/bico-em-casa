package br.com.bicoemcasa.api.modulos.avaliacoes.service;

import br.com.bicoemcasa.api.core.excecao.AvaliacaoJaExisteException;
import br.com.bicoemcasa.api.core.excecao.AvaliacaoNaoPertenceAoPerfilException;
import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.modulos.avaliacoes.contrato.AvaliacaoService;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.AvaliacaoRequest;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.AvaliacaoResponse;
import br.com.bicoemcasa.api.modulos.avaliacoes.models.Avaliacao;
import br.com.bicoemcasa.api.modulos.avaliacoes.repository.AvaliacaoRepository;
import br.com.bicoemcasa.api.modulos.contratacoes.contrato.ContratacaoService;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.ContratacaoResponse;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.PerfilResponse;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AvaliacaoServiceImpl implements AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final ContratacaoService contratacaoService;
    private final PerfilService perfilService;

    public AvaliacaoServiceImpl(
            AvaliacaoRepository avaliacaoRepository,
            ContratacaoService contratacaoService,
            PerfilService perfilService
    ) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.contratacaoService = contratacaoService;
        this.perfilService = perfilService;
    }

    @Override
    public AvaliacaoResponse criar(UUID contratacaoId, AvaliacaoRequest request, Long usuarioId) {
        if (avaliacaoRepository.existsByContratacaoId(contratacaoId)) {
            throw new AvaliacaoJaExisteException("Esta contratação já foi avaliada");
        }

        // buscarPorId já garante que a contratação pertence ao cliente autenticado
        ContratacaoResponse contratacao = contratacaoService.buscarPorId(contratacaoId, usuarioId);

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setContratacaoId(contratacaoId);
        avaliacao.setAutorPerfilId(contratacao.clienteId());
        avaliacao.setAvaliadoPerfilId(contratacao.profissionalId());
        avaliacao.setNota(request.nota());
        avaliacao.setComentario(request.comentario());

        avaliacaoRepository.save(avaliacao);

        return paraResponse(avaliacao);
    }

    @Override
    @Transactional(readOnly = true)
    public AvaliacaoResponse buscarPorId(UUID id) {
        return paraResponse(buscarOuFalhar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvaliacaoResponse> buscarPorAvaliado(UUID avaliadoPerfilId) {
        return avaliacaoRepository.findByAvaliadoPerfilId(avaliadoPerfilId).stream()
                .map(this::paraResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvaliacaoResponse> buscarPorAutor(UUID autorPerfilId) {
        return avaliacaoRepository.findByAutorPerfilId(autorPerfilId).stream()
                .map(this::paraResponse)
                .toList();
    }

    @Override
    public AvaliacaoResponse alterar(UUID id, AvaliacaoRequest request, Long usuarioId) {
        Avaliacao avaliacao = buscarAvaliacaoDoAutor(id, usuarioId);
        avaliacao.setNota(request.nota());
        avaliacao.setComentario(request.comentario());

        avaliacaoRepository.save(avaliacao);

        return paraResponse(avaliacao);
    }

    @Override
    public void deletar(UUID id, Long usuarioId) {
        Avaliacao avaliacao = buscarAvaliacaoDoAutor(id, usuarioId);
        avaliacaoRepository.delete(avaliacao);
    }

    private Avaliacao buscarAvaliacaoDoAutor(UUID id, Long usuarioId) {
        Avaliacao avaliacao = buscarOuFalhar(id);
        PerfilResponse cliente = perfilService.buscarPorUsuarioIdETipo(usuarioId, PerfilTipo.CLIENTE);

        if (!avaliacao.getAutorPerfilId().equals(cliente.id())) {
            throw new AvaliacaoNaoPertenceAoPerfilException("Esta avaliação não pertence ao seu perfil");
        }

        return avaliacao;
    }

    private Avaliacao buscarOuFalhar(UUID id) {
        return avaliacaoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Avaliação não encontrada"));
    }

    private AvaliacaoResponse paraResponse(Avaliacao avaliacao) {
        return new AvaliacaoResponse(
                avaliacao.getId(),
                avaliacao.getContratacaoId(),
                avaliacao.getAutorPerfilId(),
                avaliacao.getAvaliadoPerfilId(),
                avaliacao.getNota(),
                avaliacao.getComentario(),
                avaliacao.getCriadoEm(),
                avaliacao.getAtualizadoEm()
        );
    }
}
