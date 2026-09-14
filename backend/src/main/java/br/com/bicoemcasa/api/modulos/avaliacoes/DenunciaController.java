package br.com.bicoemcasa.api.modulos.avaliacoes;

import br.com.bicoemcasa.api.modulos.avaliacoes.dto.DenunciaAtualizacaoRequest;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.DenunciaRequest;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.DenunciaResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.DenunciaAnaliseRequest;
import br.com.bicoemcasa.api.modulos.avaliacoes.models.StatusDenuncia;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/denuncias")

public class DenunciaController {
    private static final String HEADER_PERFIL = "X-Perfil-id";

    private  final DenunciaService denunciaService;

    public DenunciaController(DenunciaService denunciaService){
        this.denunciaService = denunciaService; 
    }

    @PostMapping
    public ResponseEntity<DenunciaResponse> criar(
            @RequestHeader(HEADER_PERFIL) UUID perfilId,
            @Valid @RequestBody DenunciaRequest request) 
        {
        DenunciaResponse criada = denunciaService.criar(perfilId, request);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criada.id())
                .toUri();
        return ResponseEntity.created(local).body(criada);
    }

    @GetMapping ("/{id}")
    public DenunciaResponse buscarPorId(
        @PathVariable UUID id , 
        @RequestHeader(HEADER_PERFIL) UUID perfilId){
            return denunciaService.buscarPorId(id, perfilId);
        }

    @PutMapping ("/{id}")
    public  DenunciaResponse atualizar(
        @PathVariable UUID id,
        @RequestHeader(HEADER_PERFIL) UUID perfilId,
        @Valid @RequestBody DenunciaAtualizacaoRequest request
    ){
        return denunciaService.atualizar(id, perfilId, request);
    }

    @DeleteMapping ("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(
            @PathVariable UUID id,
            @RequestHeader(HEADER_PERFIL) UUID perfilId) {
        denunciaService.excluir(id, perfilId);
    } 
        
    @GetMapping("/minhas")
    public PagedModel<DenunciaResponse> listarMinhas(
            @RequestHeader(HEADER_PERFIL) UUID perfilId,
            @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(denunciaService.listarDoAutor(perfilId, pageable));
    }

    @GetMapping
    public PagedModel<DenunciaResponse> listarParaAdmin(
            @RequestHeader(HEADER_PERFIL) UUID perfilId,
            @RequestParam(required = false) StatusDenuncia status,
            @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(denunciaService.listarParaAdmin(perfilId, status, pageable));
    }

    @PostMapping("/{id}/analise")
    public DenunciaResponse analisar(
            @PathVariable UUID id,
            @RequestHeader(HEADER_PERFIL) UUID perfilId,
            @Valid @RequestBody DenunciaAnaliseRequest request) {
        return denunciaService.analisar(id, perfilId, request);
    }

        
    
    
}
