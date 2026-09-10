package br.com.bicoemcasa.api.modulos.avaliacoes;


import br.com.bicoemcasa.api.modulos.avaliacoes.dto.AvaliacaoRequest;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.AvaliacaoResponse;
import br.com.bicoemcasa.api.modulos.contratacoes.Contratacao;
import br.com.bicoemcasa.api.modulos.contratacoes.ContratacaoService;
import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AvaliacaoServiceImpl implements AvaliacaoService {

    private final AvaliacaoRepository repository;

    private final ContratacaoService contratacaoService;

    public AvaliacaoServiceImpl(AvaliacaoRepository repository,
                                ContratacaoService contratacaoService){
        this.repository = repository;
        this.contratacaoService = contratacaoService;
    }

    private AvaliacaoResponse toResponse(Avaliacao avaliacao){

        return new AvaliacaoResponse(
                avaliacao.getId(),
                avaliacao.getContratacaoId(),
                avaliacao.getAutorId(),
                avaliacao.getAvaliadoId(),
                avaliacao.getNota(),
                avaliacao.getComentario(),
                avaliacao.getCriadoEm(),
                avaliacao.getAtualizadoEm()
        );
    }

    @Override
    public AvaliacaoResponse criar(UUID contratacaoId, AvaliacaoRequest request) {

        if (repository.existsByContratacaoId(contratacaoId)){
            throw new RuntimeException("Já existe uma avaliação!");
        }

        Contratacao contratacao = contratacaoService.buscarPorId(contratacaoId);

        Avaliacao avaliacao = new Avaliacao();

        avaliacao.setAutorId(contratacao.getClienteId());
        avaliacao.setAvaliadoId(contratacao.getProfissionalId());
        avaliacao.setContratacaoId(contratacaoId);
        avaliacao.setNota(request.nota());
        avaliacao.setComentario(request.comentario());


        Avaliacao avaliacaoSalva = repository.save(avaliacao);

        return toResponse(avaliacaoSalva);
    }

    @Override
    public AvaliacaoResponse buscarPorId(UUID id) {

        Optional<Avaliacao> avaliacao = repository.findById(id);

        Avaliacao avaliacaoEncontrada = avaliacao.orElseThrow(
                () -> new EntidadeNaoEncontradaException(
                        "Avaliação não encontrada: " + id
                        )
        );

        return toResponse(avaliacaoEncontrada);

    }

    @Override
    public List<AvaliacaoResponse> buscarPorAvaliado(Long avaliadoId) {

        List<Avaliacao> avaliacoes =repository.findByAvaliadoId(avaliadoId);

        return avaliacoes.stream()
                .map(avaliacao -> toResponse(avaliacao))
                .toList();
    }

    @Override
    public AvaliacaoResponse alterar(UUID id, AvaliacaoRequest
                                     request){

        Avaliacao avaliacaoExistente = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Avaliação não encontrada!"));


        avaliacaoExistente.setNota(request.nota());
        avaliacaoExistente.setComentario(request.comentario());


        Avaliacao avaliacaoSalva = repository.save(avaliacaoExistente);
        return toResponse(avaliacaoSalva);
    }

    @Override
    public void deletar(UUID id){
        repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Avaliação não encontrada!"));

        repository.deleteById(id);
    }
}
