package br.com.bicoemcasa.api.modulos.profissionais;

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
@Table(name = "portfolio_midia")
@Getter
@Setter
public class PortfolioMidia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "portfolio_id", nullable = false)
    private Long portfolioId;

    @Column(nullable = false, unique = true, length = 120)
    private String chave;

    @Column(name = "content_type", nullable = false, length = 40)
    private String contentType;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @PrePersist
    protected void criar() {
        criadoEm = OffsetDateTime.now();
    }
}
