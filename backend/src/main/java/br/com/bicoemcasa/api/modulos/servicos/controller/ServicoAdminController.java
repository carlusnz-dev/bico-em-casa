package br.com.bicoemcasa.api.modulos.servicos.controller;
import br.com.bicoemcasa.api.modulos.servicos.contrato.ServicoAdminService;
import br.com.bicoemcasa.api.modulos.servicos.dto.*;
import br.com.bicoemcasa.api.core.paginacao.PaginaResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController
@RequestMapping("/api/admin")
public class ServicoAdminController {
    private final ServicoAdminService service;
    public ServicoAdminController(ServicoAdminService service) { this.service=service; }
    @GetMapping("/servicos")
    public PaginaResponse<ServicoAdminResponse> listar(@AuthenticationPrincipal Jwt jwt,
        @RequestParam(defaultValue="0") int pagina, @RequestParam(defaultValue="20") int tamanho,
        @RequestParam(required=false) Boolean ativo, @RequestParam(required=false) UUID categoria,
        @RequestParam(defaultValue="") String busca) {
        return service.listar(Long.valueOf(jwt.getSubject()),pagina,tamanho,ativo,categoria,busca);
    }
    @PutMapping("/servicos/{id}/moderar")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void editar(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,
        @Valid @RequestBody ServicoModeracaoRequest dados, HttpServletRequest request) {
        service.editar(Long.valueOf(jwt.getSubject()),id,dados,request.getRemoteAddr());
    }
    @PatchMapping("/servicos/{id}/desativar")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void desativar(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,
        @Valid @RequestBody DesativacaoRequest dados,HttpServletRequest request) {
        service.situacao(Long.valueOf(jwt.getSubject()),id,false,dados.motivo(),request.getRemoteAddr());
    }
    @PatchMapping("/servicos/{id}/reativar")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void reativar(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,HttpServletRequest request) {
        service.situacao(Long.valueOf(jwt.getSubject()),id,true,"Reativação pela moderação",request.getRemoteAddr());
    }
    @PutMapping("/categorias/{id}")
    public TagResponse renomear(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,
        @Valid @RequestBody TagRequest dados,HttpServletRequest request) {
        return service.renomear(Long.valueOf(jwt.getSubject()),id,dados,request.getRemoteAddr());
    }
    @DeleteMapping("/categorias/{id}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void remover(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,HttpServletRequest request) {
        service.removerCategoria(Long.valueOf(jwt.getSubject()),id,request.getRemoteAddr());
    }
}
