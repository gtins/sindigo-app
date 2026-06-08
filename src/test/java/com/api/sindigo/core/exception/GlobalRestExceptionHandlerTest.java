package com.api.sindigo.core.exception;

import com.api.sindigo.exception.BusinessRuleException;
import com.api.sindigo.exception.ResourceNotFoundException;
import com.api.sindigo.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalRestExceptionHandlerTest {

    private final GlobalRestExceptionHandler handler = new GlobalRestExceptionHandler();

    @Test
    void handleResourceNotFoundReturnsNotFoundStatus() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Condomínio não encontrado");
        
        ResponseEntity<?> response = handler.handleResourceNotFound(exception);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleBusinessRuleReturnsBAadRequestStatus() {
        BusinessRuleException exception = new BusinessRuleException("Regra de negócio violada");
        
        ResponseEntity<?> response = handler.handleBusinessRule(exception);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleValidationReturnsBAadRequestStatus() {
        ValidationException exception = new ValidationException("Erro de validação: campo inválido");
        
        ResponseEntity<?> response = handler.handleValidation(exception);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleIllegalArgumentReturnsBAadRequestStatus() {
        IllegalArgumentException exception = new IllegalArgumentException("Argumento inválido");
        
        ResponseEntity<?> response = handler.handleIllegalArgument(exception);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleGenericExceptionReturnsInternalServerErrorStatus() {
        Exception exception = new RuntimeException("Erro inesperado");
        
        ResponseEntity<?> response = handler.handleGeneric(exception);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleResourceNotFoundIncludesCorrectErrorType() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Usuário não encontrado");
        
        ResponseEntity<?> response = handler.handleResourceNotFound(exception);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleBusinessRuleIncludesErrorMessage() {
        BusinessRuleException exception = new BusinessRuleException("Não é possível deletar este registro");
        
        ResponseEntity<?> response = handler.handleBusinessRule(exception);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleMultipleDifferentExceptions() {
        ResponseEntity<?> response1 = handler.handleResourceNotFound(new ResourceNotFoundException("Não achou"));
        ResponseEntity<?> response2 = handler.handleValidation(new ValidationException("Inválido"));
        ResponseEntity<?> response3 = handler.handleGeneric(new Exception("Erro genérico"));

        assertEquals(HttpStatus.NOT_FOUND, response1.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response3.getStatusCode());
    }

    @Test
    void handleBusinessRuleWithNullMessage() {
        BusinessRuleException exception = new BusinessRuleException(null);
        
        ResponseEntity<?> response = handler.handleBusinessRule(exception);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handlesValidationExceptionWithSpecialCharacters() {
        ValidationException exception = new ValidationException("Campo inválido: @#$%&*()");
        
        ResponseEntity<?> response = handler.handleValidation(exception);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}

