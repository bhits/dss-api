package gov.samhsa.mhc.dss.service.document.redact.base;

import gov.samhsa.mhc.common.document.accessor.DocumentAccessor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.util.List;
import java.util.Set;

public abstract class AbstractDocumentLevelRedactionHandler extends
        AbstractRedactionHandler {

    public AbstractDocumentLevelRedactionHandler(
            DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    protected AbstractDocumentLevelRedactionHandler() {
    }

    public abstract void execute(Document xmlDocument,
                                 Set<String> redactSectionCodesAndGeneratedEntryIds,
                                 List<Node> listOfNodes) throws XPathExpressionException;
}
