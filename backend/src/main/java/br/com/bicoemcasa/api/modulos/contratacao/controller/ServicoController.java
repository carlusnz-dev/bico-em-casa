package br.com.bicoemcasa.api.modulos.contratacao.controller;

import br.com.bicoemcasa.api.modulos.contratacao.dto.ServicoResponseDTO;
import br.com.bicoemcasa.api.modulos.contratacao.services.ServicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
public class ServicoController {

    private final ServicoService servicoService;

    @GetMapping
    public ResponseEntity<List<ServicoResponseDTO>> listarServicosAtivos(){
        List<ServicoResponseDTO> servicos = servicoService.listarServicosAtivos();
        return ResponseEntity.ok(servicos);
    }
}
