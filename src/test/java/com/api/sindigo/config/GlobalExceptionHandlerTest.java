package com.api.sindigo.config;

import com.api.sindigo.exception.BusinessRuleException;
import com.api.sindigo.exception.ResourceNotFoundException;
import com.api.sindigo.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesBusinessRuleExceptionAsBadRequest() {
        var response = handler.handleBusinessRuleException(new BusinessRuleException("regra quebrada"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertBodyMessage(response.getBody(), "regra quebrada");
    }

    @Test
    void handlesResourceNotFoundExceptionAsNotFound() {
        var response = handler.handleResourceNotFoundException(new ResourceNotFoundException("não achou"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertBodyMessage(response.getBody(), "não achou");
    }

    @Test
    void handlesValidationExceptionAsBadRequest() {
        var response = handler.handleValidationException(new ValidationException("inválido"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertBodyMessage(response.getBody(), "inválido");
    }

    @Test
    void handlesUploadSizeExceededExceptionAsPayloadTooLarge() {
        var response = handler.handleMaxUploadSizeExceededException(new MaxUploadSizeExceededException(10L));

        assertEquals(413, response.getStatusCode().value());
        assertBodyMessage(response.getBody(), "File size exceeds maximum allowed size of 10MB");
    }

    @Test
    void handlesS3ExceptionAsInternalServerError() {
        S3Exception ex = mock(S3Exception.class);
        AwsErrorDetails details = mock(AwsErrorDetails.class);
        when(ex.awsErrorDetails()).thenReturn(details);
        when(details.errorCode()).thenReturn("InternalError");
        when(ex.getMessage()).thenReturn("boom");

        var response = handler.handleS3Exception(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertBodyMessage(response.getBody(), "Error communicating with storage service. Please try again later.");
    }

    @Test
    void handlesGenericExceptionAsInternalServerError() {
        var response = handler.handleGenericException(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertBodyMessage(response.getBody(), "An unexpected error occurred. Please try again later.");
    }

    @SuppressWarnings("unchecked")
    private void assertBodyMessage(Object body, String expectedMessage) {
        assertTrue(body instanceof Map);
        Map<String, Object> map = (Map<String, Object>) body;
        assertEquals(expectedMessage, map.get("message"));
        assertTrue(map.containsKey("timestamp"));
        assertTrue(map.containsKey("status"));
        assertTrue(map.containsKey("error"));
    }
}


