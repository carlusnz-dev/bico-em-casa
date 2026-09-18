package br.com.bicoemcasa.api.modulos.profissionais;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Optional<Portfolio> findByPerfilId(UUID perfilId);
    boolean existsBySlugUrl(String slugUrl);
}
