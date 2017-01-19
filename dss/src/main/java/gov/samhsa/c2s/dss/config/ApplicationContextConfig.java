package gov.samhsa.c2s.dss.config;

import ch.qos.logback.audit.AuditException;
import gov.samhsa.c2s.common.audit.AuditClient;
import gov.samhsa.c2s.common.audit.AuditClientImpl;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationContextConfig {

    public static final String CCDA_R1_VALIDATOR_SERVICE = "ccdaR1ValidatorService";
    public static final String CCDA_R2_VALIDATOR_SERVICE = "ccdaR2ValidatorService";

    @Bean
    @ConditionalOnBean(AuditClientProperties.class)
    public AuditClient auditClient(AuditClientProperties auditClientProperties) throws AuditException {
        return new AuditClientImpl("DSSAuditClient", auditClientProperties.getHost(), auditClientProperties.getPort());
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
    public CCDAValidatorService ccdaValidatorServiceR1(@Autowired DssProperties dssProperties) {
        return new CCDAValidatorServiceImpl(dssProperties.getValidator().getCCda().getR1(), restTemplate());
    }

    @Bean
    @Qualifier(CCDA_R2_VALIDATOR_SERVICE)
    public CCDAValidatorService ccdaValidatorServiceR2(@Autowired DssProperties dssProperties) {
        return new CCDAValidatorServiceImpl(dssProperties.getValidator().getCCda().getR2(), restTemplate());
    }

    @Bean
    public RestOperations restTemplate() {
        return new RestTemplate();
    }
}