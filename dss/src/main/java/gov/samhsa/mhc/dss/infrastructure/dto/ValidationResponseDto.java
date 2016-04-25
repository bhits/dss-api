/**
 * Created by Jiahao.Li on 4/13/2016.
 */
package gov.samhsa.mhc.dss.infrastructure.dto;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ValidationResponseDto {
    private Map<String, AtomicInteger> validationSummary;
    private List<DocumentValidationResult> validationDetails;

    public Map<String, AtomicInteger> getValidationSummary() {
        return validationSummary;
    }

    public void setValidationSummary(Map<String, AtomicInteger> validationSummary) {
        this.validationSummary = validationSummary;
    }

    public List<DocumentValidationResult> getValidationDetails() {
        return validationDetails;
    }

    public void setValidationDetails(List<DocumentValidationResult> validationDetails) {
        this.validationDetails = validationDetails;
    }
}
