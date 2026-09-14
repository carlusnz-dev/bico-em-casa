package br.com.bicoemcasa.api.modulos.profissionais;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "portfolio")
@Getter
@Setter
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "perfil_id", nullable = false, unique = true)
    private UUID perfilId;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(length = 1000)
    private String descricao;

    @Column(name = "slug_url", nullable = false, unique = true, length = 120)
    private String slugUrl;

    @Column(name = "foto_capa_url", length = 500)
    private String fotoCapaUrl;

    @Column(name = "criado_em")
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em")
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
