package br.com.bicoemcasa.api.modulos.autenticacao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "refresh_token",
        indexes = {
                @Index(name = "idx_refresh_token_usuario_id", columnList = "usuario_id"),
                @Index(name = "idx_refresh_token_familia_id", columnList = "familia_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_refresh_token_hash_token",
                        columnNames = "hash_token"
                )
        }
)
@Getter
@Setter
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "hash_token", nullable = false)
    private String hashToken;

    @Column(name = "familia_id", nullable = false)
    private UUID familiaId;

    @Column(name = "substituido_por", unique = true)
    private UUID substituidoPor;

    @Column(name = "expira_em", nullable = false)
    private OffsetDateTime expiraEm;

    @Column(name = "revogado_em")
    private OffsetDateTime revogadoEm;

    @Column(name = "ip_origem")
    private String ipOrigem;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @PrePersist
    private void criar() {
        this.criadoEm = OffsetDateTime.now();
    }
}
