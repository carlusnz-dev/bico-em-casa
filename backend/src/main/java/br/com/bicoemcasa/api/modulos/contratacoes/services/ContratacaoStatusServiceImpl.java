package br.com.bicoemcasa.api.modulos.contratacoes.services;

import br.com.bicoemcasa.api.core.excecao.ContratacaoNaoPertenceAoPerfilException;
import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.core.excecao.TransicaoStatusInvalidaException;
import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.modulos.contratacoes.contrato.ContratacaoStatusService;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.AlterarStatusRequest;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.ContratacaoResponse;
import br.com.bicoemcasa.api.modulos.contratacoes.models.Contratacao;
import br.com.bicoemcasa.api.modulos.contratacoes.models.StatusContratacao;
import br.com.bicoemcasa.api.modulos.contratacoes.repository.ContratacaoRepository;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.PerfilResponse;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ContratacaoStatusServiceImpl implements ContratacaoStatusService {

    private final ContratacaoRepository contratacaoRepository;
    private final PerfilService perfilService;

    public ContratacaoStatusServiceImpl(
            ContratacaoRepository contratacaoRepository,
            PerfilService perfilService
    ) {
        this.contratacaoRepository = contratacaoRepository;
        this.perfilService = perfilService;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<ContratacaoResponse> listarRecebidas(Long usuarioId, int pagina, int tamanho) {
        PerfilResponse profissional = perfilService.buscarPorUsuarioIdETipo(usuarioId, PerfilTipo.PROFISSIONAL);
        var paginacao = PageRequest.of(pagina, tamanho, Sort.by("criadoEm").descending());
        return PaginaResponse.de(contratacaoRepository.findByProfissionalId(profissional.id(), paginacao), this::paraResponse);
    }

    @Override
    public ContratacaoResponse alterarStatus(UUID id, AlterarStatusRequest request, Long usuarioId) {
        Contratacao contratacao = contratacaoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Contratação não encontrada"));

        PerfilResponse profissional = perfilService.buscarPorUsuarioIdETipo(usuarioId, PerfilTipo.PROFISSIONAL);
        if (!contratacao.getProfissionalId().equals(profissional.id())) {
            throw new ContratacaoNaoPertenceAoPerfilException("Esta contratação não pertence ao seu perfil profissional");
        }

        validarTransicao(contratacao.getStatus(), request.status());

        contratacao.setStatus(request.status());
        contratacaoRepository.save(contratacao);

        return paraResponse(contratacao);
    }

    private void validarTransicao(StatusContratacao atual, StatusContratacao novo) {
        if (atual != StatusContratacao.EM_ANDAMENTO) {
            throw new TransicaoStatusInvalidaException("Contratações concluídas ou canceladas não podem ter o status alterado");
        }
        if (novo != StatusContratacao.CONCLUIDO && novo != StatusContratacao.CANCELADO) {
            throw new TransicaoStatusInvalidaException("Transição de status inválida: permitido apenas CONCLUIDO ou CANCELADO");
        }
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
                contratacao.isArquivada(),
                contratacao.getCriadoEm()
        );
    }
}
