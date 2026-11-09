package br.com.bicoemcasa.api.modulos.usuarios.controller;

import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.UsuarioAdminService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.AdministradorRequest;
import br.com.bicoemcasa.api.modulos.usuarios.dto.MotivoSituacaoUsuarioRequest;
import br.com.bicoemcasa.api.modulos.usuarios.dto.UsuarioAdminResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/usuarios")
public class UsuarioAdminController {
    private final UsuarioAdminService service;

    public UsuarioAdminController(UsuarioAdminService service) {
        this.service = service;
    }

    @GetMapping
    public PaginaResponse<UsuarioAdminResponse> listar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "") String busca
    ) {
        return service.listar(Long.valueOf(jwt.getSubject()), pagina, tamanho, ativo, busca);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioAdminResponse criar(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AdministradorRequest dados,
            HttpServletRequest request
    ) {
        return service.criarAdministrador(Long.valueOf(jwt.getSubject()), dados, request.getRemoteAddr());
    }

    @PatchMapping("/{id}/suspender")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void suspender(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody MotivoSituacaoUsuarioRequest dados,
            HttpServletRequest request
    ) {
        service.alterarSituacao(Long.valueOf(jwt.getSubject()), id, false, dados.motivo(), request.getRemoteAddr());
    }

    @PatchMapping("/{id}/reativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reativar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody MotivoSituacaoUsuarioRequest dados,
            HttpServletRequest request
    ) {
        service.alterarSituacao(Long.valueOf(jwt.getSubject()), id, true, dados.motivo(), request.getRemoteAddr());
    }
}
