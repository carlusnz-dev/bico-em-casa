package br.com.bicoemcasa.api.modulos.servicos.dto;
import jakarta.validation.constraints.*;
import java.util.Set;
import java.util.UUID;
public record ServicoModeracaoRequest(
    @NotBlank @Size(max=120) String titulo,
    @NotBlank @Size(max=1000) String descricao,
    @NotEmpty Set<@NotNull UUID> tagIds,
    @NotBlank @Size(max=1000) String motivo) {}
