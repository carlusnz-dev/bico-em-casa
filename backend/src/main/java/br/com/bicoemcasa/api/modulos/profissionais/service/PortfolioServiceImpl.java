package br.com.bicoemcasa.api.modulos.profissionais.service;

import br.com.bicoemcasa.api.modulos.profissionais.Portfolio;
import br.com.bicoemcasa.api.modulos.profissionais.PortfolioRepository;
import br.com.bicoemcasa.api.modulos.profissionais.contrato.PortfolioService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    //VARIAVEL
    private final PortfolioRepository portfolioRepository;

    //CONSTRUTOR COM PARAMETROS
    public PortfolioServiceImpl(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    //METODO CRIAR RECEBE UM 'portifolio' DO TIPO 'Portifolio' E SALVA NO 'portfolioRepository'
    @Override
    public Portfolio criar(Portfolio portfolio) {
        return portfolioRepository.save(portfolio);
    }


    //METODO PARA LISTAR PORTIFOLIOS ONDE VAMSO NO REPOSITORY (FIND ALL) E LISTAR
    @Override
    public List<Portfolio> listar() {
        return portfolioRepository.findAll();
    }

    @Override   //Buscar portfolio por Id
    public Portfolio buscarPorId(Long id){
        return portfolioRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Portfolio não encontrado"));
    }

    @Override
    public Portfolio atualizar(Long id, Portfolio portfolio){

        //Passa para a variavel o Portfolio de id escolhido
        Portfolio portfolioExistente = buscarPorId(id);

        //Passa o novo titulo para o Titulo anterior já criado
        portfolioExistente.setTitulo(portfolio.getTitulo());

        //Passa nova descrição no lugar da antiga
        portfolioExistente.setDescricao(portfolio.getDescricao());

        return portfolioRepository.save(portfolioExistente);
    }

    @Override
    public void deletar(Long id){
        buscarPorId(id); //Caso nao exista o metedo buscar por id já para automaticando, visto que coloquei essa opção
        portfolioRepository.deleteById(id);
    }
}