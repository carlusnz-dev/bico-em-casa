package br.com.bicoemcasa.api.modulos.profissionais;

import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.modulos.profissionais.dto.ConfirmarFotoCapaRequest;
import br.com.bicoemcasa.api.modulos.profissionais.dto.PortfolioRequest;
import br.com.bicoemcasa.api.modulos.profissionais.dto.PortfolioResponse;
import br.com.bicoemcasa.api.modulos.profissionais.dto.SolicitarUploadFotoCapaRequest;
import br.com.bicoemcasa.api.modulos.profissionais.dto.UrlUploadResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
@Validated
public class PortfolioController {
    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping
    public ResponseEntity<PortfolioResponse> criar(
            @Valid @RequestBody PortfolioRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = portfolioService.criar(request, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/meu")
    public ResponseEntity<PortfolioResponse> buscarMeu(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(portfolioService.buscarMeu(Long.valueOf(jwt.getSubject())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PortfolioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(portfolioService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<PaginaResponse<PortfolioResponse>> listarTodos(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "A página não pode ser negativa") int pagina,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "O tamanho mínimo da página é 1") @Max(value = 50, message = "O tamanho máximo da página é 50") int tamanho
    ) {
        return ResponseEntity.ok(portfolioService.listarTodos(pagina, tamanho));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PortfolioResponse> editar(
            @PathVariable Long id,
            @Valid @RequestBody PortfolioRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = portfolioService.editar(id, request, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/foto-capa/upload")
    public ResponseEntity<UrlUploadResponse> solicitarUploadFotoCapa(
            @PathVariable Long id,
            @Valid @RequestBody SolicitarUploadFotoCapaRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = portfolioService.solicitarUploadFotoCapa(id, request, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/foto-capa/confirmar")
    public ResponseEntity<PortfolioResponse> confirmarFotoCapa(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmarFotoCapaRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = portfolioService.confirmarFotoCapa(id, request, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(response);
    }
}
