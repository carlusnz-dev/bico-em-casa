package br.com.bicoemcasa.api.modulos.usuarios.dto;

import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdministradorRequest(
        @NotBlank @Size(min = 3, max = 150) String nomeCompleto,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Pattern(regexp = "[0-9]{11}") String cpf,
        @NotBlank @Size(min = 8, max = 50) String senha,
        @NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "[a-z0-9._-]+") String nomeUsuario,
        @NotBlank @Size(min = 2, max = 100) String nomeExibicao
) {
    public UsuarioRequest paraUsuarioRequest() {
        var perfil = new PerfilRequest(PerfilTipo.ADMIN, nomeUsuario, nomeExibicao, null, null, null);
        return new UsuarioRequest(nomeCompleto, email, cpf, senha, perfil);
    }
}
