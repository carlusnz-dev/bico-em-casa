package br.com.bicoemcasa.api.modulos.servicos.services;

import br.com.bicoemcasa.api.modulos.servicos.contrato.TagService;
import br.com.bicoemcasa.api.modulos.servicos.dto.TagResponse;
import br.com.bicoemcasa.api.modulos.servicos.models.Tag;
import br.com.bicoemcasa.api.modulos.servicos.repository.TagRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;

    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public List<TagResponse> listarTodas() {
        return tagRepository.findAll(Sort.by("nome")).stream()
                .map(tag -> new TagResponse(tag.getId(), tag.getNome(), tag.getSlug()))
                .toList();
    }
}
