package br.com.bicoemcasa.api.modulos.autenticacao;

import br.com.bicoemcasa.api.modulos.autenticacao.contrato.AutenticacaoService;
import br.com.bicoemcasa.api.modulos.autenticacao.dto.CadastroRequest;
import br.com.bicoemcasa.api.modulos.autenticacao.dto.CadastroResponse;
import br.com.bicoemcasa.api.modulos.autenticacao.dto.LoginRequest;
import br.com.bicoemcasa.api.modulos.autenticacao.dto.LoginResponse;
import br.com.bicoemcasa.api.modulos.autenticacao.dto.LogoutResponse;
import br.com.bicoemcasa.api.modulos.autenticacao.dto.RenovarResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/autenticacao")
public class AutenticacaoController {
    private static final String COOKIE_REFRESH_TOKEN = "refreshToken";
    private static final Duration VALIDADE_COOKIE = Duration.ofDays(30);

    private final AutenticacaoService service;

    public AutenticacaoController(AutenticacaoService service) {
        this.service = service;
    }

    private static String cookieDeRefreshToken(String valor, Duration validade) {
        return ResponseCookie.from(COOKIE_REFRESH_TOKEN, valor)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(validade)
                .build()
                .toString();
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<CadastroResponse> cadastrar(@Valid @RequestBody CadastroRequest request) {
        CadastroResponse cadastroResponse = service.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastroResponse);
    }

    @PostMapping("/entrar")
    public ResponseEntity<LoginResponse> entrar(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = service.entrar(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieDeRefreshToken(loginResponse.tokenBruto(), VALIDADE_COOKIE))
                .body(loginResponse);
    }

    @PostMapping("/renovar")
    public ResponseEntity<RenovarResponse> renovar(@CookieValue(COOKIE_REFRESH_TOKEN) String refreshToken) {
        RenovarResponse renovarResponse = service.renovar(refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieDeRefreshToken(renovarResponse.tokenBruto(), VALIDADE_COOKIE))
                .body(renovarResponse);
    }

    @PostMapping("/sair")
    public ResponseEntity<LogoutResponse> sair(@CookieValue(COOKIE_REFRESH_TOKEN) String refreshToken) {
        LogoutResponse logoutResponse = service.sair(refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieDeRefreshToken("", Duration.ZERO))
                .body(logoutResponse);
    }
}
