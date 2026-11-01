package br.com.bicoemcasa.api.modulos.servicos.repository;

import br.com.bicoemcasa.api.modulos.servicos.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select t from Tag t where t.id = :id")
    java.util.Optional<Tag> buscarParaAtualizar(UUID id);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_READ)
    @org.springframework.data.jpa.repository.Query("select t from Tag t where t.id in :ids order by t.id")
    java.util.List<Tag> buscarParaVincular(java.util.Set<UUID> ids);
    @org.springframework.data.jpa.repository.Query("select count(t)>0 from Tag t where lower(trim(t.nome))=lower(trim(:nome)) and t.id<>:id")
    boolean nomeEmUso(String nome, UUID id);
    @org.springframework.data.jpa.repository.Query(value="select count(*) from servico_tag where tag_id=:id",nativeQuery=true)
    long contarVinculos(UUID id);

}
