package br.com.bicoemcasa.api.modulos.servicos.controller;

import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.modulos.servicos.contrato.ServicoService;
import br.com.bicoemcasa.api.modulos.servicos.dto.ServicoRequest;
import br.com.bicoemcasa.api.modulos.servicos.dto.ServicoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/servico")
public class ServicoController {
    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @PostMapping
    public ResponseEntity<ServicoResponse> criar(
            @Valid @RequestBody ServicoRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = servicoService.criar(request, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/meus")
    public ResponseEntity<PaginaResponse<ServicoResponse>> listarMeus(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = servicoService.listarMeus(Long.valueOf(jwt.getSubject()), pagina, tamanho);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(servicoService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<PaginaResponse<ServicoResponse>> listarAtivos(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho
    ) {
        return ResponseEntity.ok(servicoService.listarAtivos(pagina, tamanho));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponse> editar(
            @PathVariable UUID id,
            @Valid @RequestBody ServicoRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = servicoService.editar(id, request, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<ServicoResponse> ativar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = servicoService.ativar(id, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<ServicoResponse> desativar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = servicoService.desativar(id, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ServicoResponse> deletar (
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        servicoService.deletar(id, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }
}
