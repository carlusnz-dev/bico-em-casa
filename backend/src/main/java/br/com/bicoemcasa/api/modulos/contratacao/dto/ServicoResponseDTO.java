package br.com.bicoemcasa.api.modulos.contratacao.dto;

import br.com.bicoemcasa.api.modulos.contratacao.models.Servico;

import java.math.BigDecimal;
import java.util.UUID;

public record ServicoResponseDTO(
        UUID id,
        UUID perfilId,
        String titulo,
        String descricao,
        BigDecimal precoPrevio,
        String unidadePreco
) {
    public static ServicoResponseDTO converter(Servico servico){
        return new ServicoResponseDTO(
                servico.getId(),
                servico.getPerfilId(),
                servico.getTitulo(),
                servico.getDescricao(),
                servico.getPrecoPrevio(),
                servico.getUnidadePreco()
        );
    }

}
