package br.com.bicoemcasa.api.modulos.contratacoes.controller;

import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.modulos.contratacoes.contrato.ContratacaoStatusService;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.AlterarStatusRequest;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.ContratacaoResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/contratacao")
public class ContratacaoStatusController {

    private final ContratacaoStatusService contratacaoStatusService;

    public ContratacaoStatusController(ContratacaoStatusService contratacaoStatusService) {
        this.contratacaoStatusService = contratacaoStatusService;
    }

    @GetMapping("/recebidas")
    public ResponseEntity<PaginaResponse<ContratacaoResponse>> listarRecebidas(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = contratacaoStatusService.listarRecebidas(Long.valueOf(jwt.getSubject()), pagina, tamanho);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ContratacaoResponse> alterarStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AlterarStatusRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = contratacaoStatusService.alterarStatus(id, request, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(response);
    }
}
