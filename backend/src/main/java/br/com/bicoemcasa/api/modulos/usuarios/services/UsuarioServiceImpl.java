package br.com.bicoemcasa.api.modulos.usuarios.services;

import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.UsuarioService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.*;
import br.com.bicoemcasa.api.modulos.usuarios.models.Usuario;
import br.com.bicoemcasa.api.modulos.usuarios.repository.UsuarioRepository;
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
    public Optional<CredenciaisUsuario> buscarCredenciaisPorEmail(String email) {
        return repository.findByEmail(email)
                .map(usuario -> new CredenciaisUsuario(usuario.getId(), usuario.getHashSenha()));
    }

    @Override
    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        // verificação da senha
        if (request.senha().length() < 8) {
            throw new RuntimeException("Senha possui menos de 8 caractéres");
        }

        String hashSenha = encoder.encode(request.senha());

        // Setters
        Usuario usuarioNovo = new Usuario();
        usuarioNovo.setNome(request.nome());
        usuarioNovo.setEmail(request.email());
        usuarioNovo.setCpf(request.cpf());
        usuarioNovo.setHashSenha(hashSenha);
        repository.save(usuarioNovo); // salva o usuário no banco
        PerfilResponse perfilNovo = perfilService.criar(usuarioNovo.getId(), request.perfil()); // cria o perfil do usuário criado

        return new UsuarioResponse(usuarioNovo.getId(), perfilNovo.id(), usuarioNovo.getNome(), usuarioNovo.getEmail());
    }

    @Override
    public UsuarioResponse alterarStatusPorEmail(String email) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado"));

        usuario.setAtivo(false);
        repository.save(usuario);
        UUID perfilId = perfilService.buscarPorUsuarioId(usuario.getId()).id();

        return new UsuarioResponse(
                usuario.getId(),
                perfilId,
                usuario.getNome(),
                usuario.getEmail()
        );
    }
}
