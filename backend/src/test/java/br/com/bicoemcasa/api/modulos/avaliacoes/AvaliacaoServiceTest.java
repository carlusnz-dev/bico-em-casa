package br.com.bicoemcasa.api.modulos.avaliacoes;

import br.com.bicoemcasa.api.core.excecao.AvaliacaoNaoPertenceAoPerfilException;
import br.com.bicoemcasa.api.core.excecao.TransicaoStatusInvalidaException;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.AvaliacaoRequest;
import br.com.bicoemcasa.api.modulos.avaliacoes.repository.AvaliacaoRepository;
import br.com.bicoemcasa.api.modulos.avaliacoes.service.AvaliacaoServiceImpl;
import br.com.bicoemcasa.api.modulos.contratacoes.contrato.ContratacaoService;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.ContratacaoResponse;
import br.com.bicoemcasa.api.modulos.contratacoes.models.StatusContratacao;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.PerfilResponse;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AvaliacaoServiceTest {
    private final AvaliacaoRepository avaliacoes = mock(AvaliacaoRepository.class);
    private final ContratacaoService contratacoes = mock(ContratacaoService.class);
    private final PerfilService perfis = mock(PerfilService.class);
    private final AvaliacaoServiceImpl servico = new AvaliacaoServiceImpl(avaliacoes, contratacoes, perfis);
    private final UUID id = UUID.randomUUID();
    private final UUID cliente = UUID.randomUUID();
    private final UUID profissional = UUID.randomUUID();
    private final AvaliacaoRequest avaliacao = new AvaliacaoRequest(5, "Serviço bem feito");

    @Test
    void impedeProfissionalDeAvaliarAPropriaContratacao() {
        when(contratacoes.buscarPorId(id, 2L)).thenReturn(contratacao(StatusContratacao.CONCLUIDO));
        when(perfis.encontrarPorUsuarioETipo(2L, PerfilTipo.CLIENTE)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> servico.criar(id, avaliacao, 2L))
                .isInstanceOf(AvaliacaoNaoPertenceAoPerfilException.class);
        verifyNoInteractions(avaliacoes);
    }

    @Test
    void exigeConclusaoAntesDaAvaliacao() {
        when(contratacoes.buscarPorId(id, 1L)).thenReturn(contratacao(StatusContratacao.EM_ANDAMENTO));
        prepararClienteTitular();
        assertThatThrownBy(() -> servico.criar(id, avaliacao, 1L))
                .isInstanceOf(TransicaoStatusInvalidaException.class);
        verifyNoInteractions(avaliacoes);
    }

    @Test
    void clienteTitularAvaliaServicoConcluido() {
        when(contratacoes.buscarPorId(id, 1L)).thenReturn(contratacao(StatusContratacao.CONCLUIDO));
        prepararClienteTitular();
        var criada = servico.criar(id, avaliacao, 1L);
        assertThat(criada.autorPerfilId()).isEqualTo(cliente);
        assertThat(criada.avaliadoPerfilId()).isEqualTo(profissional);
    }

    private void prepararClienteTitular() {
        var perfil = new PerfilResponse(cliente, 1L, PerfilTipo.CLIENTE, "cliente", "Cliente", null, null, null);
        when(perfis.encontrarPorUsuarioETipo(1L, PerfilTipo.CLIENTE)).thenReturn(Optional.of(perfil));
    }

    private ContratacaoResponse contratacao(StatusContratacao status) {
        return new ContratacaoResponse(id, UUID.randomUUID(), cliente, profissional, "Instalação",
                new BigDecimal("100.00"), null, status, false, null);
    }
}
