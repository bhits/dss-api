/*******************************************************************************
 * Open Behavioral Health Information Technology Architecture (OBHITA.org)
 * <p/>
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
 * <p/>
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
import gov.samhsa.mhc.brms.domain.FactModel;
import gov.samhsa.mhc.brms.domain.RuleExecutionContainer;
import gov.samhsa.mhc.brms.domain.XacmlResult;
import gov.samhsa.mhc.brms.service.RuleExecutionService;
import gov.samhsa.mhc.brms.service.dto.AssertAndExecuteClinicalFactsResponse;
import gov.samhsa.mhc.common.log.Logger;
import gov.samhsa.mhc.common.log.LoggerFactory;
import gov.samhsa.mhc.common.marshaller.SimpleMarshaller;
import gov.samhsa.mhc.common.marshaller.SimpleMarshallerException;
import gov.samhsa.mhc.common.validation.XmlValidation;
import gov.samhsa.mhc.common.validation.exception.XmlDocumentReadFailureException;
import gov.samhsa.mhc.dss.infrastructure.valueset.ValueSetService;
import gov.samhsa.mhc.dss.infrastructure.valueset.dto.ConceptCodeAndCodeSystemOidDto;
import gov.samhsa.mhc.dss.infrastructure.valueset.dto.ValueSetQueryDto;
import gov.samhsa.mhc.dss.service.document.*;
import gov.samhsa.mhc.dss.service.document.dto.RedactedDocument;
import gov.samhsa.mhc.dss.service.document.template.DocumentType;
import gov.samhsa.mhc.dss.service.dto.ClinicalDocumentValidationResult;
import gov.samhsa.mhc.dss.service.dto.DSSRequest;
import gov.samhsa.mhc.dss.service.dto.DSSResponse;
import gov.samhsa.mhc.dss.service.dto.SegmentDocumentResponse;
import gov.samhsa.mhc.dss.service.exception.DocumentSegmentationException;
import gov.samhsa.mhc.dss.service.exception.InvalidOriginalClinicalDocumentException;
import gov.samhsa.mhc.dss.service.exception.InvalidSegmentedClinicalDocumentException;
import gov.samhsa.mhc.dss.service.metadata.AdditionalMetadataGeneratorForSegmentedClinicalDocument;
import org.apache.axiom.attachments.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static gov.samhsa.mhc.dss.service.document.template.CCDAVersion.R1;
import static gov.samhsa.mhc.dss.service.document.template.CCDAVersion.R2;

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

    @Autowired
    private ClinicalDocumentValidation clinicalDocumentValidation;

    public DocumentSegmentationImpl() {
    }

    /**
     * Instantiates a new document processor impl.
     *
     * @param ruleExecutionService                                    the rule execution service
     * @param documentEditor                                          the document editor
     * @param marshaller                                              the marshaller
     * @param documentRedactor                                        the document redactor
     * @param documentTagger                                          the document tagger
     * @param documentFactModelExtractor                              the document fact model extractor
     * @param embeddedClinicalDocumentExtractor                       the embedded clinical document extractor
     * @param valueSetService                                         the value set service
     * @param additionalMetadataGeneratorForSegmentedClinicalDocument the additional metadata generator for segmented clinical
     *                                                                document
     */
    @Autowired
    public DocumentSegmentationImpl(
            RuleExecutionService ruleExecutionService,
            DocumentEditor documentEditor,
            SimpleMarshaller marshaller,
            DocumentRedactor documentRedactor,
            DocumentTagger documentTagger,
            DocumentFactModelExtractor documentFactModelExtractor,
            EmbeddedClinicalDocumentExtractor embeddedClinicalDocumentExtractor,
            ValueSetService valueSetService,
            AdditionalMetadataGeneratorForSegmentedClinicalDocument additionalMetadataGeneratorForSegmentedClinicalDocument) {
        this.ruleExecutionService = ruleExecutionService;
        this.documentEditor = documentEditor;
        this.marshaller = marshaller;
        this.documentRedactor = documentRedactor;
        this.documentTagger = documentTagger;
        this.documentFactModelExtractor = documentFactModelExtractor;
        this.embeddedClinicalDocumentExtractor = embeddedClinicalDocumentExtractor;
        this.valueSetService = valueSetService;
        this.additionalMetadataGeneratorForSegmentedClinicalDocument = additionalMetadataGeneratorForSegmentedClinicalDocument;
        this.xmlValidator = createXmlValidator();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gov.samhsa.mhc.documentsegmentation.DocumentSegmentation#segmentDocument
     * (java.lang.String, java.lang.String, boolean, boolean, boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public DSSResponse segmentDocument(DSSRequest dssRequest)
            throws XmlDocumentReadFailureException,
            InvalidSegmentedClinicalDocumentException, AuditException, InvalidOriginalClinicalDocumentException {
        final Charset charset = getCharset(dssRequest.getDocumentEncoding());
        String document = new String(dssRequest.getDocument(), charset);
        Assert.hasText(document);

        //Validate Original Document
        ClinicalDocumentValidationResult clinicalDocumentValidationResult = clinicalDocumentValidation.validateClinicalDocument(charset, document);

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

            // Get and set value set categories to clinical facts
            final List<ConceptCodeAndCodeSystemOidDto> conceptCodeAndCodeSystemOidDtoList =
                    factModel.getClinicalFactList().stream()
                            .map(fact -> new ConceptCodeAndCodeSystemOidDto(fact.getCode(), fact.getCodeSystem()))
                            .collect(Collectors.toList());

            // Get value set categories
            final List<ValueSetQueryDto> valueSetCategories = valueSetService
                    .lookupValueSetCategories(conceptCodeAndCodeSystemOidDtoList);
            factModel.getClinicalFactList()
                    .stream()
                    .forEach(fact -> valueSetCategories.stream()
                            .filter(dto -> fact.getCode().equals(dto.getConceptCode()) && fact.getCodeSystem().equals(dto.getCodeSystemOid()))
                            .map(ValueSetQueryDto::getVsCategoryCodes)
                            .filter(Objects::nonNull)
                            .findAny().ifPresent(fact::setValueSetCategories));

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

        //Validate Segmented Document
        clinicalDocumentValidation.validateClinicalDocumentAddAudited(charset, document, dssRequest,
                factModel, redactedDocument, rulesFired);

        DSSResponse dssResponse = new DSSResponse();
        dssResponse.setSegmentedDocument(segmentDocumentResponse.getSegmentedDocumentXml().getBytes(DEFAULT_ENCODING));
        dssResponse.setEncoding(DEFAULT_ENCODING.toString());
        dssResponse.setCCDADocument(isCCDADocument(clinicalDocumentValidationResult.getDocumentType()));
        if (dssRequest.getEnableTryPolicyResponse().orElse(Boolean.FALSE)) {
            dssResponse.setTryPolicyDocument(segmentDocumentResponse.getTryPolicyDocumentXml().getBytes(DEFAULT_ENCODING));
        }
        return dssResponse;
    }

    /*
     * (non-Javadoc)
     *
     * @see gov.samhsa.mhc.documentsegmentation.DocumentSegmentation#
     * setAdditionalMetadataForSegmentedClinicalDocument
     * (gov.samhsa.consent2share
     * .schema.documentsegmentation.SegmentDocumentResponse, java.lang.String,
     * java.lang.String, java.lang.String,
     * gov.samhsa.mhc.brms.domain.XacmlResult)
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
     * @see gov.samhsa.mhc.documentsegmentation.DocumentSegmentation#
     * setDocumentPayloadRawData
     * (gov.samhsa.consent2share.schema.documentsegmentation
     * .SegmentDocumentResponse, boolean, java.lang.String, java.lang.String,
     * gov.samhsa.mhc.brms.domain.XacmlResult)
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

    private XmlValidation createXmlValidator() {
        return new XmlValidation(this.getClass().getClassLoader()
                .getResourceAsStream(C32_CDA_XSD_PATH + C32_CDA_XSD_NAME),
                C32_CDA_XSD_PATH);
    }

    private Charset getCharset(Optional<String> documentEncoding) {
        return documentEncoding.map(Charset::forName).orElse(DEFAULT_ENCODING);
    }

    private String marshal(Object o) {
        try {
            return marshaller.marshal(o);
        } catch (SimpleMarshallerException e) {
            throw new DocumentSegmentationException(e);
        }
    }

    private boolean isCCDADocument(DocumentType documentType) {
        return documentType.isCCDA(R1) || documentType.isCCDA(R2);
    }
}
