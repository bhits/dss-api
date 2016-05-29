package gov.samhsa.mhc.dss.service.dto;

import gov.samhsa.mhc.dss.service.document.template.DocumentType;

/**
 * Created by Jiahao.Li on 5/26/2016.
 */
public class ClinicalDocumentValidationResult {
    private DocumentType documentType;

    public ClinicalDocumentValidationResult(DocumentType documentType) {
        this.documentType = documentType;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }
}
