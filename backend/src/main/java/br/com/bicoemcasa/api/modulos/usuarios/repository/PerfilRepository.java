package br.com.bicoemcasa.api.modulos.usuarios.repository;

import br.com.bicoemcasa.api.modulos.usuarios.models.Perfil;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, UUID> {
    Optional<Perfil> findByUsuarioIdAndTipo(Long usuarioId, PerfilTipo tipo);

    boolean existsByNomeUsuario(String nomeUsuario);
}
