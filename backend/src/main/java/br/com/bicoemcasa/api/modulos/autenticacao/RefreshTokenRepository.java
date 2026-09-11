package br.com.bicoemcasa.api.modulos.autenticacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    List<RefreshToken> findByUsuarioId(Long usuarioId);

    List<RefreshToken> findByFamiliaId(UUID familiaId);
    Optional<RefreshToken> findByHashToken(String hashToken);
}
