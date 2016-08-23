package gov.samhsa.c2s.dss.service.document.redact.impl.documentlevel;

import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessorException;
import gov.samhsa.c2s.dss.service.document.dto.RedactionHandlerResult;
import gov.samhsa.c2s.dss.service.document.redact.RedactionHandlerException;
import gov.samhsa.c2s.dss.service.document.redact.base.AbstractDocumentLevelRedactionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Service
@ConfigurationProperties(prefix = "c2s.dss.redact")
public class UnsupportedSectionHandler extends
        AbstractDocumentLevelRedactionHandler {

    private static final String XPATH_SECTION = "//hl7:structuredBody/hl7:component[child::hl7:section[child::hl7:code[@code='%1']]]";
    private static final String XPATH_ALL_SECTION_CODES = "//hl7:structuredBody/hl7:component/hl7:section/hl7:code/@code";

    private List<String> sectionWhiteList = new ArrayList<>();

    public UnsupportedSectionHandler() {
    }

    @Autowired
    public UnsupportedSectionHandler(DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    public List<String> getSectionWhiteList() {
        return sectionWhiteList;
    }

    @Override
    public RedactionHandlerResult execute(Document xmlDocument) {
        try {
            // Get complete section list
            final Stream<Node> sectionList = documentAccessor.getNodeListAsStream(xmlDocument,
                    XPATH_ALL_SECTION_CODES);

            // Check if every section code is in the white list. If not add it to
            // redaction list.
            final Set<String> sectionRedactionList = sectionList
                    .map(Node::getNodeValue)
                    .filter(StringUtils::hasText)
                    .filter(nodeValue -> !sectionWhiteList.contains(nodeValue))
                    .collect(toSet());

            // Add redaction list to the global list.
            return sectionRedactionList.stream()
                    .map(sectionCode -> addNodesToList(xmlDocument, XPATH_SECTION, sectionCode))
                    .reduce(RedactionHandlerResult::concat)
                    .orElseGet(RedactionHandlerResult::new);
        } catch (DocumentAccessorException e) {
            throw new RedactionHandlerException(e);
        }
    }
}
