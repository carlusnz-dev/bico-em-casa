package br.com.bicoemcasa.api.modulos.servicos;

import br.com.bicoemcasa.api.core.RegistroAuditoria;
import br.com.bicoemcasa.api.core.excecao.ModeracaoConflitoException;
import br.com.bicoemcasa.api.lib.armazenamento.ClienteArmazenamentoS3;
import br.com.bicoemcasa.api.modulos.servicos.contrato.*;
import br.com.bicoemcasa.api.modulos.servicos.dto.*;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ServicoAdminIntegrationTest {
    @Container @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.6-alpine");
    @MockitoBean JwtEncoder encoder;
    @MockitoBean JwtDecoder decoder;
    @MockitoBean ClienteArmazenamentoS3 armazenamento;
    @Autowired ServicoAdminService admin;
    @Autowired ServicoService servicos;
    @Autowired PerfilService perfis;
    @Autowired RegistroAuditoria auditoria;
    @Autowired JdbcClient jdbc;
    @Autowired EntityManager em;
    @Autowired MockMvc mvc;
    UUID profissional, categoria, servico;
    Long administrador, dono, cliente;

    Long usuario(String nome) {
        return jdbc.sql("insert into usuario(nome,email,hash_senha,cpf,ativo,email_verificacao) values(:nome,:email,'hash',:cpf,true,false) returning id")
            .param("nome",nome).param("email",UUID.randomUUID()+"@teste.local").param("cpf",UUID.randomUUID().toString().substring(0,11)).query(Long.class).single();
    }
    UUID perfil(Long usuario, String tipo) {
        return jdbc.sql("insert into perfil(id,usuario_id,tipo,nome_usuario,nome_exibicao,criado_em,atualizado_em) values(gen_random_uuid(),:usuario,:tipo,:nome,:nome,now(),now()) returning id")
            .param("usuario",usuario).param("tipo",tipo).param("nome",UUID.randomUUID().toString()).query(UUID.class).single();
    }
    UUID oferta(String titulo, boolean ativo) {
        return jdbc.sql("insert into servico(id,perfil_id,titulo,descricao,preco_previo,unidade_preco,ativo,criado_em,atualizado_em) values(gen_random_uuid(),:perfil,:titulo,'Descrição',100,'SERVICO',:ativo,clock_timestamp(),now()) returning id")
            .param("perfil",profissional).param("titulo",titulo).param("ativo",ativo).query(UUID.class).single();
    }
    @BeforeEach void preparar() {
        administrador=usuario("Administrador"); perfil(administrador,"ADMIN");
        dono=usuario("João Elétrico"); profissional=perfil(dono,"PROFISSIONAL");
        cliente=usuario("Cliente"); perfil(cliente,"CLIENTE");
        categoria=jdbc.sql("select id from tag limit 1").query(UUID.class).single();
        servico=oferta("Instalação elétrica",true);
        jdbc.sql("insert into servico_tag values(:servico,:tag)").param("servico",servico).param("tag",categoria).update();
    }
    @Test void paginaFiltraNomeTituloCategoriaESituacao() {
        oferta("Outro serviço",false);
        assertThat(admin.listar(administrador,0,100,null,null,"").tamanho()).isEqualTo(50);
        assertThat(admin.listar(administrador,0,1,null,null,"").totalPaginas()).isEqualTo(2);
        assertThat(admin.listar(administrador,0,20,true,categoria,"INSTALACAO").totalElementos()).isEqualTo(1);
        assertThat(admin.listar(administrador,0,20,null,null,"JOAO ELETRICO").totalElementos()).isEqualTo(2);
        assertThat(admin.listar(administrador,0,20,false,categoria,"").totalElementos()).isZero();
    }
    @Test void desativacaoPreservaContratacaoEBloqueiaReativacaoPeloDono() {
        var contrato=jdbc.sql("insert into contratacao(id,servico_id,cliente_id,profissional_id,titulo_servico,preco_servico,status,criado_em,atualizado_em) values(gen_random_uuid(),:servico,:cliente,:profissional,'Instalação',100,'EM_ANDAMENTO',now(),now()) returning id")
            .param("servico",servico).param("cliente",perfis.buscarPorUsuarioId(cliente).id()).param("profissional",profissional).query(UUID.class).single();
        admin.situacao(administrador,servico,false,"Oferta em desacordo com as regras","127.0.0.1");
        em.flush();
        assertThat(servicos.listarAtivos(0,20,null).conteudo()).isEmpty();
        assertThat(jdbc.sql("select status from contratacao where id=:id").param("id",contrato).query(String.class).single()).isEqualTo("EM_ANDAMENTO");
        assertThat(servicos.listarMeus(dono,0,20).conteudo().getFirst().motivoModeracao()).contains("desacordo");
        assertThatThrownBy(() -> servicos.ativar(servico,dono)).isInstanceOf(ModeracaoConflitoException.class);
    }
    @Test void reativacaoAuditadaRestauraOferta() {
        admin.situacao(administrador,servico,false,"Oferta inadequada para o site","127.0.0.1");
        admin.situacao(administrador,servico,true,"Análise revista","127.0.0.1");
        em.flush();
        assertThat(servicos.listarAtivos(0,20,null).totalElementos()).isEqualTo(1);
        assertThat(auditoria.motivoDesativacao(servico.toString())).isNull();
        assertThat(jdbc.sql("select count(*) from log_acao where alvo_id=:id").param("id",servico.toString()).query(Long.class).single()).isEqualTo(2);
    }
    @Test void correcaoGravaAntesDepoisENotificaProfissional() {
        admin.editar(administrador,servico,new ServicoModeracaoRequest("Título corrigido","Descrição corrigida",Set.of(categoria),"Correção de informação"),"127.0.0.1");
        em.flush();
        assertThat(servicos.buscarPorId(servico).titulo()).isEqualTo("Título corrigido");
        assertThat(perfis.listarAvisos(dono)).hasSize(1);
        assertThat(jdbc.sql("select detalhes->'anterior'->>'titulo' from log_acao where alvo_id=:id").param("id",servico.toString()).query(String.class).single()).isEqualTo("Instalação elétrica");
        assertThat(jdbc.sql("select detalhes->'novo'->>'titulo' from log_acao where alvo_id=:id").param("id",servico.toString()).query(String.class).single()).isEqualTo("Título corrigido");
    }
    @Test void renomearPreservaVinculosESlug() {
        String slug=jdbc.sql("select slug from tag where id=:id").param("id",categoria).query(String.class).single();
        admin.renomear(administrador,categoria,new TagRequest("Nome corrigido"),"127.0.0.1");
        assertThat(admin.listar(administrador,0,20,null,categoria,"").totalElementos()).isEqualTo(1);
        assertThat(jdbc.sql("select slug from tag where id=:id").param("id",categoria).query(String.class).single()).isEqualTo(slug);
        assertThatThrownBy(() -> admin.removerCategoria(administrador,categoria,"127.0.0.1")).isInstanceOf(ModeracaoConflitoException.class).hasMessageContaining("1 serviço");
    }
    @Test void duplicidadeIgnoraCaixaEEspacos() {
        String outroNome=jdbc.sql("select nome from tag where id<>:id limit 1").param("id",categoria).query(String.class).single();
        assertThatThrownBy(() -> admin.renomear(administrador,categoria,new TagRequest(" "+outroNome.toUpperCase(Locale.ROOT)+" "),"127.0.0.1"))
            .isInstanceOf(ModeracaoConflitoException.class);
    }
    @Test void autenticacaoAutorizacaoEValidacaoHttp() throws Exception {
        mvc.perform(get("/api/admin/servicos")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/servicos").with(jwt().jwt(j -> j.subject(cliente.toString()))))
            .andExpect(status().isForbidden()).andExpect(content().contentTypeCompatibleWith("application/problem+json"));
        mvc.perform(patch("/api/admin/servicos/"+servico+"/desativar").with(jwt().jwt(j -> j.subject(administrador.toString())))
            .contentType("application/json").content("{\"motivo\":\"curto\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erros.motivo").exists());
        assertThat(servicos.buscarPorId(servico).ativo()).isTrue();
    }
}
