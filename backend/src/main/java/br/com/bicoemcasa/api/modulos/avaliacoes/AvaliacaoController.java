package br.com.bicoemcasa.api.modulos.avaliacoes;


import br.com.bicoemcasa.api.modulos.avaliacoes.dto.AvaliacaoRequest;
import br.com.bicoemcasa.api.modulos.avaliacoes.dto.AvaliacaoResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService){
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping("/{id}")
    public AvaliacaoResponse buscarPorId(@PathVariable UUID id) {
        return avaliacaoService.buscarPorId(id);
    }

    @GetMapping("/avaliado/{avaliadoId}")
    public List<AvaliacaoResponse> buscarPorAvaliado(@PathVariable Long avaliadoId) {
        return avaliacaoService.buscarPorAvaliado(avaliadoId);
    }


    @PostMapping("/{contratacaoId}")
    public AvaliacaoResponse criar(@PathVariable UUID contratacaoId,
                                   @Valid @RequestBody AvaliacaoRequest request) {
        return avaliacaoService.criar(contratacaoId , request);
    }

    @PutMapping("/{id}")
    public AvaliacaoResponse alterar(@PathVariable UUID id,
                                    @Valid @RequestBody AvaliacaoRequest request){

        return avaliacaoService.alterar(id, request);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable UUID id){

        avaliacaoService.deletar(id);
    }
}
