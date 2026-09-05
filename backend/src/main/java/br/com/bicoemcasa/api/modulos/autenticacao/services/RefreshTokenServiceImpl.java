package br.com.bicoemcasa.api.modulos.autenticacao.services;

import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.modulos.autenticacao.RefreshToken;
import br.com.bicoemcasa.api.modulos.autenticacao.RefreshTokenRepository;
import br.com.bicoemcasa.api.modulos.autenticacao.contrato.RefreshTokenService;
import br.com.bicoemcasa.api.modulos.autenticacao.dto.RefreshTokenRequest;
import br.com.bicoemcasa.api.modulos.autenticacao.dto.RefreshTokenResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository repository;

    public RefreshTokenServiceImpl(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    private List<RefreshTokenResponse> tokenResponseList(List<RefreshToken> tokens) {
        return tokens.stream()
                .map(token -> new RefreshTokenResponse(
                        token.getId(), token.getUsuarioId(), token.getFamiliaId(),
                        token.getExpiraEm(), token.getRevogadoEm(), token.getCriadoEm()
                ))
                .toList();
    }

    @Override
    public RefreshTokenResponse buscarPorId(UUID id) {
        RefreshToken token = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Token não encontrado: " + id));

        return new RefreshTokenResponse(
                token.getId(),
                token.getUsuarioId(),
                token.getFamiliaId(),
                token.getExpiraEm(),
                token.getRevogadoEm(),
                token.getCriadoEm()
        );
    }

    @Override
    public List<RefreshTokenResponse> buscarPorUsuarioId(Long usuarioId) {
        List<RefreshToken> tokens = repository.findByUsuarioId(usuarioId);

        return tokenResponseList(tokens);
    }

    @Override
    public List<RefreshTokenResponse> buscarPorFamiliaId(UUID familiaId) {
        List<RefreshToken> tokens = repository.findByFamiliaId(familiaId);

        return tokenResponseList(tokens);
    }

    @Override
    public RefreshTokenResponse criar(RefreshTokenRequest request) {
        RefreshToken token = new RefreshToken();
        token.setUsuarioId(request.usuarioId());
        token.setFamiliaId(request.familiaId());
        token.setHashToken(request.hashToken());
        token.setExpiraEm(request.expiraEm());

        repository.save(token);

        return new RefreshTokenResponse(
                token.getId(),
                token.getUsuarioId(),
                token.getFamiliaId(),
                token.getExpiraEm(),
                token.getRevogadoEm(),
                token.getCriadoEm()
        );
    }
}
