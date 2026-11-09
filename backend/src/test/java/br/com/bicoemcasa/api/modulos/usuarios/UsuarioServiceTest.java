package br.com.bicoemcasa.api.modulos.usuarios;

import br.com.bicoemcasa.api.core.excecao.CadastroNaoPermitidoException;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.PerfilRequest;
import br.com.bicoemcasa.api.modulos.usuarios.dto.UsuarioRequest;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import br.com.bicoemcasa.api.modulos.usuarios.repository.UsuarioRepository;
import br.com.bicoemcasa.api.modulos.usuarios.services.UsuarioServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @Mock UsuarioRepository usuarios;
    @Mock PasswordEncoder encoder;
    @Mock PerfilService perfis;
    @InjectMocks UsuarioServiceImpl service;

    @Test
    void cadastroPublicoRecusaAdministradorAntesDePersistir() {
        var perfil = new PerfilRequest(PerfilTipo.ADMIN, "admin", "Administrador", null, null, null);
        var cadastro = new UsuarioRequest("Administrador", "admin@exemplo.test", "12345678901", "senha-exemplo", perfil);

        assertThatThrownBy(() -> service.criar(cadastro)).isInstanceOf(CadastroNaoPermitidoException.class);
        verifyNoInteractions(usuarios, encoder, perfis);
    }
}
