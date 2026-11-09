package br.com.bicoemcasa.api.modulos.usuarios.dto;
import java.util.UUID;
public record AvisoModeracaoResponse(UUID id, String mensagem, String alvoId, String titulo) {}
