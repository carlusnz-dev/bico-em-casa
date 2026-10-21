package br.com.bicoemcasa.api.modulos.autenticacao.dto;

import br.com.bicoemcasa.api.modulos.usuarios.dto.EnderecoRequest;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CadastroRequest(
        @NotBlank String nomeCompleto,
        @NotBlank @Size(max = 50) String nomeUsuario,
        @NotBlank String email,
        @NotBlank String telefone,
        @NotBlank String nomeExibicao,
        @NotNull PerfilTipo cadastroTipo,
        @NotBlank @Size(min = 11, max = 11) String cpf,
        @NotBlank String senha,
        String bio,
        @Valid EnderecoRequest endereco
) {
    @AssertTrue(message = "Profissionais devem informar um endereço")
    public boolean isEnderecoValidoParaTipoDeCadastro() {
        return cadastroTipo != PerfilTipo.PROFISSIONAL || endereco != null;
    }
}
