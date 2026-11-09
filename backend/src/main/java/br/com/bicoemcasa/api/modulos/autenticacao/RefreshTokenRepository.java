package br.com.bicoemcasa.api.modulos.autenticacao;

import br.com.bicoemcasa.api.modulos.autenticacao.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    List<RefreshToken> findByUsuarioId(Long usuarioId);

    List<RefreshToken> findByFamiliaId(UUID familiaId);
    Optional<RefreshToken> findByHashToken(String hashToken);

    @Query("SELECT t.usuarioId FROM RefreshToken t WHERE t.hashToken = :hashToken")
    Optional<Long> buscarUsuarioIdPorHashToken(String hashToken);

    @Modifying
    @Query("""
            UPDATE RefreshToken t SET t.revogadoEm = :agora
            WHERE t.usuarioId = :usuarioId AND t.revogadoEm IS NULL
            """)
    void revogarPorUsuario(Long usuarioId, OffsetDateTime agora);
}
