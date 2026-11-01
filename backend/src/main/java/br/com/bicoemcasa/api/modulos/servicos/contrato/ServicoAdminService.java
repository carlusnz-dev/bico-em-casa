package br.com.bicoemcasa.api.modulos.servicos.contrato;
import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import br.com.bicoemcasa.api.modulos.servicos.dto.*;
import java.util.UUID;
public interface ServicoAdminService {
    PaginaResponse<ServicoAdminResponse> listar(Long usuario, int pagina, int tamanho, Boolean ativo, UUID categoria, String busca);
    void editar(Long usuario, UUID id, ServicoModeracaoRequest dados, String ip);
    void situacao(Long usuario, UUID id, boolean ativo, String motivo, String ip);
    TagResponse renomear(Long usuario, UUID id, TagRequest dados, String ip);
    void removerCategoria(Long usuario, UUID id, String ip);
}
