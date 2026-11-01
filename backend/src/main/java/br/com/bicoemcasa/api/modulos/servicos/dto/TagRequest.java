package br.com.bicoemcasa.api.modulos.servicos.dto;
import jakarta.validation.constraints.*;
public record TagRequest(@NotBlank @Size(max=50) String nome) {
    public TagRequest { if (nome != null) nome = nome.strip(); }
}
