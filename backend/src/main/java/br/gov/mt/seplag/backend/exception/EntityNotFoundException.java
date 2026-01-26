package br.gov.mt.seplag.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção para entidade não encontrada
 * Retorna HTTP 404 (Not Found)
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String entityName, Long id) {
        super(String.format("%s não encontrado(a) com ID: %d", entityName, id));
    }

    public EntityNotFoundException(String entityName, String identifier) {
        super(String.format("%s não encontrado(a): %s", entityName, identifier));
    }
}