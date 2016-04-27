package gov.samhsa.mhc.dss.service.document.redact.base;

import gov.samhsa.mhc.common.document.accessor.DocumentAccessor;
import gov.samhsa.mhc.dss.service.document.dto.RedactionHandlerResult;
import org.w3c.dom.Document;

public abstract class AbstractDocumentLevelRedactionHandler extends
        AbstractRedactionHandler {

    public AbstractDocumentLevelRedactionHandler(
            DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    protected AbstractDocumentLevelRedactionHandler() {
    }

    public abstract RedactionHandlerResult execute(Document xmlDocument);
}
