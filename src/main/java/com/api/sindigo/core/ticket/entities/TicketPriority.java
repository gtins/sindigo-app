package com.api.sindigo.core.ticket.entities;

public enum TicketPriority {
    BAIXA,            // Pode esperar semanas
    MEDIA,            // Uma semana
    ALTA,             // Alguns dias
    URGENTE,          // Próximas 24h
    CRITICA           // Risco à segurança/estrutura
}

