package gov.samhsa.mhc.dss.config;

import gov.samhsa.mhc.brms.domain.ClinicalFact;
import gov.samhsa.mhc.brms.domain.FactModel;
import gov.samhsa.mhc.brms.domain.RuleExecutionContainer;
import gov.samhsa.mhc.brms.domain.XacmlResult;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessor;
import gov.samhsa.mhc.dss.service.document.DocumentRedactor;
import gov.samhsa.mhc.dss.service.document.dto.RedactionHandlerResult;
import gov.samhsa.mhc.dss.service.document.redact.base.AbstractClinicalFactLevelRedactionHandler;
import gov.samhsa.mhc.dss.service.document.redact.base.AbstractDocumentLevelRedactionHandler;
import gov.samhsa.mhc.dss.service.document.redact.base.AbstractObligationLevelRedactionHandler;
import gov.samhsa.mhc.dss.service.document.redact.base.AbstractPostRedactionLevelRedactionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.w3c.dom.Document;

@Configuration
@ConditionalOnBean(DocumentRedactor.class)
public class RedactionHandlerIdentityConfig {

    public static final String IDENTITY = "IDENTITY";

    @Bean
    @ConditionalOnMissingBean(AbstractDocumentLevelRedactionHandler.class)
    public AbstractDocumentLevelRedactionHandler identityDocumentLevelRedactionHandler() {
        return new AbstractDocumentLevelRedactionHandler() {
            @Override
            public RedactionHandlerResult execute(Document xmlDocument) {
                return RedactionHandlerResult.empty();
            }

            @Override
            public String toString() {
                return IDENTITY;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(AbstractObligationLevelRedactionHandler.class)
    public AbstractObligationLevelRedactionHandler identityObligationLevelRedactionHandler(DocumentAccessor documentAccessor) {
        return new AbstractObligationLevelRedactionHandler(documentAccessor) {
            @Override
            public RedactionHandlerResult execute(Document xmlDocument, XacmlResult xacmlResult, FactModel factModel, Document factModelDocument, RuleExecutionContainer ruleExecutionContainer, String obligationValue) {
                return RedactionHandlerResult.empty();
            }

            @Override
            public String toString() {
                return IDENTITY;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(AbstractClinicalFactLevelRedactionHandler.class)
    public AbstractClinicalFactLevelRedactionHandler identityClinicalFactLevelRedactionHandler(DocumentAccessor documentAccessor) {
        return new AbstractClinicalFactLevelRedactionHandler(documentAccessor) {
            @Override
            public RedactionHandlerResult execute(Document xmlDocument, XacmlResult xacmlResult, FactModel factModel, Document factModelDocument, ClinicalFact fact, RuleExecutionContainer ruleExecutionContainer) {
                return RedactionHandlerResult.empty();
            }

            @Override
            public String toString() {
                return IDENTITY;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(AbstractPostRedactionLevelRedactionHandler.class)
    public AbstractPostRedactionLevelRedactionHandler identityPostRedactionLevelRedactionHandler(DocumentAccessor documentAccessor) {
        return new AbstractPostRedactionLevelRedactionHandler(documentAccessor) {
            @Override
            public void execute(Document xmlDocument, XacmlResult xacmlResult, FactModel factModel, Document factModelDocument, RuleExecutionContainer ruleExecutionContainer, RedactionHandlerResult preRedactionResults) {
                // do nothing
            }

            @Override
            public String toString() {
                return IDENTITY;
            }
        };
    }
}
