package gov.samhsa.c2s.dss.service.document.template;

import org.w3c.dom.Document;

public interface DocumentTypeResolver {
    DocumentType resolve(Document document);
    DocumentType resolve(String document);
}
