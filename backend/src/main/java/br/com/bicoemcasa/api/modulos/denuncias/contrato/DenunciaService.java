package br.com.bicoemcasa.api.modulos.denuncias.contrato;

import br.com.bicoemcasa.api.modulos.denuncias.dto.DenunciaAnaliseRequest;
import br.com.bicoemcasa.api.modulos.denuncias.dto.DenunciaAtualizacaoRequest;
import br.com.bicoemcasa.api.modulos.denuncias.dto.DenunciaRequest;
import br.com.bicoemcasa.api.modulos.denuncias.dto.DenunciaResponse;
import br.com.bicoemcasa.api.modulos.denuncias.models.StatusDenuncia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DenunciaService {
    DenunciaResponse criar(Long usuarioId, DenunciaRequest request);

    DenunciaResponse buscarPorId(UUID id, Long usuarioId);

    Page<DenunciaResponse> listarDoAutor(Long usuarioId, Pageable pageable);

    Page<DenunciaResponse> listarParaAdmin(Long usuarioIdAdmin, StatusDenuncia status, Pageable pageable);

    DenunciaResponse atualizar(UUID id, Long usuarioId, DenunciaAtualizacaoRequest request);

    void excluir(UUID id, Long usuarioId);

    DenunciaResponse analisar(UUID id, Long usuarioIdAdmin, DenunciaAnaliseRequest request);
}
