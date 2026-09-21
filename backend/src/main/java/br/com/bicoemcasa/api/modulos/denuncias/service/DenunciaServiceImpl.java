package br.com.bicoemcasa.api.modulos.denuncias.service;

import br.com.bicoemcasa.api.core.excecao.DenunciaNaoPendenteException;
import br.com.bicoemcasa.api.core.excecao.DenunciaNaoPertenceAoPerfilException;
import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.modulos.denuncias.contrato.DenunciaService;
import br.com.bicoemcasa.api.modulos.denuncias.dto.DenunciaAnaliseRequest;
import br.com.bicoemcasa.api.modulos.denuncias.dto.DenunciaAtualizacaoRequest;
import br.com.bicoemcasa.api.modulos.denuncias.dto.DenunciaRequest;
import br.com.bicoemcasa.api.modulos.denuncias.dto.DenunciaResponse;
import br.com.bicoemcasa.api.modulos.denuncias.models.Denuncia;
import br.com.bicoemcasa.api.modulos.denuncias.models.StatusDenuncia;
import br.com.bicoemcasa.api.modulos.denuncias.repository.DenunciaRepository;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.PerfilResponse;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class DenunciaServiceImpl implements DenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final PerfilService perfilService;

    public DenunciaServiceImpl(DenunciaRepository denunciaRepository, PerfilService perfilService) {
        this.denunciaRepository = denunciaRepository;
        this.perfilService = perfilService;
    }

    @Override
    @Transactional
    public DenunciaResponse criar(Long usuarioId, DenunciaRequest request) {
        PerfilResponse autor = perfilService.buscarPorUsuarioId(usuarioId);

        Denuncia denuncia = new Denuncia();
        denuncia.setAutorPerfilId(autor.id());
        denuncia.setDenunciadoPerfilId(request.denunciadoPerfilId());
        denuncia.setContratacaoId(request.contratacaoId());
        denuncia.setAlvoTipo(request.alvoTipo());
        denuncia.setAlvoId(request.alvoId().trim().toLowerCase());
        denuncia.setMotivo(request.motivo());
        denuncia.setDescricao(request.descricao());

        denunciaRepository.save(denuncia);

        return paraResponse(denuncia);
    }

    @Override
    @Transactional(readOnly = true)
    public DenunciaResponse buscarPorId(UUID id, Long usuarioId) {
        return paraResponse(buscarDenunciaDoAutor(id, usuarioId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DenunciaResponse> listarDoAutor(Long usuarioId, Pageable pageable) {
        PerfilResponse autor = perfilService.buscarPorUsuarioId(usuarioId);
        return denunciaRepository.findByAutorPerfilId(autor.id(), pageable).map(this::paraResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DenunciaResponse> listarParaAdmin(Long usuarioIdAdmin, StatusDenuncia status, Pageable pageable) {
        perfilService.buscarPorUsuarioIdETipo(usuarioIdAdmin, PerfilTipo.ADMIN);

        Page<Denuncia> pagina = status == null
                ? denunciaRepository.findAll(pageable)
                : denunciaRepository.findByStatus(status, pageable);

        return pagina.map(this::paraResponse);
    }

    @Override
    @Transactional
    public DenunciaResponse atualizar(UUID id, Long usuarioId, DenunciaAtualizacaoRequest request) {
        Denuncia denuncia = buscarDenunciaDoAutor(id, usuarioId);
        exigirPendente(denuncia);

        denuncia.setMotivo(request.motivo());
        denuncia.setDescricao(request.descricao());

        denunciaRepository.saveAndFlush(denuncia);

        return paraResponse(denuncia);
    }

    @Override
    @Transactional
    public void excluir(UUID id, Long usuarioId) {
        Denuncia denuncia = buscarDenunciaDoAutor(id, usuarioId);
        exigirPendente(denuncia);

        denunciaRepository.delete(denuncia);
    }

    @Override
    @Transactional
    public DenunciaResponse analisar(UUID id, Long usuarioIdAdmin, DenunciaAnaliseRequest request) {
        PerfilResponse admin = perfilService.buscarPorUsuarioIdETipo(usuarioIdAdmin, PerfilTipo.ADMIN);
        Denuncia denuncia = buscarExistente(id);
        exigirPendente(denuncia);

        denuncia.setStatus(request.resultado());
        denuncia.setParecer(request.parecer());
        denuncia.setAnalisadoPorPerfilId(admin.id());
        denuncia.setAnalisadoEm(OffsetDateTime.now());

        denunciaRepository.saveAndFlush(denuncia);

        return paraResponse(denuncia);
    }

    private Denuncia buscarExistente(UUID id) {
        return denunciaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Denúncia não encontrada"));
    }

    private Denuncia buscarDenunciaDoAutor(UUID id, Long usuarioId) {
        Denuncia denuncia = buscarExistente(id);
        PerfilResponse autor = perfilService.buscarPorUsuarioId(usuarioId);

        if (!denuncia.getAutorPerfilId().equals(autor.id())) {
            throw new DenunciaNaoPertenceAoPerfilException("Esta denúncia não pertence ao seu perfil");
        }

        return denuncia;
    }

    private void exigirPendente(Denuncia denuncia) {
        if (denuncia.getStatus() != StatusDenuncia.PENDENTE) {
            throw new DenunciaNaoPendenteException("Apenas denúncias pendentes podem ser atualizadas, excluídas ou analisadas");
        }
    }

    private DenunciaResponse paraResponse(Denuncia denuncia) {
        return new DenunciaResponse(
                denuncia.getId(),
                denuncia.getAutorPerfilId(),
                denuncia.getDenunciadoPerfilId(),
                denuncia.getContratacaoId(),
                denuncia.getAlvoTipo(),
                denuncia.getAlvoId(),
                denuncia.getMotivo(),
                denuncia.getDescricao(),
                denuncia.getStatus(),
                denuncia.getAnalisadoPorPerfilId(),
                denuncia.getAnalisadoEm(),
                denuncia.getParecer(),
                denuncia.getCriadoEm(),
                denuncia.getAtualizadoEm()
        );
    }
}
