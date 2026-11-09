package br.com.bicoemcasa.api.modulos.contratacoes.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "contratacao_mensagem")
@Getter
@Setter
public class ContratacaoMensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "contratacao_id", nullable = false)
    private UUID contratacaoId;

    @Column(name = "autor_perfil_id", nullable = false)
    private UUID autorPerfilId;

    @Column(name = "autor_nome", nullable = false, length = 100)
    private String autorNome;

    @Column(nullable = false, length = 1000)
    private String conteudo;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @PrePersist
    protected void criar() {
        this.criadoEm = OffsetDateTime.now();
    }
}
