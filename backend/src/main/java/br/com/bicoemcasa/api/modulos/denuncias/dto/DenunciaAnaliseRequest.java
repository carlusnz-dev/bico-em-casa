package br.com.bicoemcasa.api.modulos.denuncias.dto;

import br.com.bicoemcasa.api.modulos.denuncias.models.StatusDenuncia;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DenunciaAnaliseRequest(
        @NotNull(message = "O resultado da análise é obrigatório")
        StatusDenuncia resultado,

        @NotBlank(message = "O parecer é obrigatório")
        @Size(max = 1000, message = "O parecer deve ter no máximo 1000 caracteres")
        String parecer
) {
    @AssertTrue(message = "O resultado deve ser PROCEDENTE ou IMPROCEDENTE")
    boolean isResultadoValido() {
        return resultado != StatusDenuncia.PENDENTE;
    }
}
