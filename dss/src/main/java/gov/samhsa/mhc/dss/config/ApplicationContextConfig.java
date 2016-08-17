package gov.samhsa.mhc.dss.config;

import ch.qos.logback.audit.AuditException;
import gov.samhsa.mhc.common.audit.AuditService;
import gov.samhsa.mhc.common.audit.AuditServiceImpl;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessor;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessorImpl;
import gov.samhsa.mhc.common.document.converter.DocumentXmlConverter;
import gov.samhsa.mhc.common.document.converter.DocumentXmlConverterImpl;
import gov.samhsa.mhc.common.document.transformer.XmlTransformer;
import gov.samhsa.mhc.common.document.transformer.XmlTransformerImpl;
import gov.samhsa.mhc.common.filereader.FileReader;
import gov.samhsa.mhc.common.filereader.FileReaderImpl;
import gov.samhsa.mhc.common.marshaller.SimpleMarshaller;
import gov.samhsa.mhc.common.marshaller.SimpleMarshallerImpl;
import gov.samhsa.mhc.dss.infrastructure.validator.CCDAValidatorService;
import gov.samhsa.mhc.dss.infrastructure.validator.CCDAValidatorServiceImpl;
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
            @Value("${mhc.dss.audit-service.host}") String host,
            @Value("${mhc.dss.audit-service.port}") int port) throws AuditException {
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
    public CCDAValidatorService ccdaValidatorServiceR1(@Value("${mhc.dss.validator.c-cda.r1}") String endpoint) {
        return new CCDAValidatorServiceImpl(endpoint, restTemplate());
    }

    @Bean
    @Qualifier(CCDA_R2_VALIDATOR_SERVICE)
    public CCDAValidatorService ccdaValidatorServiceR2(@Value("${mhc.dss.validator.c-cda.r2}") String endpoint) {
        return new CCDAValidatorServiceImpl(endpoint, restTemplate());
    }

    @Bean
    public RestOperations restTemplate() {
        return new RestTemplate();
    }
}
