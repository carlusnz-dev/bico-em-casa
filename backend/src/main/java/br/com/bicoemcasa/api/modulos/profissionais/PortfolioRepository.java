package br.com.bicoemcasa.api.modulos.profissionais;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
// esse extends diz Entidade = Portifolio    Id = Long  (QUER DIZER QUE O PORTFOLIO VAI TRABALHAR COM OBJETOS PORtFOLIOS E O ID DELES É LONG)

}
