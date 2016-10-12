package gov.samhsa.c2s.dss.service.document.dto;

import lombok.Data;
import org.w3c.dom.Node;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Data
public class RedactionHandlerResult {
    private List<Node> redactNodeList = new LinkedList<>();
    private Set<String> redactSectionCodesAndGeneratedEntryIds = new HashSet<>();
    private Set<String> redactSectionSet = new HashSet<>();
    private Set<String> redactCategorySet = new HashSet<>();

    public static RedactionHandlerResult empty() {
        return new RedactionHandlerResult();
    }

    public RedactionHandlerResult concat(RedactionHandlerResult other) {
        RedactionHandlerResult newInstance = empty();
        // from other
        newInstance.getRedactNodeList().addAll(other.getRedactNodeList());
        newInstance.getRedactSectionCodesAndGeneratedEntryIds().addAll(other.getRedactSectionCodesAndGeneratedEntryIds());
        newInstance.getRedactSectionSet().addAll(other.getRedactSectionSet());
        newInstance.getRedactCategorySet().addAll(other.getRedactCategorySet());
        // from this
        newInstance.getRedactNodeList().addAll(getRedactNodeList());
        newInstance.getRedactSectionCodesAndGeneratedEntryIds().addAll(getRedactSectionCodesAndGeneratedEntryIds());
        newInstance.getRedactSectionSet().addAll(getRedactSectionSet());
        newInstance.getRedactCategorySet().addAll(getRedactCategorySet());
        return newInstance;
    }
}
