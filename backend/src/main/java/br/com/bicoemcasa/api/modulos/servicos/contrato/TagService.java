package br.com.bicoemcasa.api.modulos.servicos.contrato;

import br.com.bicoemcasa.api.modulos.servicos.dto.TagResponse;

import java.util.List;

public interface TagService {
    List<TagResponse> listarTodas();
}
