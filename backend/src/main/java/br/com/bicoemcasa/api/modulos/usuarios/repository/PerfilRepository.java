package br.com.bicoemcasa.api.modulos.usuarios.repository;

import br.com.bicoemcasa.api.modulos.usuarios.models.Perfil;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, UUID> {
    Optional<Perfil> findByUsuarioId(Long usuarioId);

    Optional<Perfil> findByUsuarioIdAndTipo(Long usuarioId, PerfilTipo tipo);

    Optional<Perfil> findByNomeExibicao(String nomeExibicao);

    boolean existsByNomeUsuario(String nomeUsuario);

    @Query("""
            SELECT perfil.id
            FROM Perfil perfil
            WHERE perfil.tipo = :tipo
              AND perfil.endereco IS NOT NULL
              AND LOCATE(LOWER(:cidade), LOWER(perfil.endereco.cidade)) > 0
            """)
    List<UUID> buscarIdsPorTipoECidadeContendo(
            @Param("tipo") PerfilTipo tipo,
            @Param("cidade") String cidade
    );
}
