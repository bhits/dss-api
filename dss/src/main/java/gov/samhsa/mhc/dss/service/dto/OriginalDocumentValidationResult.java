package gov.samhsa.mhc.dss.service.dto;

import gov.samhsa.mhc.common.validation.XmlValidationResult;
import gov.samhsa.mhc.dss.infrastructure.dto.ValidationResponseDto;
import gov.samhsa.mhc.dss.service.document.template.DocumentType;

/**
 * Created by Jiahao.Li on 5/26/2016.
 */
public class OriginalDocumentValidationResult {
    private XmlValidationResult originalClinicalDocumentValidationResult;
    private ValidationResponseDto originalCCDADocumentValidationResult;
    private DocumentType documentType;

    public OriginalDocumentValidationResult(XmlValidationResult originalClinicalDocumentValidationResult, ValidationResponseDto originalCCDADocumentValidationResult, DocumentType documentType) {
        this.originalClinicalDocumentValidationResult = originalClinicalDocumentValidationResult;
        this.originalCCDADocumentValidationResult = originalCCDADocumentValidationResult;
        this.documentType = documentType;
    }

    public XmlValidationResult getOriginalClinicalDocumentValidationResult() {
        return originalClinicalDocumentValidationResult;
    }

    public void setOriginalClinicalDocumentValidationResult(XmlValidationResult originalClinicalDocumentValidationResult) {
        this.originalClinicalDocumentValidationResult = originalClinicalDocumentValidationResult;
    }

    public ValidationResponseDto getOriginalCCDADocumentValidationResult() {
        return originalCCDADocumentValidationResult;
    }

    public void setOriginalCCDADocumentValidationResult(ValidationResponseDto originalCCDADocumentValidationResult) {
        this.originalCCDADocumentValidationResult = originalCCDADocumentValidationResult;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }
}
