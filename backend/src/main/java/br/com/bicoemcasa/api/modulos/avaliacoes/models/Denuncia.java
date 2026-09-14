package br.com.bicoemcasa.api.modulos.avaliacoes.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "denuncia")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor

public class Denuncia {
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ; 

    @Column(name = "autor_perfil_id" , nullable = false)
    private  UUID  autorPerfilId;

    @Column(name = "denunciado_perfil_id")
    private  UUID  denunciadoPerfilId;

    @Column(name = "contratacao_id")
    private UUID contratacaoId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name="alvo_tipo" , nullable = false , columnDefinition = "tipo_alvo_denuncia")
    private TipoAlvoDenuncia alvoTipo; 

    @Column(name="alvo_id" , nullable = false,length = 64)
    private String alvoId;

    @Column(nullable = false,length = 80)
    private String motivo;

    @Column(length = 1000)
    private String descricao;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> fotos;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false , columnDefinition = "status_denuncia" )
    private StatusDenuncia status;

    @Column(name="analisado_por_perfil_id" )
    private UUID  analisadoPorPerfilId;

    @Column(name="analisado_em" )
    private OffsetDateTime analisadoEm;

    @Column(length = 1000)
    private String parecer;

    @Column(name = "criado_em" , nullable = false , updatable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em" , nullable = false)
    private OffsetDateTime atualizadoEm;


    @PrePersist 
    protected void criar(){
        OffsetDateTime agora = OffsetDateTime.now() ; 
        this.criadoEm = agora ; 
        this.atualizadoEm=agora;
        this.status = StatusDenuncia.PENDENTE;
    }

    @PreUpdate 
    protected void atualizar(){
        this.atualizadoEm= OffsetDateTime.now();
    }

}
