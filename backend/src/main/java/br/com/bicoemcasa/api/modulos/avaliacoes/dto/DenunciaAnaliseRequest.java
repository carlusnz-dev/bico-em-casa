package br.com.bicoemcasa.api.modulos.avaliacoes.dto;

import br.com.bicoemcasa.api.modulos.avaliacoes.models.StatusDenuncia;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DenunciaAnaliseRequest(
    @NotNull(message = "O resultado da analise é obrigatorio")
    StatusDenuncia resultado, 
    
    @NotBlank(message= "O parecer é obrigatorio")
    @Size(max = 1000 , message = "O parecer deve ter no maximo 1000 caracteres")
    String parecer 

) {
    @AssertTrue(message = "O resultado deve ser PROCEDENTE ou IMPROCEDENTE")
    boolean isResultadoValido(){
        return  resultado!= StatusDenuncia.PENDENTE;
    }
}
