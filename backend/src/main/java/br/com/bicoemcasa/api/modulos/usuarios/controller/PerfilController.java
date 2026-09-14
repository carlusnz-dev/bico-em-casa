package br.com.bicoemcasa.api.modulos.usuarios.controller;

import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.PerfilResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {
    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping("/me")
    public ResponseEntity<PerfilResponse> buscarMeuPerfil(@AuthenticationPrincipal Jwt jwt) {
        var response = perfilService.buscarPorUsuarioId(Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerfilResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(perfilService.buscarPorId(id));
    }
}
