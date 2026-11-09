package br.com.bicoemcasa.api.modulos.usuarios;

import br.com.bicoemcasa.api.lib.armazenamento.ClienteArmazenamentoS3;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class CadastroPublicoIntegrationTest {
    @Container @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.6-alpine");

    @MockitoBean JwtEncoder encoder;
    @MockitoBean JwtDecoder decoder;
    @MockitoBean ClienteArmazenamentoS3 armazenamento;
    @Autowired MockMvc mvc;
    @Autowired JdbcClient jdbc;

    @Test
    void asDuasRotasPublicasRecusamAdministrador() throws Exception {
        mvc.perform(post("/api/autenticacao/cadastrar").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "nomeCompleto":"Conta de teste","nomeUsuario":"contateste","nomeExibicao":"Conta de teste",
                  "email":"cadastro-publico@exemplo.test","telefone":"41999999999",
                  "cadastroTipo":"ADMIN","cpf":"12345678901","senha":"senha-de-teste"
                }
                """)).andExpect(status().isForbidden());

        mvc.perform(post("/api/usuario").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "nome":"Conta de teste","email":"cadastro-publico@exemplo.test","cpf":"12345678901",
                  "senha":"senha-de-teste","perfil":{"tipo":"ADMIN","nomeUsuario":"contateste","nomeExibicao":"Conta de teste"}
                }
                """)).andExpect(status().isForbidden());

        assertThat(jdbc.sql("select count(*) from usuario where email = 'cadastro-publico@exemplo.test'")
                .query(Long.class).single()).isZero();
    }
}
