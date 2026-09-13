package br.com.bicoemcasa.api.modulos.avaliacoes;

import br.com.bicoemcasa.api.modulos.avaliacoes.dto.DenunciaAnaliseRequest;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.DenunciaAtualizacaoRequest;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.DenunciaRequest;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.DenunciaResponse;
import br.com.bicoemcasa.api.modulos.avaliacoes.models.StatusDenuncia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;


public interface DenunciaService {
    DenunciaResponse criar(Long autorPerfilId , DenunciaRequest request); 

    DenunciaResponse buscarPorId(UUID id , Long perfilId); 

    Page<DenunciaResponse> listarDoAutor(Long autorPerfilId, Pageable pageable);

    Page<DenunciaResponse> listarParaAdmin(Long adminPerfilId,StatusDenuncia status ,Pageable pageable);

    DenunciaResponse atualizar(UUID id , Long autorPerfilId , DenunciaAtualizacaoRequest request); 

    void excluir(UUID id , Long autorPerfilId);

    DenunciaResponse analisar(UUID id , Long adminPerfilId , DenunciaAnaliseRequest request) ;



    
}
