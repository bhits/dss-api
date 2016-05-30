package gov.samhsa.mhc.dss.service;

import ch.qos.logback.audit.AuditException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.samhsa.mhc.brms.domain.FactModel;
import gov.samhsa.mhc.brms.domain.XacmlResult;
import gov.samhsa.mhc.common.audit.AuditService;
import gov.samhsa.mhc.common.audit.PredicateKey;
import gov.samhsa.mhc.common.log.Logger;
import gov.samhsa.mhc.common.log.LoggerFactory;
import gov.samhsa.mhc.common.validation.XmlValidation;
import gov.samhsa.mhc.common.validation.XmlValidationResult;
import gov.samhsa.mhc.common.validation.exception.XmlDocumentReadFailureException;
import gov.samhsa.mhc.dss.config.ApplicationContextConfig;
import gov.samhsa.mhc.dss.infrastructure.dto.ValidationResponseDto;
import gov.samhsa.mhc.dss.infrastructure.validator.CCDAValidatorService;
import gov.samhsa.mhc.dss.service.document.dto.RedactedDocument;
import gov.samhsa.mhc.dss.service.document.template.DocumentType;
import gov.samhsa.mhc.dss.service.document.template.DocumentTypeResolver;
import gov.samhsa.mhc.dss.service.dto.ClinicalDocumentValidationRequest;
import gov.samhsa.mhc.dss.service.dto.DSSRequest;
import gov.samhsa.mhc.dss.service.dto.ClinicalDocumentValidationResult;
import gov.samhsa.mhc.dss.service.exception.DocumentSegmentationException;
import gov.samhsa.mhc.dss.service.exception.InvalidOriginalClinicalDocumentException;
import gov.samhsa.mhc.dss.service.exception.InvalidSegmentedClinicalDocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.xml.sax.SAXParseException;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static gov.samhsa.mhc.dss.service.DocumentSegmentationImpl.C32_CDA_XSD_NAME;
import static gov.samhsa.mhc.dss.service.DocumentSegmentationImpl.C32_CDA_XSD_PATH;
import static gov.samhsa.mhc.dss.service.audit.DssAuditVerb.SEGMENT_DOCUMENT;
import static gov.samhsa.mhc.dss.service.audit.DssPredicateKey.*;
import static gov.samhsa.mhc.dss.service.document.template.CCDAVersion.R1;
import static gov.samhsa.mhc.dss.service.document.template.CCDAVersion.R2;

/**
 * Created by Jiahao.Li on 5/20/2016.
 */

@Service
public class ClinicalDocumentValidationImpl implements ClinicalDocumentValidation {

    private static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;
    private XmlValidationResult originalClinicalDocumentValidationResult;
    private ValidationResponseDto originalCCDADocumentValidationResult;
    private DocumentType documentType;
    private String originalDocument;

    /**
     * The logger.
     */
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
    private AuditService auditService;

    @Value("${mhc.dss.documentSegmentationImpl.defaultIsAudited}")
    private boolean defaultIsAudited;

    @Value("${mhc.dss.documentSegmentationImpl.defaultIsAuditFailureByPass}")
    private boolean defaultIsAuditFailureByPass;

    /**
     * The xml validator.
     */
    private XmlValidation xmlValidator;

    @Override
    public ClinicalDocumentValidationResult validateClinicalDocument(Charset charset, String document) throws InvalidOriginalClinicalDocumentException, XmlDocumentReadFailureException {
        originalDocument = document;
        originalClinicalDocumentValidationResult = null;
        originalCCDADocumentValidationResult = null;
        documentType = documentTypeResolver.resolve(document);
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
        } else if (documentType.isCCDA(R1) || documentType.isCCDA(R2)) {
            originalCCDADocumentValidationResult = validate(documentType, document, charset);
            if (isInvalid(originalCCDADocumentValidationResult)) {
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
    public void validateClinicalDocumentAddAudited(Charset charset, String document, DSSRequest dssRequest,
                                                   FactModel factModel, RedactedDocument redactedDocument,
                                                   String rulesFired) throws InvalidSegmentedClinicalDocumentException, AuditException, XmlDocumentReadFailureException {
        XmlValidationResult segmentedClinicalDocumentValidationResult = null;
        ValidationResponseDto segmentedCCDADocumentValidationResult;

        if (DocumentType.HITSP_C32.equals(documentType)) {
            try {
                segmentedClinicalDocumentValidationResult = xmlValidator
                        .validateWithAllErrors(document);
                Assert.notNull(segmentedClinicalDocumentValidationResult);
                if (dssRequest.getAudited().orElse(defaultIsAudited)) {
                    auditSegmentation(originalDocument, document,
                            factModel.getXacmlResult(), redactedDocument,
                            rulesFired, originalClinicalDocumentValidationResult.isValid(),
                            segmentedClinicalDocumentValidationResult.isValid(),
                            dssRequest.getAuditFailureByPass().orElse(defaultIsAuditFailureByPass));
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
        } else if (documentType.isCCDA(R1) || documentType.isCCDA(R2)) {
            segmentedCCDADocumentValidationResult = validate(documentType, document, charset);
            if (dssRequest.getAudited().orElse(defaultIsAudited)) {
                auditSegmentation(originalDocument, document,
                        factModel.getXacmlResult(), redactedDocument,
                        rulesFired, isValid(originalCCDADocumentValidationResult),
                        isValid(segmentedCCDADocumentValidationResult),
                        dssRequest.getAuditFailureByPass().orElse(defaultIsAuditFailureByPass));
            }
            if (isInvalid(segmentedCCDADocumentValidationResult)) {
                throw new InvalidSegmentedClinicalDocumentException("C-CDA validation failed for document type " + documentType);
            }
        }
    }

    private ValidationResponseDto validate(DocumentType documentType, String document, Charset charset) {
        CCDAValidatorService ccdaValidatorService = documentType.isCCDA(R1) ? ccdaR1ValidatorService : ccdaR2ValidatorService;
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
        final Map<PredicateKey, String> predicateMap = auditService
                .createPredicateMap();
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
            auditService.audit(this, xacmlResult.getMessageId(),
                    SEGMENT_DOCUMENT, xacmlResult.getPatientId(), predicateMap);
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
    }

    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        this.xmlValidator = new XmlValidation(this.getClass().getClassLoader()
                .getResourceAsStream(C32_CDA_XSD_PATH + C32_CDA_XSD_NAME),
                C32_CDA_XSD_PATH);
    }

    private Charset getCharset(Optional<String> documentEncoding) {
        return documentEncoding.map(Charset::forName).orElse(DEFAULT_ENCODING);
    }
}
