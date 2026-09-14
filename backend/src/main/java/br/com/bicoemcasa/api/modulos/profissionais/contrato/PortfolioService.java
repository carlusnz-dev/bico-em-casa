package br.com.bicoemcasa.api.modulos.profissionais.contrato;
import br.com.bicoemcasa.api.modulos.profissionais.Portfolio;
import java.util.List;

public interface PortfolioService {

    Portfolio criar(Portfolio portfolio);

    List<Portfolio> listar();

    Portfolio buscarPorId(Long Id);

    Portfolio atualizar(Long id, Portfolio portfolio);

    void deletar(Long id);


}