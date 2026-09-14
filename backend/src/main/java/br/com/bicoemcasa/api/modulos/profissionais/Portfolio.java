package br.com.bicoemcasa.api.modulos.profissionais;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "potfolio")
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profissional_id")
    private Long profissionalId;

    private String titulo;
    private String descricao;

    @CreationTimestamp  //PREENCHIDO QND PORTIFOLIO É CRIADO
    private LocalDateTime criadoEm;

    @UpdateTimestamp   //PREENCHIDO QND PORTIFOLIO É ALTERADO
    private LocalDateTime atualizadoEm;
}
