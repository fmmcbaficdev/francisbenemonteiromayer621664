package br.gov.mt.seplag.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * Exceção lançada quando há erro de validação de negócio
 * Retorna HTTP 400 (Bad Request)
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {

    private final List<String> errors;

    public ValidationException(String message) {
        super(message);
        this.errors = List.of(message);
    }

    public ValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public ValidationException(List<String> errors) {
        super("Erro de validação");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
