package br.com.bicoemcasa.api.modulos.autenticacao.dto;

import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import jakarta.validation.constraints.*;

public record CadastroRequest(
        @NotBlank String nomeCompleto,

        @NotBlank @Size(max = 50) String nomeUsuario,

        @NotBlank
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank String telefone,

        @NotBlank String nomeExibicao,

        @NotNull PerfilTipo cadastroTipo,

        @NotBlank @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 dígitos") String cpf,

        @NotBlank
        @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
        String senha,

        String bio
) { }
