package gov.samhsa.mhc.dss.infrastructure.validator;

import gov.samhsa.mhc.dss.infrastructure.dto.ValidationRequestDto;
import gov.samhsa.mhc.dss.infrastructure.dto.ValidationResponseDto;

import java.nio.charset.Charset;

public interface CCDAValidatorService {
    ValidationResponseDto validate(ValidationRequestDto request);
    ValidationResponseDto validate(String document, Charset charset);
}
