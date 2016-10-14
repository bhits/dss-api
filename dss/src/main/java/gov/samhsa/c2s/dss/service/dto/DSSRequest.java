package gov.samhsa.c2s.dss.service.dto;

import gov.samhsa.c2s.brms.domain.XacmlResult;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Data
public class DSSRequest {

    @NotNull
    protected XacmlResult xacmlResult;
    protected Optional<Boolean> audited = Optional.empty();
    protected Optional<Boolean> auditFailureByPass = Optional.empty();
    protected Optional<Boolean> enableTryPolicyResponse = Optional.empty();
    @NotNull
    private byte[] document;
    private Optional<String> documentEncoding = Optional.empty();

    public Optional<String> getDocumentEncoding() {
        return documentEncoding;
    }

    public void setDocumentEncoding(String documentEncoding) {
        this.documentEncoding = Optional.of(documentEncoding);
    }

    public Optional<Boolean> getAudited() {
        return audited;
    }

    public void setAudited(Boolean audited) {
        this.audited = Optional.of(audited);
    }

    public Optional<Boolean> getAuditFailureByPass() {
        return auditFailureByPass;
    }

    public void setAuditFailureByPass(Boolean auditFailureByPass) {
        this.auditFailureByPass = Optional.of(auditFailureByPass);
    }

    public Optional<Boolean> getEnableTryPolicyResponse() {
        return enableTryPolicyResponse;
    }

    public void setEnableTryPolicyResponse(Boolean enableTryPolicyResponse) {
        this.enableTryPolicyResponse = Optional.of(enableTryPolicyResponse);
    }
}