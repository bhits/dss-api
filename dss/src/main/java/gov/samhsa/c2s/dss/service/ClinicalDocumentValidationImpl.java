package gov.samhsa.c2s.dss.service;

import ch.qos.logback.audit.AuditException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.samhsa.c2s.brms.domain.FactModel;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.c2s.common.audit.AuditClient;
import gov.samhsa.c2s.common.audit.PredicateKey;
import gov.samhsa.c2s.common.log.Logger;
import gov.samhsa.c2s.common.log.LoggerFactory;
import gov.samhsa.c2s.common.validation.XmlValidation;
import gov.samhsa.c2s.common.validation.XmlValidationResult;
import gov.samhsa.c2s.common.validation.exception.XmlDocumentReadFailureException;
import gov.samhsa.c2s.dss.config.ApplicationContextConfig;
import gov.samhsa.c2s.dss.config.DssProperties;
import gov.samhsa.c2s.dss.infrastructure.dto.DiagnosticType;
import gov.samhsa.c2s.dss.infrastructure.dto.ValidationResponseDto;
import gov.samhsa.c2s.dss.infrastructure.validator.CCDAValidatorService;
import gov.samhsa.c2s.dss.service.audit.DssAuditVerb;
import gov.samhsa.c2s.dss.service.document.dto.RedactedDocument;
import gov.samhsa.c2s.dss.service.document.template.CCDAVersion;
import gov.samhsa.c2s.dss.service.document.template.DocumentType;
import gov.samhsa.c2s.dss.service.document.template.DocumentTypeResolver;
import gov.samhsa.c2s.dss.service.dto.ClinicalDocumentValidationRequest;
import gov.samhsa.c2s.dss.service.dto.ClinicalDocumentValidationResult;
import gov.samhsa.c2s.dss.service.dto.DSSRequest;
import gov.samhsa.c2s.dss.service.exception.AuditClientException;
import gov.samhsa.c2s.dss.service.exception.DocumentSegmentationException;
import gov.samhsa.c2s.dss.service.exception.InvalidOriginalClinicalDocumentException;
import gov.samhsa.c2s.dss.service.exception.InvalidSegmentedClinicalDocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.xml.sax.SAXParseException;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static gov.samhsa.c2s.dss.service.audit.DssPredicateKey.*;

@Service
public class ClinicalDocumentValidationImpl implements ClinicalDocumentValidation {

    private static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;
    private final Logger logger = LoggerFactory
            .getLogger(this.getClass());

    @Autowired
    @Qualifier(ApplicationContextConfig.CCDA_R1_VALIDATOR_SERVICE)
    private CCDAValidatorService ccdaR1ValidatorService;

    @Autowired
    @Qualifier(ApplicationContextConfig.CCDA_R2_VALIDATOR_SERVICE)
    private CCDAValidatorService ccdaR2ValidatorService;

    @Autowired
    private DocumentTypeResolver documentTypeResolver;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * The audit service.
     */
    @Autowired
    private Optional<AuditClient> auditClient;

    @Autowired
    private DssProperties dssProperties;

    /**
     * The xml validator.
     */
    private XmlValidation xmlValidator;

    @Override
    public ClinicalDocumentValidationResult validateClinicalDocument(final Charset charset, final String document) throws InvalidOriginalClinicalDocumentException, XmlDocumentReadFailureException {
        XmlValidationResult originalClinicalDocumentValidationResult = null;
        final DocumentType documentType = documentTypeResolver.resolve(document);
        logger.info(() -> "identified document as " + documentType);
        if (DocumentType.HITSP_C32.equals(documentType)) {
            logger.info("running only schema validation for " + documentType);
            try {
                originalClinicalDocumentValidationResult = xmlValidator
                        .validateWithAllErrors(document);
                Assert.notNull(originalClinicalDocumentValidationResult);
            } catch (final XmlDocumentReadFailureException e) {
                logger.error(e.getMessage(), e);
                throw e;
            }

            if (!originalClinicalDocumentValidationResult.isValid()) {
                logger.error("Schema validation is failed for original clinical document.");
                final String err = "InvalidOriginalClinicalDocumentException: ";
                for (final SAXParseException e : originalClinicalDocumentValidationResult
                        .getExceptions()) {
                    logger.error(() -> err + e.getMessage());
                }
                throw new InvalidOriginalClinicalDocumentException("C32 validation failed for document type " + documentType);
            }
        } else if (documentType.isCCDA(CCDAVersion.R1) || documentType.isCCDA(CCDAVersion.R2)) {
            final ValidationResponseDto originalCCDADocumentValidationResult = validate(documentType, document, charset);
            if (isInvalid(originalCCDADocumentValidationResult)) {
                originalCCDADocumentValidationResult.getValidationDetails()
                        .stream()
                        .filter(type -> type.getType().getTypeName().contains(DiagnosticType.CCDA_ERROR.getTypeName()))
                        .forEach(detail -> logger.error("Validation Error -- xPath: " + detail.getXPath() + ", Message: " + detail.getMessage()));
                throw new InvalidOriginalClinicalDocumentException("C-CDA validation failed for document type " + documentType);
            }
        } else {
            throw new InvalidOriginalClinicalDocumentException("Invalid or Unsupported document type");
        }
        return new ClinicalDocumentValidationResult(documentType, true);
    }

    @Override
    public ClinicalDocumentValidationResult validateClinicalDocument(ClinicalDocumentValidationRequest validationRequest) throws InvalidOriginalClinicalDocumentException, XmlDocumentReadFailureException {
        Optional<String> documentEncoding = validationRequest.getDocumentEncoding();
        Charset charset = getCharset(documentEncoding);
        String document = new String(validationRequest.getDocument(), charset);
        return this.validateClinicalDocument(charset, document);
    }

    @Override
    public void validateClinicalDocumentAddAudited(ClinicalDocumentValidationResult originalClinicalDocumentValidationResult,
                                                   Charset charset, String originalDocument, String document, DSSRequest dssRequest,
                                                   FactModel factModel, RedactedDocument redactedDocument,
                                                   String rulesFired) throws InvalidSegmentedClinicalDocumentException, AuditException, XmlDocumentReadFailureException {
        final DocumentType documentType = originalClinicalDocumentValidationResult.getDocumentType();
        XmlValidationResult segmentedClinicalDocumentValidationResult = null;
        ValidationResponseDto segmentedCCDADocumentValidationResult;

        if (DocumentType.HITSP_C32.equals(documentType)) {
            try {
                segmentedClinicalDocumentValidationResult = xmlValidator
                        .validateWithAllErrors(document);
                Assert.notNull(segmentedClinicalDocumentValidationResult);
                if (dssRequest.getAudited().orElse(dssProperties.getDocumentSegmentationImpl().isDefaultIsAudited())) {
                    auditSegmentation(originalDocument, document,
                            factModel.getXacmlResult(), redactedDocument,
                            rulesFired, originalClinicalDocumentValidationResult.isValidDocument(),
                            segmentedClinicalDocumentValidationResult.isValid(),
                            dssRequest.getAuditFailureByPass().orElse(dssProperties.getDocumentSegmentationImpl().isDefaultIsAuditFailureByPass()));
                }
            } catch (final XmlDocumentReadFailureException e) {
                logger.error(e.getMessage(), e);
                throw e;
            }

            if (!segmentedClinicalDocumentValidationResult.isValid()) {
                final String segmentationErr = "Schema validation is failed for segmented clinical document.";
                logger.error(segmentationErr);
                final String err = "InvalidSegmentedClinicalDocumentException: ";
                for (final SAXParseException e : segmentedClinicalDocumentValidationResult
                        .getExceptions()) {
                    logger.error(err + e.getMessage());
                }
                throw new InvalidSegmentedClinicalDocumentException(segmentationErr);
            }
        } else if (documentType.isCCDA(CCDAVersion.R1) || documentType.isCCDA(CCDAVersion.R2)) {
            segmentedCCDADocumentValidationResult = validate(documentType, document, charset);
            if (dssRequest.getAudited().orElse(dssProperties.getDocumentSegmentationImpl().isDefaultIsAudited())) {
                auditSegmentation(originalDocument, document,
                        factModel.getXacmlResult(), redactedDocument,
                        rulesFired, originalClinicalDocumentValidationResult.isValidDocument(),
                        isValid(segmentedCCDADocumentValidationResult),
                        dssRequest.getAuditFailureByPass().orElse(dssProperties.getDocumentSegmentationImpl().isDefaultIsAuditFailureByPass()));
            }
            if (isInvalid(segmentedCCDADocumentValidationResult)) {
                segmentedCCDADocumentValidationResult.getValidationDetails()
                        .stream()
                        .filter(errorType -> errorType.getType().getTypeName().contains(DiagnosticType.CCDA_ERROR.getTypeName()))
                        .forEach(detail -> logger.error("Validation Error -- xPath: " + detail.getXPath() + ", Message: " + detail.getMessage()));
                throw new InvalidSegmentedClinicalDocumentException("C-CDA validation failed for document type " + documentType);
            }
        }
    }

    private ValidationResponseDto validate(DocumentType documentType, String document, Charset charset) {
        CCDAValidatorService ccdaValidatorService = documentType.isCCDA(CCDAVersion.R1) ? ccdaR1ValidatorService : ccdaR2ValidatorService;
        ValidationResponseDto ccdaDocumentValidationResult = ccdaValidatorService.validate(document, charset);
        if (isInvalid(ccdaDocumentValidationResult)) {
            logger.info(() -> "invalid C-CDA document with version: " + documentType.getCcdaVersion().get());
            logger.debug(() -> "validation details:\n" + serialize(ccdaDocumentValidationResult));
        }
        return ccdaDocumentValidationResult;
    }

    private boolean isInvalid(ValidationResponseDto validationResponseDto) {
        return validationResponseDto.getValidationSummary().getError() > 0;
    }

    private boolean isValid(ValidationResponseDto validationResponseDto) {
        return !isInvalid(validationResponseDto);
    }

    private String serialize(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new DocumentSegmentationException(e);
        }
    }

    /**
     * Audit segmentation.
     *
     * @param originalDocument       the original document
     * @param segmentedDocument      the segmented document
     * @param xacmlResult            the xacml result
     * @param redactedDocument       the redacted document
     * @param rulesFired             the rules fired
     * @param originalDocumentValid  the original clinical document validation result
     * @param segmentedDocumentValid the segmented clinical document validation result
     * @param isAuditFailureByPass   the is audit failure by pass
     * @throws AuditException the audit exception
     */
    private void auditSegmentation(String originalDocument,
                                   String segmentedDocument, XacmlResult xacmlResult,
                                   RedactedDocument redactedDocument, String rulesFired,
                                   boolean originalDocumentValid,
                                   boolean segmentedDocumentValid,
                                   boolean isAuditFailureByPass) throws AuditException {

        Map<PredicateKey, String> predicateMap = null;
        if (auditClient.isPresent()) {
            predicateMap = auditClient.get().createPredicateMap();
            if (redactedDocument.getRedactedSectionSet().size() > 0) {
                predicateMap.put(SECTION_OBLIGATIONS_APPLIED, redactedDocument
                        .getRedactedSectionSet().toString());
            }
            if (redactedDocument.getRedactedCategorySet().size() > 0) {
                predicateMap.put(CATEGORY_OBLIGATIONS_APPLIED, redactedDocument
                        .getRedactedCategorySet().toString());
            }
            if (rulesFired != null) {
                predicateMap.put(RULES_FIRED, rulesFired);
            }
            predicateMap.put(ORIGINAL_DOCUMENT, originalDocument);
            predicateMap.put(SEGMENTED_DOCUMENT, segmentedDocument);
            predicateMap.put(ORIGINAL_DOCUMENT_VALID, Boolean
                    .toString(originalDocumentValid));
            predicateMap.put(SEGMENTED_DOCUMENT_VALID, Boolean
                    .toString(segmentedDocumentValid));
            try {
                auditClient.get().audit(this, xacmlResult.getMessageId(),
                        DssAuditVerb.SEGMENT_DOCUMENT, xacmlResult.getPatientId(), predicateMap);
            } catch (final AuditException e) {
                if (isAuditFailureByPass) {
                    // main flow should work though the audit service has some
                    // issues
                    logger.error("Audit Service is Down");
                    logger.debug(() -> "patient id" + xacmlResult.getPatientId());
                    logger.debug(() -> "original document" + originalDocument);
                    logger.debug(() -> "segmented document" + segmentedDocument);
                    // TODO send the email notification to core team
                    // Or send a notification using rabbitmq
                } else {
                    // main flow shouldn't work if audit service has some issues
                    throw e;
                }
            }
        } else {
            throw new AuditClientException("Audit Client bean not create.");
        }


    }

    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        this.xmlValidator = new XmlValidation(this.getClass().getClassLoader()
                .getResourceAsStream(DocumentSegmentationImpl.C32_CDA_XSD_PATH + DocumentSegmentationImpl.C32_CDA_XSD_NAME),
                DocumentSegmentationImpl.C32_CDA_XSD_PATH);
    }

    private Charset getCharset(Optional<String> documentEncoding) {
        return documentEncoding.map(Charset::forName).orElse(DEFAULT_ENCODING);
    }
}
