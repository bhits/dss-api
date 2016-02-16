package gov.samhsa.mhc.dss.service.document.redact.impl.documentlevel;

import gov.samhsa.mhc.dss.service.document.redact.base.AbstractDocumentLevelRedactionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UnsupportedSectionHandler extends
        AbstractDocumentLevelRedactionHandler {

    private static final String XPATH_SECTION = "//hl7:structuredBody/hl7:component[child::hl7:section[child::hl7:code[@code='%1']]]";
    private static final String XPATH_ALL_SECTION_CODES = "//hl7:structuredBody/hl7:component/hl7:section/hl7:code/@code";

    private Set<String> sectionWhiteList;

    @Override
    public void execute(Document xmlDocument,
                        Set<String> redactSectionCodesAndGeneratedEntryIds,
                        List<Node> listOfNodes) throws XPathExpressionException {
        // Create new section redaction list
        final Set<String> sectionRedactionList = new HashSet<String>();

        // Get complete section list
        final NodeList sectionList = documentAccessor.getNodeList(xmlDocument,
                XPATH_ALL_SECTION_CODES);

        // Check if every section code is in the white list. If not add it to
        // redaction list.
        for (int i = 0; i < sectionList.getLength(); i++) {
            final Node node = sectionList.item(i);
            if (!sectionWhiteList.contains(node.getNodeValue())) {
                sectionRedactionList.add(node.getNodeValue());
            }
        }

        // Add redaction list to the global list.
        for (final String header : sectionRedactionList) {
            addNodesToList(xmlDocument, listOfNodes,
                    redactSectionCodesAndGeneratedEntryIds, XPATH_SECTION,
                    header);
        }
    }
}
