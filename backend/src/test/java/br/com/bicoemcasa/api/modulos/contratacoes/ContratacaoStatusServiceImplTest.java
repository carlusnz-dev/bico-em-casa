package br.com.bicoemcasa.api.modulos.contratacoes;

import br.com.bicoemcasa.api.core.excecao.ContratacaoNaoPertenceAoPerfilException;
import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.core.excecao.TransicaoStatusInvalidaException;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.AlterarStatusRequest;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.ContratacaoResponse;
import br.com.bicoemcasa.api.modulos.contratacoes.models.Contratacao;
import br.com.bicoemcasa.api.modulos.contratacoes.models.StatusContratacao;
import br.com.bicoemcasa.api.modulos.contratacoes.repository.ContratacaoRepository;
import br.com.bicoemcasa.api.modulos.contratacoes.services.ContratacaoStatusServiceImpl;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.PerfilResponse;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContratacaoStatusServiceImplTest {

    @Mock
    private ContratacaoRepository contratacaoRepository;

    @Mock
    private PerfilService perfilService;

    @InjectMocks
    private ContratacaoStatusServiceImpl contratacaoStatusService;

    private static final UUID CONTRATACAO_ID = UUID.randomUUID();
    private static final UUID PROFISSIONAL_ID = UUID.randomUUID();
    private static final UUID OUTRO_PROFISSIONAL_ID = UUID.randomUUID();
    private static final Long USUARIO_ID = 10L;

    private Contratacao contratacao(UUID profissionalId, StatusContratacao status) {
        Contratacao contratacao = new Contratacao();
        contratacao.setId(CONTRATACAO_ID);
        contratacao.setServicoId(UUID.randomUUID());
        contratacao.setClienteId(UUID.randomUUID());
        contratacao.setProfissionalId(profissionalId);
        contratacao.setTituloServico("Troca de disjuntor");
        contratacao.setPrecoServico(new BigDecimal("150.00"));
        contratacao.setStatus(status);
        contratacao.setArquivada(false);
        return contratacao;
    }

    private PerfilResponse perfilProfissional(UUID id) {
        return new PerfilResponse(id, USUARIO_ID, PerfilTipo.PROFISSIONAL, "profissional", "Profissional", null, null, null);
    }

    @Test
    void deveConcluirContratacaoEmAndamento() {
        Contratacao entidade = contratacao(PROFISSIONAL_ID, StatusContratacao.EM_ANDAMENTO);
        when(contratacaoRepository.findById(CONTRATACAO_ID)).thenReturn(Optional.of(entidade));
        when(perfilService.buscarPorUsuarioIdETipo(USUARIO_ID, PerfilTipo.PROFISSIONAL)).thenReturn(perfilProfissional(PROFISSIONAL_ID));

        ContratacaoResponse response = contratacaoStatusService.alterarStatus(
                CONTRATACAO_ID,
                new AlterarStatusRequest(StatusContratacao.CONCLUIDO),
                USUARIO_ID
        );

        assertThat(response.status()).isEqualTo(StatusContratacao.CONCLUIDO);
        verify(contratacaoRepository).save(entidade);
    }

    @Test
    void deveCancelarContratacaoEmAndamento() {
        Contratacao entidade = contratacao(PROFISSIONAL_ID, StatusContratacao.EM_ANDAMENTO);
        when(contratacaoRepository.findById(CONTRATACAO_ID)).thenReturn(Optional.of(entidade));
        when(perfilService.buscarPorUsuarioIdETipo(USUARIO_ID, PerfilTipo.PROFISSIONAL)).thenReturn(perfilProfissional(PROFISSIONAL_ID));

        ContratacaoResponse response = contratacaoStatusService.alterarStatus(
                CONTRATACAO_ID,
                new AlterarStatusRequest(StatusContratacao.CANCELADO),
                USUARIO_ID
        );

        assertThat(response.status()).isEqualTo(StatusContratacao.CANCELADO);
        verify(contratacaoRepository).save(entidade);
    }

    @Test
    void naoDeveAlterarStatusDeContratacaoJaConcluida() {
        Contratacao entidade = contratacao(PROFISSIONAL_ID, StatusContratacao.CONCLUIDO);
        when(contratacaoRepository.findById(CONTRATACAO_ID)).thenReturn(Optional.of(entidade));
        when(perfilService.buscarPorUsuarioIdETipo(USUARIO_ID, PerfilTipo.PROFISSIONAL)).thenReturn(perfilProfissional(PROFISSIONAL_ID));

        assertThatThrownBy(() -> contratacaoStatusService.alterarStatus(
                CONTRATACAO_ID,
                new AlterarStatusRequest(StatusContratacao.CANCELADO),
                USUARIO_ID
        )).isInstanceOf(TransicaoStatusInvalidaException.class);

        verify(contratacaoRepository, never()).save(entidade);
    }

    @Test
    void naoDeveAlterarStatusDeContratacaoDeOutroProfissional() {
        Contratacao entidade = contratacao(PROFISSIONAL_ID, StatusContratacao.EM_ANDAMENTO);
        when(contratacaoRepository.findById(CONTRATACAO_ID)).thenReturn(Optional.of(entidade));
        when(perfilService.buscarPorUsuarioIdETipo(USUARIO_ID, PerfilTipo.PROFISSIONAL)).thenReturn(perfilProfissional(OUTRO_PROFISSIONAL_ID));

        assertThatThrownBy(() -> contratacaoStatusService.alterarStatus(
                CONTRATACAO_ID,
                new AlterarStatusRequest(StatusContratacao.CONCLUIDO),
                USUARIO_ID
        )).isInstanceOf(ContratacaoNaoPertenceAoPerfilException.class);

        verify(contratacaoRepository, never()).save(entidade);
    }

    @Test
    void deveLancarNaoEncontradaQuandoContratacaoNaoExiste() {
        when(contratacaoRepository.findById(CONTRATACAO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contratacaoStatusService.alterarStatus(
                CONTRATACAO_ID,
                new AlterarStatusRequest(StatusContratacao.CONCLUIDO),
                USUARIO_ID
        )).isInstanceOf(EntidadeNaoEncontradaException.class);
    }
}
