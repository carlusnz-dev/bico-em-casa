package br.com.bicoemcasa.api.modulos.servicos.controller;

import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.modulos.servicos.contrato.ServicoService;
import br.com.bicoemcasa.api.modulos.servicos.dto.PrecoMedioResponse;
import br.com.bicoemcasa.api.modulos.servicos.dto.ServicoRequest;
import br.com.bicoemcasa.api.modulos.servicos.dto.ServicoResponse;
import br.com.bicoemcasa.api.modulos.servicos.dto.TopListResponse;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/servico")
@Validated
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
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "A página não pode ser negativa") int pagina,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "O tamanho mínimo da página é 1") @Max(value = 50, message = "O tamanho máximo da página é 50") int tamanho,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = servicoService.listarMeus(Long.valueOf(jwt.getSubject()), pagina, tamanho);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(servicoService.buscarPublicoPorId(id));
    }

    @GetMapping("/meus/{id}")
    public ResponseEntity<ServicoResponse> buscarMeu(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(servicoService.buscarMeu(id, Long.valueOf(jwt.getSubject())));
    }

    @GetMapping("/meus/orcamento-medio")
    public List<PrecoMedioResponse> calcularOrcamentoMedio(@AuthenticationPrincipal Jwt jwt) {
        return servicoService.calcularOrcamentoMedio(Long.valueOf(jwt.getSubject()));
    }

    @GetMapping("/precos-medios")
    public List<PrecoMedioResponse> calcularPrecosMedios(@RequestParam @NotBlank @Size(max = 60) String categoria) {
        return servicoService.calcularPrecosMedios(categoria);
    }

    @GetMapping
    public ResponseEntity<PaginaResponse<ServicoResponse>> listarAtivos(
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int tamanho,
            @RequestParam(required = false) @Size(max = 120) String cidade,
            @RequestParam(required = false) @Size(max = 60) String categoria,
            @RequestParam(required = false) @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal precoMinimo,
            @RequestParam(required = false) @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal precoMaximo,
            @RequestParam(required = false) @Size(max = 120) String busca
    ) {
        return ResponseEntity.ok(servicoService.listarAtivos(pagina, tamanho, cidade, categoria,
                precoMinimo, precoMaximo, busca));
    }

    @GetMapping("/top/{perfilId}")
    public ResponseEntity<List<TopListResponse>> buscarTop5PorPerfilId(
            @PathVariable UUID perfilId
    ) {
        return ResponseEntity.ok(servicoService.listarTop5PorPerfilId(perfilId));
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

    @PutMapping("/{id}/reenviar")
    public ResponseEntity<ServicoResponse> reenviar(
            @PathVariable UUID id,
            @Valid @RequestBody ServicoRequest dados,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(servicoService.reenviar(id, dados, Long.valueOf(jwt.getSubject()),
                request.getRemoteAddr()));
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
