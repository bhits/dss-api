/**
 * Created by Jiahao.Li on 5/20/2016.
 */
package gov.samhsa.mhc.dss.service;

import ch.qos.logback.audit.AuditException;
import gov.samhsa.mhc.brms.domain.FactModel;
import gov.samhsa.mhc.common.validation.exception.XmlDocumentReadFailureException;
import gov.samhsa.mhc.dss.service.document.dto.RedactedDocument;
import gov.samhsa.mhc.dss.service.dto.DSSRequest;
import gov.samhsa.mhc.dss.service.dto.ClinicalDocumentValidationResult;
import gov.samhsa.mhc.dss.service.exception.InvalidOriginalClinicalDocumentException;
import gov.samhsa.mhc.dss.service.exception.InvalidSegmentedClinicalDocumentException;

import java.nio.charset.Charset;

public interface ClinicalDocumentValidation {
    /**
     * @param charset
     * @param document
     * @throws InvalidOriginalClinicalDocumentException
     */
    ClinicalDocumentValidationResult validateClinicalDocument(Charset charset, String document) throws InvalidOriginalClinicalDocumentException, XmlDocumentReadFailureException;

    /**
     * @param charset
     * @param document
     * @param dssRequest
     * @param factModel
     * @param redactedDocument
     * @param rulesFired
     * @throws InvalidSegmentedClinicalDocumentException
     * @throws AuditException
     * @throws XmlDocumentReadFailureException
     */
    void validateClinicalDocumentAddAudited(Charset charset, String document, DSSRequest dssRequest,
                                            FactModel factModel, RedactedDocument redactedDocument,
                                            String rulesFired) throws InvalidSegmentedClinicalDocumentException, AuditException, XmlDocumentReadFailureException;
}
