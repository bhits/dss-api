package gov.samhsa.mhc.dss.service.document.redact.impl.documentlevel;

import gov.samhsa.mhc.common.document.accessor.DocumentAccessor;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessorImpl;
import gov.samhsa.mhc.common.document.converter.DocumentXmlConverter;
import gov.samhsa.mhc.common.document.converter.DocumentXmlConverterImpl;
import gov.samhsa.mhc.common.filereader.FileReader;
import gov.samhsa.mhc.common.filereader.FileReaderImpl;
import gov.samhsa.mhc.common.marshaller.SimpleMarshallerException;
import gov.samhsa.mhc.dss.service.document.dto.RedactionHandlerResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class UnsupportedHeaderElementHandlerTest {

    public static final String TEST_PATH = "sampleC32/";
    public static final List<String> unsupportedHeaders =
            Arrays.asList("realmCode", "typeId", "templateId", "id", "code",
                    "title", "effectiveTime", "confidentialityCode",
                    "languageCode", "setId", "versionNumber", "copyTime",
                    "recordTarget", "author", "dataEnterer", "custodian",
                    "legalAuthenticator", "inFulfillmentOf", "documentationOf",
                    "relatedDocument", "authorization", "componentOf",
                    "component");
    private FileReader fileReader;
    private DocumentAccessor documentAccessor;
    private DocumentXmlConverter documentXmlConverter;
    private UnsupportedHeaderElementHandler sut;

    @Before
    public void setUp() throws Exception {
        fileReader = new FileReaderImpl();
        documentAccessor = new DocumentAccessorImpl();
        documentXmlConverter = new DocumentXmlConverterImpl();
        sut = new UnsupportedHeaderElementHandler(documentAccessor);
        ReflectionTestUtils.setField(sut, "headersWhiteList", unsupportedHeaders);
    }

    @Test
    public void testExecute() throws IOException, SimpleMarshallerException,
            XPathExpressionException {
        // Arrange
        String c32FileName = "c32.xml";
        String c32 = fileReader.readFile(TEST_PATH + c32FileName);
        Document c32Document = documentXmlConverter.loadDocument(c32);

        // Act
        final RedactionHandlerResult redactionResult = sut.execute(c32Document);

        // Assert
        assertEquals(redactionResult.getRedactNodeList().size(), 1);
    }
}
