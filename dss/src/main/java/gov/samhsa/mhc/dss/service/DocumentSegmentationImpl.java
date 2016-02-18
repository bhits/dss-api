/*******************************************************************************
 * Open Behavioral Health Information Technology Architecture (OBHITA.org)
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the <organization> nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.samhsa.mhc.dss.service;

import ch.qos.logback.audit.AuditException;
import gov.samhsa.acs.brms.domain.ClinicalFact;
import gov.samhsa.acs.brms.domain.FactModel;
import gov.samhsa.acs.brms.domain.RuleExecutionContainer;
import gov.samhsa.acs.brms.domain.XacmlResult;
import gov.samhsa.mhc.brms.service.RuleExecutionService;
import gov.samhsa.mhc.brms.service.dto.AssertAndExecuteClinicalFactsResponse;
import gov.samhsa.mhc.common.audit.AuditService;
import gov.samhsa.mhc.common.audit.PredicateKey;
import gov.samhsa.mhc.common.log.Logger;
import gov.samhsa.mhc.common.log.LoggerFactory;
import gov.samhsa.mhc.common.marshaller.SimpleMarshaller;
import gov.samhsa.mhc.common.marshaller.SimpleMarshallerException;
import gov.samhsa.mhc.common.validation.XmlValidation;
import gov.samhsa.mhc.common.validation.XmlValidationResult;
import gov.samhsa.mhc.common.validation.exception.XmlDocumentReadFailureException;
import gov.samhsa.mhc.dss.infrastructure.valueset.ValueSetService;
import gov.samhsa.mhc.dss.infrastructure.valueset.dto.CodeAndCodeSystemSetDto;
import gov.samhsa.mhc.dss.service.document.*;
import gov.samhsa.mhc.dss.service.document.dto.RedactedDocument;
import gov.samhsa.mhc.dss.service.dto.DSSRequest;
import gov.samhsa.mhc.dss.service.dto.DSSResponse;
import gov.samhsa.mhc.dss.service.dto.SegmentDocumentResponse;
import gov.samhsa.mhc.dss.service.exception.DocumentSegmentationException;
import gov.samhsa.mhc.dss.service.exception.InvalidSegmentedClinicalDocumentException;
import gov.samhsa.mhc.dss.service.metadata.AdditionalMetadataGeneratorForSegmentedClinicalDocument;
import org.apache.axiom.attachments.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.xml.sax.SAXParseException;

import javax.activation.DataHandler;
import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static gov.samhsa.mhc.dss.service.audit.DssAuditVerb.SEGMENT_DOCUMENT;
import static gov.samhsa.mhc.dss.service.audit.DssPredicateKey.*;

/**
 * The Class DocumentSegmentationImpl.
 */
@Service
public class DocumentSegmentationImpl implements DocumentSegmentation {

    /**
     * The Constant C32_CDA_XSD_PATH.
     */
    public static final String C32_CDA_XSD_PATH = "schema/cdar2c32/infrastructure/cda/";

    /**
     * The Constant C32_CDA_XSD_NAME.
     */
    public static final String C32_CDA_XSD_NAME = "C32_CDA.xsd";
    public static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory
            .getLogger(this.getClass());

    /**
     * The rule execution web service client.
     */
    @Autowired
    private RuleExecutionService ruleExecutionService;

    /**
     * The audit service.
     */
    @Autowired
    private AuditService auditService;

    /**
     * The document editor.
     */
    @Autowired
    private DocumentEditor documentEditor;

    /**
     * The marshaller.
     */
    @Autowired
    private SimpleMarshaller marshaller;

    /**
     * The document tagger.
     */
    @Autowired
    private DocumentTagger documentTagger;

    /**
     * The document fact model extractor.
     */
    @Autowired
    private DocumentFactModelExtractor documentFactModelExtractor;

    /**
     * The document redactor.
     */
    @Autowired
    private DocumentRedactor documentRedactor;

    /**
     * The embedded clinical document extractor.
     */
    @Autowired
    private EmbeddedClinicalDocumentExtractor embeddedClinicalDocumentExtractor;

    /**
     * The value set service.
     */
    @Autowired
    private ValueSetService valueSetService;

    /**
     * The additional metadata generator for segmented clinical document.
     */
    @Autowired
    private AdditionalMetadataGeneratorForSegmentedClinicalDocument additionalMetadataGeneratorForSegmentedClinicalDocument;

    /**
     * The xml validator.
     */
    private XmlValidation xmlValidator;

    @Value("${mhc.dss.documentSegmentationImpl.defaultIsAudited}")
    private boolean defaultIsAudited;

    @Value("${mhc.dss.documentSegmentationImpl.defaultIsAuditFailureByPass}")
    private boolean defaultIsAuditFailureByPass;

    /*
     * (non-Javadoc)
     *
     * @see
     * gov.samhsa.acs.documentsegmentation.DocumentSegmentation#segmentDocument
     * (java.lang.String, java.lang.String, boolean, boolean, boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    // TODO: 2/11/2016 string to byte 
    public DSSResponse segmentDocument(DSSRequest dssRequest)
            throws XmlDocumentReadFailureException,
            InvalidSegmentedClinicalDocumentException, AuditException {
        String document = new String(dssRequest.getDocument(), getCharset(dssRequest.getDocumentEncoding()));
        Assert.hasText(document);
        final String originalDocument = document;
        XmlValidationResult originalClinicalDocumentValidationResult = null;
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
        }

        Assert.notNull(dssRequest.getXacmlResult());
        final String enforcementPolicies = marshal(dssRequest.getXacmlResult());
        Assert.notNull(enforcementPolicies);

        RuleExecutionContainer ruleExecutionContainer = null;
        RedactedDocument redactedDocument = null;
        String rulesFired = null;
        final SegmentDocumentResponse segmentDocumentResponse = new SegmentDocumentResponse();
        FactModel factModel = null;

        try {

            document = documentEditor.setDocumentCreationDate(document);

            // FileHelper.writeStringToFile(document, "Original_C32.xml");

            // extract factModel
            String factModelXml = documentFactModelExtractor.extractFactModel(
                    document, enforcementPolicies);
            // get clinical document with generatedEntryId elements
            document = embeddedClinicalDocumentExtractor
                    .extractClinicalDocumentFromFactModel(factModelXml);
            // remove the embedded c32 from factmodel before unmarshalling
            factModelXml = documentRedactor
                    .cleanUpEmbeddedClinicalDocumentFromFactModel(factModelXml);
            factModel = marshaller.unmarshalFromXml(FactModel.class,
                    factModelXml);

            final List<CodeAndCodeSystemSetDto> codeAndCodeSystemSetDtoList = new ArrayList<CodeAndCodeSystemSetDto>();
            // Get and set value set categories to clinical facts
            for (final ClinicalFact fact : factModel.getClinicalFactList()) {
                final CodeAndCodeSystemSetDto codeAndCodeSystemSetDto = new CodeAndCodeSystemSetDto();
                codeAndCodeSystemSetDto.setConceptCode(fact.getCode());
                codeAndCodeSystemSetDto.setCodeSystemOid(fact.getCodeSystem());
                codeAndCodeSystemSetDtoList.add(codeAndCodeSystemSetDto);
            }

            // Get value set categories
            final List<Map<String, Object>> valueSetCategories = valueSetService
                    .lookupValuesetCategoriesOfMultipleCodeAndCodeSystemSet(codeAndCodeSystemSetDtoList);
            // Iterator<HashMap<String, String>> iterator=valueSetCategories.k
            // TODO (BU): Refactor this code and make sure you get the value set
            // categories by code and code system (not by index)
            for (int i = 0; i < factModel.getClinicalFactList().size(); i++) {
                final Map<String, Object> valueSetMap = valueSetCategories
                        .get(i);
                final ClinicalFact fact = factModel.getClinicalFactList()
                        .get(i);
                Assert.isTrue(fact.getCode().equals(
                        valueSetMap.get("conceptCode")));
                Assert.isTrue(fact.getCodeSystem().equals(
                        valueSetMap.get("codeSystemOid")));
                if (valueSetMap.get("vsCategoryCodes") != null) {
                    fact.setValueSetCategories(new HashSet<String>(
                            (ArrayList<String>) valueSetMap
                                    .get("vsCategoryCodes")));
                }
            }
            // FileHelper.writeStringToFile(factModel, "FactModel.xml");

            // get execution response container
            final AssertAndExecuteClinicalFactsResponse brmsResponse = ruleExecutionService
                    .assertAndExecuteClinicalFacts(factModel);
            String executionResponseContainer = brmsResponse
                    .getRuleExecutionResponseContainer();
            rulesFired = brmsResponse.getRulesFired();

            // unmarshall from xml to RuleExecutionContainer
            ruleExecutionContainer = marshaller.unmarshalFromXml(
                    RuleExecutionContainer.class, executionResponseContainer);

            // FileHelper.writeStringToFile(executionResponseContainer,
            // "ExecutionResponseContainer.xml");

            logger.info("Fact model: " + factModelXml);
            logger.info("Rule Execution Container size: "
                    + ruleExecutionContainer.getExecutionResponseList().size());

            // redact document
            redactedDocument = documentRedactor.redactDocument(document,
                    ruleExecutionContainer, factModel);
            document = redactedDocument.getRedactedDocument();

            // set tryPolicyDocument in the response
            if (dssRequest.getEnableTryPolicyResponse().orElse(Boolean.FALSE)) {
                segmentDocumentResponse
                        .setTryPolicyDocumentXml(redactedDocument
                                .getTryPolicyDocument());
            }

            // to get the itemActions from documentRedactor
            executionResponseContainer = marshaller
                    .marshal(ruleExecutionContainer);

            // tag document
            document = documentTagger.tagDocument(document,
                    executionResponseContainer);

            // clean up generatedEntryId elements from document
            document = documentRedactor.cleanUpGeneratedEntryIds(document);

            // clean up generatedServiceEventId elements from document
            document = documentRedactor
                    .cleanUpGeneratedServiceEventIds(document);

            // FileHelper.writeStringToFile(document, "Tagged_C32.xml");

            // Set segmented document in response
            segmentDocumentResponse.setSegmentedDocumentXml(document);
            // Set execution response container in response
            segmentDocumentResponse
                    .setExecutionResponseContainerXml(executionResponseContainer);

        } catch (final JAXBException e) {
            logger.error(e.getMessage(), e);
            throw new DocumentSegmentationException(e.toString(), e);
        } catch (final Throwable e) {
            logger.error(e.getMessage(), e);
            throw new DocumentSegmentationException(e.toString(), e);
        }

        XmlValidationResult segmentedClinicalDocumentValidationResult = null;
        try {
            segmentedClinicalDocumentValidationResult = xmlValidator
                    .validateWithAllErrors(document);
            Assert.notNull(segmentedClinicalDocumentValidationResult);
            if (dssRequest.getAudited().orElse(defaultIsAudited)) {
                auditSegmentation(originalDocument, document,
                        factModel.getXacmlResult(), redactedDocument,
                        rulesFired, originalClinicalDocumentValidationResult,
                        segmentedClinicalDocumentValidationResult,
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
        DSSResponse dssResponse = new DSSResponse();
        dssResponse.setSegmentedDocument(segmentDocumentResponse.getSegmentedDocumentXml().getBytes(DEFAULT_ENCODING));
        dssResponse.setEncoding(DEFAULT_ENCODING.toString());
        if(dssRequest.getEnableTryPolicyResponse().orElse(Boolean.FALSE)){
            dssResponse.setTryPolicyDocument(segmentDocumentResponse.getTryPolicyDocumentXml().getBytes(DEFAULT_ENCODING));
        }
        return dssResponse;
    }

    /*
     * (non-Javadoc)
     *
     * @see gov.samhsa.acs.documentsegmentation.DocumentSegmentation#
     * setAdditionalMetadataForSegmentedClinicalDocument
     * (gov.samhsa.consent2share
     * .schema.documentsegmentation.SegmentDocumentResponse, java.lang.String,
     * java.lang.String, java.lang.String,
     * gov.samhsa.acs.brms.domain.XacmlResult)
     */
    @Override
    public void setAdditionalMetadataForSegmentedClinicalDocument(
            SegmentDocumentResponse segmentDocumentResponse,
            String senderEmailAddress, String recipientEmailAddress,
            String xdsDocumentEntryUniqueId, XacmlResult xacmlResult) {
        final String additionalMetadataForSegmentedClinicalDocument = additionalMetadataGeneratorForSegmentedClinicalDocument
                .generateMetadataXml(xacmlResult.getMessageId(),
                        segmentDocumentResponse.getSegmentedDocumentXml(),
                        segmentDocumentResponse
                                .getExecutionResponseContainerXml(),
                        senderEmailAddress, recipientEmailAddress, xacmlResult
                                .getSubjectPurposeOfUse().getPurpose(),
                        xdsDocumentEntryUniqueId);
        // FileHelper.writeStringToFile(additionalMetadataForSegmentedClinicalDocument,"additional_metadata.xml");

        segmentDocumentResponse
                .setPostSegmentationMetadataXml(additionalMetadataForSegmentedClinicalDocument);
    }

    /*
     * (non-Javadoc)
     *
     * @see gov.samhsa.acs.documentsegmentation.DocumentSegmentation#
     * setDocumentPayloadRawData
     * (gov.samhsa.consent2share.schema.documentsegmentation
     * .SegmentDocumentResponse, boolean, java.lang.String, java.lang.String,
     * gov.samhsa.acs.brms.domain.XacmlResult)
     */
    @Override
    public void setDocumentPayloadRawData(
            SegmentDocumentResponse segmentDocumentResponse,
            boolean packageAsXdm, String senderEmailAddress,
            String recipientEmailAddress, XacmlResult xacmlResult)
            throws Exception, IOException {
        final ByteArrayDataSource rawData = documentEditor
                .setDocumentPayloadRawData(segmentDocumentResponse
                                .getSegmentedDocumentXml(), packageAsXdm,
                        senderEmailAddress, recipientEmailAddress, xacmlResult,
                        segmentDocumentResponse
                                .getExecutionResponseContainerXml(), null, null);
        segmentDocumentResponse.setDocumentPayloadRawData(new DataHandler(
                rawData));
    }

    @PostConstruct
    public void afterPropertiesSet() {
        this.xmlValidator = new XmlValidation(this.getClass().getClassLoader()
                .getResourceAsStream(C32_CDA_XSD_PATH + C32_CDA_XSD_NAME),
                C32_CDA_XSD_PATH);
    }

    private Charset getCharset(Optional<String> documentEncoding) {
        return documentEncoding.map(Charset::forName).orElse(DEFAULT_ENCODING);
    }

    /**
     * Audit segmentation.
     *
     * @param originalDocument                          the original document
     * @param segmentedDocument                         the segmented document
     * @param xacmlResult                               the xacml result
     * @param redactedDocument                          the redacted document
     * @param rulesFired                                the rules fired
     * @param originalClinicalDocumentValidationResult  the original clinical document validation result
     * @param segmentedClinicalDocumentValidationResult the segmented clinical document validation result
     * @param isAuditFailureByPass                      the is audit failure by pass
     * @throws AuditException the audit exception
     */
    private void auditSegmentation(String originalDocument,
                                   String segmentedDocument, XacmlResult xacmlResult,
                                   RedactedDocument redactedDocument, String rulesFired,
                                   XmlValidationResult originalClinicalDocumentValidationResult,
                                   XmlValidationResult segmentedClinicalDocumentValidationResult,
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
                .toString(originalClinicalDocumentValidationResult.isValid()));
        predicateMap.put(SEGMENTED_DOCUMENT_VALID, Boolean
                .toString(segmentedClinicalDocumentValidationResult.isValid()));
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

    private String marshal(Object o) {
        try {
            return marshaller.marshal(o);
        } catch (SimpleMarshallerException e) {
            throw new DocumentSegmentationException(e);
        }
    }
}
