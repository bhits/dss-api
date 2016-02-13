package gov.samhsa.mhc.dss.service.document.redact.base;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.util.List;
import java.util.Set;

public abstract class AbstractDocumentLevelRedactionHandler extends
        AbstractRedactionHandler {

    public abstract void execute(Document xmlDocument,
                                 Set<String> redactSectionCodesAndGeneratedEntryIds,
                                 List<Node> listOfNodes) throws XPathExpressionException;
}
