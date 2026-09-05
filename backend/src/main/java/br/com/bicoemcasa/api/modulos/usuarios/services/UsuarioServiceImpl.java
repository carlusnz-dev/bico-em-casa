package br.com.bicoemcasa.api.modulos.usuarios.services;

import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.UsuarioService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.CredenciaisUsuario;
import br.com.bicoemcasa.api.modulos.usuarios.dto.UsuarioRequest;
import br.com.bicoemcasa.api.modulos.usuarios.dto.UsuarioResponse;
import br.com.bicoemcasa.api.modulos.usuarios.models.Usuario;
import br.com.bicoemcasa.api.modulos.usuarios.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public UsuarioServiceImpl(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    // GET
    @Override
    public UsuarioResponse buscarPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado: " + id));

        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }

    @Override
    public UsuarioResponse buscarPorEmail(String email) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado: " + email));

        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }

    @Override
    public Optional<CredenciaisUsuario> buscarCredenciaisPorEmail(String email) {
        return repository.findByEmail(email)
                .map(usuario -> new CredenciaisUsuario(usuario.getId(), usuario.getHashSenha()));
    }

    @Override
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
        repository.save(usuarioNovo);

        return new UsuarioResponse(usuarioNovo.getId(), usuarioNovo.getNome(), usuarioNovo.getEmail());
    }
}
