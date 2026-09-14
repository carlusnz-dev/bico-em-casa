package br.com.bicoemcasa.api.modulos.profissionais;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;

@Entity //entidade do banco
@Table(name = "profissional")  // diz que a entidade vai ser armazenada na tabela profissional
public class Profissional {

    @Id // informa que  é do tipo id (CHAVE PRIMARIA)
    @GeneratedValue(strategy = GenerationType.IDENTITY)   //Gera um Id no Banco
    private Long id;

    @NotBlank
    private String nome;

    @NotBlank
    private String descricao;

    private String telefone;

    @NotBlank
    private String email;

    @NotBlank
    @Column(name = "usuario_id")   //Pq Java usa camelCase e banco usa snake_case
    private Long usuarioId;
}
