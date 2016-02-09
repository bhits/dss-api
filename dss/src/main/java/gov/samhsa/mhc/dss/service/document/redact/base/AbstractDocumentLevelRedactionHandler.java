package gov.samhsa.mhc.dss.service.document.redact.base;

import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import gov.samhsa.mhc.common.document.accessor.DocumentAccessor;

public abstract class AbstractDocumentLevelRedactionHandler extends
        AbstractRedactionHandler {

    public AbstractDocumentLevelRedactionHandler(
            DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    public abstract void execute(Document xmlDocument,
                                 Set<String> redactSectionCodesAndGeneratedEntryIds,
                                 List<Node> listOfNodes) throws XPathExpressionException;
}
