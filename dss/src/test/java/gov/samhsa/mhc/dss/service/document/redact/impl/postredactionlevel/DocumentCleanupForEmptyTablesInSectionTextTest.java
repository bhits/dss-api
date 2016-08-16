package gov.samhsa.mhc.dss.service.document.redact.impl.postredactionlevel;

import gov.samhsa.mhc.common.document.accessor.DocumentAccessor;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessorImpl;
import gov.samhsa.mhc.common.document.converter.DocumentXmlConverter;
import gov.samhsa.mhc.common.document.converter.DocumentXmlConverterImpl;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class DocumentCleanupForEmptyTablesInSectionTextTest {
    private final DocumentXmlConverter documentXmlConverter = new DocumentXmlConverterImpl();
    private final DocumentAccessor documentAccessor = new DocumentAccessorImpl();
    private DocumentCleanupForEmptyTablesInSectionText sut = new DocumentCleanupForEmptyTablesInSectionText(documentAccessor);

    @Test
    public void execute_No_Tbody_In_Table() throws Exception {
        // Arrange
        final String documentXmlString = IOUtils.toString(new ClassPathResource("sampleC32/segmentedC32_No_Tbody_In_Table.xml").getInputStream(), StandardCharsets.UTF_8);
        final Document document = documentXmlConverter.loadDocument(documentXmlString);
        assertEquals(1, documentAccessor.getNodeListAsStream(document, DocumentCleanupForEmptyTablesInSectionText.XPATH_SECTION_TEXT_TABLE_WITH_NO_TBODY).count());
        assertEquals(0, documentAccessor.getNodeListAsStream(document, DocumentCleanupForEmptyTablesInSectionText.XPATH_SECTION_TEXT_TABLE_WITH_NO_TR_IN_TBODY).count());

        // Act
        sut.execute(document, null, null, null, null, null);

        // Assert
        assertEquals(0, documentAccessor.getNodeListAsStream(document, DocumentCleanupForEmptyTablesInSectionText.XPATH_SECTION_TEXT_TABLE_WITH_NO_TBODY).count());
        assertEquals(0, documentAccessor.getNodeListAsStream(document, DocumentCleanupForEmptyTablesInSectionText.XPATH_SECTION_TEXT_TABLE_WITH_NO_TR_IN_TBODY).count());
    }

    @Test
    public void execute_No_Tr_In_Tbody() throws Exception {
        // Arrange
        final String documentXmlString = IOUtils.toString(new ClassPathResource("sampleC32/segmentedC32_No_Tr_In_Tbody.xml").getInputStream(), StandardCharsets.UTF_8);
        final Document document = documentXmlConverter.loadDocument(documentXmlString);
        assertEquals(0, documentAccessor.getNodeListAsStream(document, DocumentCleanupForEmptyTablesInSectionText.XPATH_SECTION_TEXT_TABLE_WITH_NO_TBODY).count());
        assertEquals(1, documentAccessor.getNodeListAsStream(document, DocumentCleanupForEmptyTablesInSectionText.XPATH_SECTION_TEXT_TABLE_WITH_NO_TR_IN_TBODY).count());

        // Act
        sut.execute(document, null, null, null, null, null);

        // Assert
        assertEquals(0, documentAccessor.getNodeListAsStream(document, DocumentCleanupForEmptyTablesInSectionText.XPATH_SECTION_TEXT_TABLE_WITH_NO_TBODY).count());
        assertEquals(0, documentAccessor.getNodeListAsStream(document, DocumentCleanupForEmptyTablesInSectionText.XPATH_SECTION_TEXT_TABLE_WITH_NO_TR_IN_TBODY).count());
    }

    @Test
    public void execute_No_Tr_In_Tbody_No_Tbody_In_Table() throws Exception {
        // Arrange
        final String documentXmlString = IOUtils.toString(new ClassPathResource("sampleC32/segmentedC32_No_Tr_In_Tbody_No_Tbody_In_Table.xml").getInputStream(), StandardCharsets.UTF_8);
        final Document document = documentXmlConverter.loadDocument(documentXmlString);
        assertEquals(1, documentAccessor.getNodeListAsStream(document, DocumentCleanupForEmptyTablesInSectionText.XPATH_SECTION_TEXT_TABLE_WITH_NO_TBODY).count());
        assertEquals(1, documentAccessor.getNodeListAsStream(document, DocumentCleanupForEmptyTablesInSectionText.XPATH_SECTION_TEXT_TABLE_WITH_NO_TR_IN_TBODY).count());

        // Act
        sut.execute(document, null, null, null, null, null);

        // Assert
        assertEquals(0, documentAccessor.getNodeListAsStream(document, DocumentCleanupForEmptyTablesInSectionText.XPATH_SECTION_TEXT_TABLE_WITH_NO_TBODY).count());
        assertEquals(0, documentAccessor.getNodeListAsStream(document, DocumentCleanupForEmptyTablesInSectionText.XPATH_SECTION_TEXT_TABLE_WITH_NO_TR_IN_TBODY).count());
    }
}