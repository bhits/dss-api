package gov.samhsa.mhc.dss.service.document;

import gov.samhsa.mhc.brms.domain.FactModel;
import gov.samhsa.mhc.brms.service.RuleExecutionServiceImpl;
import gov.samhsa.mhc.brms.service.guvnor.GuvnorServiceImpl;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessorImpl;
import gov.samhsa.mhc.common.document.converter.DocumentXmlConverterImpl;
import gov.samhsa.mhc.common.document.transformer.XmlTransformer;
import gov.samhsa.mhc.common.document.transformer.XmlTransformerImpl;
import gov.samhsa.mhc.common.filereader.FileReaderImpl;
import gov.samhsa.mhc.common.marshaller.SimpleMarshallerImpl;
import gov.samhsa.mhc.common.namespace.DefaultNamespaceContext;
import gov.samhsa.mhc.dss.config.DocumentTaggerConfig;
import gov.samhsa.mhc.dss.config.CustomSection;
import gov.samhsa.mhc.dss.service.exception.DocumentSegmentationException;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DocumentTaggerImplTest {

    public static final String ADDITIONAL_SECTION_TEXT_1 = "              The patient has authorized the sharing of their continuity of care document in a C32 summary format that has been provided by Prince George's County Health Department and its affiliates as a partial reference service; it is not to be used as a substitute for a medical intake process and/or diagnosis, which must be provided by the patient's own physician or reviewing practitioner.<br/>\n" +
            "              <br/>CAUTION:<br/>The following history may not be complete<br/>\n" +
            "              <list>\n" +
            "                  <item>Patients may have purchased or obtained care through sources which bypass Prince George's County Health Department supplying the history.</item>\n" +
            "                  <item>Electronic Medical Record (EMR) systems may have their own unique method for accepting and/or formatting data sent by the system of origin.</item>\n" +
            "              </list>";
    public static final String ADDITIONAL_SECTION_TEXT_2 = "              \"This information has been disclosed to you from records protected by\n" +
            "              Federal confidentiality rules (42 CFR part 2). The Federal rules prohibit\n" +
            "              you from making any further disclosure of this information unless further\n" +
            "              disclosure is expressly permitted by the written consent of the person to\n" +
            "              whom it pertains or as otherwise permitted by 42 CFR part 2. A general\n" +
            "              authorization for the release of medical or other information is NOT\n" +
            "              sufficient for this purpose. The Federal rules restrict any use of the\n" +
            "              information to criminally investigate or prosecute any alcohol or drug abuse\n" +
            "              patient.\"<br/>(42 C.F.R. - 2.32)";
    private static final String N = "N";
    private static final String R = "R";
    private static final String V = "V";
    private static final String PROBLEMS_SECTION = "11450-4";
    private static final String ALLERGIES_SECTION = "48765-2";
    private static final String MEDICATIONS_SECTION = "10160-0";
    private static final String RESULTS_SECTION = "30954-2";
    private static final String REDACT = "REDACT";
    private static final String NO_ACTION = "NO_ACTION";
    private static FileReaderImpl fileReader;

    private static SimpleMarshallerImpl marshaller;
    private static DocumentFactModelExtractorImpl documentFactModelExtractor;
    private static EmbeddedClinicalDocumentExtractor embeddedClinicalDocumentExtractor;
    private static DocumentAccessorImpl documentAccessor;
    private static RuleExecutionServiceImpl ruleExecutionService;
    private static GuvnorServiceImpl guvnorServiceMock;
    private static String c32;

    private static String remC32;
    private static String robustC32;
    private static String executionResponseContainer;
    private static String remExecutionResponseContainer;
    private static String testRuleExecutionResponseContainer_xml;
    private static String messageId;
    private static DocumentXmlConverterImpl documentXmlConverter;
    private static DocumentTaggerImpl documentTagger;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private XmlTransformer xmlTransformer;

    @Before
    public void setUp() throws Exception {
        // Arrange
        fileReader = new FileReaderImpl();
        documentAccessor = new DocumentAccessorImpl();
        marshaller = new SimpleMarshallerImpl();
        xmlTransformer = new XmlTransformerImpl(marshaller);
        documentFactModelExtractor = new DocumentFactModelExtractorImpl(
                xmlTransformer);
        documentXmlConverter = new DocumentXmlConverterImpl();
        embeddedClinicalDocumentExtractor = new EmbeddedClinicalDocumentExtractorImpl(
                documentXmlConverter, documentAccessor);
        guvnorServiceMock = mock(GuvnorServiceImpl.class);
        final String ruleSource = fileReader
                .readFile("testAnnotationRules.txt");
        when(guvnorServiceMock.getVersionedRulesFromPackage()).thenReturn(
                ruleSource);
        ruleExecutionService = new RuleExecutionServiceImpl(guvnorServiceMock,
                marshaller);
        c32 = fileReader.readFile("sampleC32/c32.xml");
        remC32 = fileReader.readFile("testRemC32.xml");
        robustC32 = fileReader
                .readFile("testMU_Rev3_HITSP_C32C83_4Sections_RobustEntries_NoErrors.xml");
        executionResponseContainer = fileReader
                .readFile("ruleExecutionResponseContainer.xml");
        remExecutionResponseContainer = fileReader
                .readFile("testRemRuleExecutionContainer.xml");
        testRuleExecutionResponseContainer_xml = fileReader
                .readFile("testRuleExecutionResponseContainer.xml");
        messageId = UUID.randomUUID().toString();
        documentXmlConverter = new DocumentXmlConverterImpl();

        documentTagger = new DocumentTaggerImpl();
        ReflectionTestUtils.setField(documentTagger, "xmlTransformer", xmlTransformer);
        CustomSection customSection1 = new CustomSection();
        customSection1.setCode("DISCLAIMER");
        customSection1.setCodeSystem("2.25.85119437033116720353817881047915448747");
        customSection1.setCodeSystemName("Consent2Share Disclaimer Codes");
        customSection1.setDisplayName("DISCLAIMER");
        customSection1.setTitle("***PLEASE READ THE DISCLAIMER***");
        customSection1.setText(ADDITIONAL_SECTION_TEXT_1);
        CustomSection customSection2 = new CustomSection();
        customSection2.setCode("PROHIBITION_ON_REDISCLOSURE");
        customSection2.setCodeSystem("2.25.85119437033116720353817881047915448747");
        customSection2.setCodeSystemName("Consent2Share Disclaimer Codes");
        customSection2.setDisplayName("PROHIBITION ON RE-DISCLOSURE");
        customSection2.setTitle("***PLEASE READ PROHIBITION ON RE-DISCLOSURE***");
        customSection2.setText(ADDITIONAL_SECTION_TEXT_2);
        DocumentTaggerConfig documentTaggerConfig = new DocumentTaggerConfig();
        documentTaggerConfig.getAdditionalSections().addAll(Arrays.asList(customSection1, customSection2));
        ReflectionTestUtils.setField(documentTagger, "documentTaggerConfig", documentTaggerConfig);
        ReflectionTestUtils.setField(documentTagger, "marshaller", new SimpleMarshallerImpl());
    }

    @Test
    public void testTagDocument() throws Exception {
        // Arrange
        logger.debug(c32);

        // Act
        final String taggedDocument = documentTagger.tagDocument(c32,
                executionResponseContainer);

        // Assert
        logger.debug(taggedDocument);

        assertTrue(!taggedDocument.contains("<confidentialityCode/>"));
        assertTrue(taggedDocument
                .contains("<confidentialityCode xmlns:ds4p=\"http://www.siframework.org/ds4p\""));
        assertTrue(taggedDocument.contains(ADDITIONAL_SECTION_TEXT_1));
        assertTrue(taggedDocument.contains(ADDITIONAL_SECTION_TEXT_2));
        assertTrue(taggedDocument.contains("code=\"R\""));
        assertTrue(taggedDocument
                .contains("codeSystem=\"2.16.840.1.113883.5.25\""));

        assertTrue(c32.contains("<confidentialityCode/>"));
        assertTrue(!c32
                .contains("<confidentialityCode xmlns:ds4p=\"http://www.siframework.org/ds4p\""));
        assertTrue(!c32.contains("code=\"R\""));
        assertTrue(!c32.contains("codeSystem=\"2.16.840.1.113883.5.25\""));
    }

    @Test
    public void testTagDocument_Confidentiality_N() throws Exception {
        // Arrange
        final String problem1 = REDACT;
        final String problem2 = REDACT;
        final String allergy1 = REDACT;
        final String allergy2 = REDACT;
        final String medication1 = REDACT;
        final String medication2 = REDACT;
        final String result1 = REDACT;
        final String result2 = REDACT;

        final String expectedDocumentLevelConfidentiality = N;
        final String expectedProblemsSectionLevelConfidentiality = N;
        final String expectedAllergiesSectionLevelConfidentiality = N;
        final String expectedMedicationsSectionLevelConfidentiality = N;
        final String expectedResultsSectionLevelConfidentiality = N;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_R() throws Exception {
        // Arrange
        final String responseContainer = remExecutionResponseContainer.replace(
                "@itemAction", REDACT);
        final String expectedDocumentLevelConfidentiality = R;
        final String expectedProblemsSectionLevelConfidentiality = R;

        // Act
        final String taggedDocument = documentTagger.tagDocument(remC32,
                responseContainer);

        // Assert
        final Document doc = documentXmlConverter.loadDocument(taggedDocument);
        // --Document Level
        verifyDocumentLevelConfidentiality(doc,
                expectedDocumentLevelConfidentiality);
        // --Problems Section Level
        verifySectionLevelConfidentiality(doc, PROBLEMS_SECTION,
                expectedProblemsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_R_From_Allergies()
            throws Exception {
        // Arrange
        final String problem1 = REDACT;
        final String problem2 = REDACT;
        final String allergy1 = NO_ACTION;
        final String allergy2 = REDACT;
        final String medication1 = REDACT;
        final String medication2 = REDACT;
        final String result1 = REDACT;
        final String result2 = REDACT;

        final String expectedDocumentLevelConfidentiality = R;
        final String expectedProblemsSectionLevelConfidentiality = N;
        final String expectedAllergiesSectionLevelConfidentiality = R;
        final String expectedMedicationsSectionLevelConfidentiality = N;
        final String expectedResultsSectionLevelConfidentiality = N;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_R_From_Medications()
            throws Exception {
        // Arrange
        final String problem1 = REDACT;
        final String problem2 = REDACT;
        final String allergy1 = REDACT;
        final String allergy2 = REDACT;
        final String medication1 = NO_ACTION;
        final String medication2 = REDACT;
        final String result1 = REDACT;
        final String result2 = REDACT;

        final String expectedDocumentLevelConfidentiality = R;
        final String expectedProblemsSectionLevelConfidentiality = N;
        final String expectedAllergiesSectionLevelConfidentiality = N;
        final String expectedMedicationsSectionLevelConfidentiality = R;
        final String expectedResultsSectionLevelConfidentiality = N;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_R_From_Problems()
            throws Exception {
        // Arrange
        final String problem1 = NO_ACTION;
        final String problem2 = REDACT;
        final String allergy1 = REDACT;
        final String allergy2 = REDACT;
        final String medication1 = REDACT;
        final String medication2 = REDACT;
        final String result1 = REDACT;
        final String result2 = REDACT;

        final String expectedDocumentLevelConfidentiality = R;
        final String expectedProblemsSectionLevelConfidentiality = R;
        final String expectedAllergiesSectionLevelConfidentiality = N;
        final String expectedMedicationsSectionLevelConfidentiality = N;
        final String expectedResultsSectionLevelConfidentiality = N;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_R_From_Results()
            throws Exception {
        // Arrange
        final String problem1 = REDACT;
        final String problem2 = REDACT;
        final String allergy1 = REDACT;
        final String allergy2 = REDACT;
        final String medication1 = REDACT;
        final String medication2 = REDACT;
        final String result1 = NO_ACTION;
        final String result2 = REDACT;

        final String expectedDocumentLevelConfidentiality = R;
        final String expectedProblemsSectionLevelConfidentiality = N;
        final String expectedAllergiesSectionLevelConfidentiality = N;
        final String expectedMedicationsSectionLevelConfidentiality = N;
        final String expectedResultsSectionLevelConfidentiality = R;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V() throws Exception {
        // Arrange
        final String responseContainer = remExecutionResponseContainer.replace(
                "@itemAction", NO_ACTION);
        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = V;

        // Act
        final String taggedDocument = documentTagger.tagDocument(remC32,
                responseContainer);

        // Assert
        final Document doc = documentXmlConverter.loadDocument(taggedDocument);
        verifyDocumentLevelConfidentiality(doc,
                expectedDocumentLevelConfidentiality);
        // --Problems Section Level
        verifySectionLevelConfidentiality(doc, PROBLEMS_SECTION,
                expectedProblemsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V_Allergy_Overrides_Medication()
            throws Exception {
        // Arrange
        final String problem1 = REDACT;
        final String problem2 = REDACT;
        final String allergy1 = REDACT;
        final String allergy2 = NO_ACTION;
        final String medication1 = NO_ACTION;
        final String medication2 = REDACT;
        final String result1 = REDACT;
        final String result2 = REDACT;

        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = N;
        final String expectedAllergiesSectionLevelConfidentiality = V;
        final String expectedMedicationsSectionLevelConfidentiality = R;
        final String expectedResultsSectionLevelConfidentiality = N;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V_Allergy_Overrides_Problem()
            throws Exception {
        // Arrange
        final String problem1 = NO_ACTION;
        final String problem2 = REDACT;
        final String allergy1 = REDACT;
        final String allergy2 = NO_ACTION;
        final String medication1 = REDACT;
        final String medication2 = REDACT;
        final String result1 = REDACT;
        final String result2 = REDACT;

        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = R;
        final String expectedAllergiesSectionLevelConfidentiality = V;
        final String expectedMedicationsSectionLevelConfidentiality = N;
        final String expectedResultsSectionLevelConfidentiality = N;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V_Allergy_Overrides_Result()
            throws Exception {
        // Arrange
        final String problem1 = REDACT;
        final String problem2 = REDACT;
        final String allergy1 = REDACT;
        final String allergy2 = NO_ACTION;
        final String medication1 = REDACT;
        final String medication2 = REDACT;
        final String result1 = NO_ACTION;
        final String result2 = REDACT;

        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = N;
        final String expectedAllergiesSectionLevelConfidentiality = V;
        final String expectedMedicationsSectionLevelConfidentiality = N;
        final String expectedResultsSectionLevelConfidentiality = R;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V_From_Allergies1()
            throws Exception {
        // Arrange
        final String problem1 = REDACT;
        final String problem2 = REDACT;
        final String allergy1 = REDACT;
        final String allergy2 = NO_ACTION;
        final String medication1 = REDACT;
        final String medication2 = REDACT;
        final String result1 = REDACT;
        final String result2 = REDACT;

        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = N;
        final String expectedAllergiesSectionLevelConfidentiality = V;
        final String expectedMedicationsSectionLevelConfidentiality = N;
        final String expectedResultsSectionLevelConfidentiality = N;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V_From_Allergies2()
            throws Exception {
        // Arrange
        final String problem1 = REDACT;
        final String problem2 = REDACT;
        final String allergy1 = NO_ACTION;
        final String allergy2 = NO_ACTION;
        final String medication1 = REDACT;
        final String medication2 = REDACT;
        final String result1 = REDACT;
        final String result2 = REDACT;

        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = N;
        final String expectedAllergiesSectionLevelConfidentiality = V;
        final String expectedMedicationsSectionLevelConfidentiality = N;
        final String expectedResultsSectionLevelConfidentiality = N;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V_From_Medications1()
            throws Exception {
        // Arrange
        final String problem1 = REDACT;
        final String problem2 = REDACT;
        final String allergy1 = REDACT;
        final String allergy2 = REDACT;
        final String medication1 = REDACT;
        final String medication2 = NO_ACTION;
        final String result1 = REDACT;
        final String result2 = REDACT;

        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = N;
        final String expectedAllergiesSectionLevelConfidentiality = N;
        final String expectedMedicationsSectionLevelConfidentiality = V;
        final String expectedResultsSectionLevelConfidentiality = N;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V_From_Medications2()
            throws Exception {
        // Arrange
        final String problem1 = REDACT;
        final String problem2 = REDACT;
        final String allergy1 = REDACT;
        final String allergy2 = REDACT;
        final String medication1 = NO_ACTION;
        final String medication2 = NO_ACTION;
        final String result1 = REDACT;
        final String result2 = REDACT;

        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = N;
        final String expectedAllergiesSectionLevelConfidentiality = N;
        final String expectedMedicationsSectionLevelConfidentiality = V;
        final String expectedResultsSectionLevelConfidentiality = N;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V_From_Problems1()
            throws Exception {
        // Arrange
        final String problem1 = REDACT;
        final String problem2 = NO_ACTION;
        final String allergy1 = REDACT;
        final String allergy2 = REDACT;
        final String medication1 = REDACT;
        final String medication2 = REDACT;
        final String result1 = REDACT;
        final String result2 = REDACT;

        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = V;
        final String expectedAllergiesSectionLevelConfidentiality = N;
        final String expectedMedicationsSectionLevelConfidentiality = N;
        final String expectedResultsSectionLevelConfidentiality = N;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V_From_Problems2()
            throws Exception {
        // Arrange
        final String problem1 = NO_ACTION;
        final String problem2 = NO_ACTION;
        final String allergy1 = REDACT;
        final String allergy2 = REDACT;
        final String medication1 = REDACT;
        final String medication2 = REDACT;
        final String result1 = REDACT;
        final String result2 = REDACT;

        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = V;
        final String expectedAllergiesSectionLevelConfidentiality = N;
        final String expectedMedicationsSectionLevelConfidentiality = N;
        final String expectedResultsSectionLevelConfidentiality = N;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V_From_Results1()
            throws Exception {
        // Arrange
        final String problem1 = REDACT;
        final String problem2 = REDACT;
        final String allergy1 = REDACT;
        final String allergy2 = REDACT;
        final String medication1 = REDACT;
        final String medication2 = REDACT;
        final String result1 = REDACT;
        final String result2 = NO_ACTION;

        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = N;
        final String expectedAllergiesSectionLevelConfidentiality = N;
        final String expectedMedicationsSectionLevelConfidentiality = N;
        final String expectedResultsSectionLevelConfidentiality = V;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V_From_Results2()
            throws Exception {
        // Arrange
        final String problem1 = REDACT;
        final String problem2 = REDACT;
        final String allergy1 = REDACT;
        final String allergy2 = REDACT;
        final String medication1 = REDACT;
        final String medication2 = REDACT;
        final String result1 = NO_ACTION;
        final String result2 = NO_ACTION;

        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = N;
        final String expectedAllergiesSectionLevelConfidentiality = N;
        final String expectedMedicationsSectionLevelConfidentiality = N;
        final String expectedResultsSectionLevelConfidentiality = V;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V_Medication_Overrides_Allergy()
            throws Exception {
        // Arrange
        final String problem1 = REDACT;
        final String problem2 = REDACT;
        final String allergy1 = NO_ACTION;
        final String allergy2 = REDACT;
        final String medication1 = REDACT;
        final String medication2 = NO_ACTION;
        final String result1 = REDACT;
        final String result2 = REDACT;

        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = N;
        final String expectedAllergiesSectionLevelConfidentiality = R;
        final String expectedMedicationsSectionLevelConfidentiality = V;
        final String expectedResultsSectionLevelConfidentiality = N;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V_Medication_Overrides_Problem()
            throws Exception {
        // Arrange
        final String problem1 = NO_ACTION;
        final String problem2 = REDACT;
        final String allergy1 = REDACT;
        final String allergy2 = REDACT;
        final String medication1 = REDACT;
        final String medication2 = NO_ACTION;
        final String result1 = REDACT;
        final String result2 = REDACT;

        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = R;
        final String expectedAllergiesSectionLevelConfidentiality = N;
        final String expectedMedicationsSectionLevelConfidentiality = V;
        final String expectedResultsSectionLevelConfidentiality = N;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V_Medication_Overrides_Result()
            throws Exception {
        // Arrange
        final String problem1 = REDACT;
        final String problem2 = REDACT;
        final String allergy1 = REDACT;
        final String allergy2 = REDACT;
        final String medication1 = REDACT;
        final String medication2 = NO_ACTION;
        final String result1 = NO_ACTION;
        final String result2 = REDACT;

        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = N;
        final String expectedAllergiesSectionLevelConfidentiality = N;
        final String expectedMedicationsSectionLevelConfidentiality = V;
        final String expectedResultsSectionLevelConfidentiality = R;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V_Problem_Overrides_Allergy()
            throws Exception {
        // Arrange
        final String problem1 = REDACT;
        final String problem2 = NO_ACTION;
        final String allergy1 = NO_ACTION;
        final String allergy2 = REDACT;
        final String medication1 = REDACT;
        final String medication2 = REDACT;
        final String result1 = REDACT;
        final String result2 = REDACT;

        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = V;
        final String expectedAllergiesSectionLevelConfidentiality = R;
        final String expectedMedicationsSectionLevelConfidentiality = N;
        final String expectedResultsSectionLevelConfidentiality = N;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V_Problem_Overrides_Medication()
            throws Exception {
        // Arrange
        final String problem1 = REDACT;
        final String problem2 = NO_ACTION;
        final String allergy1 = REDACT;
        final String allergy2 = REDACT;
        final String medication1 = NO_ACTION;
        final String medication2 = REDACT;
        final String result1 = REDACT;
        final String result2 = REDACT;

        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = V;
        final String expectedAllergiesSectionLevelConfidentiality = N;
        final String expectedMedicationsSectionLevelConfidentiality = R;
        final String expectedResultsSectionLevelConfidentiality = N;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V_Problem_Overrides_Result()
            throws Exception {
        // Arrange
        final String problem1 = REDACT;
        final String problem2 = NO_ACTION;
        final String allergy1 = REDACT;
        final String allergy2 = REDACT;
        final String medication1 = REDACT;
        final String medication2 = REDACT;
        final String result1 = NO_ACTION;
        final String result2 = REDACT;

        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = V;
        final String expectedAllergiesSectionLevelConfidentiality = N;
        final String expectedMedicationsSectionLevelConfidentiality = N;
        final String expectedResultsSectionLevelConfidentiality = R;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V_Result_Overrides_Allergy()
            throws Exception {
        // Arrange
        final String problem1 = REDACT;
        final String problem2 = REDACT;
        final String allergy1 = NO_ACTION;
        final String allergy2 = REDACT;
        final String medication1 = REDACT;
        final String medication2 = REDACT;
        final String result1 = REDACT;
        final String result2 = NO_ACTION;

        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = N;
        final String expectedAllergiesSectionLevelConfidentiality = R;
        final String expectedMedicationsSectionLevelConfidentiality = N;
        final String expectedResultsSectionLevelConfidentiality = V;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V_Result_Overrides_Medication()
            throws Exception {
        // Arrange
        final String problem1 = REDACT;
        final String problem2 = REDACT;
        final String allergy1 = REDACT;
        final String allergy2 = REDACT;
        final String medication1 = NO_ACTION;
        final String medication2 = REDACT;
        final String result1 = REDACT;
        final String result2 = NO_ACTION;

        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = N;
        final String expectedAllergiesSectionLevelConfidentiality = N;
        final String expectedMedicationsSectionLevelConfidentiality = R;
        final String expectedResultsSectionLevelConfidentiality = V;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Confidentiality_V_Result_Overrides_Problem()
            throws Exception {
        // Arrange
        final String problem1 = NO_ACTION;
        final String problem2 = REDACT;
        final String allergy1 = REDACT;
        final String allergy2 = REDACT;
        final String medication1 = REDACT;
        final String medication2 = REDACT;
        final String result1 = REDACT;
        final String result2 = NO_ACTION;

        final String expectedDocumentLevelConfidentiality = V;
        final String expectedProblemsSectionLevelConfidentiality = R;
        final String expectedAllergiesSectionLevelConfidentiality = N;
        final String expectedMedicationsSectionLevelConfidentiality = N;
        final String expectedResultsSectionLevelConfidentiality = V;

        // Act and Assert
        testDocumentAndSectionLevelConfidentiality(problem1, problem2,
                allergy1, allergy2, medication1, medication2, result1, result2,
                expectedDocumentLevelConfidentiality,
                expectedProblemsSectionLevelConfidentiality,
                expectedAllergiesSectionLevelConfidentiality,
                expectedMedicationsSectionLevelConfidentiality,
                expectedResultsSectionLevelConfidentiality);
    }

    @Test
    public void testTagDocument_Entry_Level_Tagging() throws Exception {
        // Arrange
        logger.debug(c32);
        String factModelXml = documentFactModelExtractor.extractFactModel(c32,
                fileReader.readFile("testXacmlResultRedactHIV.xml"));
        c32 = embeddedClinicalDocumentExtractor
                .extractClinicalDocumentFromFactModel(factModelXml);
        factModelXml = removeEmbeddedClinicalDocument(factModelXml);
        final FactModel factModel = marshaller.unmarshalFromXml(
                FactModel.class, factModelXml);
        final String executionResponseContainer = ruleExecutionService
                .assertAndExecuteClinicalFacts(factModel)
                .getRuleExecutionResponseContainer();

        // Act
        final String taggedDocument = documentTagger.tagDocument(c32,
                executionResponseContainer);

        // Assert
        logger.debug(taggedDocument);

        assertTrue(!taggedDocument.contains("<confidentialityCode/>"));
        assertTrue(taggedDocument
                .contains("<confidentialityCode xmlns:ds4p=\"http://www.siframework.org/ds4p\""));
        assertTrue(taggedDocument.contains("code=\"R\""));
        assertTrue(taggedDocument.contains(ADDITIONAL_SECTION_TEXT_1));
        assertTrue(taggedDocument.contains(ADDITIONAL_SECTION_TEXT_2));
        assertTrue(taggedDocument
                .contains("codeSystem=\"2.16.840.1.113883.5.25\""));

        assertTrue(c32.contains("<confidentialityCode/>"));
        assertTrue(!c32
                .contains("<confidentialityCode xmlns:ds4p=\"http://www.siframework.org/ds4p\""));
        assertTrue(!c32.contains("code=\"R\""));
        assertTrue(!c32.contains("codeSystem=\"2.16.840.1.113883.5.25\""));

        final Document taggedDoc = documentXmlConverter
                .loadDocument(taggedDocument);
        verifyEntryLevelTags(taggedDoc, "d1e259",
                Arrays.asList(new String[]{"R", "NODSCLCD", "ENCRYPT"}));
        verifyEntryLevelTags(taggedDoc, "d1e220",
                Arrays.asList(new String[]{"V", "ENCRYPT", "NODSCLCD"}));
        verifyEntryLevelTags(taggedDoc, "d1e1261",
                Arrays.asList(new String[]{"R", "ENCRYPT", "NORDSCLCD"}));
    }

    @Test
    public void testTagDocument_Entry_Level_Tagging_Four_Sections()
            throws Exception {
        // Arrange
        logger.debug(c32);
        String factModelXml = documentFactModelExtractor.extractFactModel(c32,
                fileReader.readFile("testXacmlResultRedactHIV.xml"));
        c32 = embeddedClinicalDocumentExtractor
                .extractClinicalDocumentFromFactModel(factModelXml);
        factModelXml = removeEmbeddedClinicalDocument(factModelXml);
        final FactModel factModel = marshaller.unmarshalFromXml(
                FactModel.class, factModelXml);
        final String executionResponseContainer = ruleExecutionService
                .assertAndExecuteClinicalFacts(factModel)
                .getRuleExecutionResponseContainer();

        // Act
        final String taggedDocument = documentTagger.tagDocument(c32,
                executionResponseContainer);

        // Assert
        final Document taggedDoc = documentXmlConverter
                .loadDocument(taggedDocument);
        verifyEntryLevelTags(taggedDoc, "d1e1168",
                Arrays.asList(new String[]{"ENCRYPT", "NODSCLCD", "V"}));
        verifyEntryLevelTags(taggedDoc, "d1e503",
                Arrays.asList(new String[]{"ENCRYPT", "NODSCLCD", "R"}));
        verifyEntryLevelTags(taggedDoc, "d1e1168",
                Arrays.asList(new String[]{"ENCRYPT", "NODSCLCD", "V"}));
        verifyEntryLevelTags(taggedDoc, "d1e341",
                Arrays.asList(new String[]{"ENCRYPT", "NODSCLCD", "V"}));
        verifyEntryLevelTags(taggedDoc, "d1e849",
                Arrays.asList(new String[]{"ENCRYPT", "NODSCLCD", "V"}));
        verifyEntryLevelTags(taggedDoc, "d1e798",
                Arrays.asList(new String[]{"ENCRYPT", "NODSCLCD", "V"}));
        verifyEntryLevelTags(taggedDoc, "d1e220",
                Arrays.asList(new String[]{"ENCRYPT", "NODSCLCD", "V"}));
        verifyEntryLevelTags(taggedDoc, "d1e259",
                Arrays.asList(new String[]{"ENCRYPT", "NODSCLCD", "R"}));
        verifyEntryLevelTags(taggedDoc, "d1e564",
                Arrays.asList(new String[]{"ENCRYPT", "NODSCLCD", "V"}));
        verifyEntryLevelTags(taggedDoc, "d1e1261",
                Arrays.asList(new String[]{"ENCRYPT", "NORDSCLCD", "R"}));
    }

    @Test(expected = DocumentSegmentationException.class)
    public void testTagDocument_Throws_DocumentSegmentationException() {
        // Empty xml file
        @SuppressWarnings("unused")
        final String taggedDocument = documentTagger.tagDocument("",
                executionResponseContainer);
    }

    private void addNodeListToSolutionList(List<String> solutionList,
                                           NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            solutionList.add(node.getNodeValue());
        }
    }

    private Node getNode(Document xmlDocument, String xPathExpr)
            throws XPathExpressionException {
        // Create XPath instance
        final XPathFactory xpathFact = XPathFactory.newInstance();
        final XPath xpath = xpathFact.newXPath();
        xpath.setNamespaceContext(new DefaultNamespaceContext());

        // Evaluate XPath expression against parsed document
        final Node node = (Node) xpath.evaluate(xPathExpr, xmlDocument,
                XPathConstants.NODE);

        return node;
    }

    private NodeList getNodeList(Document xmlDocument, String xPathExpr)
            throws XPathExpressionException {
        // Create XPath instance
        final XPathFactory xpathFact = XPathFactory.newInstance();
        final XPath xpath = xpathFact.newXPath();
        xpath.setNamespaceContext(new DefaultNamespaceContext());

        // Evaluate XPath expression against parsed document
        final NodeList nodeList = (NodeList) xpath.evaluate(xPathExpr,
                xmlDocument, XPathConstants.NODESET);

        return nodeList;
    }

    private String removeEmbeddedClinicalDocument(String factModelXml)
            throws Exception, XPathExpressionException, IOException {
        final Document fmDoc = documentXmlConverter.loadDocument(factModelXml);
        final Node ecd = documentAccessor.getNode(fmDoc,
                "//hl7:EmbeddedClinicalDocument").get();
        ecd.getParentNode().removeChild(ecd);
        factModelXml = documentXmlConverter.convertXmlDocToString(fmDoc);
        return factModelXml;
    }

    private String setResponseContainer(String problem1, String problem2,
                                        String allergy1, String allergy2, String medication1,
                                        String medication2, String result1, String result2) {
        final String responseContainer = testRuleExecutionResponseContainer_xml
                .replace("@Problem1", problem1).replace("@Problem2", problem2)
                .replace("@Allergy1", allergy1).replace("@Allergy2", allergy2)
                .replace("@Medication1", medication1)
                .replace("@Medication2", medication2)
                .replace("@Result1", result1).replace("@Result2", result2);
        return responseContainer;
    }

    private void testDocumentAndSectionLevelConfidentiality(String problem1,
                                                            String problem2, String allergy1, String allergy2,
                                                            String medication1, String medication2, String result1,
                                                            String result2, String expectedDocumentLevelConfidentiality,
                                                            String expectedProblemsSectionLevelConfidentiality,
                                                            String expectedAllergiesSectionLevelConfidentiality,
                                                            String expectedMedicationsSectionLevelConfidentiality,
                                                            String expectedResultsSectionLevelConfidentiality)
            throws Exception, XPathExpressionException {
        final String responseContainer = setResponseContainer(problem1,
                problem2, allergy1, allergy2, medication1, medication2,
                result1, result2);

        // Act
        final String taggedDocument = documentTagger.tagDocument(robustC32,
                responseContainer);

        // Assert
        final Document doc = documentXmlConverter.loadDocument(taggedDocument);
        verifyDocumentLevelConfidentiality(doc,
                expectedDocumentLevelConfidentiality);
        verifySectionLevelConfidentiality(doc, PROBLEMS_SECTION,
                expectedProblemsSectionLevelConfidentiality);
        verifySectionLevelConfidentiality(doc, ALLERGIES_SECTION,
                expectedAllergiesSectionLevelConfidentiality);
        verifySectionLevelConfidentiality(doc, MEDICATIONS_SECTION,
                expectedMedicationsSectionLevelConfidentiality);
        verifySectionLevelConfidentiality(doc, RESULTS_SECTION,
                expectedResultsSectionLevelConfidentiality);
    }

    private void verifyCodeAttributeValue(Document document, String xPathExpr,
                                          String expectedCodeAttributeValue) throws XPathExpressionException {
        final Node documentLevelConfidentialityCodeNode = getNode(document,
                xPathExpr);
        final String documentLevelConfidentialityCodeValue = documentLevelConfidentialityCodeNode
                .getAttributes().getNamedItem("code").getNodeValue();
        assertEquals(expectedCodeAttributeValue,
                documentLevelConfidentialityCodeValue);
    }

    private void verifyDocumentLevelConfidentiality(Document doc,
                                                    String expectedDocumentLevelConfidentiality)
            throws XPathExpressionException {
        final String xPathExprDocumentLevel = "/hl7:ClinicalDocument/hl7:confidentialityCode";
        verifyCodeAttributeValue(doc, xPathExprDocumentLevel,
                expectedDocumentLevelConfidentiality);
    }

    private void verifyEntryLevelTags(Document taggedDoc,
                                      String generatedEntryId, List<String> expectedList)
            throws XPathExpressionException, XMLEncryptionException, Exception {
        final String xPathExpr = "//hl7:entry[child::hl7:generatedEntryId='$generatedEntryId']//hl7:entryRelationship[descendant::hl7:templateId[@root='2.16.840.1.113883.3.3251.1.4']]//hl7:value/@code";
        final String xPathExprForOrganizer = "//hl7:entry[child::hl7:generatedEntryId='$generatedEntryId']//hl7:component[child::hl7:organizer[child::hl7:templateId[@root='2.16.840.1.113883.3.3251.1.4']]]//hl7:value/@code";
        final LinkedList<String> solutionList = new LinkedList<String>();

        // Add everything except organizer
        NodeList nodeList = getNodeList(taggedDoc,
                xPathExpr.replace("$generatedEntryId", generatedEntryId));
        addNodeListToSolutionList(solutionList, nodeList);

        // Add organizer
        nodeList = getNodeList(taggedDoc, xPathExprForOrganizer.replace(
                "$generatedEntryId", generatedEntryId));
        addNodeListToSolutionList(solutionList, nodeList);

        // Assert
        assertTrue(solutionList.containsAll(expectedList));
    }

    private void verifySectionLevelConfidentiality(Document doc,
                                                   String sectionCode, String expectedSectionLevelConfidentiality)
            throws XPathExpressionException {
        final String xPathExprProblemsSectionLevel = "//hl7:section[hl7:code[@code='$']]/hl7:confidentialityCode"
                .replace("$", sectionCode);
        verifyCodeAttributeValue(doc, xPathExprProblemsSectionLevel,
                expectedSectionLevelConfidentiality);
    }
}
