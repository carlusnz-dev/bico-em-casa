package br.com.bicoemcasa.api.modulos.profissionais;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Optional<Portfolio> findByPerfilId(UUID perfilId);
    Page<Portfolio> findByPerfilIdIn(List<UUID> perfis, Pageable pageable);
    boolean existsBySlugUrl(String slugUrl);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Portfolio p where p.id = :id")
    Optional<Portfolio> buscarParaAtualizar(Long id);
}
