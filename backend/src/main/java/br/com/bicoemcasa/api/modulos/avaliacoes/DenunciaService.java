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
    DenunciaResponse criar(UUID  autorPerfilId , DenunciaRequest request); 

    DenunciaResponse buscarPorId(UUID id , UUID  perfilId); 

    Page<DenunciaResponse> listarDoAutor(UUID  autorPerfilId, Pageable pageable);

    Page<DenunciaResponse> listarParaAdmin(UUID  adminPerfilId,StatusDenuncia status ,Pageable pageable);

    DenunciaResponse atualizar(UUID id , UUID  autorPerfilId , DenunciaAtualizacaoRequest request); 

    void excluir(UUID id , UUID  autorPerfilId);

    DenunciaResponse analisar(UUID id , UUID  adminPerfilId , DenunciaAnaliseRequest request) ;



    
}
