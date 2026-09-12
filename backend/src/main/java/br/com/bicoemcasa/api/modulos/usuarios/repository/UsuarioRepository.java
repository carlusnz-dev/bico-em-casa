package br.com.bicoemcasa.api.modulos.usuarios.repository;

import br.com.bicoemcasa.api.modulos.usuarios.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByNome(String nome);

    @Query("SELECT u.ativo FROM Usuario u WHERE u.id = :id")
    Optional<Boolean> findAtivoById(Long id);

    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
}
