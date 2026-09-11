package br.com.bicoemcasa.api.modulos.autenticacao;

import br.com.bicoemcasa.api.modulos.autenticacao.contrato.AutenticacaoService;
import br.com.bicoemcasa.api.modulos.autenticacao.dto.LoginRequest;
import br.com.bicoemcasa.api.modulos.autenticacao.dto.LoginResponse;
import br.com.bicoemcasa.api.modulos.autenticacao.dto.LogoutResponse;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.HttpCookie;
import java.time.Duration;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/autenticacao")
public class AutenticacaoController {
    private final AutenticacaoService service;

    public AutenticacaoController(AutenticacaoService service) {
        this.service = service;
    }

    @PostMapping("/entrar")
    public ResponseEntity<LoginResponse> entrar(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = service.entrar(request);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", loginResponse.tokenBruto())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofDays(30))
                .build();
        return ResponseEntity.ok().header(
                HttpHeaders.SET_COOKIE, cookie.toString()
        ).body(loginResponse);
    }

    @PostMapping("/sair")
    public ResponseEntity<LogoutResponse> sair(@CookieValue("refreshToken") String refreshToken) {
        LogoutResponse logoutResponse = service.sair(refreshToken);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build();
        return ResponseEntity.ok().header(
                HttpHeaders.SET_COOKIE, cookie.toString()
        ).body(logoutResponse);
    }
}
