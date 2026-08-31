package br.com.bicoemcasa.api.modulos.avaliacoes;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name= "avaliacao")
@Getter
@Setter

public class Avaliacao {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "contratacao_id", unique = true, nullable = false)
    private UUID contratacaoId;

    @Column(name = "autor_id", nullable = false)
    private Long autorId;

    @Column(name = "avaliado_id", nullable = false)
    private Long avaliadoId;

    @Column(nullable = false)
    private Integer nota;

    @Column(length = 1000)
    private String comentario;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;


    @PrePersist
    protected void criar() {
        this.criadoEm = OffsetDateTime.now();
        this.atualizadoEm = OffsetDateTime.now();
    }

    @PreUpdate
    protected void atualizar() {
        this.atualizadoEm = OffsetDateTime.now();
    }

}
