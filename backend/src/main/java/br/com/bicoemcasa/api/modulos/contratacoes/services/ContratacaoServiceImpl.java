package br.com.bicoemcasa.api.modulos.contratacoes.services;

import br.com.bicoemcasa.api.core.excecao.ContratacaoNaoPertenceAoPerfilException;
import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.modulos.contratacoes.contrato.ContratacaoService;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.ContratacaoResponse;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.ContratarServicoRequest;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.EditarContratacaoRequest;
import br.com.bicoemcasa.api.modulos.contratacoes.models.Contratacao;
import br.com.bicoemcasa.api.modulos.contratacoes.models.StatusContratacao;
import br.com.bicoemcasa.api.modulos.contratacoes.repository.ContratacaoRepository;
import br.com.bicoemcasa.api.modulos.servicos.contrato.ServicoService;
import br.com.bicoemcasa.api.modulos.servicos.dto.ServicoResponse;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.PerfilResponse;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ContratacaoServiceImpl implements ContratacaoService {
    private final ContratacaoRepository contratacaoRepository;
    private final ServicoService servicoService;
    private final PerfilService perfilService;

    public ContratacaoServiceImpl(
            ContratacaoRepository contratacaoRepository,
            ServicoService servicoService,
            PerfilService perfilService
    ) {
        this.contratacaoRepository = contratacaoRepository;
        this.servicoService = servicoService;
        this.perfilService = perfilService;
    }

    @Override
    public ContratacaoResponse contratar(ContratarServicoRequest request, Long usuarioId) {
        PerfilResponse cliente = perfilService.buscarPorUsuarioIdETipo(usuarioId, PerfilTipo.CLIENTE);
        ServicoResponse servico = servicoService.buscarPorId(request.servicoId());

        if (!servico.ativo()) {
            throw new EntidadeNaoEncontradaException("Este serviço não está disponível para contratação");
        }

        Contratacao contratacao = new Contratacao();
        contratacao.setServicoId(servico.id());
        contratacao.setClienteId(cliente.id());
        contratacao.setProfissionalId(servico.perfilId());
        contratacao.setTituloServico(servico.titulo());
        contratacao.setPrecoServico(servico.precoPrevio());
        contratacao.setObservacao(request.observacao());
        contratacao.setStatus(StatusContratacao.ATIVA);

        contratacaoRepository.save(contratacao);

        return paraResponse(contratacao);
    }

    @Override
    @Transactional(readOnly = true)
    public ContratacaoResponse buscarPorId(UUID id, Long usuarioId) {
        return paraResponse(buscarContratacaoDoCliente(id, usuarioId));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<ContratacaoResponse> listarMinhas(Long usuarioId, int pagina, int tamanho) {
        PerfilResponse cliente = perfilService.buscarPorUsuarioIdETipo(usuarioId, PerfilTipo.CLIENTE);
        var paginacao = PageRequest.of(pagina, tamanho, Sort.by("criadoEm").descending());
        return PaginaResponse.de(contratacaoRepository.findByClienteId(cliente.id(), paginacao), this::paraResponse);
    }

    @Override
    public ContratacaoResponse editar(UUID id, EditarContratacaoRequest request, Long usuarioId) {
        Contratacao contratacao = buscarContratacaoDoCliente(id, usuarioId);
        contratacao.setObservacao(request.observacao());
        contratacaoRepository.save(contratacao);
        return paraResponse(contratacao);
    }

    @Override
    public ContratacaoResponse arquivar(UUID id, Long usuarioId) {
        Contratacao contratacao = buscarContratacaoDoCliente(id, usuarioId);
        contratacao.setStatus(StatusContratacao.ARQUIVADA);
        contratacaoRepository.save(contratacao);
        return paraResponse(contratacao);
    }

    @Override
    public ContratacaoResponse desarquivar(UUID id, Long usuarioId) {
        Contratacao contratacao = buscarContratacaoDoCliente(id, usuarioId);
        contratacao.setStatus(StatusContratacao.ATIVA);
        contratacaoRepository.save(contratacao);
        return paraResponse(contratacao);
    }

    @Override
    public List<Contratacao> buscarPorServico(UUID servicoId){
        return contratacaoRepository.findByServicoId(servicoId);
    }

    private Contratacao buscarContratacaoDoCliente(UUID id, Long usuarioId) {
        Contratacao contratacao = contratacaoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Contratação não encontrada"));

        PerfilResponse cliente = perfilService.buscarPorUsuarioIdETipo(usuarioId, PerfilTipo.CLIENTE);
        if (!contratacao.getClienteId().equals(cliente.id())) {
            throw new ContratacaoNaoPertenceAoPerfilException("Esta contratação não pertence ao seu perfil");
        }

        return contratacao;
    }

    private ContratacaoResponse paraResponse(Contratacao contratacao) {
        return new ContratacaoResponse(
                contratacao.getId(),
                contratacao.getServicoId(),
                contratacao.getClienteId(),
                contratacao.getProfissionalId(),
                contratacao.getTituloServico(),
                contratacao.getPrecoServico(),
                contratacao.getObservacao(),
                contratacao.getStatus(),
                contratacao.getCriadoEm()
        );
    }
}
