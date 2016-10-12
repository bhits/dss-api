package gov.samhsa.c2s.dss.infrastructure.dto;

import lombok.Data;

@Data
public class DocumentValidationResult {

    private DiagnosticType type;
    private String message;
    private String xPath;
    private String documentLineNumber;

    public DocumentValidationResult() {
    }

    public DocumentValidationResult(DiagnosticType type, String message, String xPath,
                                    String documentLineNumber) {
        this.type = type;
        this.message = message;
        this.xPath = xPath;
        this.documentLineNumber = documentLineNumber;
    }
}
