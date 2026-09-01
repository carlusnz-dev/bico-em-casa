package br.com.bicoemcasa.api.modulos.usuarios.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "perfil",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_perfil_usuario_id_tipo",
                        columnNames = {"usuario_id", "tipo"}
                ),
                @UniqueConstraint(
                        name = "uk_perfil_nome_usuario",
                        columnNames = "nome_usuario"
                )
        },
        indexes = {
                @Index(name = "idx_perfis_usuario_id", columnList = "usuario_id"),
                @Index(name = "idx_perfis_tipo", columnList = "tipo")
        }
)
@Getter
@Setter
public class Perfil {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PerfilTipo tipo;

    @Column(name = "nome_usuario", nullable = false, length = 50)
    private String nomeUsuario;

    @Column(name = "nome_exibicao", nullable = false, length = 100)
    private String nomeExibicao;

    @Column(length = 20)
    private String telefone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "endereco_id",
            foreignKey = @ForeignKey(
                    name = "fk_perfil_endereco",
                    foreignKeyDefinition =
                            "FOREIGN KEY (endereco_id) REFERENCES endereco(id) ON DELETE SET NULL"
            )
    )
    private Endereco endereco;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(length = 500)
    private String bio;

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
