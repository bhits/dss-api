package gov.samhsa.c2s.dss.service.document.redact.impl.documentlevel;

import gov.samhsa.mhc.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.dss.service.document.dto.RedactionHandlerResult;
import gov.samhsa.c2s.dss.service.document.redact.RedactionHandlerException;
import gov.samhsa.c2s.dss.service.document.redact.base.AbstractDocumentLevelRedactionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Service
@ConfigurationProperties(prefix = "c2s.dss.redact")
public class UnsupportedHeaderElementHandler extends
        AbstractDocumentLevelRedactionHandler {

    private static final String XPATH_HEADERS_PREFIX = "/hl7:ClinicalDocument/hl7:%1";
    private static final String XPATH_ALL_HEARDERS = "//hl7:ClinicalDocument/child::hl7:*";

    private List<String> headersWhiteList = new ArrayList<>();

    public UnsupportedHeaderElementHandler() {
    }

    @Autowired
    public UnsupportedHeaderElementHandler(DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    public List<String> getHeadersWhiteList() {
        return headersWhiteList;
    }

    @Override
    public RedactionHandlerResult execute(Document xmlDocument) {
        try {
            // Get complete header list
            final Stream<Node> allHeaders = documentAccessor.getNodeListAsStream(xmlDocument,
                    XPATH_ALL_HEARDERS);

            // Find the headers that are not in the white list
            final Set<String> headersRedactionList = allHeaders
                    .map(Node::getNodeName)
                    .filter(StringUtils::hasText)
                    .filter(nodeName -> !headersWhiteList.contains(nodeName))
                    .collect(toSet());

            // Add redaction list to the global list.
            return headersRedactionList.stream()
                    .map(header -> addNodesToList(xmlDocument, XPATH_HEADERS_PREFIX, header))
                    .reduce(RedactionHandlerResult::concat)
                    .orElseGet(RedactionHandlerResult::new);
        } catch (XPathExpressionException e) {
            throw new RedactionHandlerException(e);
        }
    }
}
