package br.com.bicoemcasa.api.modulos.denuncias.dto;

import br.com.bicoemcasa.api.modulos.denuncias.models.TipoAlvoDenuncia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record DenunciaRequest(
        @NotNull(message = "O tipo do alvo é obrigatório")
        TipoAlvoDenuncia alvoTipo,

        @NotBlank(message = "O id do alvo é obrigatório")
        @Pattern(
                regexp = "(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
                message = "O id do alvo deve ser um UUID válido"
        )
        String alvoId,

        @NotBlank(message = "O motivo é obrigatório")
        @Size(max = 80, message = "O motivo deve ter no máximo 80 caracteres")
        String motivo,

        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
        String descricao,

        UUID denunciadoPerfilId,

        UUID contratacaoId
) {
}
