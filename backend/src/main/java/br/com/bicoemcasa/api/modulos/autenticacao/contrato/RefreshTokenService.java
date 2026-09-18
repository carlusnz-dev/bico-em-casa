package br.com.bicoemcasa.api.modulos.autenticacao.contrato;

import br.com.bicoemcasa.api.modulos.autenticacao.dto.RefreshTokenRequest;
import br.com.bicoemcasa.api.modulos.autenticacao.dto.RefreshTokenResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface RefreshTokenService {
    RefreshTokenResponse criar(RefreshTokenRequest request);

    RefreshTokenResponse buscarPorId(UUID id);

    RefreshTokenResponse buscarPorHashToken(String hashToken);

    List<RefreshTokenResponse> buscarPorUsuarioId(Long usuarioId);

    List<RefreshTokenResponse> buscarPorFamiliaId(UUID familiaId);

    void revogarPorHashToken(String hashToken);

    RefreshTokenResponse substituir(String hashTokenAntigo, String hashTokenNovo, OffsetDateTime expiraEm);
}
