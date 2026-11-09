package br.com.bicoemcasa.api.modulos.profissionais;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioMidiaRepository extends JpaRepository<PortfolioMidia, UUID> {
    List<PortfolioMidia> findByPortfolioIdOrderByCriadoEmAscIdAsc(Long portfolioId);
    Optional<PortfolioMidia> findByIdAndPortfolioId(UUID id, Long portfolioId);
    Optional<PortfolioMidia> findByPortfolioIdAndChave(Long portfolioId, String chave);
    long countByPortfolioId(Long portfolioId);
}
