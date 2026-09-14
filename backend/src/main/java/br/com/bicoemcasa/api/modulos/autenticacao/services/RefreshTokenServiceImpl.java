package br.com.bicoemcasa.api.modulos.autenticacao.services;

import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.core.excecao.TokenInvalidoException;
import br.com.bicoemcasa.api.modulos.autenticacao.model.RefreshToken;
import br.com.bicoemcasa.api.modulos.autenticacao.RefreshTokenRepository;
import br.com.bicoemcasa.api.modulos.autenticacao.contrato.RefreshTokenService;
import br.com.bicoemcasa.api.modulos.autenticacao.dto.RefreshTokenRequest;
import br.com.bicoemcasa.api.modulos.autenticacao.dto.RefreshTokenResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository repository;

    public RefreshTokenServiceImpl(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    private RefreshTokenResponse paraResponse(RefreshToken token) {
        return new RefreshTokenResponse(
                token.getId(),
                token.getUsuarioId(),
                token.getFamiliaId(),
                token.getExpiraEm(),
                token.getRevogadoEm(),
                token.getCriadoEm()
        );
    }

    private List<RefreshTokenResponse> tokenResponseList(List<RefreshToken> tokens) {
        return tokens.stream().map(this::paraResponse).toList();
    }

    private void exigirTokenUtilizavel(RefreshToken token) {
        if (token.getRevogadoEm() != null) {
            throw new TokenInvalidoException("Token já foi revogado: " + token.getRevogadoEm());
        }

        if (token.getExpiraEm().toInstant().isBefore(Instant.now())) {
            throw new TokenInvalidoException("Token já foi expirado: " + token.getExpiraEm());
        }
    }

    @Override
    public RefreshTokenResponse buscarPorId(UUID id) {
        RefreshToken token = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Token não encontrado: " + id));

        return paraResponse(token);
    }

    @Override
    public RefreshTokenResponse buscarPorHashToken(String hashToken) {
        RefreshToken token = repository.findByHashToken(hashToken)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Token não encontrado: " + hashToken));

        return paraResponse(token);
    }

    @Override
    public List<RefreshTokenResponse> buscarPorUsuarioId(Long usuarioId) {
        return tokenResponseList(repository.findByUsuarioId(usuarioId));
    }

    @Override
    public List<RefreshTokenResponse> buscarPorFamiliaId(UUID familiaId) {
        return tokenResponseList(repository.findByFamiliaId(familiaId));
    }

    @Override
    public RefreshTokenResponse criar(RefreshTokenRequest request) {
        RefreshToken token = new RefreshToken();
        token.setUsuarioId(request.usuarioId());
        token.setFamiliaId(request.familiaId());
        token.setHashToken(request.hashToken());
        token.setExpiraEm(request.expiraEm());

        repository.save(token);

        return paraResponse(token);
    }

    @Override
    public void revogarPorHashToken(String hashToken) {
        RefreshToken refreshToken = repository.findByHashToken(hashToken)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Token não encontrado: " + hashToken));

        exigirTokenUtilizavel(refreshToken);

        refreshToken.setRevogadoEm(OffsetDateTime.now());
        repository.save(refreshToken);
    }

    @Override
    @Transactional
    public RefreshTokenResponse substituir(String hashTokenAntigo, String hashTokenNovo, OffsetDateTime expiraEm) {
        RefreshToken antigo = repository.findByHashToken(hashTokenAntigo)
                .orElseThrow(() -> new TokenInvalidoException("Refresh token não reconhecido"));

        exigirTokenUtilizavel(antigo);

        RefreshTokenResponse novo = criar(new RefreshTokenRequest(
                antigo.getUsuarioId(),
                antigo.getFamiliaId(),
                hashTokenNovo,
                expiraEm
        ));

        antigo.setSubstituidoPor(novo.id());
        antigo.setRevogadoEm(OffsetDateTime.now());
        repository.save(antigo);

        return novo;
    }
}
