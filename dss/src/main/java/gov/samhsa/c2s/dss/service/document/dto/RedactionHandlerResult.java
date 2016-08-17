package gov.samhsa.c2s.dss.service.document.dto;

import org.w3c.dom.Node;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RedactionHandlerResult {
    private List<Node> redactNodeList = new LinkedList<>();
    private Set<String> redactSectionCodesAndGeneratedEntryIds = new HashSet<>();
    private Set<String> redactSectionSet = new HashSet<>();
    private Set<String> redactCategorySet = new HashSet<>();

    public List<Node> getRedactNodeList() {
        return redactNodeList;
    }

    public void setRedactNodeList(List<Node> redactNodeList) {
        this.redactNodeList = redactNodeList;
    }

    public Set<String> getRedactSectionCodesAndGeneratedEntryIds() {
        return redactSectionCodesAndGeneratedEntryIds;
    }

    public void setRedactSectionCodesAndGeneratedEntryIds(Set<String> redactSectionCodesAndGeneratedEntryIds) {
        this.redactSectionCodesAndGeneratedEntryIds = redactSectionCodesAndGeneratedEntryIds;
    }

    public Set<String> getRedactSectionSet() {
        return redactSectionSet;
    }

    public void setRedactSectionSet(Set<String> redactSectionSet) {
        this.redactSectionSet = redactSectionSet;
    }

    public Set<String> getRedactCategorySet() {
        return redactCategorySet;
    }

    public void setRedactCategorySet(Set<String> redactCategorySet) {
        this.redactCategorySet = redactCategorySet;
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

    public static RedactionHandlerResult empty() {
        return new RedactionHandlerResult();
    }
}
