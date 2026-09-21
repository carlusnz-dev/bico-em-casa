package br.com.bicoemcasa.api.modulos.denuncias.controller;

import br.com.bicoemcasa.api.modulos.denuncias.contrato.DenunciaService;
import br.com.bicoemcasa.api.modulos.denuncias.dto.DenunciaAnaliseRequest;
import br.com.bicoemcasa.api.modulos.denuncias.dto.DenunciaAtualizacaoRequest;
import br.com.bicoemcasa.api.modulos.denuncias.dto.DenunciaRequest;
import br.com.bicoemcasa.api.modulos.denuncias.dto.DenunciaResponse;
import br.com.bicoemcasa.api.modulos.denuncias.models.StatusDenuncia;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/denuncia")
public class DenunciaController {

    private final DenunciaService denunciaService;

    public DenunciaController(DenunciaService denunciaService) {
        this.denunciaService = denunciaService;
    }

    @PostMapping
    public ResponseEntity<DenunciaResponse> criar(
            @Valid @RequestBody DenunciaRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = denunciaService.criar(Long.valueOf(jwt.getSubject()), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DenunciaResponse> buscarPorId(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(denunciaService.buscarPorId(id, Long.valueOf(jwt.getSubject())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DenunciaResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody DenunciaAtualizacaoRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = denunciaService.atualizar(id, Long.valueOf(jwt.getSubject()), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        denunciaService.excluir(id, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/minhas")
    public ResponseEntity<PagedModel<DenunciaResponse>> listarMinhas(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        var pagina = denunciaService.listarDoAutor(Long.valueOf(jwt.getSubject()), pageable);
        return ResponseEntity.ok(new PagedModel<>(pagina));
    }

    @GetMapping
    public ResponseEntity<PagedModel<DenunciaResponse>> listarParaAdmin(
            @RequestParam(required = false) StatusDenuncia status,
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        var pagina = denunciaService.listarParaAdmin(Long.valueOf(jwt.getSubject()), status, pageable);
        return ResponseEntity.ok(new PagedModel<>(pagina));
    }

    @PostMapping("/{id}/analise")
    public ResponseEntity<DenunciaResponse> analisar(
            @PathVariable UUID id,
            @Valid @RequestBody DenunciaAnaliseRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = denunciaService.analisar(id, Long.valueOf(jwt.getSubject()), request);
        return ResponseEntity.ok(response);
    }
}
