package br.com.bicoemcasa.api.modulos.contratacoes.controller;

import br.com.bicoemcasa.api.modulos.contratacoes.contrato.ContratacaoMensagemService;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.MensagemRequest;
import br.com.bicoemcasa.api.modulos.contratacoes.dto.MensagemResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contratacao/{id}/mensagens")
public class ContratacaoMensagemController {

    private final ContratacaoMensagemService mensagemService;

    public ContratacaoMensagemController(ContratacaoMensagemService mensagemService) {
        this.mensagemService = mensagemService;
    }

    @GetMapping
    public ResponseEntity<List<MensagemResponse>> listar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = mensagemService.listarMensagens(id, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<MensagemResponse> enviar(
            @PathVariable UUID id,
            @Valid @RequestBody MensagemRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var response = mensagemService.enviarMensagem(id, request, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
