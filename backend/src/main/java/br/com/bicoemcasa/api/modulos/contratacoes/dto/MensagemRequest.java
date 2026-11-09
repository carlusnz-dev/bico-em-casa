package br.com.bicoemcasa.api.modulos.contratacoes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MensagemRequest(
        @NotBlank(message = "A mensagem não pode estar vazia")
        @Size(max = 1000, message = "A mensagem deve ter no máximo 1000 caracteres")
        String conteudo,
        UUID autorPerfilId
) {
}
