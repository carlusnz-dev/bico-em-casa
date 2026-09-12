package br.com.bicoemcasa.api.core;

import br.com.bicoemcasa.api.core.excecao.EntidadeNaoEncontradaException;
import br.com.bicoemcasa.api.core.excecao.SenhaNaoBateException;
import br.com.bicoemcasa.api.core.excecao.TokenInvalidoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
                HttpStatus.BAD_REQUEST,
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
}
