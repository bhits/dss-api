package gov.samhsa.c2s.dss.service.document.dto;

import lombok.Data;
import java.util.Set;

@Data
public class RedactedDocument {
    private String redactedDocument;
    private String tryPolicyDocument;
    private Set<String> redactedSectionSet;
    private Set<String> redactedCategorySet;

    public RedactedDocument(String redactedDocument, String tryPolicyDocument,
                            Set<String> redactedSectionSet, Set<String> redactedCategorySet) {
        super();
        this.redactedDocument = redactedDocument;
        this.tryPolicyDocument = tryPolicyDocument;
        this.redactedSectionSet = redactedSectionSet;
        this.redactedCategorySet = redactedCategorySet;
    }
}
