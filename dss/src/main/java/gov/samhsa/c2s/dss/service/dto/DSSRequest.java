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
}
