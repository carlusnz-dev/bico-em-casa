package br.com.bicoemcasa.api.modulos.avaliacoes.dto;


import br.com.bicoemcasa.api.modulos.avaliacoes.models.TipoAlvoDenuncia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;




public record DenunciaRequest(
    @NotNull(message = "o tupo do alvo é obrigatorio")
    TipoAlvoDenuncia alvoTipo,

    @NotBlank(message = "o id do alvo é obrigatorio")
    @Size(max = 64 , message = "o id do alvo deve ter no maximo 64 caracteres ")
    String alvoId , 

    @NotBlank(message = "o motivo é obrigatorio")
    @Size(max = 80 , message = "O motivo deve ter no mximo 80 caracteres ")
    String motivo , 


    @Size(max = 1000 , message = "A descriçao deve ter no mximo 1000 caracteres ")
    String descricao , 

    Long denunciadoPerfilId , 

    UUID contratacaoId
){ 

}


