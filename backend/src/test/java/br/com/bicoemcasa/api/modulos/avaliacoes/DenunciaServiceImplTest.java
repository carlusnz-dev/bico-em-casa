package br.com.bicoemcasa.api.modulos.avaliacoes;

import br.com.bicoemcasa.api.core.excecao.RegraNegocioException;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.DenunciaRequest;
import br.com.bicoemcasa.api.modulos.avaliacoes.models.Denuncia;
import br.com.bicoemcasa.api.modulos.avaliacoes.models.TipoAlvoDenuncia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.DenunciaAnaliseRequest;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.DenunciaAtualizacaoRequest;
import br.com.bicoemcasa.api.modulos.avaliacoes.models.StatusDenuncia;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.DenunciaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;


@ExtendWith(MockitoExtension.class)
class DenunciaServiceImplTest {

    @Mock
    private DenunciaRepository denunciaRepository;

    @InjectMocks
    private DenunciaServiceImpl denunciaService;
    
    private static final UUID ID = UUID.randomUUID();

    private Denuncia denuncia(Long autorPerfilId, StatusDenuncia status) {
        return Denuncia.builder()
                .id(ID)
                .autorPerfilId(autorPerfilId)
                .alvoTipo(TipoAlvoDenuncia.SERVICO)
                .alvoId(UUID.randomUUID().toString())
                .motivo("Golpe")
                .status(status)
                .build();
    }
        @Test
    void buscarDenunciaDeOutroAutorDaNaoEncontrada() {
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(denuncia(1L, StatusDenuncia.PENDENTE)));

        assertThatThrownBy(() -> denunciaService.buscarPorId(ID, 2L))
                .isInstanceOf(EntidadeNaoEncontradaException.class);
    }

    @Test
    void buscarDenunciaInexistenteDaNaoEncontrada() {
        when(denunciaRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> denunciaService.buscarPorId(ID, 1L))
                .isInstanceOf(EntidadeNaoEncontradaException.class);
    }

    @Test
    void atualizarDenunciaJaJulgadaNaoPode() {
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(denuncia(1L, StatusDenuncia.PROCEDENTE)));
        DenunciaAtualizacaoRequest request = new DenunciaAtualizacaoRequest("Novo motivo", null);

        assertThatThrownBy(() -> denunciaService.atualizar(ID, 1L, request))
                .isInstanceOf(RegraNegocioException.class);
        verify(denunciaRepository, never()).saveAndFlush(any());
    }

    @Test
    void excluirDenunciaJaJulgadaNaoPode() {
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(denuncia(1L, StatusDenuncia.IMPROCEDENTE)));

        assertThatThrownBy(() -> denunciaService.excluir(ID, 1L))
                .isInstanceOf(RegraNegocioException.class);
        verify(denunciaRepository, never()).delete(any());
    }

    @Test
    void analisarDenunciaJaJulgadaNaoPode() {
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(denuncia(1L, StatusDenuncia.PROCEDENTE)));
        DenunciaAnaliseRequest request = new DenunciaAnaliseRequest(StatusDenuncia.IMPROCEDENTE, "Mudei de ideia");

        assertThatThrownBy(() -> denunciaService.analisar(ID, 99L, request))
                .isInstanceOf(RegraNegocioException.class);
        verify(denunciaRepository, never()).saveAndFlush(any());
    }


    @Test
    void criarGuardaIdDoAlvoPadronizado() {
        when(denunciaRepository.save(any(Denuncia.class))).thenAnswer(chamada -> chamada.getArgument(0));
        DenunciaRequest request = new DenunciaRequest(TipoAlvoDenuncia.PERFIL, " 007 ", "Perfil ofensivo", null, null, null);

        denunciaService.criar(1L, request);

        ArgumentCaptor<Denuncia> salva = ArgumentCaptor.forClass(Denuncia.class);
        verify(denunciaRepository).save(salva.capture());
        assertThat(salva.getValue().getAlvoId()).isEqualTo("7");
        assertThat(salva.getValue().getAutorPerfilId()).isEqualTo(1L);
    }

    @Test
    void criarRecusaIdDeAlvoInvalido() {
        DenunciaRequest request = new DenunciaRequest(TipoAlvoDenuncia.PERFIL, "abc", "Perfil ofensivo", null, null, null);

        assertThatThrownBy(() -> denunciaService.criar(1L, request))
                .isInstanceOf(RegraNegocioException.class);
        verify(denunciaRepository, never()).save(any());
    }
        @Test
    void atualizarTrocaOTextoEApagaDescricaoNaoEnviada() {
        Denuncia pendente = denuncia(1L, StatusDenuncia.PENDENTE);
        pendente.setDescricao("Descrição antiga");
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(pendente));
        when(denunciaRepository.saveAndFlush(any(Denuncia.class))).thenAnswer(chamada -> chamada.getArgument(0));

        DenunciaResponse resposta = denunciaService.atualizar(ID, 1L, new DenunciaAtualizacaoRequest("Novo motivo", null));

        assertThat(resposta.motivo()).isEqualTo("Novo motivo");
        assertThat(resposta.descricao()).isNull();
    }

    @Test
    void excluirApagaDenunciaPendenteDoAutor() {
        Denuncia pendente = denuncia(1L, StatusDenuncia.PENDENTE);
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(pendente));

        denunciaService.excluir(ID, 1L);

        verify(denunciaRepository).delete(pendente);
    }

    @Test
    void analisarRegistraResultadoParecerEQuemAnalisou() {
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(denuncia(1L, StatusDenuncia.PENDENTE)));
        when(denunciaRepository.saveAndFlush(any(Denuncia.class))).thenAnswer(chamada -> chamada.getArgument(0));

        DenunciaResponse resposta = denunciaService.analisar(ID, 99L,
                new DenunciaAnaliseRequest(StatusDenuncia.PROCEDENTE, "Golpe confirmado"));

        assertThat(resposta.status()).isEqualTo(StatusDenuncia.PROCEDENTE);
        assertThat(resposta.parecer()).isEqualTo("Golpe confirmado");
        assertThat(resposta.analisadoPorPerfilId()).isEqualTo(99L);
        assertThat(resposta.analisadoEm()).isNotNull();
    }

    @Test
    void listarParaAdminSemStatusBuscaTodas() {
        Pageable pagina = PageRequest.of(0, 20);
        when(denunciaRepository.findAll(pagina)).thenReturn(new PageImpl<>(List.of(denuncia(1L, StatusDenuncia.PENDENTE))));

        Page<DenunciaResponse> resposta = denunciaService.listarParaAdmin(99L, null, pagina);

        assertThat(resposta.getTotalElements()).isEqualTo(1);
        verify(denunciaRepository, never()).findByStatus(any(), any());
    }

    @Test
    void listarParaAdminComStatusFiltra() {
        Pageable pagina = PageRequest.of(0, 20);
        when(denunciaRepository.findByStatus(StatusDenuncia.PENDENTE, pagina))
                .thenReturn(new PageImpl<>(List.of(denuncia(1L, StatusDenuncia.PENDENTE))));

        Page<DenunciaResponse> resposta = denunciaService.listarParaAdmin(99L, StatusDenuncia.PENDENTE, pagina);

        assertThat(resposta.getContent()).hasSize(1);
        verify(denunciaRepository, never()).findAll(any(Pageable.class));
    }

}
