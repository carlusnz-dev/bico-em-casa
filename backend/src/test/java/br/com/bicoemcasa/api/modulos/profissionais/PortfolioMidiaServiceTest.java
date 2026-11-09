package br.com.bicoemcasa.api.modulos.profissionais;

import br.com.bicoemcasa.api.core.excecao.PortfolioNaoPertenceAoPerfilException;
import br.com.bicoemcasa.api.lib.armazenamento.ClienteArmazenamentoS3;
import br.com.bicoemcasa.api.modulos.profissionais.dto.ConfirmarMidiaRequest;
import br.com.bicoemcasa.api.modulos.usuarios.contrato.PerfilService;
import br.com.bicoemcasa.api.modulos.usuarios.dto.PerfilResponse;
import br.com.bicoemcasa.api.modulos.usuarios.models.PerfilTipo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PortfolioMidiaServiceTest {
    private final PortfolioRepository portfolios = mock(PortfolioRepository.class);
    private final PortfolioMidiaRepository midias = mock(PortfolioMidiaRepository.class);
    private final PerfilService perfis = mock(PerfilService.class);
    private final ClienteArmazenamentoS3 armazenamento = mock(ClienteArmazenamentoS3.class);
    private final PortfolioServiceImpl servico = new PortfolioServiceImpl(portfolios, midias, perfis, armazenamento);
    private final UUID dono = UUID.randomUUID();
    private final String chave = "galeria/1/" + UUID.randomUUID();

    @BeforeEach
    void preparar() {
        var portfolio = new Portfolio();
        portfolio.setId(1L);
        portfolio.setPerfilId(dono);
        when(portfolios.buscarParaAtualizar(1L)).thenReturn(Optional.of(portfolio));
        when(perfis.buscarPorUsuarioIdETipo(1L, PerfilTipo.PROFISSIONAL)).thenReturn(perfil(dono, 1L));
    }

    @Test
    void confirmaVideoSeExistirComFormatoETamanhoValidos() {
        when(armazenamento.verificarObjeto("portfolios", chave, "video/mp4", 25L * 1024 * 1024)).thenReturn(true);
        when(armazenamento.construirUrlPublica("portfolios", chave)).thenReturn("http://localhost/portfolios/" + chave);
        when(midias.save(any())).thenAnswer(invocacao -> {
            PortfolioMidia midia = invocacao.getArgument(0);
            midia.setId(UUID.randomUUID());
            return midia;
        });
        var resposta = servico.confirmarMidia(1L, new ConfirmarMidiaRequest(chave, "video/mp4"), 1L);
        assertThat(resposta.url()).endsWith(chave);
        assertThat(resposta.contentType()).isEqualTo("video/mp4");
        assertThat(resposta.id()).isNotNull();
    }

    @Test
    void rejeitaArquivoQueAindaNaoFoiEnviado() {
        assertThatThrownBy(() -> servico.confirmarMidia(1L, new ConfirmarMidiaRequest(chave, "image/jpeg"), 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Arquivo ausente");
    }

    @Test
    void bloqueiaConfirmacaoPorOutroProfissional() {
        when(perfis.buscarPorUsuarioIdETipo(2L, PerfilTipo.PROFISSIONAL)).thenReturn(perfil(UUID.randomUUID(), 2L));
        assertThatThrownBy(() -> servico.confirmarMidia(1L, new ConfirmarMidiaRequest(chave, "image/jpeg"), 2L))
                .isInstanceOf(PortfolioNaoPertenceAoPerfilException.class);
        verifyNoInteractions(armazenamento, midias);
    }

    @Test
    void bloqueiaChaveDeOutroPortfolio() {
        var arquivoAlheio = new ConfirmarMidiaRequest("galeria/2/" + UUID.randomUUID(), "image/jpeg");
        assertThatThrownBy(() -> servico.confirmarMidia(1L, arquivoAlheio, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Chave de upload inválida");
        verifyNoInteractions(armazenamento, midias);
    }

    private PerfilResponse perfil(UUID id, long usuario) {
        return new PerfilResponse(id, usuario, PerfilTipo.PROFISSIONAL, "perfil", "Perfil", null, null, null);
    }
}
