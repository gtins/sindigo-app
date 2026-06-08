package com.api.sindigo.core.security;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecretsValidatorTest {

    private static final String VALID_JWT_SECRET =
            "this-is-a-valid-jwt-secret-with-more-than-thirty-two-characters";

    private static final String VALID_ADMIN_SECRET =
            "valid-admin-secret-key";

    private static final String WEAK_JWT_SECRET =
            "your-super-secret-key-for-development";

    private static final String SHORT_JWT_SECRET =
            "short-secret";

    private static final String WEAK_ADMIN_SECRET =
            "sindigo-hash-teste-admin";

    private static final String SHORT_ADMIN_SECRET =
            "short";

    @Test
    void validateSecretsDoesNotThrowWhenSecretsAreStrong() throws Exception {
        Environment environment = environmentWithProfiles();
        SecretsValidator validator = validatorWith(VALID_JWT_SECRET, VALID_ADMIN_SECRET, environment);

        assertNotNull(validator);
        assertDoesNotThrow(validator::validateSecrets);
        assertEquals(0, environment.getActiveProfiles().length);
    }

    @Test
    void validateSecretsRejectsNullJwtSecret() throws Exception {
        Environment environment = environmentWithProfiles();
        SecretsValidator validator = validatorWith(null, VALID_ADMIN_SECRET, environment);

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::validateSecrets);

        assertNotNull(exception);
        assertEquals("APP_JWT_SECRET environment variable is required", exception.getMessage());
        assertTrue(exception.getMessage().contains("APP_JWT_SECRET"));
    }

    @Test
    void validateSecretsRejectsEmptyJwtSecret() throws Exception {
        Environment environment = environmentWithProfiles();
        SecretsValidator validator = validatorWith("", VALID_ADMIN_SECRET, environment);

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::validateSecrets);

        assertNotNull(exception);
        assertEquals("APP_JWT_SECRET environment variable is required", exception.getMessage());
        assertTrue(exception.getMessage().contains("environment variable is required"));
    }

    @Test
    void validateSecretsAllowsWeakJwtSecretOutsideProduction() throws Exception {
        Environment environment = environmentWithProfiles("dev");
        SecretsValidator validator = validatorWith(WEAK_JWT_SECRET, VALID_ADMIN_SECRET, environment);

        assertNotNull(validator);
        assertDoesNotThrow(validator::validateSecrets);
        assertEquals("dev", environment.getActiveProfiles()[0]);
    }

    @Test
    void validateSecretsRejectsWeakJwtSecretWhenProdProfileIsActive() throws Exception {
        Environment environment = environmentWithProfiles("prod");
        SecretsValidator validator = validatorWith(WEAK_JWT_SECRET, VALID_ADMIN_SECRET, environment);

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::validateSecrets);

        assertNotNull(exception);
        assertEquals("JWT_SECRET é muito fraco para produção", exception.getMessage());
        assertEquals("prod", environment.getActiveProfiles()[0]);
    }

    @Test
    void validateSecretsRejectsShortJwtSecretWhenProductionProfileIsActive() throws Exception {
        Environment environment = environmentWithProfiles("production");
        SecretsValidator validator = validatorWith(SHORT_JWT_SECRET, VALID_ADMIN_SECRET, environment);

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::validateSecrets);

        assertNotNull(exception);
        assertEquals("JWT_SECRET é muito fraco para produção", exception.getMessage());
        assertEquals("production", environment.getActiveProfiles()[0]);
    }

    @Test
    void validateSecretsRejectsNullAdminSecret() throws Exception {
        Environment environment = environmentWithProfiles();
        SecretsValidator validator = validatorWith(VALID_JWT_SECRET, null, environment);

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::validateSecrets);

        assertNotNull(exception);
        assertEquals("APP_ADMIN_SECRET_KEY environment variable is required", exception.getMessage());
        assertTrue(exception.getMessage().contains("APP_ADMIN_SECRET_KEY"));
    }

    @Test
    void validateSecretsRejectsEmptyAdminSecret() throws Exception {
        Environment environment = environmentWithProfiles();
        SecretsValidator validator = validatorWith(VALID_JWT_SECRET, "", environment);

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::validateSecrets);

        assertNotNull(exception);
        assertEquals("APP_ADMIN_SECRET_KEY environment variable is required", exception.getMessage());
        assertTrue(exception.getMessage().contains("environment variable is required"));
    }

    @Test
    void validateSecretsAllowsWeakAdminSecretOutsideProduction() throws Exception {
        Environment environment = environmentWithProfiles("dev");
        SecretsValidator validator = validatorWith(VALID_JWT_SECRET, WEAK_ADMIN_SECRET, environment);

        assertNotNull(validator);
        assertDoesNotThrow(validator::validateSecrets);
        assertEquals("dev", environment.getActiveProfiles()[0]);
    }

    @Test
    void validateSecretsRejectsWeakAdminSecretWhenProdProfileIsActive() throws Exception {
        Environment environment = environmentWithProfiles("prod");
        SecretsValidator validator = validatorWith(VALID_JWT_SECRET, WEAK_ADMIN_SECRET, environment);

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::validateSecrets);

        assertNotNull(exception);
        assertEquals("ADMIN_SECRET_KEY é muito fraco para produção", exception.getMessage());
        assertEquals("prod", environment.getActiveProfiles()[0]);
    }

    @Test
    void validateSecretsRejectsShortAdminSecretWhenProductionProfileIsActive() throws Exception {
        Environment environment = environmentWithProfiles("production");
        SecretsValidator validator = validatorWith(VALID_JWT_SECRET, SHORT_ADMIN_SECRET, environment);

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::validateSecrets);

        assertNotNull(exception);
        assertEquals("ADMIN_SECRET_KEY é muito fraco para produção", exception.getMessage());
        assertEquals("production", environment.getActiveProfiles()[0]);
    }

    private SecretsValidator validatorWith(
            String jwtSecret,
            String adminSecretKey,
            Environment environment
    ) throws Exception {
        SecretsValidator validator = new SecretsValidator(environment);

        setPrivateField(validator, "jwtSecret", jwtSecret);
        setPrivateField(validator, "adminSecretKey", adminSecretKey);

        return validator;
    }

    private Environment environmentWithProfiles(String... profiles) {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(profiles);
        return environment;
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}