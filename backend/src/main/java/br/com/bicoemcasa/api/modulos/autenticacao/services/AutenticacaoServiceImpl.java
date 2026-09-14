package br.com.bicoemcasa.api.modulos.autenticacao.services;

import br.com.bicoemcasa.api.core.excecao.CadastroNaoPermitidoException;
import br.com.bicoemcasa.api.core.excecao.SenhaNaoBateException;
import br.com.bicoemcasa.api.modulos.autenticacao.contrato.AutenticacaoService;
import br.com.bicoemcasa.api.modulos.autenticacao.contrato.RefreshTokenService;
import br.com.bicoemcasa.api.modulos.autenticacao.dto.*;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.UsuarioService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.CredenciaisUsuario;
import br.com.bicoemcasa.api.modulos.usuarios.dto.PerfilRequest;
import br.com.bicoemcasa.api.modulos.usuarios.dto.UsuarioRequest;
import br.com.bicoemcasa.api.modulos.usuarios.dto.UsuarioResponse;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AutenticacaoServiceImpl implements AutenticacaoService {
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioService usuarioService;
    private final JwtEncoder jwtEncoder;

    private static final SecureRandom secure = new SecureRandom();
    private static final Base64.Encoder base64 = Base64.getUrlEncoder();

    private static final int TAMANHO_TOKEN_BYTES = 32;
    private static final int VALIDADE_ACCESS_TOKEN_MINUTOS = 60;
    private static final int VALIDADE_REFRESH_TOKEN_DIAS = 30;

    public AutenticacaoServiceImpl(
            RefreshTokenService refreshTokenService,
            UsuarioService usuarioService,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder
    ) {
        this.refreshTokenService = refreshTokenService;
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    private static String geracaoToken(int byteLength) {
        byte[] randomBytes = new byte[byteLength];
        secure.nextBytes(randomBytes);
        return base64.encodeToString(randomBytes);
    }

    private static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível nesta JVM", e);
        }
    }

    private String emitirAccessToken(Long usuarioId) {
        Instant agora = Instant.now();
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .subject(usuarioId.toString())
                .issuedAt(agora)
                .expiresAt(agora.plus(VALIDADE_ACCESS_TOKEN_MINUTOS, ChronoUnit.MINUTES))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    private static OffsetDateTime expiracaoDoRefreshToken() {
        return OffsetDateTime.now().plusDays(VALIDADE_REFRESH_TOKEN_DIAS);
    }

    @Override
    public LoginResponse entrar(LoginRequest request) {
        CredenciaisUsuario credencial = usuarioService.buscarCredenciaisPorEmail(request.email())
                .filter(c -> passwordEncoder.matches(request.senha(), c.hashSenha()))
                .orElseThrow(() -> new SenhaNaoBateException("Credenciais inválidas"));

        String tokenBruto = geracaoToken(TAMANHO_TOKEN_BYTES);

        refreshTokenService.criar(new RefreshTokenRequest(
                credencial.id(),
                UUID.randomUUID(),
                hashToken(tokenBruto),
                expiracaoDoRefreshToken()
        ));

        return new LoginResponse(true, emitirAccessToken(credencial.id()), tokenBruto);
    }

    @Override
    @Transactional
    public RenovarResponse renovar(String tokenBruto) {
        String tokenBrutoNovo = geracaoToken(TAMANHO_TOKEN_BYTES);

        RefreshTokenResponse novo = refreshTokenService.substituir(
                hashToken(tokenBruto),
                hashToken(tokenBrutoNovo),
                expiracaoDoRefreshToken()
        );

        return new RenovarResponse(emitirAccessToken(novo.usuarioId()), tokenBrutoNovo);
    }

    @Override
    public LogoutResponse sair(String hashTokenBruto) {
        String hash = hashToken(hashTokenBruto);
        Long usuarioId = refreshTokenService.buscarPorHashToken(hash).usuarioId();
        refreshTokenService.revogarPorHashToken(hash);

        return new LogoutResponse(
                usuarioId,
                OffsetDateTime.now()
        );
    }

    @Override
    @Transactional
    public CadastroResponse criar(CadastroRequest request) {
        if (request.cadastroTipo() == PerfilTipo.ADMIN) {
            throw new CadastroNaoPermitidoException("Cadastro público não pode criar conta do tipo ADMIN");
        }

        PerfilRequest perfilRequest = new PerfilRequest(
                request.cadastroTipo(),
                request.nomeUsuario(),
                request.nomeExibicao(),
                request.telefone(),
                request.bio()
        );

        UsuarioRequest usuarioRequest = new UsuarioRequest(
                request.nomeCompleto(),
                request.email(),
                request.cpf(),
                request.senha(),
                perfilRequest
        );

        UsuarioResponse usuarioNovo = usuarioService.criar(usuarioRequest);

        return new CadastroResponse(
                usuarioNovo.perfilId(),
                perfilRequest.nomeExibicao(),
                perfilRequest.tipo().toString(),
                OffsetDateTime.now()
        );
    }
}
