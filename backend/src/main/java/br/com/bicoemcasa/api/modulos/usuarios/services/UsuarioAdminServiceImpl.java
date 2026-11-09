package br.com.bicoemcasa.api.modulos.usuarios.services;

import br.com.bicoemcasa.api.core.RegistroAuditoria;
import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.core.excecao.ModeracaoConflitoException;
import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.modulos.autenticacao.contrato.RefreshTokenService;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.UsuarioAdminService;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.UsuarioService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.AdministradorRequest;
import br.com.bicoemcasa.api.modulos.usuarios.dto.UsuarioAdminResponse;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import br.com.bicoemcasa.api.modulos.usuarios.models.Usuario;
import br.com.bicoemcasa.api.modulos.usuarios.repository.PerfilRepository;
import br.com.bicoemcasa.api.modulos.usuarios.repository.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class UsuarioAdminServiceImpl implements UsuarioAdminService {
    private final UsuarioRepository usuarios;
    private final PerfilRepository perfis;
    private final UsuarioService contas;
    private final RefreshTokenService refreshTokens;
    private final RegistroAuditoria auditoria;

    public UsuarioAdminServiceImpl(
            UsuarioRepository usuarios, PerfilRepository perfis, UsuarioService contas,
            RefreshTokenService refreshTokens, RegistroAuditoria auditoria
    ) {
        this.usuarios = usuarios;
        this.perfis = perfis;
        this.contas = contas;
        this.refreshTokens = refreshTokens;
        this.auditoria = auditoria;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<UsuarioAdminResponse> listar(Long autor, int pagina, int tamanho, Boolean ativo, String busca) {
        contas.exigirAdministradorAtivo(autor);
        String termo = busca == null ? "" : busca.strip().toLowerCase(Locale.ROOT);
        String padrao = "%" + termo.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        var paginacao = PageRequest.of(Math.max(0, pagina), Math.clamp(tamanho, 1, 50), Sort.by("id").descending());
        var resultado = usuarios.listarParaAdministracao(ativo, padrao, paginacao);
        Map<Long, List<PerfilTipo>> papeisPorUsuario = new HashMap<>();
        if (resultado.hasContent()) {
            var ids = resultado.map(Usuario::getId).getContent();
            for (var papel : perfis.buscarPapeisDosUsuarios(ids)) {
                papeisPorUsuario.computeIfAbsent(papel.getUsuarioId(), id -> new ArrayList<>()).add(papel.getTipo());
            }
        }
        return PaginaResponse.de(resultado, u -> paraResponse(u, papeisPorUsuario.getOrDefault(u.getId(), List.of())));
    }

    @Override
    @Transactional
    public UsuarioAdminResponse criarAdministrador(Long autor, AdministradorRequest dados, String ip) {
        var novo = contas.criarAdministrador(autor, dados.paraUsuarioRequest());
        var usuario = usuarios.findById(novo.id()).orElseThrow();
        auditoria.registrar(autor, "CRIAR_ADMINISTRADOR", "usuario", novo.id().toString(), ip,
                "Provisionamento administrativo", Map.of(), Map.of("papel", PerfilTipo.ADMIN.name()));
        return paraResponse(usuario, List.of(PerfilTipo.ADMIN));
    }

    @Override
    @Transactional
    public void alterarSituacao(Long autor, Long id, boolean ativo, String motivo, String ip) {
        // A ordem dos locks evita suspensões cruzadas que deixariam os dois administradores inativos.
        var contasBloqueadas = usuarios.buscarParaAtualizarEmOrdem(List.of(autor, id));
        contas.exigirAdministradorAtivo(autor);
        if (!ativo && autor.equals(id)) {
            throw new ModeracaoConflitoException("O administrador não pode suspender a própria conta");
        }
        var usuario = contasBloqueadas.stream().filter(conta -> conta.getId().equals(id)).findFirst()
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado"));
        if (usuario.isAtivo() == ativo) {
            throw new ModeracaoConflitoException(ativo ? "Usuário já está ativo" : "Usuário já está suspenso");
        }

        usuario.setAtivo(ativo);
        if (!ativo) {
            usuario.setVersaoSessao(usuario.getVersaoSessao() + 1);
            refreshTokens.revogarPorUsuario(id);
        }
        auditoria.registrar(autor, ativo ? "REATIVAR_USUARIO" : "SUSPENDER_USUARIO", "usuario", id.toString(),
                ip, motivo, Map.of("ativo", !ativo), Map.of("ativo", ativo));
    }

    private UsuarioAdminResponse paraResponse(Usuario usuario, List<PerfilTipo> papeis) {
        return new UsuarioAdminResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.isAtivo(), List.copyOf(papeis), usuario.getCriadoEm());
    }
}
