package br.com.bicoemcasa.api.modulos.contratacoes.controller;

import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.modulos.contratacoes.contrato.ContratacaoService;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.ContratacaoResponse;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.ContratarServicoRequest;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.EditarContratacaoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/contratacao")
public class ContratacaoController {
    private final ContratacaoService contratacaoService;

    public ContratacaoController(ContratacaoService contratacaoService) {
        this.contratacaoService = contratacaoService;
    }

    @PostMapping
    public ResponseEntity<ContratacaoResponse> contratar(
            @Valid @RequestBody ContratarServicoRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = contratacaoService.contratar(request, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/minhas")
    public ResponseEntity<PaginaResponse<ContratacaoResponse>> listarMinhas(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = contratacaoService.listarMinhas(Long.valueOf(jwt.getSubject()), pagina, tamanho);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContratacaoResponse> buscarPorId(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = contratacaoService.buscarPorId(id, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContratacaoResponse> editar(
            @PathVariable UUID id,
            @Valid @RequestBody EditarContratacaoRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = contratacaoService.editar(id, request, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/arquivar")
    public ResponseEntity<ContratacaoResponse> arquivar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = contratacaoService.arquivar(id, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/desarquivar")
    public ResponseEntity<ContratacaoResponse> desarquivar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = contratacaoService.desarquivar(id, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(response);
    }
}
