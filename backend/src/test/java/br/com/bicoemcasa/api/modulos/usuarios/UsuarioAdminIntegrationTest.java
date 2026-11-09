package br.com.bicoemcasa.api.modulos.usuarios;

import br.com.bicoemcasa.api.ChavesRsaDeTeste;
import br.com.bicoemcasa.api.lib.armazenamento.ClienteArmazenamentoS3;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpCookie;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UsuarioAdminIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.6-alpine");

    private static final String ADMIN = "/api/admin/usuarios";
    private static final String LOGIN = "/api/autenticacao/entrar";
    private static final String RENOVAR = "/api/autenticacao/renovar";
    private static final String PERFIL = "/api/perfil/me";
    private static final String SENHA = "senha-exclusiva-do-teste";
    private static final String MOTIVO = "Análise de segurança da conta";

    @MockitoBean ClienteArmazenamentoS3 armazenamento;
    @Autowired MockMvc mvc;
    @Autowired JdbcClient jdbc;
    @Autowired ObjectMapper mapper;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtEncoder jwtEncoder;

    @DynamicPropertySource
    static void chavesDeTeste(DynamicPropertyRegistry props) throws GeneralSecurityException, IOException {
        ChavesRsaDeTeste.configurar(props);
    }

    @Test
    void nenhumCadastroPublicoAceitaAdministrador() throws Exception {
        var cadastro = dadosAdministrador();
        var email = cadastro.get("email");
        mvc.perform(json(post("/api/autenticacao/cadastrar"), Map.of(
                "nomeCompleto", cadastro.get("nomeCompleto"), "email", email,
                "cpf", cadastro.get("cpf"), "senha", SENHA, "cadastroTipo", "ADMIN",
                "nomeUsuario", cadastro.get("nomeUsuario"), "nomeExibicao", cadastro.get("nomeExibicao"),
                "telefone", "41999999999"
        ))).andExpect(status().isForbidden());
        mvc.perform(json(post("/api/usuario"), Map.of(
                "nome", cadastro.get("nomeCompleto"), "email", email, "cpf", cadastro.get("cpf"), "senha", SENHA,
                "perfil", Map.of("tipo", "ADMIN", "nomeUsuario", cadastro.get("nomeUsuario"),
                        "nomeExibicao", cadastro.get("nomeExibicao"))
        ))).andExpect(status().isForbidden());
        assertThat(jdbc.sql("SELECT count(*) FROM usuario WHERE email = :email")
                .param("email", email).query(Long.class).single()).isZero();
    }

    @Test
    void somenteAdministradorAtivoPodeProvisionarEListar() throws Exception {
        var admin = inserirConta("ADMIN");
        var cliente = inserirConta("CLIENTE");
        var dados = dadosAdministrador();
        mvc.perform(json(post(ADMIN), dados)).andExpect(status().isUnauthorized());
        mvc.perform(json(post(ADMIN), dados).header(HttpHeaders.AUTHORIZATION, bearer(entrar(cliente).accessToken())))
                .andExpect(status().isForbidden());

        var sessaoAdmin = entrar(admin);
        mvc.perform(json(post(ADMIN), dados).header(HttpHeaders.AUTHORIZATION, bearer(sessaoAdmin.accessToken())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.papeis[0]").value("ADMIN"))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.senha").doesNotExist());
        mvc.perform(get(ADMIN).header(HttpHeaders.AUTHORIZATION, bearer(sessaoAdmin.accessToken()))
                        .param("ativo", "true").param("busca", dados.get("email")).param("tamanho", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.tamanho").value(50));
        mvc.perform(json(post(LOGIN), Map.of("email", dados.get("email"), "senha", SENHA)))
                .andExpect(status().isOk());
        var duplicado = dadosAdministrador();
        mvc.perform(json(post("/api/usuario"), Map.of(
                "nome", duplicado.get("nomeCompleto"), "email", dados.get("email"),
                "cpf", duplicado.get("cpf"), "senha", SENHA, "perfil", Map.of(
                        "tipo", "CLIENTE", "nomeUsuario", duplicado.get("nomeUsuario"),
                        "nomeExibicao", duplicado.get("nomeExibicao"))
        ))).andExpect(status().isConflict());
    }

    @Test
    void suspensaoInvalidaLoginRefreshEAccessMesmoAposReativacao() throws Exception {
        var admin = inserirConta("ADMIN");
        var cliente = inserirConta("CLIENTE");
        var tokenAdmin = bearer(entrar(admin).accessToken());
        var sessaoAnterior = entrar(cliente);
        var jwtAnterior = bearer(sessaoAnterior.accessToken());
        var jwtLegado = bearer(jwtSemVersao(cliente.id()));
        mvc.perform(get(PERFIL).header(HttpHeaders.AUTHORIZATION, jwtLegado)).andExpect(status().isOk());

        mvc.perform(json(patch(ADMIN + "/" + cliente.id() + "/suspender"), Map.of("motivo", " curto "))
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(status().isBadRequest());
        mvc.perform(json(patch(ADMIN + "/" + cliente.id() + "/suspender"), Map.of("motivo", " "))
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erros.motivo").exists());
        mvc.perform(json(patch(ADMIN + "/" + admin.id() + "/suspender"), Map.of("motivo", MOTIVO))
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(status().isConflict());
        alterarSituacao(tokenAdmin, cliente.id(), "suspender");
        mvc.perform(get(ADMIN).header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                        .param("ativo", "false").param("busca", cliente.email()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElementos").value(1));
        mvc.perform(get(PERFIL).header(HttpHeaders.AUTHORIZATION, jwtAnterior)).andExpect(status().isUnauthorized());
        mvc.perform(json(post(LOGIN), Map.of("email", cliente.email(), "senha", SENHA)))
                .andExpect(status().isUnauthorized());
        renovar(sessaoAnterior).andExpect(status().isBadRequest());

        alterarSituacao(tokenAdmin, cliente.id(), "reativar");
        mvc.perform(get(PERFIL).header(HttpHeaders.AUTHORIZATION, jwtAnterior)).andExpect(status().isUnauthorized());
        mvc.perform(get(PERFIL).header(HttpHeaders.AUTHORIZATION, jwtLegado)).andExpect(status().isUnauthorized());
        renovar(sessaoAnterior).andExpect(status().isBadRequest());
        var novaSessao = entrar(cliente);
        mvc.perform(get(PERFIL).header(HttpHeaders.AUTHORIZATION, bearer(novaSessao.accessToken())))
                .andExpect(status().isOk());
        renovar(novaSessao).andExpect(status().isOk());

        assertThat(jdbc.sql("""
                SELECT acao || ':' || (detalhes ->> 'motivo')
                FROM log_acao WHERE autor_usuario_id = :autor AND alvo_tipo = 'usuario' AND alvo_id = :alvo
                """).param("autor", admin.id()).param("alvo", cliente.id().toString()).query(String.class).list())
                .containsExactlyInAnyOrder("SUSPENDER_USUARIO:" + MOTIVO, "REATIVAR_USUARIO:" + MOTIVO);
    }

    private void alterarSituacao(String tokenAdmin, Long id, String acao) throws Exception {
        mvc.perform(json(patch(ADMIN + "/" + id + "/" + acao), Map.of("motivo", MOTIVO))
                        .header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(status().isNoContent());
    }

    private org.springframework.test.web.servlet.ResultActions renovar(Sessao sessao) throws Exception {
        return mvc.perform(post(RENOVAR).cookie(new Cookie("refreshToken", sessao.refreshToken())));
    }

    private Conta inserirConta(String papel) {
        var email = UUID.randomUUID() + "@exemplo.test";
        var id = jdbc.sql("""
                INSERT INTO usuario (nome, email, cpf, hash_senha, ativo, email_verificacao, criado_em)
                VALUES (:nome, :email, :cpf, :hash, true, false, now()) RETURNING id
                """).param("nome", "Conta " + papel).param("email", email).param("cpf", cpf())
                .param("hash", encoder.encode(SENHA)).query(Long.class).single();
        jdbc.sql("""
                INSERT INTO perfil (id, usuario_id, tipo, nome_usuario, nome_exibicao, criado_em, atualizado_em)
                VALUES (gen_random_uuid(), :usuario, :papel, :nome, :nome, now(), now())
                """).param("usuario", id).param("papel", papel).param("nome", "conta-" + id).update();
        return new Conta(id, email);
    }

    private Sessao entrar(Conta conta) throws Exception {
        var resposta = mvc.perform(json(post(LOGIN), Map.of("email", conta.email(), "senha", SENHA)))
                .andExpect(status().isOk()).andReturn().getResponse();
        String access = mapper.readTree(resposta.getContentAsString()).get("accessToken").asText();
        String refresh = HttpCookie.parse(resposta.getHeader(HttpHeaders.SET_COOKIE)).getFirst().getValue();
        return new Sessao(access, refresh);
    }

    private String jwtSemVersao(Long usuario) {
        Instant agora = Instant.now();
        var claims = JwtClaimsSet.builder().subject(usuario.toString()).issuedAt(agora)
                .expiresAt(agora.plusSeconds(300)).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private Map<String, String> dadosAdministrador() {
        var sufixo = UUID.randomUUID().toString();
        return Map.of("nomeCompleto", "Nova Pessoa Administradora", "email", sufixo + "@exemplo.test",
                "cpf", cpf(), "senha", SENHA, "nomeUsuario", "admin-" + sufixo, "nomeExibicao", "Nova Admin");
    }

    private String cpf() {
        return Long.toString(ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L));
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, Object body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body));
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private record Conta(Long id, String email) {}
    private record Sessao(String accessToken, String refreshToken) {}
}
