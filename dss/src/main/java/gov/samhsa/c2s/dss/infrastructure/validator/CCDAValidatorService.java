package gov.samhsa.c2s.dss.infrastructure.validator;

import gov.samhsa.c2s.dss.infrastructure.dto.ValidationRequestDto;
import gov.samhsa.c2s.dss.infrastructure.dto.ValidationResponseDto;

import java.nio.charset.Charset;

public interface CCDAValidatorService {
    ValidationResponseDto validate(ValidationRequestDto request);
    ValidationResponseDto validate(String document, Charset charset);
}
