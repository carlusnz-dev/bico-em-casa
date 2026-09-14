package br.com.bicoemcasa.api.modulos.contratacoes.models;

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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "contratacao")
@Getter
@Setter
public class Contratacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "servico_id", nullable = false)
    private UUID servicoId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "profissional_id", nullable = false)
    private UUID profissionalId;

    @Column(name = "titulo_servico", nullable = false, length = 120)
    private String tituloServico;

    @Column(name = "preco_servico", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoServico;

    @Column(length = 1000)
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusContratacao status;

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
