package gov.samhsa.c2s.dss.infrastructure.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Data
public class ValidationRequestDto {

    public static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.toString();

    @NotNull
    private byte[] document;
    private String documentEncoding = DEFAULT_ENCODING;
}
