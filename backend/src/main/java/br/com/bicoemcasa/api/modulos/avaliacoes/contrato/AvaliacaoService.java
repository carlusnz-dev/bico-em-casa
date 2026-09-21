package br.com.bicoemcasa.api.modulos.avaliacoes.contrato;

import br.com.bicoemcasa.api.modulos.avaliacoes.dto.AvaliacaoRequest;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.AvaliacaoResponse;

import java.util.List;
import java.util.UUID;

public interface AvaliacaoService {
    AvaliacaoResponse criar(UUID contratacaoId, AvaliacaoRequest request, Long usuarioId);
    AvaliacaoResponse buscarPorId(UUID id);
    List<AvaliacaoResponse> buscarPorAvaliado(UUID avaliadoPerfilId);
    List<AvaliacaoResponse> buscarPorAutor(UUID autorPerfilId);
    AvaliacaoResponse alterar(UUID id, AvaliacaoRequest request, Long usuarioId);
    void deletar(UUID id, Long usuarioId);
    Double calcularMediaPorProfissional(Long profissionalId);
    Double calcularMediaPorServico(UUID servicoId);
}
