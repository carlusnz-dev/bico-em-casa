package br.com.bicoemcasa.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Teste de fumaça da fundação do backend.
 *
 * <p>Sobe o contexto completo com as migrations reais do Flyway em PostgreSQL 18.
 */
@Testcontainers
@SpringBootTest(properties = {
		"armazenamento.endpoint=http://127.0.0.1:9000",
		"armazenamento.access-key=chave-de-teste",
		"armazenamento.secret-key=segredo-de-teste"
})
class BicoEmCasaApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres =
			new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.6-alpine"));

	@DynamicPropertySource
	static void chavesRsa(DynamicPropertyRegistry props) throws GeneralSecurityException, IOException {
		ChavesRsaDeTeste.configurar(props);
	}

	@Test
	void contextoSobeComPostgresReal() {
	}
}
