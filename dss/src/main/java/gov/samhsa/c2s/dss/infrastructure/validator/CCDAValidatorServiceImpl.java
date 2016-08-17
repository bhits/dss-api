package gov.samhsa.c2s.dss.infrastructure.validator;

import gov.samhsa.c2s.dss.infrastructure.dto.ValidationRequestDto;
import gov.samhsa.c2s.dss.infrastructure.dto.ValidationResponseDto;
import org.springframework.util.Assert;
import org.springframework.web.client.RestOperations;

import java.nio.charset.Charset;

public class CCDAValidatorServiceImpl implements CCDAValidatorService {
    private final String endpoint;
    private final RestOperations restTemplate;

    public CCDAValidatorServiceImpl(String endpoint, RestOperations restTemplate) {
        Assert.hasText(endpoint, "endpoint for C-CDA validator must have text");
        Assert.notNull(restTemplate, "restTemplate cannot be null");
        this.endpoint = endpoint;
        this.restTemplate = restTemplate;
    }

    @Override
    public ValidationResponseDto validate(ValidationRequestDto request) {
        return restTemplate.postForObject(endpoint, request, ValidationResponseDto.class);
    }

    @Override
    public ValidationResponseDto validate(String document, Charset charset) {
        ValidationRequestDto request = new ValidationRequestDto();
        request.setDocument(document.getBytes(charset));
        request.setDocumentEncoding(charset.displayName());
        return validate(request);
    }
}
