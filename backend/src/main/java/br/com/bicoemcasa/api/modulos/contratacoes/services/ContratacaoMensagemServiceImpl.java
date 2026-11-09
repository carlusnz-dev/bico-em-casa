package br.com.bicoemcasa.api.modulos.contratacoes.services;

import br.com.bicoemcasa.api.core.excecao.ContratacaoNaoPertenceAoPerfilException;
import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.modulos.contratacoes.contrato.ContratacaoMensagemService;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.MensagemRequest;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.MensagemResponse;
import br.com.bicoemcasa.api.modulos.contratacoes.models.Contratacao;
import br.com.bicoemcasa.api.modulos.contratacoes.models.ContratacaoMensagem;
import br.com.bicoemcasa.api.modulos.contratacoes.repository.ContratacaoMensagemRepository;
import br.com.bicoemcasa.api.modulos.contratacoes.repository.ContratacaoRepository;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.PerfilResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ContratacaoMensagemServiceImpl implements ContratacaoMensagemService {

    private final ContratacaoRepository contratacaoRepository;
    private final ContratacaoMensagemRepository mensagemRepository;
    private final PerfilService perfilService;

    public ContratacaoMensagemServiceImpl(
            ContratacaoRepository contratacaoRepository,
            ContratacaoMensagemRepository mensagemRepository,
            PerfilService perfilService
    ) {
        this.contratacaoRepository = contratacaoRepository;
        this.mensagemRepository = mensagemRepository;
        this.perfilService = perfilService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensagemResponse> listarMensagens(UUID contratacaoId, Long usuarioId) {
        resolverParticipante(contratacaoId, usuarioId, null);
        return mensagemRepository.findByContratacaoIdOrderByCriadoEmAsc(contratacaoId)
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    @Override
    public MensagemResponse enviarMensagem(UUID contratacaoId, MensagemRequest request, Long usuarioId) {
        PerfilResponse autor = resolverParticipante(contratacaoId, usuarioId, request.autorPerfilId());

        ContratacaoMensagem mensagem = new ContratacaoMensagem();
        mensagem.setContratacaoId(contratacaoId);
        mensagem.setAutorPerfilId(autor.id());
        mensagem.setAutorNome(autor.nomeExibicao());
        mensagem.setConteudo(request.conteudo().trim());

        mensagemRepository.save(mensagem);

        return paraResponse(mensagem);
    }

    private PerfilResponse resolverParticipante(UUID contratacaoId, Long usuarioId, UUID autorPerfilIdPreferido) {
        Contratacao contratacao = contratacaoRepository.findById(contratacaoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Contratação não encontrada"));

        PerfilResponse cliente = perfilService.buscarPorId(contratacao.getClienteId());
        PerfilResponse profissional = perfilService.buscarPorId(contratacao.getProfissionalId());

        if (autorPerfilIdPreferido != null) {
            if (autorPerfilIdPreferido.equals(cliente.id())) {
                return cliente;
            }
            if (autorPerfilIdPreferido.equals(profissional.id())) {
                return profissional;
            }
        }

        if (usuarioId.equals(cliente.usuarioId())) {
            return cliente;
        }

        if (usuarioId.equals(profissional.usuarioId())) {
            return profissional;
        }

        throw new ContratacaoNaoPertenceAoPerfilException("Esta contratação não pertence ao seu perfil");
    }

    private MensagemResponse paraResponse(ContratacaoMensagem mensagem) {
        return new MensagemResponse(
                mensagem.getId(),
                mensagem.getContratacaoId(),
                mensagem.getAutorPerfilId(),
                mensagem.getAutorNome(),
                mensagem.getConteudo(),
                mensagem.getCriadoEm()
        );
    }
}
