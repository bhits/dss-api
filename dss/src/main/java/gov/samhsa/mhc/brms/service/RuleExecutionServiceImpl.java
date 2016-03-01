/*******************************************************************************
 * Open Behavioral Health Information Technology Architecture (OBHITA.org)
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the <organization> nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.samhsa.mhc.brms.service;

import gov.samhsa.mhc.brms.domain.ClinicalFact;
import gov.samhsa.mhc.brms.domain.FactModel;
import gov.samhsa.mhc.brms.domain.RuleExecutionContainer;
import gov.samhsa.mhc.brms.service.dto.AssertAndExecuteClinicalFactsResponse;
import gov.samhsa.mhc.brms.service.guvnor.GuvnorService;
import gov.samhsa.mhc.common.log.Logger;
import gov.samhsa.mhc.common.log.LoggerFactory;
import gov.samhsa.mhc.common.marshaller.SimpleMarshaller;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.*;
import org.drools.definition.rule.Rule;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.DefaultAgendaEventListener;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.util.HashSet;
import java.util.Set;

/**
 * The Class RuleExecutionServiceImpl.
 */
@Service
public class RuleExecutionServiceImpl implements RuleExecutionService {

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory
            .getLogger(this.getClass());

    /**
     * The guvnor service.
     */
    @Autowired
    private GuvnorService guvnorService;

    /**
     * The marshaller.
     */
    @Autowired
    private SimpleMarshaller marshaller;

    public RuleExecutionServiceImpl() {
    }

    public RuleExecutionServiceImpl(GuvnorService guvnorService,
                                    SimpleMarshaller marshaller) {
        super();
        this.guvnorService = guvnorService;
        this.marshaller = marshaller;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gov.samhsa.mhc.brms.RuleExecutionService#assertAndExecuteClinicalFacts
     * (gov.samhsa.mhc.brms.domain.FactModel)
     */
    @Override
    public AssertAndExecuteClinicalFactsResponse assertAndExecuteClinicalFacts(
            FactModel factModel) {
        RuleExecutionContainer executionResponseContainer = null;
        final AssertAndExecuteClinicalFactsResponse assertAndExecuteResponse = new AssertAndExecuteClinicalFactsResponse();
        String executionResponseContainerXMLString = null;
        final Set<String> firedRuleNames = new HashSet<String>();

        final StatefulKnowledgeSession session = createStatefulKnowledgeSession();
        try {
            session.insert(factModel.getXacmlResult());
            for (final ClinicalFact clinicalFact : factModel
                    .getClinicalFactList()) {
                session.insert(clinicalFact);
            }

            session.addEventListener(new DefaultAgendaEventListener() {
                @Override
                public void afterActivationFired(AfterActivationFiredEvent event) {
                    super.afterActivationFired(event);
                    final Rule rule = event.getActivation().getRule();
                    firedRuleNames.add(rule.getName());
                }
            });

            session.fireAllRules();

            logger.debug(() -> "Fired rules: " + firedRuleNames.toString());

            executionResponseContainer = (RuleExecutionContainer) session
                    .getGlobal("ruleExecutionContainer");

            // Marshal rule execution response
            executionResponseContainerXMLString = marshaller
                    .marshal(executionResponseContainer);
            if (firedRuleNames.size() > 0) {
                assertAndExecuteResponse.setRulesFired(firedRuleNames
                        .toString());
            }
        } catch (final Throwable e) {
            logger.error(e.getMessage(), e);
        } finally {
            firedRuleNames.clear();
            if (session != null) {
                session.dispose();
            }
        }
        assertAndExecuteResponse
                .setRuleExecutionResponseContainer(executionResponseContainerXMLString);
        return assertAndExecuteResponse;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gov.samhsa.mhc.brms.RuleExecutionService#assertAndExecuteClinicalFacts
     * (java.lang.String)
     */
    @Override
    public AssertAndExecuteClinicalFactsResponse assertAndExecuteClinicalFacts(
            String factModelXmlString) {
        FactModel factModel = null;
        try {
            factModel = marshaller.unmarshalFromXml(FactModel.class,
                    factModelXmlString);
        } catch (final JAXBException e) {
            logger.error(e.getMessage(), e);
        }
        return assertAndExecuteClinicalFacts(factModel);
    }

    /**
     * Creates the stateful knowledge session.
     *
     * @return the stateful knowledge session
     */
    StatefulKnowledgeSession createStatefulKnowledgeSession() {
        StatefulKnowledgeSession session = null;
        try {
            final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
                    .newKnowledgeBuilder();

            final String casRules = guvnorService
                    .getVersionedRulesFromPackage();

            kbuilder.add(
                    ResourceFactory.newByteArrayResource(casRules.getBytes()),
                    ResourceType.DRL);

            final KnowledgeBuilderErrors errors = kbuilder.getErrors();
            if (errors.size() > 0) {
                for (final KnowledgeBuilderError error : errors) {
                    logger.error(error.toString());
                }
            }

            final KnowledgeBase knowledgeBase = KnowledgeBaseFactory
                    .newKnowledgeBase();
            knowledgeBase.addKnowledgePackages(kbuilder.getKnowledgePackages());

            session = knowledgeBase.newStatefulKnowledgeSession();
            session.setGlobal("ruleExecutionContainer",
                    new RuleExecutionContainer());
        } catch (final Exception e) {
            logger.error(e.toString(), e);
        }
        return session;
    }
}
