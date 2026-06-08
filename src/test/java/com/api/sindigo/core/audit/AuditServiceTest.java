package com.api.sindigo.core.audit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuditServiceTest {

    @Test
    void auditServiceCanBeInstantiated() {
        AuditService auditService = new AuditService();

        assertNotNull(auditService);
        assertEquals(AuditService.class, auditService.getClass());
        assertDoesNotThrow(AuditService::new);
    }
}