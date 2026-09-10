package br.com.bicoemcasa.api.modulos.avaliacoes;


import br.com.bicoemcasa.api.modulos.avaliacoes.dto.AvaliacaoRequest;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.AvaliacaoResponse;
import java.util.List;
import java.util.UUID;


public interface AvaliacaoService {

    AvaliacaoResponse criar(UUID contratacaoId, AvaliacaoRequest request);
    AvaliacaoResponse alterar(UUID id,AvaliacaoRequest request);
    void deletar(UUID id);
    AvaliacaoResponse buscarPorId(UUID Id);
    List<AvaliacaoResponse> buscarPorAvaliado(Long avaliadoId);

}