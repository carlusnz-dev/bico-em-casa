package br.com.bicoemcasa.api.modulos.usuarios.repository;

import br.com.bicoemcasa.api.modulos.usuarios.models.Perfil;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, UUID> {
    @org.springframework.data.jpa.repository.Query(value = """
        select p.id from perfil p join usuario u on u.id=p.usuario_id
        where p.tipo='PROFISSIONAL' and (
        translate(lower(u.nome), 'áàâãäéèêëíìîïóòôõöúùûüç', 'aaaaaeeeeiiiiooooouuuuc') like :termo escape '!'
        or translate(lower(u.email), 'áàâãäéèêëíìîïóòôõöúùûüç', 'aaaaaeeeeiiiiooooouuuuc') like :termo escape '!')
        """, nativeQuery = true)
    java.util.List<UUID> buscarProfissionais(String termo);

    Optional<Perfil> findByUsuarioId(Long usuarioId);

    Optional<Perfil> findByUsuarioIdAndTipo(Long usuarioId, PerfilTipo tipo);

    Optional<Perfil> findByNomeExibicao(String nomeExibicao);

    boolean existsByNomeUsuario(String nomeUsuario);
}
