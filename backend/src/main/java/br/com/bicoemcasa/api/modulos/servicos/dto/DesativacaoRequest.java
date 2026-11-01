package br.com.bicoemcasa.api.modulos.servicos.dto;
import jakarta.validation.constraints.*;
public record DesativacaoRequest(@NotBlank @Size(min=10, max=1000) String motivo) {
    public DesativacaoRequest { if (motivo != null) motivo = motivo.strip(); }
}
