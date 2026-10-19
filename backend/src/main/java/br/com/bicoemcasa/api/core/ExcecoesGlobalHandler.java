package br.com.bicoemcasa.api.core;

import br.com.bicoemcasa.api.core.excecao.AvaliacaoJaExisteException;
import br.com.bicoemcasa.api.core.excecao.AvaliacaoNaoPertenceAoPerfilException;
import br.com.bicoemcasa.api.core.excecao.CadastroNaoPermitidoException;
import br.com.bicoemcasa.api.core.excecao.ContratacaoNaoPertenceAoPerfilException;
import br.com.bicoemcasa.api.core.excecao.DenunciaNaoPendenteException;
import br.com.bicoemcasa.api.core.excecao.DenunciaNaoPertenceAoPerfilException;
import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.core.excecao.PortfolioConflitoException;
import br.com.bicoemcasa.api.core.excecao.PortfolioNaoPertenceAoPerfilException;
import br.com.bicoemcasa.api.core.excecao.SenhaNaoBateException;
import br.com.bicoemcasa.api.core.excecao.ServicoNaoPertenceAoPerfilException;
import br.com.bicoemcasa.api.core.excecao.TokenInvalidoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExcecoesGlobalHandler {
    // 404
    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ProblemDetail tratarEntidadeNaoEncontrada(
            EntidadeNaoEncontradaException ex,
            HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Entidade não encontrada");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }
    // erro de validação do formulário / DTO
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail tratarValidacaoDados(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Um ou mais campos estão inválidos"
        );
        problemDetail.setTitle("Erro de validação");
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        // coletar os campos e a mensagem de erro correspondente
        Map<String, String> erros = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

        problemDetail.setProperty("erros", erros);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(SenhaNaoBateException.class)
    public ProblemDetail credenciaisInvalidas(
            SenhaNaoBateException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "E-mail ou senha estão incorretas"
        );
        problemDetail.setTitle("Credenciais inválidas");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(CadastroNaoPermitidoException.class)
    public ProblemDetail cadastroNaoPermitido(
            CadastroNaoPermitidoException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                ex.getMessage()
        );
        problemDetail.setTitle("Cadastro não permitido");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(ServicoNaoPertenceAoPerfilException.class)
    public ProblemDetail servicoNaoPertenceAoPerfil(
            ServicoNaoPertenceAoPerfilException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                ex.getMessage()
        );
        problemDetail.setTitle("Ação não permitida");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(ContratacaoNaoPertenceAoPerfilException.class)
    public ProblemDetail contratacaoNaoPertenceAoPerfil(
            ContratacaoNaoPertenceAoPerfilException ex,
            HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                ex.getMessage()
        );
        problemDetail.setTitle("Ação não permitida");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(AvaliacaoJaExisteException.class)
    public ProblemDetail avaliacaoJaExiste(
            AvaliacaoJaExisteException ex,
            HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problemDetail.setTitle("Avaliação já existe");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(AvaliacaoNaoPertenceAoPerfilException.class)
    public ProblemDetail avaliacaoNaoPertenceAoPerfil(
            AvaliacaoNaoPertenceAoPerfilException ex,
            HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                ex.getMessage()
        );
        problemDetail.setTitle("Ação não permitida");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(PortfolioNaoPertenceAoPerfilException.class)
    public ProblemDetail portfolioNaoPertenceAoPerfil(
            PortfolioNaoPertenceAoPerfilException ex,
            HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                ex.getMessage()
        );
        problemDetail.setTitle("Ação não permitida");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(DenunciaNaoPertenceAoPerfilException.class)
    public ProblemDetail denunciaNaoPertenceAoPerfil(
            DenunciaNaoPertenceAoPerfilException ex,
            HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                ex.getMessage()
        );
        problemDetail.setTitle("Ação não permitida");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(PortfolioConflitoException.class)
    public ProblemDetail portfolioConflito(
            PortfolioConflitoException ex,
            HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problemDetail.setTitle("Conflito");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(DenunciaNaoPendenteException.class)
    public ProblemDetail denunciaNaoPendente(
            DenunciaNaoPendenteException ex,
            HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problemDetail.setTitle("Denúncia não está pendente");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ProblemDetail tokenInvalido(
            TokenInvalidoException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );

        problemDetail.setTitle("Token inválido");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail tratarArgumentoTipoInvalido(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "O parâmetro " + ex.getName() + " recebeu um valor inválido"
        );
        problemDetail.setTitle("Parâmetro inválido");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    // erro de validação de @RequestParam / @PathVariable em controller com @Validated
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail tratarParametroInvalido(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Um ou mais parâmetros estão inválidos"
        );
        problemDetail.setTitle("Erro de validação");
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        // o caminho vem como "metodo.parametro"; fica só o nome do parâmetro
        Map<String, String> erros = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violacao -> {
                            String caminho = violacao.getPropertyPath().toString();
                            return caminho.substring(caminho.lastIndexOf('.') + 1);
                        },
                        ConstraintViolation::getMessage,
                        (primeira, segunda) -> primeira
                ));

        problemDetail.setProperty("erros", erros);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    // violação de constraint do banco, como e-mail, CPF ou nome de usuário já cadastrado
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail tratarViolacaoDeIntegridade(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Já existe um registro com esses dados"
        );
        problemDetail.setTitle("Conflito");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }
}
