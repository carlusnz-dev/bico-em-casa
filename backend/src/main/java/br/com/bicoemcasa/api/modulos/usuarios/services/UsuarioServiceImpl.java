package br.com.bicoemcasa.api.modulos.usuarios.services;

import br.com.bicoemcasa.api.core.excecao.CadastroNaoPermitidoException;
import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.core.excecao.ModeracaoConflitoException;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.UsuarioService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.*;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import br.com.bicoemcasa.api.modulos.usuarios.models.Usuario;
import br.com.bicoemcasa.api.modulos.usuarios.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioServiceImpl implements UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final PerfilService perfilService;

    public UsuarioServiceImpl(
            UsuarioRepository repository,
            PasswordEncoder encoder,
            PerfilService perfilService) {
        this.repository = repository;
        this.encoder = encoder;
        this.perfilService = perfilService;
    }

    // GET
    @Override
    public UsuarioResponse buscarPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado: " + id));
        UUID perfilId = perfilService.buscarPorUsuarioId(usuario.getId()).id();

        return new UsuarioResponse(usuario.getId(), perfilId, usuario.getNome(), usuario.getEmail());
    }

    @Override
    public UsuarioResponse buscarPorEmail(String email) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado: " + email));
        UUID perfilId = perfilService.buscarPorUsuarioId(usuario.getId()).id();

        return new UsuarioResponse(usuario.getId(), perfilId, usuario.getNome(), usuario.getEmail());
    }

    @Override
    public UsuarioExiste usuarioExiste(Long id) {
        boolean usuarioExiste = repository.existsById(id);
        boolean statusDoUsuario = repository.findAtivoById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado: " + id));

        return new UsuarioExiste(usuarioExiste, statusDoUsuario);
    }

    @Override
    public Optional<CredenciaisUsuario> buscarCredenciaisAtivasParaLogin(String email) {
        return repository.buscarAtivoPorEmailParaAtualizar(email)
                .map(usuario -> new CredenciaisUsuario(usuario.getId(), usuario.getHashSenha(), usuario.getVersaoSessao()));
    }

    @Override
    public Optional<Long> buscarVersaoAtivaParaRenovacao(Long id) {
        return repository.buscarParaAtualizar(id).filter(Usuario::isAtivo).map(Usuario::getVersaoSessao);
    }

    @Override
    public boolean sessaoValida(Long id, long versao) {
        return repository.existsByIdAndAtivoTrueAndVersaoSessao(id, versao);
    }

    @Override
    public void exigirAdministradorAtivo(Long id) {
        if (!repository.existsByIdAndAtivoTrue(id)
                || perfilService.encontrarPorUsuarioETipo(id, PerfilTipo.ADMIN).isEmpty()) {
            throw new AccessDeniedException("Acesso restrito ao administrador");
        }
    }

    @Override
    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        if (request.perfil().tipo() == PerfilTipo.ADMIN) {
            throw new CadastroNaoPermitidoException("Cadastro público não pode criar conta do tipo ADMIN");
        }
        return persistirCadastro(request);
    }

    @Override
    @Transactional
    public UsuarioResponse criarAdministrador(Long autorId, UsuarioRequest request) {
        repository.buscarParaAtualizar(autorId)
                .orElseThrow(() -> new AccessDeniedException("Acesso restrito ao administrador"));
        exigirAdministradorAtivo(autorId);
        if (request.perfil().tipo() != PerfilTipo.ADMIN) {
            throw new IllegalArgumentException("O provisionamento administrativo requer perfil ADMIN");
        }
        if (repository.existsByCpf(request.cpf())) {
            throw new ModeracaoConflitoException("Já existe um usuário com esse CPF");
        }
        return persistirCadastro(request);
    }

    private UsuarioResponse persistirCadastro(UsuarioRequest request) {
        String hashSenha = encoder.encode(request.senha());

        // A autenticação identifica a conta por e-mail, enquanto a restrição legada cobre o par e-mail/CPF.
        repository.bloquearCadastroPorEmail(request.email());
        if (repository.existsByEmail(request.email())) {
            throw new ModeracaoConflitoException("Já existe um usuário com esse e-mail");
        }

        Usuario usuarioNovo = new Usuario();
        usuarioNovo.setNome(request.nome());
        usuarioNovo.setEmail(request.email());
        usuarioNovo.setCpf(request.cpf());
        usuarioNovo.setHashSenha(hashSenha);
        usuarioNovo.setAtivo(true);
        repository.save(usuarioNovo);
        PerfilResponse perfilNovo = perfilService.criar(usuarioNovo.getId(), request.perfil());

        return new UsuarioResponse(usuarioNovo.getId(), perfilNovo.id(), usuarioNovo.getNome(), usuarioNovo.getEmail());
    }
}
