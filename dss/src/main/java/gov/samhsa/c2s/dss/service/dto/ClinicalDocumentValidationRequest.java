package gov.samhsa.c2s.dss.service.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Data
public class ClinicalDocumentValidationRequest {
    @NotNull
    private byte[] document;
    private Optional<String> documentEncoding = Optional.empty();
}
