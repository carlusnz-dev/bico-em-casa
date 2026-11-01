package br.com.bicoemcasa.api.modulos.servicos;

import br.com.bicoemcasa.api.core.RegistroAuditoria;
import br.com.bicoemcasa.api.core.excecao.*;
import br.com.bicoemcasa.api.modulos.servicos.dto.*;
import br.com.bicoemcasa.api.modulos.servicos.models.*;
import br.com.bicoemcasa.api.modulos.servicos.repository.*;
import br.com.bicoemcasa.api.modulos.servicos.services.*;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.PerfilResponse;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoAdminServiceTest {
    @Mock ServicoRepository servicos;
    @Mock TagRepository tags;
    @Mock PerfilService perfis;
    @Mock RegistroAuditoria auditoria;
    @InjectMocks ServicoAdminServiceImpl admin;
    private final UUID id=UUID.randomUUID(), perfilId=UUID.randomUUID();
    private void perfil(PerfilTipo tipo) {
        when(perfis.buscarPorUsuarioId(1L)).thenReturn(new PerfilResponse(perfilId,1L,tipo,"nome","Nome",null,null,null));
    }
    private Servico servico() {
        Servico s=new Servico(); s.setId(id); s.setPerfilId(perfilId); s.setTitulo("Original"); s.setDescricao("Descrição"); s.setAtivo(true);
        when(servicos.buscarParaAtualizar(id)).thenReturn(Optional.of(s)); return s;
    }
    @Test void todasAsAcoesRecusamClienteAntesDeAcessarDados() {
        perfil(PerfilTipo.CLIENTE);
        assertThatThrownBy(() -> admin.listar(1L,0,20,null,null,"")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> admin.editar(1L,id,null,"127.0.0.1")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> admin.situacao(1L,id,false,"Motivo","127.0.0.1")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> admin.renomear(1L,id,null,"127.0.0.1")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> admin.removerCategoria(1L,id,"127.0.0.1")).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(servicos,tags,auditoria);
    }
    @Test void usuarioSemPerfilRecebeAcessoNegado() {
        when(perfis.buscarPorUsuarioId(1L)).thenThrow(new EntidadeNaoEncontradaException("Perfil não encontrado"));
        assertThatThrownBy(() -> admin.listar(1L,0,20,null,null,"")).isInstanceOf(AccessDeniedException.class);
    }
    @Test void desativacaoAuditaAutorMotivoIpESituacaoAnterior() {
        perfil(PerfilTipo.ADMIN); Servico s=servico();
        admin.situacao(1L,id,false,"Oferta inadequada","127.0.0.1");
        assertThat(s.isAtivo()).isFalse();
        verify(auditoria).registrar(eq(1L),eq("DESATIVAR_SERVICO"),eq("servico"),eq(id.toString()),eq("127.0.0.1"),eq("Oferta inadequada"),argThat(m -> m.get("ativo").equals(true)),argThat(m -> m.get("ativo").equals(false)));
        verify(servicos,never()).delete(any(Servico.class));
    }
    @Test void categoriaVinculadaNaoPodeSerExcluida() {
        perfil(PerfilTipo.ADMIN); Tag t=new Tag(); t.setId(id);
        when(tags.buscarParaAtualizar(id)).thenReturn(Optional.of(t)); when(tags.contarVinculos(id)).thenReturn(3L);
        assertThatThrownBy(() -> admin.removerCategoria(1L,id,"127.0.0.1")).isInstanceOf(ModeracaoConflitoException.class).hasMessageContaining("3 serviço");
        verify(tags,never()).delete(any()); verifyNoInteractions(auditoria);
    }
    @Test void profissionalNaoPodeReativarServicoModerado() {
        Servico s=new Servico(); s.setId(id); s.setPerfilId(perfilId);
        when(servicos.buscarParaAtualizar(id)).thenReturn(Optional.of(s));
        when(perfis.buscarPorUsuarioIdETipo(1L,PerfilTipo.PROFISSIONAL)).thenReturn(new PerfilResponse(perfilId,1L,PerfilTipo.PROFISSIONAL,"nome","Nome",null,null,null));
        when(auditoria.motivoDesativacao(id.toString())).thenReturn("Oferta inadequada");
        var dono=new ServicoServiceImpl(servicos,tags,perfis,auditoria);
        assertThatThrownBy(() -> dono.ativar(id,1L)).isInstanceOf(ModeracaoConflitoException.class);
        assertThat(s.isAtivo()).isFalse(); verify(servicos,never()).save(any());
    }
    @Test void motivoDaDesativacaoExigeDezCaracteresAposTrim() {
        try(var factory=Validation.buildDefaultValidatorFactory()) {
            var validator=factory.getValidator();
            assertThat(validator.validate(new DesativacaoRequest("         curto         "))).isNotEmpty();
            assertThat(validator.validate(new DesativacaoRequest("Oferta inadequada"))).isEmpty();
        }
    }
}
