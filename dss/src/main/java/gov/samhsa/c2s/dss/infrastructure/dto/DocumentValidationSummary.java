package gov.samhsa.c2s.dss.infrastructure.dto;

import lombok.Data;

@Data
public class DocumentValidationSummary {
    private String validatorType;
    private String documentType;
    private int error;
    private int warning;
    private int info;

    public DocumentValidationSummary() {
    }

    public DocumentValidationSummary(String validatorType, String documentType, int error, int warning, int info) {
        this.validatorType = validatorType;
        this.documentType = documentType;
        this.error = error;
        this.warning = warning;
        this.info = info;
    }

}
