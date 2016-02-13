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
public class UnsupportedHeaderElementHandler extends
        AbstractDocumentLevelRedactionHandler {

    private static final String XPATH_HEADERS_PREFIX = "/hl7:ClinicalDocument/hl7:%1";
    private static final String XPATH_ALL_HEARDERS = "//hl7:ClinicalDocument/child::hl7:*";
    // TODO: value
    @Value("${mhc.dss.UnsupportedHeaderElementHandler.headersWhiteList}")
    private Set<String> headersWhiteList;

    @Override
    public void execute(Document xmlDocument,
                        Set<String> redactSectionCodesAndGeneratedEntryIds,
                        List<Node> listOfNodes) throws XPathExpressionException {
        // Create new headers redaction list
        final Set<String> headersRedactionList = new HashSet<String>();

        // Get complete section list
        final NodeList sectionList = documentAccessor.getNodeList(xmlDocument,
                XPATH_ALL_HEARDERS);

        // Check if every section code is in the white list. If not add it to
        // redaction list.
        for (int i = 0; i < sectionList.getLength(); i++) {
            final Node node = sectionList.item(i);
            if (!headersWhiteList.contains(node.getNodeName())) {
                headersRedactionList.add(node.getNodeName());
            }
        }

        // Add redaction list to the global list.
        for (final String header : headersRedactionList) {
            addNodesToList(xmlDocument, listOfNodes,
                    redactSectionCodesAndGeneratedEntryIds,
                    XPATH_HEADERS_PREFIX, header);
        }
    }
}
