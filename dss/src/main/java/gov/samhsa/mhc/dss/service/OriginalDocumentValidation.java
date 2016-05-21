/**
 * Created by Jiahao.Li on 5/20/2016.
 */
package gov.samhsa.mhc.dss.service;

import gov.samhsa.mhc.common.validation.XmlValidationResult;
import gov.samhsa.mhc.dss.infrastructure.dto.ValidationResponseDto;
import gov.samhsa.mhc.dss.service.document.template.DocumentType;
import gov.samhsa.mhc.dss.service.exception.InvalidOriginalClinicalDocumentException;

import java.nio.charset.Charset;

public interface OriginalDocumentValidation {
    /**
     * @param charset
     * @param document
     * @throws InvalidOriginalClinicalDocumentException
     */
    void validateOriginalClinicalDocument(Charset charset, String document) throws InvalidOriginalClinicalDocumentException;

    /**
     * @return
     */
    DocumentType getDocumentType();

    /**
     * @return
     */
    ValidationResponseDto getOriginalCCDADocumentValidationResult();

    /**
     * @return
     */
    XmlValidationResult getOriginalClinicalDocumentValidationResult();

    /**
     * @param documentType
     * @param document
     * @param charset
     * @return
     */
    ValidationResponseDto validate(DocumentType documentType, String document, Charset charset);

    /**
     * @param validationResponseDto
     * @return
     */
    boolean isInvalid(ValidationResponseDto validationResponseDto);
}
