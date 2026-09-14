package br.com.bicoemcasa.api.modulos.contratacao.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "servico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Servico{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "perfil_id", nullable = false)
    private UUID perfilId;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(length = 1000)
    private String descricao;

    @Column(name = "preco_previo", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoPrevio;

    @Column(name = "unidade_preco", nullable = false, length = 20)
    private String unidadePreco = "SERVICO";

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    @PrePersist
    protected void aoCriar(){
        this.criadoEm = OffsetDateTime.now();
        this.atualizadoEm = OffsetDateTime.now();
        if (this.ativo == null){
            this.ativo = true;
        }
    }
    @PreUpdate
    protected void aoAtualizar(){
        this.atualizadoEm = OffsetDateTime.now();
    }
}