package br.com.bicoemcasa.api.modulos.contratacoes.contrato;

import br.com.bicoemcasa.api.modulos.contratacoes.dto.MensagemRequest;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.MensagemResponse;

import java.util.List;
import java.util.UUID;

public interface ContratacaoMensagemService {
    List<MensagemResponse> listarMensagens(UUID contratacaoId, Long usuarioId);
    MensagemResponse enviarMensagem(UUID contratacaoId, MensagemRequest request, Long usuarioId);
}
