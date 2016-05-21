package gov.samhsa.mhc.dss.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.samhsa.mhc.common.log.Logger;
import gov.samhsa.mhc.common.log.LoggerFactory;
import gov.samhsa.mhc.common.validation.XmlValidation;
import gov.samhsa.mhc.common.validation.XmlValidationResult;
import gov.samhsa.mhc.common.validation.exception.XmlDocumentReadFailureException;
import gov.samhsa.mhc.dss.config.ApplicationContextConfig;
import gov.samhsa.mhc.dss.infrastructure.dto.ValidationResponseDto;
import gov.samhsa.mhc.dss.infrastructure.validator.CCDAValidatorService;
import gov.samhsa.mhc.dss.service.document.template.DocumentType;
import gov.samhsa.mhc.dss.service.document.template.DocumentTypeResolver;
import gov.samhsa.mhc.dss.service.exception.DocumentSegmentationException;
import gov.samhsa.mhc.dss.service.exception.InvalidOriginalClinicalDocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.xml.sax.SAXParseException;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;

import static gov.samhsa.mhc.dss.service.DocumentSegmentationImpl.C32_CDA_XSD_NAME;
import static gov.samhsa.mhc.dss.service.DocumentSegmentationImpl.C32_CDA_XSD_PATH;
import static gov.samhsa.mhc.dss.service.document.template.CCDAVersion.R1;
import static gov.samhsa.mhc.dss.service.document.template.CCDAVersion.R2;

/**
 * Created by Jiahao.Li on 5/20/2016.
 */

@Service
public class OriginalDocumentValidationImpl implements OriginalDocumentValidation {

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
     * The xml validator.
     */
    private XmlValidation xmlValidator;

    private DocumentType documentType;
    private XmlValidationResult originalClinicalDocumentValidationResult;
    private ValidationResponseDto originalCCDADocumentValidationResult;

    @Override
    public void validateOriginalClinicalDocument(Charset charset, String document) throws InvalidOriginalClinicalDocumentException {
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
    }

    @Override
    public ValidationResponseDto validate(DocumentType documentType, String document, Charset charset) {
        CCDAValidatorService ccdaValidatorService = documentType.isCCDA(R1) ? ccdaR1ValidatorService : ccdaR2ValidatorService;
        ValidationResponseDto ccdaDocumentValidationResult = ccdaValidatorService.validate(document, charset);
        if (isInvalid(ccdaDocumentValidationResult)) {
            logger.info(() -> "invalid C-CDA document with version: " + documentType.getCcdaVersion().get());
            logger.debug(() -> "validation details:\n" + serialize(ccdaDocumentValidationResult));
        }
        return ccdaDocumentValidationResult;
    }

    @Override
    public boolean isInvalid(ValidationResponseDto validationResponseDto) {
        return validationResponseDto.getValidationSummary().getError() > 0;
    }

    @Override
    public XmlValidationResult getOriginalClinicalDocumentValidationResult() {
        return originalClinicalDocumentValidationResult;
    }

    @Override
    public ValidationResponseDto getOriginalCCDADocumentValidationResult() {
        return originalCCDADocumentValidationResult;
    }

    @Override
    public DocumentType getDocumentType() {
        return documentType;
    }

    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        this.xmlValidator = new XmlValidation(this.getClass().getClassLoader()
                .getResourceAsStream(C32_CDA_XSD_PATH + C32_CDA_XSD_NAME),
                C32_CDA_XSD_PATH);
    }

    private String serialize(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new DocumentSegmentationException(e);
        }
    }
}
