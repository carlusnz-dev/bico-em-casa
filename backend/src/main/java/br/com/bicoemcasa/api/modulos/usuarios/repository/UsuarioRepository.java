package br.com.bicoemcasa.api.modulos.usuarios.repository;

import br.com.bicoemcasa.api.modulos.usuarios.dominio.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findAllByAtivo();

    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
}
