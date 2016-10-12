package gov.samhsa.c2s.dss.infrastructure.dto;

import lombok.Data;

import java.util.List;

@Data
public class ValidationResponseDto {
    private DocumentValidationSummary validationSummary;
    private List<DocumentValidationResult> validationDetails;

}
