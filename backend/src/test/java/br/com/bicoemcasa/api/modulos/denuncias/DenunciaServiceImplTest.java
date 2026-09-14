package br.com.bicoemcasa.api.modulos.denuncias;

import br.com.bicoemcasa.api.core.excecao.DenunciaNaoPendenteException;
import br.com.bicoemcasa.api.core.excecao.DenunciaNaoPertenceAoPerfilException;
import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.modulos.denuncias.dto.DenunciaAnaliseRequest;
import br.com.bicoemcasa.api.modulos.denuncias.dto.DenunciaAtualizacaoRequest;
import br.com.bicoemcasa.api.modulos.denuncias.dto.DenunciaRequest;
import br.com.bicoemcasa.api.modulos.denuncias.dto.DenunciaResponse;
import br.com.bicoemcasa.api.modulos.denuncias.models.Denuncia;
import br.com.bicoemcasa.api.modulos.denuncias.models.StatusDenuncia;
import br.com.bicoemcasa.api.modulos.denuncias.models.TipoAlvoDenuncia;
import br.com.bicoemcasa.api.modulos.denuncias.repository.DenunciaRepository;
import br.com.bicoemcasa.api.modulos.denuncias.service.DenunciaServiceImpl;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.PerfilResponse;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DenunciaServiceImplTest {

    @Mock
    private DenunciaRepository denunciaRepository;

    @Mock
    private PerfilService perfilService;

    @InjectMocks
    private DenunciaServiceImpl denunciaService;

    private static final UUID ID = UUID.randomUUID();
    private static final UUID AUTOR_PERFIL_ID = UUID.randomUUID();
    private static final UUID OUTRO_PERFIL_ID = UUID.randomUUID();
    private static final UUID ADMIN_PERFIL_ID = UUID.randomUUID();
    private static final Long AUTOR_USUARIO_ID = 1L;
    private static final Long OUTRO_USUARIO_ID = 2L;
    private static final Long ADMIN_USUARIO_ID = 3L;

    private Denuncia denuncia(UUID autorPerfilId, StatusDenuncia status) {
        Denuncia denuncia = new Denuncia();
        denuncia.setId(ID);
        denuncia.setAutorPerfilId(autorPerfilId);
        denuncia.setAlvoTipo(TipoAlvoDenuncia.SERVICO);
        denuncia.setAlvoId(UUID.randomUUID().toString());
        denuncia.setMotivo("Golpe");
        denuncia.setStatus(status);
        return denuncia;
    }

    private PerfilResponse perfil(UUID id) {
        return new PerfilResponse(id, 1L, PerfilTipo.CLIENTE, "usuario", "Usuário", null, null, null);
    }

    @Test
    void buscarDenunciaDeOutroAutorLancaExcecaoDeNaoPertencimento() {
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(denuncia(AUTOR_PERFIL_ID, StatusDenuncia.PENDENTE)));
        when(perfilService.buscarPorUsuarioId(OUTRO_USUARIO_ID)).thenReturn(perfil(OUTRO_PERFIL_ID));

        assertThatThrownBy(() -> denunciaService.buscarPorId(ID, OUTRO_USUARIO_ID))
                .isInstanceOf(DenunciaNaoPertenceAoPerfilException.class);
    }

    @Test
    void buscarDenunciaInexistenteDaNaoEncontrada() {
        when(denunciaRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> denunciaService.buscarPorId(ID, AUTOR_USUARIO_ID))
                .isInstanceOf(EntidadeNaoEncontradaException.class);
    }

    @Test
    void atualizarDenunciaJaJulgadaNaoPode() {
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(denuncia(AUTOR_PERFIL_ID, StatusDenuncia.PROCEDENTE)));
        when(perfilService.buscarPorUsuarioId(AUTOR_USUARIO_ID)).thenReturn(perfil(AUTOR_PERFIL_ID));
        DenunciaAtualizacaoRequest request = new DenunciaAtualizacaoRequest("Novo motivo", null);

        assertThatThrownBy(() -> denunciaService.atualizar(ID, AUTOR_USUARIO_ID, request))
                .isInstanceOf(DenunciaNaoPendenteException.class);
        verify(denunciaRepository, never()).saveAndFlush(any());
    }

    @Test
    void excluirDenunciaJaJulgadaNaoPode() {
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(denuncia(AUTOR_PERFIL_ID, StatusDenuncia.IMPROCEDENTE)));
        when(perfilService.buscarPorUsuarioId(AUTOR_USUARIO_ID)).thenReturn(perfil(AUTOR_PERFIL_ID));

        assertThatThrownBy(() -> denunciaService.excluir(ID, AUTOR_USUARIO_ID))
                .isInstanceOf(DenunciaNaoPendenteException.class);
        verify(denunciaRepository, never()).delete(any());
    }

    @Test
    void analisarDenunciaJaJulgadaNaoPode() {
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(denuncia(AUTOR_PERFIL_ID, StatusDenuncia.PROCEDENTE)));
        lenient().when(perfilService.buscarPorUsuarioIdETipo(ADMIN_USUARIO_ID, PerfilTipo.ADMIN)).thenReturn(perfil(ADMIN_PERFIL_ID));
        DenunciaAnaliseRequest request = new DenunciaAnaliseRequest(StatusDenuncia.IMPROCEDENTE, "Mudei de ideia");

        assertThatThrownBy(() -> denunciaService.analisar(ID, ADMIN_USUARIO_ID, request))
                .isInstanceOf(DenunciaNaoPendenteException.class);
        verify(denunciaRepository, never()).saveAndFlush(any());
    }

    @Test
    void criarGuardaIdDoAlvoPadronizado() {
        when(perfilService.buscarPorUsuarioId(AUTOR_USUARIO_ID)).thenReturn(perfil(AUTOR_PERFIL_ID));
        when(denunciaRepository.save(any(Denuncia.class))).thenAnswer(chamada -> chamada.getArgument(0));
        DenunciaRequest request = new DenunciaRequest(
                TipoAlvoDenuncia.PERFIL, " 4A22E231-8FC5-4E3A-B72F-218E26E1C20C ", "Perfil ofensivo", null, null, null);

        denunciaService.criar(AUTOR_USUARIO_ID, request);

        ArgumentCaptor<Denuncia> salva = ArgumentCaptor.forClass(Denuncia.class);
        verify(denunciaRepository).save(salva.capture());
        assertThat(salva.getValue().getAlvoId()).isEqualTo("4a22e231-8fc5-4e3a-b72f-218e26e1c20c");
        assertThat(salva.getValue().getAutorPerfilId()).isEqualTo(AUTOR_PERFIL_ID);
    }

    @Test
    void requestRecusaIdDeAlvoInvalido() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        DenunciaRequest request = new DenunciaRequest(TipoAlvoDenuncia.PERFIL, "abc", "Perfil ofensivo", null, null, null);

        Set<ConstraintViolation<DenunciaRequest>> violacoes = validator.validate(request);

        assertThat(violacoes).isNotEmpty();
    }

    @Test
    void atualizarTrocaOTextoEApagaDescricaoNaoEnviada() {
        Denuncia pendente = denuncia(AUTOR_PERFIL_ID, StatusDenuncia.PENDENTE);
        pendente.setDescricao("Descrição antiga");
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(pendente));
        when(perfilService.buscarPorUsuarioId(AUTOR_USUARIO_ID)).thenReturn(perfil(AUTOR_PERFIL_ID));
        when(denunciaRepository.saveAndFlush(any(Denuncia.class))).thenAnswer(chamada -> chamada.getArgument(0));

        DenunciaResponse resposta = denunciaService.atualizar(
                ID, AUTOR_USUARIO_ID, new DenunciaAtualizacaoRequest("Novo motivo", null));

        assertThat(resposta.motivo()).isEqualTo("Novo motivo");
        assertThat(resposta.descricao()).isNull();
    }

    @Test
    void excluirApagaDenunciaPendenteDoAutor() {
        Denuncia pendente = denuncia(AUTOR_PERFIL_ID, StatusDenuncia.PENDENTE);
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(pendente));
        when(perfilService.buscarPorUsuarioId(AUTOR_USUARIO_ID)).thenReturn(perfil(AUTOR_PERFIL_ID));

        denunciaService.excluir(ID, AUTOR_USUARIO_ID);

        verify(denunciaRepository).delete(pendente);
    }

    @Test
    void analisarRegistraResultadoParecerEQuemAnalisou() {
        when(denunciaRepository.findById(ID)).thenReturn(Optional.of(denuncia(AUTOR_PERFIL_ID, StatusDenuncia.PENDENTE)));
        when(perfilService.buscarPorUsuarioIdETipo(ADMIN_USUARIO_ID, PerfilTipo.ADMIN)).thenReturn(perfil(ADMIN_PERFIL_ID));
        when(denunciaRepository.saveAndFlush(any(Denuncia.class))).thenAnswer(chamada -> chamada.getArgument(0));

        DenunciaResponse resposta = denunciaService.analisar(ID, ADMIN_USUARIO_ID,
                new DenunciaAnaliseRequest(StatusDenuncia.PROCEDENTE, "Golpe confirmado"));

        assertThat(resposta.status()).isEqualTo(StatusDenuncia.PROCEDENTE);
        assertThat(resposta.parecer()).isEqualTo("Golpe confirmado");
        assertThat(resposta.analisadoPorPerfilId()).isEqualTo(ADMIN_PERFIL_ID);
        assertThat(resposta.analisadoEm()).isNotNull();
    }

    @Test
    void listarParaAdminSemStatusBuscaTodas() {
        Pageable pagina = PageRequest.of(0, 20);
        when(perfilService.buscarPorUsuarioIdETipo(ADMIN_USUARIO_ID, PerfilTipo.ADMIN)).thenReturn(perfil(ADMIN_PERFIL_ID));
        when(denunciaRepository.findAll(pagina)).thenReturn(new PageImpl<>(List.of(denuncia(AUTOR_PERFIL_ID, StatusDenuncia.PENDENTE))));

        Page<DenunciaResponse> resposta = denunciaService.listarParaAdmin(ADMIN_USUARIO_ID, null, pagina);

        assertThat(resposta.getTotalElements()).isEqualTo(1);
        verify(denunciaRepository, never()).findByStatus(any(), any());
    }

    @Test
    void listarParaAdminComStatusFiltra() {
        Pageable pagina = PageRequest.of(0, 20);
        when(perfilService.buscarPorUsuarioIdETipo(ADMIN_USUARIO_ID, PerfilTipo.ADMIN)).thenReturn(perfil(ADMIN_PERFIL_ID));
        when(denunciaRepository.findByStatus(StatusDenuncia.PENDENTE, pagina))
                .thenReturn(new PageImpl<>(List.of(denuncia(AUTOR_PERFIL_ID, StatusDenuncia.PENDENTE))));

        Page<DenunciaResponse> resposta = denunciaService.listarParaAdmin(ADMIN_USUARIO_ID, StatusDenuncia.PENDENTE, pagina);

        assertThat(resposta.getContent()).hasSize(1);
        verify(denunciaRepository, never()).findAll(any(Pageable.class));
    }
}
