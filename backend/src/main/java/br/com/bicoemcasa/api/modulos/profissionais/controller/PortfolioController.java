package br.com.bicoemcasa.api.modulos.profissionais.controller;
import org.springframework.web.bind.annotation.*;
import br.com.bicoemcasa.api.modulos.profissionais.contrato.PortfolioService;
import br.com.bicoemcasa.api.modulos.profissionais.Portfolio;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;


@RequestMapping("/portfolios") //define a rota base.
@RestController   //recebe requests HTTP.
public class PortfolioController {

    private final PortfolioService portfolioService;

    //Construtor
    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping   // Quando tiver uma request Tipo Post (EXECUTAR METODO ABAIXO)
    public Portfolio criar(@RequestBody Portfolio portfolio){
        return portfolioService.criar(portfolio);
    }

    @GetMapping  //Responde uma request Get devolvendo varios portifolios
    public List<Portfolio> listar(){
        return portfolioService.listar();
    }

    @GetMapping("{id}")   //Resquest Get no endereço {id}
    public Portfolio buscarPorId(@PathVariable Long id){
        return portfolioService.buscarPorId(id);
    }
    @PutMapping("/{id}")
    public Portfolio atualizar(@PathVariable Long id, @RequestBody Portfolio portfolio){
        return portfolioService.atualizar(id, portfolio);
    }
    @DeleteMapping("/{id}") //Quando chegar uma request DELETE para /portfolios/{id}, execute esse metodo
    public void deletar(@PathVariable Long id){
        portfolioService.deletar(id);
    }
}
