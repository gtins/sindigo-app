package com.api.sindigo.core.attachment.enums;

public enum AttachmentCategory {
    TICKET_OPENING_EVIDENCE("ticket_opening_evidence", "Evidência de Abertura do Chamado"),
    TICKET_CLOSING_EVIDENCE("ticket_closing_evidence", "Evidência de Fechamento do Chamado"),
    INVOICE("invoice", "Nota Fiscal"),
    COMPLETION_PROOF("completion_proof", "Comprovante de Conclusão"),
    ACTIVITY_EVIDENCE("activity_evidence", "Evidência de Atividade"),
    OTHER("other", "Outro");

    private final String code;
    private final String description;

    AttachmentCategory(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static AttachmentCategory fromCode(String code) {
        for (AttachmentCategory category : AttachmentCategory.values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        return OTHER;
    }
}

