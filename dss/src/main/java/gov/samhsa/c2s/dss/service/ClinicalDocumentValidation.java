package gov.samhsa.c2s.dss.service;

import ch.qos.logback.audit.AuditException;
import gov.samhsa.c2s.brms.domain.FactModel;
import gov.samhsa.c2s.common.validation.exception.XmlDocumentReadFailureException;
import gov.samhsa.c2s.dss.service.document.dto.RedactedDocument;
import gov.samhsa.c2s.dss.service.dto.ClinicalDocumentValidationRequest;
import gov.samhsa.c2s.dss.service.dto.ClinicalDocumentValidationResult;
import gov.samhsa.c2s.dss.service.dto.DSSRequest;
import gov.samhsa.c2s.dss.service.exception.InvalidOriginalClinicalDocumentException;
import gov.samhsa.c2s.dss.service.exception.InvalidSegmentedClinicalDocumentException;

import java.nio.charset.Charset;

public interface ClinicalDocumentValidation {
    /**
     * @param charset
     * @param document
     * @throws InvalidOriginalClinicalDocumentException
     */
    ClinicalDocumentValidationResult validateClinicalDocument(Charset charset, String document) throws InvalidOriginalClinicalDocumentException, XmlDocumentReadFailureException;

    /**
     * @param validationRequest
     * @return
     * @throws InvalidOriginalClinicalDocumentException
     * @throws XmlDocumentReadFailureException
     */
    ClinicalDocumentValidationResult validateClinicalDocument(ClinicalDocumentValidationRequest validationRequest) throws InvalidOriginalClinicalDocumentException, XmlDocumentReadFailureException;

    /**
     * @param originalClinicalDocumentValidationResult
     * @param charset
     * @param originalDocument
     * @param document
     * @param dssRequest
     * @param factModel
     * @param redactedDocument
     * @param rulesFired
     * @throws InvalidSegmentedClinicalDocumentException
     * @throws AuditException
     * @throws XmlDocumentReadFailureException
     */
    void validateClinicalDocumentAddAudited(ClinicalDocumentValidationResult originalClinicalDocumentValidationResult,
                                            Charset charset, String originalDocument, String document, DSSRequest dssRequest,
                                            FactModel factModel, RedactedDocument redactedDocument,
                                            String rulesFired) throws InvalidSegmentedClinicalDocumentException, AuditException, XmlDocumentReadFailureException;
}
