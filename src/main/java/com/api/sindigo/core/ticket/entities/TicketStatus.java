package com.api.sindigo.core.ticket.entities;

public enum TicketStatus {
    ABERTO,           // Chamado recém criado
    EM_ANALISE,       // Síndico/gestor analisando
    PLANEJADO,        // Já tem atividades atribuídas
    EM_EXECUCAO,      // Pelo menos uma atividade em andamento
    AGUARDANDO,       // Esperando material, autorização, etc
    RESOLVIDO,        // Problema foi solucionado
    FECHADO,          // Formalmente encerrado
    CANCELADO         // Não será resolvido
}

