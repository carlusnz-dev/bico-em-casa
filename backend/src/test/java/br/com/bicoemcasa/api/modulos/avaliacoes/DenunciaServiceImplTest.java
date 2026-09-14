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
    private static final UUID AUTOR = UUID.randomUUID();
    private static final UUID OUTRO_AUTOR = UUID.randomUUID();
    private static final UUID ADMIN = UUID.randomUUID();

    private Denuncia denuncia(UUID autorPerfilId, StatusDenuncia status) {
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
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(denuncia(AUTOR, StatusDenuncia.PENDENTE)));

        assertThatThrownBy(() -> denunciaService.buscarPorId(ID, OUTRO_AUTOR))
                .isInstanceOf(EntidadeNaoEncontradaException.class);
    }

    @Test
    void buscarDenunciaInexistenteDaNaoEncontrada() {
        when(denunciaRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> denunciaService.buscarPorId(ID, AUTOR))
                .isInstanceOf(EntidadeNaoEncontradaException.class);
    }

    @Test
    void atualizarDenunciaJaJulgadaNaoPode() {
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(denuncia(AUTOR, StatusDenuncia.PROCEDENTE)));
        DenunciaAtualizacaoRequest request = new DenunciaAtualizacaoRequest("Novo motivo", null);

        assertThatThrownBy(() -> denunciaService.atualizar(ID, AUTOR, request))
                .isInstanceOf(RegraNegocioException.class);
        verify(denunciaRepository, never()).saveAndFlush(any());
    }

    @Test
    void excluirDenunciaJaJulgadaNaoPode() {
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(denuncia(AUTOR, StatusDenuncia.IMPROCEDENTE)));

        assertThatThrownBy(() -> denunciaService.excluir(ID, AUTOR))
                .isInstanceOf(RegraNegocioException.class);
        verify(denunciaRepository, never()).delete(any());
    }

    @Test
    void analisarDenunciaJaJulgadaNaoPode() {
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(denuncia(AUTOR, StatusDenuncia.PROCEDENTE)));
        DenunciaAnaliseRequest request = new DenunciaAnaliseRequest(StatusDenuncia.IMPROCEDENTE, "Mudei de ideia");

        assertThatThrownBy(() -> denunciaService.analisar(ID, ADMIN, request))
                .isInstanceOf(RegraNegocioException.class);
        verify(denunciaRepository, never()).saveAndFlush(any());
    }


    @Test
    void criarGuardaIdDoAlvoPadronizado() {
        when(denunciaRepository.save(any(Denuncia.class))).thenAnswer(chamada -> chamada.getArgument(0));
        DenunciaRequest request = new DenunciaRequest(TipoAlvoDenuncia.PERFIL, " 4A22E231-8FC5-4E3A-B72F-218E26E1C20C ", "Perfil ofensivo", null, null, null);

        denunciaService.criar(AUTOR, request);

        ArgumentCaptor<Denuncia> salva = ArgumentCaptor.forClass(Denuncia.class);
        verify(denunciaRepository).save(salva.capture());
        assertThat(salva.getValue().getAlvoId()).	isEqualTo("4a22e231-8fc5-4e3a-b72f-218e26e1c20c");
        assertThat(salva.getValue().getAutorPerfilId()).isEqualTo(AUTOR);
    }

    @Test
    void criarRecusaIdDeAlvoInvalido() {
        DenunciaRequest request = new DenunciaRequest(TipoAlvoDenuncia.PERFIL, "abc", "Perfil ofensivo", null, null, null);

        assertThatThrownBy(() -> denunciaService.criar(AUTOR, request))
                .isInstanceOf(RegraNegocioException.class);
        verify(denunciaRepository, never()).save(any());
    }
        @Test
    void atualizarTrocaOTextoEApagaDescricaoNaoEnviada() {
        Denuncia pendente = denuncia(AUTOR, StatusDenuncia.PENDENTE);
        pendente.setDescricao("Descrição antiga");
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(pendente));
        when(denunciaRepository.saveAndFlush(any(Denuncia.class))).thenAnswer(chamada -> chamada.getArgument(0));

        DenunciaResponse resposta = denunciaService.atualizar(ID, AUTOR, new DenunciaAtualizacaoRequest("Novo motivo", null));

        assertThat(resposta.motivo()).isEqualTo("Novo motivo");
        assertThat(resposta.descricao()).isNull();
    }

    @Test
    void excluirApagaDenunciaPendenteDoAutor() {
        Denuncia pendente = denuncia(AUTOR, StatusDenuncia.PENDENTE);
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(pendente));

        denunciaService.excluir(ID, AUTOR);

        verify(denunciaRepository).delete(pendente);
    }

    @Test
    void analisarRegistraResultadoParecerEQuemAnalisou() {
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(denuncia(AUTOR, StatusDenuncia.PENDENTE)));
        when(denunciaRepository.saveAndFlush(any(Denuncia.class))).thenAnswer(chamada -> chamada.getArgument(0));

        DenunciaResponse resposta = denunciaService.analisar(ID, ADMIN,
                new DenunciaAnaliseRequest(StatusDenuncia.PROCEDENTE, "Golpe confirmado"));

        assertThat(resposta.status()).isEqualTo(StatusDenuncia.PROCEDENTE);
        assertThat(resposta.parecer()).isEqualTo("Golpe confirmado");
        assertThat(resposta.analisadoPorPerfilId()).isEqualTo(ADMIN);
        assertThat(resposta.analisadoEm()).isNotNull();
    }

    @Test
    void listarParaAdminSemStatusBuscaTodas() {
        Pageable pagina = PageRequest.of(0, 20);
        when(denunciaRepository.findAll(pagina)).thenReturn(new PageImpl<>(List.of(denuncia(AUTOR, StatusDenuncia.PENDENTE))));

        Page<DenunciaResponse> resposta = denunciaService.listarParaAdmin(ADMIN, null, pagina);

        assertThat(resposta.getTotalElements()).isEqualTo(1);
        verify(denunciaRepository, never()).findByStatus(any(), any());
    }

    @Test
    void listarParaAdminComStatusFiltra() {
        Pageable pagina = PageRequest.of(0, 20);
        when(denunciaRepository.findByStatus(StatusDenuncia.PENDENTE, pagina))
                .thenReturn(new PageImpl<>(List.of(denuncia(AUTOR, StatusDenuncia.PENDENTE))));

        Page<DenunciaResponse> resposta = denunciaService.listarParaAdmin(ADMIN, StatusDenuncia.PENDENTE, pagina);

        assertThat(resposta.getContent()).hasSize(1);
        verify(denunciaRepository, never()).findAll(any(Pageable.class));
    }

}
