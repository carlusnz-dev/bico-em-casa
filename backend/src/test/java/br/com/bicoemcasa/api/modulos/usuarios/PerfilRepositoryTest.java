package br.com.bicoemcasa.api.modulos.usuarios;

import br.com.bicoemcasa.api.modulos.usuarios.models.Endereco;
import br.com.bicoemcasa.api.modulos.usuarios.models.Perfil;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import br.com.bicoemcasa.api.modulos.usuarios.repository.EnderecoRepository;
import br.com.bicoemcasa.api.modulos.usuarios.repository.PerfilRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@Testcontainers
class PerfilRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.6-alpine"));

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Test
    void buscaPorTrechoDaCidadeSemDiferenciarMaiusculasEMantemApenasProfissionais() {
        UUID profissionalEmSaoPaulo = criarPerfil(PerfilTipo.PROFISSIONAL, "São Paulo");
        criarPerfil(PerfilTipo.CLIENTE, "São Paulo");
        criarPerfil(PerfilTipo.PROFISSIONAL, "Campinas");

        assertThat(perfilRepository.buscarIdsPorTipoECidadeContendo(
                PerfilTipo.PROFISSIONAL,
                "PAUL"
        )).containsExactly(profissionalEmSaoPaulo);
    }

    private UUID criarPerfil(PerfilTipo tipo, String cidade) {
        Endereco endereco = new Endereco();
        endereco.setLogradouro("Rua de teste");
        endereco.setBairro("Centro");
        endereco.setCidade(cidade);
        endereco.setUf("SP");
        enderecoRepository.save(endereco);

        Perfil perfil = new Perfil();
        perfil.setTipo(tipo);
        perfil.setNomeUsuario(UUID.randomUUID().toString());
        perfil.setNomeExibicao("Perfil de teste");
        perfil.setEndereco(endereco);
        return perfilRepository.saveAndFlush(perfil).getId();
    }
}
