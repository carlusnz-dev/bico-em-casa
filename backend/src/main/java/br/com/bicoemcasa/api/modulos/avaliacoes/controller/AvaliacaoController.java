package br.com.bicoemcasa.api.modulos.avaliacoes.controller;

import br.com.bicoemcasa.api.modulos.avaliacoes.contrato.AvaliacaoService;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.AvaliacaoRequest;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.AvaliacaoResponse;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/avaliacao")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping("/{contratacaoId}")
    public ResponseEntity<AvaliacaoResponse> criar(
            @PathVariable UUID contratacaoId,
            @Valid @RequestBody AvaliacaoRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = avaliacaoService.criar(contratacaoId, request, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvaliacaoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(avaliacaoService.buscarPorId(id));
    }

    @GetMapping("/avaliado/{avaliadoPerfilId}")
    public ResponseEntity<List<AvaliacaoResponse>> buscarPorAvaliado(@PathVariable UUID avaliadoPerfilId) {
        return ResponseEntity.ok(avaliacaoService.buscarPorAvaliado(avaliadoPerfilId));
    }

    @GetMapping("/autor/{autorPerfilId}")
    public ResponseEntity<List<AvaliacaoResponse>> buscarPorAutor(@PathVariable UUID autorPerfilId) {
        return ResponseEntity.ok(avaliacaoService.buscarPorAutor(autorPerfilId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvaliacaoResponse> alterar(
            @PathVariable UUID id,
            @Valid @RequestBody AvaliacaoRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = avaliacaoService.alterar(id, request, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        avaliacaoService.deletar(id, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/avaliado/{avaliadoPerfilId}/media")
    public Double calcularMediaPorProfissional(@PathVariable Long avaliadoId){
        return avaliacaoService.calcularMediaPorProfissional(avaliadoId);
    }

    @GetMapping("/servico/{servicoId}/media")
    public Double calcularMediaPorServico(@PathVariable UUID servicoId) {
        return avaliacaoService.calcularMediaPorServico((servicoId));
    }
}
