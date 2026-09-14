package br.com.bicoemcasa.api.modulos.usuarios.services;

import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.PerfilRequest;
import br.com.bicoemcasa.api.modulos.usuarios.dto.PerfilResponse;
import br.com.bicoemcasa.api.modulos.usuarios.models.Perfil;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import br.com.bicoemcasa.api.modulos.usuarios.repository.PerfilRepository;
import br.com.bicoemcasa.api.modulos.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PerfilServiceImpl implements PerfilService {
    private final PerfilRepository repository;
    private final UsuarioRepository usuarioRepository;

    public PerfilServiceImpl(PerfilRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public PerfilResponse buscarPorId(UUID id) {
        Perfil perfil = repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Perfil não encontrado"));

        return new PerfilResponse(perfil.getId(), perfil.getUsuario().getId(),
                perfil.getTipo(), perfil.getNomeUsuario(), perfil.getNomeExibicao(),
                perfil.getFotoUrl());
    }

    @Override
    public PerfilResponse buscarPorUsuarioId(Long usuarioId) {
        Perfil perfil = repository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Perfil não encontrado"));

        return new PerfilResponse(perfil.getId(), perfil.getUsuario().getId(),
                perfil.getTipo(), perfil.getNomeUsuario(), perfil.getNomeExibicao(),
                perfil.getFotoUrl());
    }

    @Override
    public PerfilResponse buscarPorUsuarioIdETipo(Long usuarioId, PerfilTipo tipo) {
        Perfil perfil = repository.findByUsuarioIdAndTipo(usuarioId, tipo)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Perfil não encontrado"));

        return new PerfilResponse(perfil.getId(), perfil.getUsuario().getId(),
                perfil.getTipo(), perfil.getNomeUsuario(), perfil.getNomeExibicao(),
                perfil.getFotoUrl());
    }

    @Override
    public PerfilResponse buscarPorSlug(String slugUrl) {
        Perfil perfil = repository.findByNomeExibicao(slugUrl)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Perfil não encontrado"));

        return new PerfilResponse(perfil.getId(), perfil.getUsuario().getId(),
                perfil.getTipo(), perfil.getNomeUsuario(), perfil.getNomeExibicao(),
                perfil.getFotoUrl());
    }

    @Override
    public PerfilResponse criar(Long usuarioId, PerfilRequest request) {
        Perfil perfilNovo = new Perfil();
        perfilNovo.setTipo(request.tipo());
        perfilNovo.setNomeUsuario(request.nomeUsuario());
        perfilNovo.setNomeExibicao(request.nomeExibicao());
        perfilNovo.setUsuario(usuarioRepository.getReferenceById(usuarioId));
        perfilNovo.setTelefone(request.telefone());
        perfilNovo.setBio(request.bio());

        repository.save(perfilNovo);

        return new PerfilResponse(perfilNovo.getId(), usuarioId, perfilNovo.getTipo(),
                perfilNovo.getNomeUsuario(), perfilNovo.getNomeExibicao(),
                perfilNovo.getFotoUrl());
    }
}
