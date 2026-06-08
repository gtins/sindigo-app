package com.api.sindigo.config;

import com.api.sindigo.exception.BusinessRuleException;
import com.api.sindigo.exception.ResourceNotFoundException;
import com.api.sindigo.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesBusinessRuleExceptionAsBadRequest() {
        var response = handler.handleBusinessRuleException(new BusinessRuleException("regra quebrada"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertErrorResponse(response.getBody(), 400, "Bad Request", "regra quebrada");
    }

    @Test
    void handlesResourceNotFoundExceptionAsNotFound() {
        var response = handler.handleResourceNotFoundException(new ResourceNotFoundException("não achou"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertErrorResponse(response.getBody(), 404, "Not Found", "não achou");
    }

    @Test
    void handlesValidationExceptionAsBadRequest() {
        var response = handler.handleValidationException(new ValidationException("inválido"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertErrorResponse(response.getBody(), 400, "Bad Request", "inválido");
    }

    @Test
    void handlesUploadSizeExceededExceptionAsPayloadTooLarge() {
        var response = handler.handleMaxUploadSizeExceededException(new MaxUploadSizeExceededException(10L));

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertErrorResponse(
                response.getBody(),
                413,
                "Payload Too Large",
                "File size exceeds maximum allowed size of 10MB"
        );
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
        assertErrorResponse(
                response.getBody(),
                500,
                "Internal Server Error",
                "Error communicating with storage service. Please try again later."
        );
    }

    @Test
    void handlesGenericExceptionAsInternalServerError() {
        var response = handler.handleGenericException(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertErrorResponse(
                response.getBody(),
                500,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later."
        );
    }

    private void assertErrorResponse(
            Object body,
            int expectedStatus,
            String expectedError,
            String expectedMessage
    ) {
        assertNotNull(body);
        assertNotNull(readField(body, "timestamp"));
        assertEquals(expectedStatus, readField(body, "status"));
        assertEquals(expectedError, readField(body, "error"));
        assertEquals(expectedMessage, readField(body, "message"));
    }

    private Object readField(Object body, String accessorName) {
        try {
            Method method = body.getClass().getDeclaredMethod(accessorName);
            method.setAccessible(true);
            return method.invoke(body);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Could not read error response field: " + accessorName, e);
        }
    }
}