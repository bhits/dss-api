package gov.samhsa.c2s.dss.config;

import ch.qos.logback.audit.AuditException;
import gov.samhsa.c2s.common.audit.AuditService;
import gov.samhsa.c2s.common.audit.AuditServiceImpl;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessorImpl;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverter;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverterImpl;
import gov.samhsa.c2s.common.document.transformer.XmlTransformer;
import gov.samhsa.c2s.common.document.transformer.XmlTransformerImpl;
import gov.samhsa.c2s.common.filereader.FileReader;
import gov.samhsa.c2s.common.filereader.FileReaderImpl;
import gov.samhsa.c2s.common.marshaller.SimpleMarshaller;
import gov.samhsa.c2s.common.marshaller.SimpleMarshallerImpl;
import gov.samhsa.c2s.dss.infrastructure.validator.CCDAValidatorService;
import gov.samhsa.c2s.dss.infrastructure.validator.CCDAValidatorServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationContextConfig {

    public static final String CCDA_R1_VALIDATOR_SERVICE = "ccdaR1ValidatorService";
    public static final String CCDA_R2_VALIDATOR_SERVICE = "ccdaR2ValidatorService";

    @Bean
    public AuditService auditService(
            @Value("${c2s.dss.audit-service.host}") String host,
            @Value("${c2s.dss.audit-service.port}") int port) throws AuditException {
        return new AuditServiceImpl("DSSAuditService", host, port);
    }

    @Bean
    public SimpleMarshaller simpleMarshaller() {
        return new SimpleMarshallerImpl();
    }

    @Bean
    public FileReader fileReader() {
        return new FileReaderImpl();
    }

    @Bean
    public DocumentXmlConverter documentXmlConverter() {
        return new DocumentXmlConverterImpl();
    }

    @Bean
    public DocumentAccessor documentAccessor() {
        return new DocumentAccessorImpl();
    }

    @Bean
    public XmlTransformer xmlTransformer() {
        return new XmlTransformerImpl(simpleMarshaller());
    }

    @Bean
    @Qualifier(CCDA_R1_VALIDATOR_SERVICE)
    public CCDAValidatorService ccdaValidatorServiceR1(@Value("${c2s.dss.validator.c-cda.r1}") String endpoint) {
        return new CCDAValidatorServiceImpl(endpoint, restTemplate());
    }

    @Bean
    @Qualifier(CCDA_R2_VALIDATOR_SERVICE)
    public CCDAValidatorService ccdaValidatorServiceR2(@Value("${c2s.dss.validator.c-cda.r2}") String endpoint) {
        return new CCDAValidatorServiceImpl(endpoint, restTemplate());
    }

    @Bean
    public RestOperations restTemplate() {
        return new RestTemplate();
    }
}
