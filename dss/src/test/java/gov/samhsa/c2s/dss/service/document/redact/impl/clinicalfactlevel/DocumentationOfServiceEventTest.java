package gov.samhsa.c2s.dss.service.document.redact.impl.clinicalfactlevel;

import gov.samhsa.c2s.brms.domain.ClinicalFact;
import gov.samhsa.c2s.brms.domain.FactModel;
import gov.samhsa.c2s.brms.domain.RuleExecutionContainer;
import gov.samhsa.c2s.dss.service.document.redact.impl.clinicalfactlevel.DocumentationOfServiceEvent;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessorImpl;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverter;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverterImpl;
import gov.samhsa.c2s.common.filereader.FileReader;
import gov.samhsa.c2s.common.filereader.FileReaderImpl;
import gov.samhsa.c2s.common.marshaller.SimpleMarshaller;
import gov.samhsa.c2s.common.marshaller.SimpleMarshallerException;
import gov.samhsa.c2s.common.marshaller.SimpleMarshallerImpl;
import gov.samhsa.c2s.dss.service.document.EmbeddedClinicalDocumentExtractor;
import gov.samhsa.c2s.dss.service.document.EmbeddedClinicalDocumentExtractorImpl;
import gov.samhsa.c2s.dss.service.document.dto.RedactionHandlerResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class DocumentationOfServiceEventTest {

    public static final String TEST_PATH = "sampleC32-redactionHandlers/";
    public static final String FACTMODEL_PATH = "factmodel/";
    public static final String RULEEXECUTIONCONTAINER_PATH = "ruleexecutioncontainer/";

    private FileReader fileReader;
    private SimpleMarshaller marshaller;
    private DocumentAccessor documentAccessor;
    private DocumentXmlConverter documentXmlConverter;
    private EmbeddedClinicalDocumentExtractor embeddedClinicalDocumentExtractor;

    private DocumentationOfServiceEvent sut;

    @Before
    public void setUp() throws Exception {
        fileReader = new FileReaderImpl();
        marshaller = new SimpleMarshallerImpl();
        documentAccessor = new DocumentAccessorImpl();
        documentXmlConverter = new DocumentXmlConverterImpl();
        embeddedClinicalDocumentExtractor = new EmbeddedClinicalDocumentExtractorImpl(
                documentXmlConverter, documentAccessor);
        sut = new DocumentationOfServiceEvent(documentAccessor);
    }

    @Test
    public void testExecute() throws IOException, SimpleMarshallerException,
            XPathExpressionException {
        // Arrange
        String c32FileName = "c32.xml";
        String factmodelXml = fileReader.readFile(TEST_PATH + FACTMODEL_PATH
                + c32FileName);
        String c32 = embeddedClinicalDocumentExtractor
                .extractClinicalDocumentFromFactModel(factmodelXml);
        String ruleExecutionContainerXml = fileReader.readFile(TEST_PATH
                + RULEEXECUTIONCONTAINER_PATH + c32FileName);
        RuleExecutionContainer ruleExecutionContainer = marshaller
                .unmarshalFromXml(RuleExecutionContainer.class,
                        ruleExecutionContainerXml);
        Document c32Document = documentXmlConverter.loadDocument(c32);
        Document factModelDocument = documentXmlConverter
                .loadDocument(factmodelXml);
        FactModel factModel = marshaller.unmarshalFromXml(FactModel.class,
                factmodelXml);
        ClinicalFact fact = factModel.getClinicalFactList().get(1);
        Set<String> valueSetCategories = new HashSet<String>();
        valueSetCategories.add("HIV");
        valueSetCategories.add("ETH");
        fact.setValueSetCategories(valueSetCategories);

        // Act
        final RedactionHandlerResult response = sut.execute(c32Document, factModel.getXacmlResult(), factModel,
                factModelDocument, fact, ruleExecutionContainer);

        // Assert
        assertEquals(1, response.getRedactNodeList().size());
        assertEquals(1, response.getRedactSectionCodesAndGeneratedEntryIds().size());
        assertEquals(1, response.getRedactCategorySet().size());
        assertEquals(Node.ELEMENT_NODE, response.getRedactNodeList().get(0).getNodeType());
        assertEquals("serviceEvent", response.getRedactNodeList().get(0).getNodeName());
        assertEquals("d1e89",
                response.getRedactSectionCodesAndGeneratedEntryIds().toArray()[0]);
        assertTrue("ETH".equals(response.getRedactCategorySet().toArray()[0])
                || "HIV".equals(response.getRedactCategorySet().toArray()[0]));
    }
}
