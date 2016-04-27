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
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class UnsupportedSectionHandlerTest {

    public static final String TEST_PATH = "sampleC32/";
    public static final List<String> sectionWhiteList = Arrays.asList("11450-4", "48765-2", "10160-0", "30954-2");

    private FileReader fileReader;
    private DocumentAccessor documentAccessor;
    private DocumentXmlConverter documentXmlConverter;
    private UnsupportedSectionHandler sut;

    @Before
    public void setUp() throws Exception {
        fileReader = new FileReaderImpl();
        documentAccessor = new DocumentAccessorImpl();
        documentXmlConverter = new DocumentXmlConverterImpl();
        sut = new UnsupportedSectionHandler(documentAccessor);
        ReflectionTestUtils.setField(sut, "sectionWhiteList", sectionWhiteList);
    }


    @Test
    public void testExecute()
            throws IOException, SimpleMarshallerException,
            XPathExpressionException {
        // Arrange
        String c32FileName = "JohnHalamkaCCDDocument_C32.xml";
        String c32 = fileReader.readFile(TEST_PATH + c32FileName);
        Document c32Document = documentXmlConverter.loadDocument(c32);

        // Act
        final RedactionHandlerResult response = sut.execute(c32Document);

        // Assert
        assertEquals(12, response.getRedactNodeList().size());
    }
}
