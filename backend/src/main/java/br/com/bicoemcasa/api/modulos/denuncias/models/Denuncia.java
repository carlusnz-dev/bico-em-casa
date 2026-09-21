package br.com.bicoemcasa.api.modulos.denuncias.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "denuncia")
@Getter
@Setter
public class Denuncia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "autor_perfil_id", nullable = false)
    private UUID autorPerfilId;

    @Column(name = "denunciado_perfil_id")
    private UUID denunciadoPerfilId;

    @Column(name = "contratacao_id")
    private UUID contratacaoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "alvo_tipo", nullable = false, length = 20)
    private TipoAlvoDenuncia alvoTipo;

    @Column(name = "alvo_id", nullable = false, length = 64)
    private String alvoId;

    @Column(nullable = false, length = 80)
    private String motivo;

    @Column(length = 1000)
    private String descricao;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> fotos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusDenuncia status;

    @Column(name = "analisado_por_perfil_id")
    private UUID analisadoPorPerfilId;

    @Column(name = "analisado_em")
    private OffsetDateTime analisadoEm;

    @Column(length = 1000)
    private String parecer;

    @Column(name = "criado_em")
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em")
    private OffsetDateTime atualizadoEm;

    @PrePersist
    protected void criar() {
        this.criadoEm = OffsetDateTime.now();
        this.atualizadoEm = OffsetDateTime.now();
        this.status = StatusDenuncia.PENDENTE;
    }

    @PreUpdate
    protected void atualizar() {
        this.atualizadoEm = OffsetDateTime.now();
    }
}
