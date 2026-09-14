package br.com.bicoemcasa.api.modulos.avaliacoes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DenunciaAtualizacaoRequest(
    @NotBlank(message = "O motivo é obrigatorio")
    @Size(max = 80 , message = "O motivo deve ter no maximo 80 caracteres")
    String motivo , 

    @Size(max = 1000, message = "A descrição deve ter no maximo 1000 carcteres")
    String descricao
) {
} 
