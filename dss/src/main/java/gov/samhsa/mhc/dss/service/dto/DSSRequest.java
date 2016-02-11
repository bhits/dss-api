package gov.samhsa.mhc.dss.service.dto;

import gov.samhsa.mhc.brms.domain.XacmlResult;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public class DSSRequest {

    @NotNull
    protected XacmlResult xacmlResult;
    protected Optional<Boolean> audited;
    protected Optional<Boolean> auditFailureByPass;
    protected Optional<Boolean> enableTryPolicyResponse;
    @NotNull
    private byte[] document;
    private Optional<String> documentEncoding;

    public byte[] getDocument() {
        return document;
    }

    public void setDocument(byte[] document) {
        this.document = document;
    }

    public Optional<String> getDocumentEncoding() {
        return documentEncoding;
    }

    public void setDocumentEncoding(Optional<String> documentEncoding) {
        this.documentEncoding = documentEncoding;
    }

    public XacmlResult getXacmlResult() {
        return xacmlResult;
    }

    public void setXacmlResult(XacmlResult xacmlResult) {
        this.xacmlResult = xacmlResult;
    }

    public Optional<Boolean> getAudited() {
        return audited;
    }

    public void setAudited(Optional<Boolean> audited) {
        this.audited = audited;
    }

    public Optional<Boolean> getAuditFailureByPass() {
        return auditFailureByPass;
    }

    public void setAuditFailureByPass(Optional<Boolean> auditFailureByPass) {
        this.auditFailureByPass = auditFailureByPass;
    }

    public Optional<Boolean> getEnableTryPolicyResponse() {
        return enableTryPolicyResponse;
    }

    public void setEnableTryPolicyResponse(Optional<Boolean> enableTryPolicyResponse) {
        this.enableTryPolicyResponse = enableTryPolicyResponse;
    }
}
