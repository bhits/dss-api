package gov.samhsa.c2s.dss.config;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "c2s.dss")
@Data
public class DssProperties {

    @NotNull
    @Valid
    private Validator validator;

    @NotNull
    @Valid
    private DocumentSegmentationImpl documentSegmentationImpl;

    @Data
    public static class Validator {
        @Valid
        private CCda cCda;

        @Data
        public static class CCda {
            @NotEmpty
            private String r1;

            @NotEmpty
            private String r2;
        }
    }

    @Data
    public static class DocumentSegmentationImpl {
        @NotNull
        private boolean defaultIsAudited;

        @NotNull
        private boolean defaultIsAuditFailureByPass;
    }
}