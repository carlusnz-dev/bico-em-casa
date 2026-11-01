package br.com.bicoemcasa.api.modulos.servicos.services;

import br.com.bicoemcasa.api.core.RegistroAuditoria;
import br.com.bicoemcasa.api.core.excecao.*;
import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.modulos.servicos.contrato.ServicoAdminService;
import br.com.bicoemcasa.api.modulos.servicos.dto.*;
import br.com.bicoemcasa.api.modulos.servicos.models.*;
import br.com.bicoemcasa.api.modulos.servicos.repository.*;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServicoAdminServiceImpl implements ServicoAdminService {
    private final ServicoRepository servicos;
    private final TagRepository tags;
    private final PerfilService perfis;
    private final RegistroAuditoria auditoria;
    public ServicoAdminServiceImpl(ServicoRepository servicos, TagRepository tags, PerfilService perfis, RegistroAuditoria auditoria) {
        this.servicos=servicos; this.tags=tags; this.perfis=perfis; this.auditoria=auditoria;
    }
    private void exigirAdmin(Long usuario) {
        try {
            if (perfis.buscarPorUsuarioId(usuario).tipo() == PerfilTipo.ADMIN) return;
        } catch (EntidadeNaoEncontradaException ex) {
            throw new AccessDeniedException("Acesso restrito ao administrador");
        }
        throw new AccessDeniedException("Acesso restrito ao administrador");
    }
    @Override
    @Transactional(readOnly=true)
    public PaginaResponse<ServicoAdminResponse> listar(Long usuario, int pagina, int tamanho, Boolean ativo, UUID categoria, String busca) {
        exigirAdmin(usuario);
        String termo = Normalizer.normalize(busca == null ? "" : busca.strip(), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
        String padrao = "%" + termo.replace("!","!!").replace("%","!%").replace("_","!_") + "%";
        List<UUID> profissionais = termo.isEmpty() ? List.of() : perfis.buscarProfissionais(padrao);
        Specification<Servico> filtro = (raiz, query, cb) -> {
            var condicoes = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (ativo != null) condicoes.add(cb.equal(raiz.get("ativo"), ativo));
            if (categoria != null) condicoes.add(cb.equal(raiz.join("tags").get("id"), categoria));
            if (!termo.isEmpty()) {
                var titulo = cb.function("translate", String.class, cb.lower(raiz.get("titulo")),
                    cb.literal("áàâãäéèêëíìîïóòôõöúùûüç"), cb.literal("aaaaaeeeeiiiiooooouuuuc"));
                condicoes.add(cb.or(cb.like(titulo,padrao,'!'),
                    profissionais.isEmpty() ? cb.disjunction() : raiz.get("perfilId").in(profissionais)));
            }
            return cb.and(condicoes.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        var resultado = servicos.findAll(filtro, PageRequest.of(Math.max(0,pagina), Math.min(50,Math.max(1,tamanho)),
            Sort.by(Sort.Order.desc("criadoEm"),Sort.Order.desc("id"))));
        var nomes = new HashMap<UUID,String>();
        return PaginaResponse.de(resultado, s -> new ServicoAdminResponse(resposta(s),
            nomes.computeIfAbsent(s.getPerfilId(), id -> perfis.buscarPorId(id).nomeUsuario())));
    }
    private Servico buscar(UUID id) {
        return servicos.buscarParaAtualizar(id).orElseThrow(() -> new EntidadeNaoEncontradaException("Serviço não encontrado"));
    }
    private Map<String,Object> campos(Servico s) {
        return Map.of("titulo", s.getTitulo(), "descricao",s.getDescricao(),"tagIds",
            s.getTags().stream().map(t -> t.getId().toString()).sorted().toList(), "ativo",s.isAtivo());
    }
    @Override
    public void editar(Long usuario, UUID id, ServicoModeracaoRequest dados, String ip) {
        exigirAdmin(usuario);
        Servico s=buscar(id);
        var anteriores=campos(s);
        var categorias=tags.buscarParaVincular(dados.tagIds());
        if (categorias.size()!=dados.tagIds().size()) throw new EntidadeNaoEncontradaException("Categoria não encontrada");
        s.setTitulo(dados.titulo().strip()); s.setDescricao(dados.descricao().strip()); s.setTags(new HashSet<>(categorias));
        auditoria.registrar(usuario,"EDITAR_SERVICO","servico",id.toString(),ip,dados.motivo(),anteriores,campos(s));
        perfis.notificarModeracao(s.getPerfilId(), id, dados.motivo());
    }
    @Override
    public void situacao(Long usuario, UUID id, boolean ativo, String motivo, String ip) {
        exigirAdmin(usuario);
        Servico s=buscar(id);
        var anterior=campos(s); s.setAtivo(ativo);
        auditoria.registrar(usuario,ativo ? "REATIVAR_SERVICO" : "DESATIVAR_SERVICO", "servico",id.toString(),ip,motivo,anterior,campos(s));
    }
    @Override
    public TagResponse renomear(Long usuario, UUID id, TagRequest dados, String ip) {
        exigirAdmin(usuario);
        Tag t=tags.buscarParaAtualizar(id).orElseThrow(() -> new EntidadeNaoEncontradaException("Categoria não encontrada"));
        if (tags.nomeEmUso(dados.nome(),id)) throw new ModeracaoConflitoException("Já existe uma categoria com esse nome");
        String anterior=t.getNome(); t.setNome(dados.nome());
        tags.flush();
        auditoria.registrar(usuario,"RENOMEAR_CATEGORIA","tag",id.toString(),ip,"Manutenção de categoria",Map.of("nome",anterior),Map.of("nome",t.getNome()));
        return new TagResponse(t.getId(),t.getNome(),t.getSlug());
    }
    @Override
    public void removerCategoria(Long usuario, UUID id, String ip) {
        exigirAdmin(usuario);
        Tag t=tags.buscarParaAtualizar(id).orElseThrow(() -> new EntidadeNaoEncontradaException("Categoria não encontrada"));
        long quantidade=tags.contarVinculos(id);
        if (quantidade>0) throw new ModeracaoConflitoException("Categoria usada por " + quantidade + " serviço(s)");
        tags.delete(t); tags.flush();
        auditoria.registrar(usuario,"REMOVER_CATEGORIA","tag",id.toString(),ip,"Categoria sem vínculos",Map.of("nome",t.getNome()),Map.of());
    }
    private ServicoResponse resposta(Servico s) {
        return new ServicoResponse(s.getId(),s.getPerfilId(),s.getTitulo(),s.getDescricao(),s.getPrecoPrevio(),s.getUnidadePreco(),s.isAtivo(),s.getTags().stream().map(Tag::getId).collect(Collectors.toSet()),s.getCriadoEm());
    }
}
