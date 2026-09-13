package br.com.bicoemcasa.api.modulos.avaliacoes;

import br.com.bicoemcasa.api.core.excecao.RegraNegocioException;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.DenunciaAnaliseRequest;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.DenunciaAtualizacaoRequest;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.DenunciaRequest;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.DenunciaResponse;
import br.com.bicoemcasa.api.modulos.avaliacoes.models.Denuncia;
import br.com.bicoemcasa.api.modulos.avaliacoes.models.StatusDenuncia;
import br.com.bicoemcasa.api.modulos.avaliacoes.models.TipoAlvoDenuncia;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class DenunciaServiceImpl implements DenunciaService {

    private final DenunciaRepository denunciaRepository; 

    public  DenunciaServiceImpl(DenunciaRepository denunciaRepository){ 
        this.denunciaRepository = denunciaRepository; 
    }

    @Override 
    @Transactional
    public DenunciaResponse criar(Long autorPerfilId, DenunciaRequest request){
        String alvoId = normalizarAlvoId(request.alvoTipo(),request.alvoId());

        Denuncia denuncia = Denuncia.builder()
            .autorPerfilId(autorPerfilId)
            .denunciadoPerfilId(request.denunciadoPerfilId())
            .contratacaoId(request.contratacaoId())
            .alvoTipo(request.alvoTipo())
            .alvoId(alvoId)
            .motivo(request.motivo())
            .descricao(request.descricao())
            .build();
        return DenunciaResponse.de(denunciaRepository.save(denuncia)); 

    } 

    @Override  
    @Transactional(readOnly = true)
    public DenunciaResponse buscarPorId(UUID id , Long perfilId){
        return  DenunciaResponse.de(buscarDoAutor(id,perfilId));
    }
    @Override  
    @Transactional(readOnly = true)
    public Page<DenunciaResponse> listarDoAutor(Long autorPerfilId, Pageable pageable){
        return  denunciaRepository.findByAutorPerfilId(autorPerfilId, pageable).map(DenunciaResponse::de);
    }
    @Override  
    @Transactional(readOnly = true)
    public Page<DenunciaResponse> listarParaAdmin(Long adminPerfilId,StatusDenuncia status ,Pageable pageable){
        Page<Denuncia> pagina = status == null ? denunciaRepository.findAll(pageable): denunciaRepository.findByStatus(status, pageable);
        return  pagina.map(DenunciaResponse::de);
    }
    @Override  
    @Transactional

    public  DenunciaResponse atualizar(UUID id , Long autorPerfilId , DenunciaAtualizacaoRequest request){
        Denuncia denuncia = buscarDoAutor(id, autorPerfilId); 
        exigirPendente(denuncia);

        denuncia.setMotivo(request.motivo());
        denuncia.setDescricao((request.descricao()));

        return  DenunciaResponse.de(denunciaRepository.saveAndFlush(denuncia));
    }
    @Override 
    @Transactional 
    public  void excluir(UUID id , Long autorPerfilId ){
        Denuncia denuncia = buscarDoAutor(id, autorPerfilId);
        exigirPendente(denuncia);

        denunciaRepository.delete(denuncia);
    }
    @Override  
    @Transactional 
    public  DenunciaResponse analisar(UUID id , Long adminPerfilId , DenunciaAnaliseRequest request){
        Denuncia denuncia = buscarExistente(id);
        exigirPendente(denuncia);

        denuncia.setStatus(request.resultado());
        denuncia.setParecer(request.parecer());
        denuncia.setAnalisadoPorPerfilId(adminPerfilId);
        denuncia.setAnalisadoEm(OffsetDateTime.now());

        return DenunciaResponse.de(denunciaRepository.saveAndFlush(denuncia));
    }

    private Denuncia buscarExistente(UUID id) {
        return denunciaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Denúncia não encontrada"));
    }

     private Denuncia buscarDoAutor(UUID id, Long perfilId) {
        Denuncia denuncia = buscarExistente(id);
        if (!denuncia.getAutorPerfilId().equals(perfilId)) {
            throw new EntidadeNaoEncontradaException("Acesso negado");
        }
        return denuncia;
    }

    private String normalizarAlvoId(TipoAlvoDenuncia alvoTipo , String alvoId){ 
        String mensagem = "O id do alvo nao e valido para o tipo " + alvoTipo ;
        try{
            if (alvoTipo == TipoAlvoDenuncia.PERFIL) {
                Long perfilId  = Long.parseLong(alvoId.trim()); 
                if(perfilId <=0 )
                {
                    throw new RegraNegocioException(mensagem);
                }
                return  String.valueOf(perfilId);
            }
            return  UUID.fromString(alvoId.trim()).toString();
        }catch(IllegalArgumentException e ){
            throw new RegraNegocioException(mensagem);
        }
    }

    private void exigirPendente(Denuncia denuncia) {
        if (denuncia.getStatus() != StatusDenuncia.PENDENTE) {
            throw new RegraNegocioException("Apenas denúncias pendentes podem ser atualizadas ou excluídas");
        }
    }
    
}
