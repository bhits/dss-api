package gov.samhsa.mhc.dss.service.document.template;

import org.w3c.dom.Document;

public interface DocumentTypeResolver {
    DocumentType resolve(Document document);
}
