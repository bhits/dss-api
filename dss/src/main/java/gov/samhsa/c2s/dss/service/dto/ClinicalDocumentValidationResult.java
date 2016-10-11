package gov.samhsa.c2s.dss.service.dto;

import gov.samhsa.c2s.dss.service.document.template.DocumentType;
import lombok.Data;

@Data
public class ClinicalDocumentValidationResult {
    private DocumentType documentType;
    private boolean isValidDocument;

    public ClinicalDocumentValidationResult(DocumentType documentType, boolean isValidDocument) {
        this.documentType = documentType;
        this.isValidDocument = isValidDocument;
    }
}
