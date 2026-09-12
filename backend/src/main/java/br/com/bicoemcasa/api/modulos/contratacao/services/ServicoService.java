package br.com.bicoemcasa.api.modulos.contratacao.services;

import br.com.bicoemcasa.api.modulos.contratacao.dto.ServicoResponseDTO;
import br.com.bicoemcasa.api.modulos.contratacao.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
public class ServicoService {
    private final ServicoRepository servicoRepository;

    @Transactional(readOnly = true)
    public List<ServicoResponseDTO> listarServicosAtivos(){
        return servicoRepository.findByAtivoTrue().stream().map(ServicoResponseDTO::converter).toList();
    }

}
