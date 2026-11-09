package br.com.bicoemcasa.api.modulos.servicos.dto;

import br.com.bicoemcasa.api.modulos.servicos.models.UnidadePreco;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record PrecoMedioResponse(UnidadePreco unidadePreco, BigDecimal precoMedio, Long quantidadeServicos) {
    public PrecoMedioResponse(UnidadePreco unidadePreco, Double media, Long quantidadeServicos) {
        this(unidadePreco, BigDecimal.valueOf(media).setScale(2, RoundingMode.HALF_UP), quantidadeServicos);
    }
}
