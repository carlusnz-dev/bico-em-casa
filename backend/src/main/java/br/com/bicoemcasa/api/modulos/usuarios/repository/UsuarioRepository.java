package br.com.bicoemcasa.api.modulos.usuarios.repository;

import br.com.bicoemcasa.api.modulos.usuarios.models.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByNome(String nome);

    @Query("SELECT u.ativo FROM Usuario u WHERE u.id = :id")
    Optional<Boolean> findAtivoById(Long id);

    boolean existsByIdAndAtivoTrue(Long id);
    boolean existsByIdAndAtivoTrueAndVersaoSessao(Long id, long versaoSessao);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);

    @Query(value = """
            SELECT 1 FROM (
                SELECT pg_advisory_xact_lock(hashtextextended(:email, 89405012))
            ) AS bloqueio
            """, nativeQuery = true)
    int bloquearCadastroPorEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM Usuario u WHERE u.email = :email AND u.ativo = true")
    Optional<Usuario> buscarAtivoPorEmailParaAtualizar(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM Usuario u WHERE u.id = :id")
    Optional<Usuario> buscarParaAtualizar(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM Usuario u WHERE u.id IN :ids ORDER BY u.id")
    List<Usuario> buscarParaAtualizarEmOrdem(Collection<Long> ids);

    @Query("""
            SELECT u FROM Usuario u
            WHERE u.ativo = coalesce(:ativo, u.ativo)
              AND (lower(u.nome) LIKE :busca ESCAPE '!' OR lower(u.email) LIKE :busca ESCAPE '!')
            """)
    Page<Usuario> listarParaAdministracao(Boolean ativo, String busca, Pageable pageable);
}
