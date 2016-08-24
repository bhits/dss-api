// TODO: 2/12/2016 Fix unit test
/*
package gov.samhsa.c2s.dss.service;

import RuleExecutionContainer;
import FactModel;
import RuleExecutionServiceImpl;
import XacmlResult;
import GuvnorServiceImpl;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessorImpl;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverterImpl;
import gov.samhsa.c2s.common.filereader.FileReaderImpl;
import gov.samhsa.c2s.common.marshaller.SimpleMarshallerImpl;
import gov.samhsa.c2s.common.document.transformer.XmlTransformer;
import gov.samhsa.c2s.common.document.transformer.XmlTransformerImpl;
import gov.samhsa.c2s.common.util.EncryptTool;
import gov.samhsa.c2s.common.validation.exception.XmlDocumentReadFailureException;
import SegmentDocumentResponse;
import InvalidOriginalClinicalDocumentException;
import InvalidSegmentedClinicalDocumentException;
import AdditionalMetadataGeneratorForSegmentedClinicalDocumentImpl;
import DocumentEditorImpl;
import DocumentEncrypterImpl;
import DocumentFactModelExtractorImpl;
import DocumentRedactor;
import DocumentRedactorImpl;
import DocumentTaggerImpl;
import EmbeddedClinicalDocumentExtractorImpl;
import MetadataGeneratorImpl;
import AbstractClinicalFactLevelRedactionHandler;
import AbstractDocumentLevelRedactionHandler;
import AbstractObligationLevelRedactionHandler;
import AbstractPostRedactionLevelRedactionHandler;
import Entry;
import HumanReadableTextNodeByCode;
import HumanReadableTextNodeByDisplayName;
import UnsupportedHeaderElementHandler;
import Section;
import DocumentCleanupForNoEntryAndNoSection;
import RuleExecutionResponseMarkerForRedactedEntries;
import ValueSetServiceImplMock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Key;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.utils.EncryptionConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ch.qos.logback.audit.AuditException;

public class DocumentSegmentationImplIT {

    public static final Set<String> unsupportedHeaders = new HashSet<String>(
            Arrays.asList("realmCode", "custodian"));
    private final static String DISCLAIMER_DOCUMENT = "<disclaimerText><text>This is a disclaimer text</text></disclaimerText>";

    private static String xacmlResult;

    private static String ruleExecutionResponseContainer;
    private static String c32Document;
    private static XacmlResult xacmlResultObject;
    private static String endpointAddressGuvnorService;
    private static RuleExecutionServiceImpl ruleExecutionService;

    private static SimpleMarshallerImpl marshaller;
    private static FileReaderImpl fileReader;
    private static DocumentXmlConverterImpl documentXmlConverter;
    private static MetadataGeneratorImpl metadataGenerator;
    private static DocumentEditorImpl documentEditor;
    private static DocumentTaggerImpl documentTagger;
    private static DocumentEncrypterImpl documentEncrypter;
    private static DocumentRedactor documentRedactor;
    private static DocumentFactModelExtractorImpl documentFactModelExtractor;
    private static DocumentAccessorImpl documentAccessor;
    private static EmbeddedClinicalDocumentExtractorImpl embeddedClinicalDocumentExtractor;
    private static AdditionalMetadataGeneratorForSegmentedClinicalDocumentImpl additionalMetadataGeneratorForSegmentedClinicalDocumentImpl;
    private static XmlTransformer xmlTransformer;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Before
    public void setUp() throws IOException {
        fileReader = new FileReaderImpl();
        documentXmlConverter = new DocumentXmlConverterImpl();
        marshaller = new SimpleMarshallerImpl();
        documentEncrypter = new DocumentEncrypterImpl(documentXmlConverter);
        documentAccessor = new DocumentAccessorImpl();
        xmlTransformer = new XmlTransformerImpl(marshaller);
        metadataGenerator = new MetadataGeneratorImpl(xmlTransformer);
        documentEditor = new DocumentEditorImpl(metadataGenerator, fileReader,
                documentXmlConverter, new DocumentAccessorImpl());
        documentTagger = new DocumentTaggerImpl(DISCLAIMER_DOCUMENT,
                xmlTransformer);
        final Set<AbstractObligationLevelRedactionHandler> obligationLevelChain = new HashSet<AbstractObligationLevelRedactionHandler>();
        final Set<AbstractClinicalFactLevelRedactionHandler> clinicalFactLevelChain = new HashSet<AbstractClinicalFactLevelRedactionHandler>();
        final Set<AbstractPostRedactionLevelRedactionHandler> postRedactionChain = new HashSet<AbstractPostRedactionLevelRedactionHandler>();
        final Set<AbstractDocumentLevelRedactionHandler> documentLevelRedactionHandlers = new HashSet<AbstractDocumentLevelRedactionHandler>();
        documentLevelRedactionHandlers.add(new UnsupportedHeaderElementHandler(
                documentAccessor, unsupportedHeaders));
        obligationLevelChain.add(new Section(documentAccessor));
        clinicalFactLevelChain.add(new Entry(documentAccessor));
        clinicalFactLevelChain.add(new HumanReadableTextNodeByCode(
                documentAccessor));
        clinicalFactLevelChain.add(new HumanReadableTextNodeByDisplayName(
                documentAccessor));
        postRedactionChain.add(new DocumentCleanupForNoEntryAndNoSection(
                documentAccessor));
        postRedactionChain
                .add(new RuleExecutionResponseMarkerForRedactedEntries(
                        documentAccessor));
        documentRedactor = new DocumentRedactorImpl(marshaller,
                documentXmlConverter, documentAccessor,
                documentLevelRedactionHandlers, obligationLevelChain,
                clinicalFactLevelChain, postRedactionChain);
        documentFactModelExtractor = new DocumentFactModelExtractorImpl(
                xmlTransformer);
        additionalMetadataGeneratorForSegmentedClinicalDocumentImpl = new AdditionalMetadataGeneratorForSegmentedClinicalDocumentImpl(
                xmlTransformer);
        embeddedClinicalDocumentExtractor = new EmbeddedClinicalDocumentExtractorImpl(
                documentXmlConverter, documentAccessor);

        c32Document = fileReader.readFile("sampleC32/c32.xml");
        xacmlResult = "<xacmlResult><pdpDecision>Permit</pdpDecision><purposeOfUse>TREAT</purposeOfUse><messageId>4617a579-1881-4e40-9f98-f85bd81d6502</messageId><homeCommunityId>2.16.840.1.113883.3.467</homeCommunityId><pdpObligation>urn:oasis:names:tc:xspa:2.0:resource:org:us-privacy-law:42CFRPart2</pdpObligation><pdpObligation>urn:oasis:names:tc:xspa:2.0:resource:org:refrain-policy:NORDSLCD</pdpObligation><pdpObligation>urn:oasis:names:tc:xspa:2.0:resource:patient:redact:ETH</pdpObligation><pdpObligation>urn:oasis:names:tc:xspa:2.0:resource:patient:redact:PSY</pdpObligation><pdpObligation>urn:oasis:names:tc:xspa:2.0:resource:patient:mask:HIV</pdpObligation></xacmlResult>";
        ruleExecutionResponseContainer = "<ruleExecutionContainer><executionResponseList><executionResponse><c32SectionLoincCode>11450-4</c32SectionLoincCode><c32SectionTitle>Problems</c32SectionTitle><code>66214007</code><codeSystemName>SNOMED CT</codeSystemName><displayName>Substance Abuse Disorder</displayName><documentObligationPolicy>ENCRYPT</documentObligationPolicy><documentRefrainPolicy>NORDSLCD</documentRefrainPolicy><impliedConfSection>R</impliedConfSection><itemAction>REDACT</itemAction><observationId>e11275e7-67ae-11db-bd13-0800200c9a66b827vs52h7</observationId><sensitivity>ETH</sensitivity><USPrivacyLaw>42CFRPart2</USPrivacyLaw></executionResponse><executionResponse><c32SectionLoincCode>11450-4</c32SectionLoincCode><c32SectionTitle>Problems</c32SectionTitle><code>111880001</code><codeSystemName>SNOMED CT</codeSystemName><displayName>Acute HIV</displayName><documentObligationPolicy>ENCRYPT</documentObligationPolicy><documentRefrainPolicy>NORDSLCD</documentRefrainPolicy><impliedConfSection>R</impliedConfSection><itemAction>MASK</itemAction><observationId>d11275e7-67ae-11db-bd13-0800200c9a66</observationId><sensitivity>HIV</sensitivity><USPrivacyLaw>42CFRPart2</USPrivacyLaw></executionResponse></executionResponseList></ruleExecutionContainer>";
        endpointAddressGuvnorService = "http://localhost:7070/guvnor-5.5.0.Final-tomcat-6.0/rest/packages/AnnotationRules/source";

        ruleExecutionService = new RuleExecutionServiceImpl(
                new GuvnorServiceImpl(endpointAddressGuvnorService, "admin",
                        "admin"), new SimpleMarshallerImpl());
        try {
            xacmlResultObject = marshaller.unmarshalFromXml(XacmlResult.class,
                    xacmlResult);
        } catch (final JAXBException e) {
            logger.error(e.getMessage(), e);
        }
    }

    // Integration test
    @Test
    public void testSegmentDocument_Decrypt_Document() {

        RuleExecutionContainer ruleExecutionContainer = null;
        String document = "";

        try {
            final JAXBContext jaxbContext = JAXBContext
                    .newInstance(RuleExecutionContainer.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext
                    .createUnmarshaller();
            final ByteArrayInputStream input = new ByteArrayInputStream(
                    ruleExecutionResponseContainer.getBytes());
            ruleExecutionContainer = (RuleExecutionContainer) jaxbUnmarshaller
                    .unmarshal(input);
            final FactModel factModel = new FactModel();
            factModel.setXacmlResult(xacmlResultObject);
            document = documentRedactor.redactDocument(c32Document,
                    ruleExecutionContainer, factModel).getRedactedDocument();

            Document processedDoc = documentXmlConverter.loadDocument(document);
            FileHelper
                    .writeDocToFile(processedDoc, "unitTest_Redacted_C32.xml");

            // commented out for redact-only application
            // document = documentSegmentation.maskElement(document,
            // ruleExecutionContainer, xacmlResultObject);

            processedDoc = documentXmlConverter.loadDocument(document);
            FileHelper.writeDocToFile(processedDoc, "unitTest_Masked_C32.xml");

			*/
/*
             * Generate the key to be used for decrypting the xml data
			 * encryption key.
			 *//*

            final Key desedeEncryptKey = EncryptTool.generateKeyEncryptionKey();
            final Key desedeMaskKey = EncryptTool.generateKeyEncryptionKey();

            document = documentEncrypter.encryptDocument(desedeEncryptKey,
                    document, ruleExecutionContainer);

            processedDoc = documentXmlConverter.loadDocument(document);

            FileHelper.writeDocToFile(processedDoc,
                    "unitTest_Encrypted_C32.xml");

            */
/*************************************************
             * DECRYPT DOCUMENT
             *************************************************//*

            final Element encryptedDataElement = (Element) processedDoc
                    .getElementsByTagNameNS(
                            EncryptionConstants.EncryptionSpecNS,
                            EncryptionConstants._TAG_ENCRYPTEDDATA).item(0);

			*/
/*
             * The key to be used for decrypting xml data would be obtained from
			 * the keyinfo of the EncrypteData using the kek.
			 *//*

            final XMLCipher xmlCipher = XMLCipher.getInstance();
            xmlCipher.init(XMLCipher.DECRYPT_MODE, null);
            xmlCipher.setKEK(desedeEncryptKey);

			*/
/*
             * The following doFinal call replaces the encrypted data with
			 * decrypted contents in the document.
			 *//*

            if (encryptedDataElement != null) {
                xmlCipher.doFinal(processedDoc, encryptedDataElement);
            }

            FileHelper.writeDocToFile(processedDoc,
                    "unitTest_Decrypted_C32.xml");

            */
/*************************************************
             * DECRYPT ELEMENTS
             *************************************************//*

            NodeList encryptedDataElements = processedDoc
                    .getElementsByTagNameNS(
                            EncryptionConstants.EncryptionSpecNS,
                            EncryptionConstants._TAG_ENCRYPTEDDATA);

            while (encryptedDataElements.getLength() > 0) {
				*/
/*
				 * The key to be used for decrypting xml data would be obtained
				 * from the keyinfo of the EncrypteData using the kek.
				 *//*

                final XMLCipher xmlMaskCipher = XMLCipher.getInstance();
                xmlMaskCipher.init(XMLCipher.DECRYPT_MODE, null);
                xmlMaskCipher.setKEK(desedeMaskKey);

                xmlMaskCipher.doFinal(processedDoc,
                        (Element) encryptedDataElements.item(0));

                encryptedDataElements = processedDoc.getElementsByTagNameNS(
                        EncryptionConstants.EncryptionSpecNS,
                        EncryptionConstants._TAG_ENCRYPTEDDATA);
            }

            FileHelper.writeDocToFile(processedDoc,
                    "unitTest_DecryptedUnMasked_C32.xml");

        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }

        Assert.assertNotSame(document, c32Document);
    }

    // Integration test
    @Test
    public void testSegmentDocument_Segment_Document()
            throws InvalidOriginalClinicalDocumentException,
            InvalidSegmentedClinicalDocumentException,
            XmlDocumentReadFailureException, AuditException {

        final DocumentSegmentationImpl documentSegmentation = new DocumentSegmentationImpl(
                ruleExecutionService, null, documentEditor, marshaller,
                documentRedactor, documentTagger, documentFactModelExtractor,
                embeddedClinicalDocumentExtractor, new ValueSetServiceImplMock(
                fileReader),
                additionalMetadataGeneratorForSegmentedClinicalDocumentImpl);
        final SegmentDocumentResponse result = documentSegmentation
                .segmentDocument(c32Document.toString(), xacmlResult, false,
                        true, true);

        Assert.assertNotNull(result);
    }
}
*/
