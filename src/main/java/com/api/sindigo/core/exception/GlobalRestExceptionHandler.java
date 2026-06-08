package com.api.sindigo.core.exception;

import com.api.sindigo.exception.BusinessRuleException;
import com.api.sindigo.exception.ResourceNotFoundException;
import com.api.sindigo.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalRestExceptionHandler {

	private static final String RESOURCE_NOT_FOUND_ERROR = "Recurso não encontrado";
	private static final String BUSINESS_RULE_ERROR = "Regra de negócio violada";
	private static final String VALIDATION_ERROR = "Erro de validação";
	private static final String INVALID_REQUEST_ERROR = "Requisição inválida";
	private static final String INTERNAL_SERVER_ERROR = "Erro interno do servidor";
	private static final String UNEXPECTED_ERROR_MESSAGE = "Ocorreu um erro inesperado. Tente novamente mais tarde.";

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
		return buildErrorResponse(
				HttpStatus.NOT_FOUND,
				RESOURCE_NOT_FOUND_ERROR,
				ex.getMessage()
		);
	}

	@ExceptionHandler(BusinessRuleException.class)
	public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex) {
		return buildErrorResponse(
				HttpStatus.BAD_REQUEST,
				BUSINESS_RULE_ERROR,
				ex.getMessage()
		);
	}

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
		return buildErrorResponse(
				HttpStatus.BAD_REQUEST,
				VALIDATION_ERROR,
				ex.getMessage()
		);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
		return buildErrorResponse(
				HttpStatus.BAD_REQUEST,
				INVALID_REQUEST_ERROR,
				ex.getMessage()
		);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
		return buildErrorResponse(
				HttpStatus.INTERNAL_SERVER_ERROR,
				INTERNAL_SERVER_ERROR,
				UNEXPECTED_ERROR_MESSAGE
		);
	}

	private ResponseEntity<ErrorResponse> buildErrorResponse(
			HttpStatus status,
			String error,
			String message
	) {
		ErrorResponse response = new ErrorResponse(
				error,
				message,
				status.value()
		);

		return ResponseEntity.status(status).body(response);
	}

	private record ErrorResponse(
			String error,
			String message,
			int status
	) {
	}
}