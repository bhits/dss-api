/*******************************************************************************
 * Open Behavioral Health Information Technology Architecture (OBHITA.org)
 * <p/>
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
 * <p/>
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
package gov.samhsa.mhc.dss.service.document.redact.impl.clinicalfactlevel;

import gov.samhsa.mhc.brms.domain.ClinicalFact;
import gov.samhsa.mhc.brms.domain.FactModel;
import gov.samhsa.mhc.brms.domain.RuleExecutionContainer;
import gov.samhsa.mhc.brms.domain.XacmlResult;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessor;
import gov.samhsa.mhc.dss.service.document.redact.base.AbstractClinicalFactLevelRedactionHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.util.List;
import java.util.Set;

/**
 * The Class HumanReadableTextNodeByDisplayName.
 */
@Service
public class HumanReadableTextNodeByDisplayName extends
        AbstractClinicalFactLevelRedactionHandler {

    /**
     * The Constant XPATH_HUMAN_READABLE_TEXT_NODE.
     */
    public static final String XPATH_HUMAN_READABLE_TEXT_NODE = "//hl7:section[child::hl7:entry[child::hl7:generatedEntryId/text()='%1']]/hl7:text//*/text()[contains(lower-case(.), '%2')]";

    /**
     * Instantiates a new document node collector for human readable text node
     * by display name.
     *
     * @param documentAccessor the document accessor
     */
    @Autowired
    public HumanReadableTextNodeByDisplayName(DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    /*
     * (non-Javadoc)
     *
     * @see gov.samhsa.mhc.dss.service.document.redact.
     * AbstractClinicalFactLevelCallback#execute(org.w3c.dom.Document,
     * gov.samhsa.mhc.brms.domain.XacmlResult,
     * gov.samhsa.mhc.brms.domain.FactModel, org.w3c.dom.Document,
     * gov.samhsa.mhc.brms.domain.ClinicalFact,
     * gov.samhsa.mhc.brms.domain.RuleExecutionContainer, java.util.List,
     * java.util.Set, java.util.Set)
     */
    @Override
    public void execute(Document xmlDocument, XacmlResult xacmlResult,
                        FactModel factModel, Document factModelDocument, ClinicalFact fact,
                        RuleExecutionContainer ruleExecutionContainer,
                        List<Node> listOfNodes,
                        Set<String> redactSectionCodesAndGeneratedEntryIds,
                        Set<String> redactSensitiveCategoryCodes)
            throws XPathExpressionException {
        String displayName = fact.getDisplayName().toLowerCase();
        if (!StringUtils.isBlank(displayName)) {
            String foundCategory = findMatchingCategory(xacmlResult, fact);
            // If there is at least one value set category in obligations
            if (foundCategory != null) {
                addNodesToList(xmlDocument, listOfNodes,
                        redactSectionCodesAndGeneratedEntryIds,
                        XPATH_HUMAN_READABLE_TEXT_NODE, fact.getEntry(),
                        displayName);
                redactSensitiveCategoryCodes.add(foundCategory);
            }
        }
    }
}
