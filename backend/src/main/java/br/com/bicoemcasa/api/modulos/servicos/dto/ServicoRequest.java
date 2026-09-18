package br.com.bicoemcasa.api.modulos.servicos.dto;

import br.com.bicoemcasa.api.modulos.servicos.models.UnidadePreco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record ServicoRequest(
        @NotBlank(message = "O título não pode estar em branco") @Size(max = 120) String titulo,
        @NotBlank(message = "A descrição não pode estar em branco") @Size(max = 1000) String descricao,
        @NotNull(message = "Informe o preço de referência") @PositiveOrZero BigDecimal precoPrevio,
        @NotNull(message = "Selecione uma unidade de preço") UnidadePreco unidadePreco,
        @NotEmpty(message = "Selecione ao menos uma categoria") Set<UUID> tagIds
) {
}
