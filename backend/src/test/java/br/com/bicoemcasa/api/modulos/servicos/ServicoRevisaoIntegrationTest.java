package br.com.bicoemcasa.api.modulos.servicos;

import br.com.bicoemcasa.api.lib.armazenamento.ClienteArmazenamentoS3;
import br.com.bicoemcasa.api.modulos.servicos.contrato.ServicoAdminService;
import br.com.bicoemcasa.api.modulos.servicos.contrato.ServicoService;
import br.com.bicoemcasa.api.modulos.servicos.dto.PrecoMedioResponse;
import br.com.bicoemcasa.api.modulos.servicos.dto.ServicoRequest;
import br.com.bicoemcasa.api.modulos.servicos.models.UnidadePreco;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Transactional
class ServicoRevisaoIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.6-alpine");

    @MockitoBean JwtEncoder encoder;
    @MockitoBean JwtDecoder decoder;
    @MockitoBean ClienteArmazenamentoS3 armazenamento;
    @Autowired ServicoAdminService admin;
    @Autowired ServicoService servicos;
    @Autowired PerfilService perfis;
    @Autowired JdbcClient jdbc;
    @Autowired EntityManager em;

    private Long administrador;
    private Long profissional;
    private UUID categoria;
    private String slug;

    @BeforeEach
    void preparar() {
        administrador = criarUsuario("ADMIN", "Curitiba");
        profissional = criarUsuario("PROFISSIONAL", "Curitiba");
        categoria = jdbc.sql("select id from tag order by slug limit 1").query(UUID.class).single();
        slug = jdbc.sql("select slug from tag where id = :id").param("id", categoria).query(String.class).single();
    }

    @Test
    void publicaAoCadastrarERepublicaSomenteAoAprovarReenvio() {
        var primeiro = servicos.criar(oferta("Instalação elétrica", "100.00", UnidadePreco.SERVICO), profissional);
        var segundo = servicos.criar(oferta("Limpeza elétrica", "120.00", UnidadePreco.SERVICO), profissional);
        em.flush();
        assertThat(primeiro.ativo()).isTrue();
        assertThat(admin.listarPendentes(administrador, 0, 20).conteudo())
                .extracting(item -> item.servico().id()).containsExactly(primeiro.id(), segundo.id());

        admin.revisar(administrador, primeiro.id(), false, "Revisar descrição da oferta", "127.0.0.1");
        em.flush();
        assertThat(servicos.buscarMeu(primeiro.id(), profissional).statusRevisao()).isEqualTo("REPROVADO");
        assertThat(servicos.buscarMeu(primeiro.id(), profissional).motivoReprovacao()).contains("descrição");
        assertThat(perfis.listarAvisos(profissional)).singleElement()
                .satisfies(aviso -> assertThat(aviso.titulo()).isEqualTo("Serviço reprovado na revisão"));
        assertThat(servicos.listarAtivos(0, 20, null).conteudo()).extracting(item -> item.id())
                .containsExactly(segundo.id());

        var corrigido = servicos.reenviar(primeiro.id(),
                oferta("Instalação revisada", "105.00", UnidadePreco.SERVICO), profissional, "127.0.0.1");
        assertThat(corrigido.ativo()).isFalse();
        assertThat(corrigido.statusRevisao()).isEqualTo("PENDENTE");

        admin.revisar(administrador, primeiro.id(), true, "Aprovado após correção", "127.0.0.1");
        em.flush();
        var aprovado = servicos.buscarMeu(primeiro.id(), profissional);
        assertThat(aprovado.ativo()).isTrue();
        assertThat(aprovado.statusRevisao()).isEqualTo("APROVADO");
        assertThat(aprovado.motivoReprovacao()).isNull();
        assertThat(admin.listarPendentes(administrador, 0, 20).conteudo())
                .extracting(item -> item.servico().id()).containsExactly(segundo.id());
    }

    @Test
    void filtraCatalogoEMantemMediasSeparadasPorUnidadeEProfissional() {
        var eletrica = servicos.criar(oferta("Instalação elétrica", "100.00", UnidadePreco.SERVICO), profissional);
        servicos.criar(oferta("Limpeza", "150.00", UnidadePreco.SERVICO), profissional);
        servicos.criar(oferta("Visita técnica", "70.00", UnidadePreco.HORA), profissional);
        Long outro = criarUsuario("PROFISSIONAL", "Londrina");
        servicos.criar(oferta("Instalação elétrica", "200.00", UnidadePreco.SERVICO), outro);
        var inativo = servicos.criar(oferta("Instalação elétrica", "1000.00", UnidadePreco.SERVICO), outro);
        servicos.desativar(inativo.id(), outro);
        em.flush();

        assertThat(servicos.listarAtivos(0, 20, "Curitiba", slug, new BigDecimal("90"), new BigDecimal("120"),
                "ELETRICA").conteudo()).extracting(item -> item.id()).containsExactly(eletrica.id());
        assertThat(servicos.calcularPrecosMedios(slug)).containsExactlyInAnyOrder(
                media(UnidadePreco.SERVICO, "150.00", 3L), media(UnidadePreco.HORA, "70.00", 1L));
        assertThat(servicos.calcularOrcamentoMedio(profissional)).containsExactlyInAnyOrder(
                media(UnidadePreco.SERVICO, "125.00", 2L), media(UnidadePreco.HORA, "70.00", 1L));
    }

    private PrecoMedioResponse media(UnidadePreco unidade, String valor, Long quantidade) {
        return new PrecoMedioResponse(unidade, new BigDecimal(valor), quantidade);
    }

    private ServicoRequest oferta(String titulo, String preco, UnidadePreco unidade) {
        return new ServicoRequest(titulo, "Descrição da oferta", new BigDecimal(preco), unidade, Set.of(categoria));
    }

    private Long criarUsuario(String tipo, String cidade) {
        String identificador = UUID.randomUUID().toString();
        Long usuario = jdbc.sql("""
                insert into usuario(nome, email, email_verificacao, hash_senha, cpf, ativo)
                values (:nome, :email, false, 'hash', :cpf, true) returning id
                """).param("nome", tipo).param("email", identificador + "@teste.local")
                .param("cpf", identificador.substring(0, 11)).query(Long.class).single();
        Long endereco = jdbc.sql("""
                insert into endereco(logradouro, bairro, cidade, uf, criado_em, atualizado_em)
                values ('Rua teste', 'Centro', :cidade, 'PR', now(), now()) returning id
                """).param("cidade", cidade).query(Long.class).single();
        jdbc.sql("""
                insert into perfil(id, usuario_id, tipo, nome_usuario, nome_exibicao, endereco_id, criado_em, atualizado_em)
                values (gen_random_uuid(), :usuario, :tipo, :nome, :nome, :endereco, now(), now())
                """).param("usuario", usuario).param("tipo", tipo).param("nome", identificador)
                .param("endereco", endereco).update();
        return usuario;
    }
}
