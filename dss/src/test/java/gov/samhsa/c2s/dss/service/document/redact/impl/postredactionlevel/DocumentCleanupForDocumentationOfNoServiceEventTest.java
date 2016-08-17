package gov.samhsa.c2s.dss.service.document.redact.impl.postredactionlevel;

import gov.samhsa.c2s.brms.domain.FactModel;
import gov.samhsa.c2s.brms.domain.RuleExecutionContainer;
import gov.samhsa.c2s.dss.service.document.redact.impl.postredactionlevel.DocumentCleanupForDocumentationOfNoServiceEvent;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessor;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessorImpl;
import gov.samhsa.mhc.common.document.converter.DocumentXmlConverter;
import gov.samhsa.mhc.common.document.converter.DocumentXmlConverterImpl;
import gov.samhsa.mhc.common.filereader.FileReader;
import gov.samhsa.mhc.common.filereader.FileReaderImpl;
import gov.samhsa.mhc.common.marshaller.SimpleMarshaller;
import gov.samhsa.mhc.common.marshaller.SimpleMarshallerException;
import gov.samhsa.mhc.common.marshaller.SimpleMarshallerImpl;
import gov.samhsa.c2s.dss.service.document.EmbeddedClinicalDocumentExtractor;
import gov.samhsa.c2s.dss.service.document.EmbeddedClinicalDocumentExtractorImpl;
import gov.samhsa.c2s.dss.service.document.dto.RedactionHandlerResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class DocumentCleanupForDocumentationOfNoServiceEventTest {

    public static final String TEST_PATH = "sampleC32-redactionHandlers/";
    public static final String FACTMODEL_PATH = "factmodel/";
    public static final String RULEEXECUTIONCONTAINER_PATH = "ruleexecutioncontainer/";

    private FileReader fileReader;
    private SimpleMarshaller marshaller;
    private DocumentAccessor documentAccessor;
    private DocumentXmlConverter documentXmlConverter;
    private EmbeddedClinicalDocumentExtractor embeddedClinicalDocumentExtractor;

    private DocumentCleanupForDocumentationOfNoServiceEvent sut;

    @Before
    public void setUp() throws Exception {
        fileReader = new FileReaderImpl();
        marshaller = new SimpleMarshallerImpl();
        documentAccessor = new DocumentAccessorImpl();
        documentXmlConverter = new DocumentXmlConverterImpl();
        embeddedClinicalDocumentExtractor = new EmbeddedClinicalDocumentExtractorImpl(documentXmlConverter, documentAccessor);
        sut = new DocumentCleanupForDocumentationOfNoServiceEvent(documentAccessor);
    }

    @Test
    public void testExecute_DocumentationOf_Has_ServiceEvent() throws IOException, SimpleMarshallerException, XPathExpressionException {
        // Arrange
        String c32FileName = "c32.xml";
        String factmodelXml = fileReader.readFile(TEST_PATH + FACTMODEL_PATH + c32FileName);
        String c32 = embeddedClinicalDocumentExtractor.extractClinicalDocumentFromFactModel(factmodelXml);
        String ruleExecutionContainerXml = fileReader.readFile(TEST_PATH + RULEEXECUTIONCONTAINER_PATH + c32FileName);
        RuleExecutionContainer ruleExecutionContainer = marshaller.unmarshalFromXml(RuleExecutionContainer.class, ruleExecutionContainerXml);
        Document c32Document = documentXmlConverter.loadDocument(c32);
        Document factModelDocument = documentXmlConverter.loadDocument(factmodelXml);
        FactModel factModel = marshaller.unmarshalFromXml(FactModel.class, factmodelXml);
        RedactionHandlerResult preRedactionResults = new RedactionHandlerResult();

        // Act
        sut.execute(c32Document, factModel.getXacmlResult(), factModel,
                factModelDocument, ruleExecutionContainer, preRedactionResults);

        // Assert
        assertEquals(1, documentAccessor.getNodeList(c32Document, "//hl7:documentationOf").getLength());
        assertEquals(3, documentAccessor.getNodeList(c32Document, "//hl7:documentationOf").item(0).getChildNodes().getLength());
        assertEquals("serviceEvent", documentAccessor.getNodeList(c32Document, "//hl7:documentationOf").item(0).getChildNodes().item(1).getNodeName());
    }

    @Test
    public void testExecute_DocumentationOf_Does_Not_Have_ServiceEvent() throws IOException, SimpleMarshallerException, XPathExpressionException {
        // Arrange
        String c32FileName = "c32-emptyDocumentationOf.xml";
        String factmodelXml = fileReader.readFile(TEST_PATH + FACTMODEL_PATH + c32FileName);
        String c32 = embeddedClinicalDocumentExtractor.extractClinicalDocumentFromFactModel(factmodelXml);
        String ruleExecutionContainerXml = fileReader.readFile(TEST_PATH + RULEEXECUTIONCONTAINER_PATH + c32FileName);
        RuleExecutionContainer ruleExecutionContainer = marshaller.unmarshalFromXml(RuleExecutionContainer.class, ruleExecutionContainerXml);
        Document c32Document = documentXmlConverter.loadDocument(c32);
        Document factModelDocument = documentXmlConverter.loadDocument(factmodelXml);
        FactModel factModel = marshaller.unmarshalFromXml(FactModel.class, factmodelXml);
        RedactionHandlerResult preRedactionResults = new RedactionHandlerResult();

        // Act
        sut.execute(c32Document, factModel.getXacmlResult(), factModel,
                factModelDocument, ruleExecutionContainer, preRedactionResults);

        // Assert
        assertEquals(0, documentAccessor.getNodeList(c32Document, "//hl7:documentationOf").getLength());
    }
}
