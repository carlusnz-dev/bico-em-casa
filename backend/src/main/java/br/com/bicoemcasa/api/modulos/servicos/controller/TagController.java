package br.com.bicoemcasa.api.modulos.servicos.controller;

import br.com.bicoemcasa.api.modulos.servicos.contrato.TagService;
import br.com.bicoemcasa.api.modulos.servicos.dto.TagResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tag")
public class TagController {
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<List<TagResponse>> listarTodas() {
        return ResponseEntity.ok(tagService.listarTodas());
    }
}
