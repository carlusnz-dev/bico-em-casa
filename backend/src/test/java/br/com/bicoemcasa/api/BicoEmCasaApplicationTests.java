package br.com.bicoemcasa.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Teste de fumaça da fundação do backend.
 *
 * <p>Não testa regra de negócio nenhuma — não há entidade nem migration ainda. Ele prova que a
 * combinação de dependências do {@code pom.xml} funciona de ponta a ponta: o contexto do Spring
 * Boot 4.1 sobe, o driver JDBC do PostgreSQL conecta no container, o Testcontainers 2.0 sobe o
 * container corretamente e o Flyway 12.4.0 não recusa o PostgreSQL 18 (mesmo sem nenhuma
 * migration a aplicar).
 */
@Testcontainers
@SpringBootTest
class BicoEmCasaApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres =
			new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.6-alpine"));

	@Test
	void contextoSobeComPostgresReal() {
	}
}
