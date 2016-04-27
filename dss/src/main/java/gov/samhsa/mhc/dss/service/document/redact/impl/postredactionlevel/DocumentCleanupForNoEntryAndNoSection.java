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
package gov.samhsa.mhc.dss.service.document.redact.impl.postredactionlevel;

import gov.samhsa.mhc.brms.domain.FactModel;
import gov.samhsa.mhc.brms.domain.RuleExecutionContainer;
import gov.samhsa.mhc.brms.domain.XacmlResult;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessor;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessorException;
import gov.samhsa.mhc.dss.service.document.dto.RedactionHandlerResult;
import gov.samhsa.mhc.dss.service.document.redact.RedactionHandlerException;
import gov.samhsa.mhc.dss.service.document.redact.base.AbstractPostRedactionLevelRedactionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * The Class DocumentCleanupForNoEntryAndNoSection.
 */
@Service
@ConfigurationProperties(prefix = "mhc.dss.redact")
public class DocumentCleanupForNoEntryAndNoSection extends
        AbstractPostRedactionLevelRedactionHandler {

    /**
     * The Constant URN_HL7_V3.
     */
    public static final String URN_HL7_V3 = "urn:hl7-org:v3";
    /**
     * The Constant TAG_COMPONENT.
     */
    public static final String TAG_COMPONENT = "component";
    /**
     * The Constant TAG_SECTION.
     */
    public static final String TAG_SECTION = "section";
    /**
     * The Constant XPATH_NO_COMPONENT_IN_STRUCTURED_BODY.
     */
    public static final String XPATH_NO_COMPONENT_IN_STRUCTURED_BODY = "/hl7:ClinicalDocument/hl7:component/hl7:structuredBody[not(hl7:component)]";
    /**
     * The Constant XPATH_SECTION_COMPONENT_WITH_NO_ENTRY.
     */
    public static final String XPATH_SECTION_COMPONENT_WITH_NO_ENTRY = "/hl7:ClinicalDocument/hl7:component/hl7:structuredBody/hl7:component[hl7:section[not(hl7:entry)]]";

    private List<String> sectionWhiteList = new ArrayList<>();

    /**
     * Instantiates a new document cleanup for no section.
     *
     * @param documentAccessor the document accessor
     */
    @Autowired
    public DocumentCleanupForNoEntryAndNoSection(
            DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    @Override
    public void execute(Document xmlDocument, XacmlResult xacmlResult,
                        FactModel factModel, Document factModelDocument,
                        RuleExecutionContainer ruleExecutionContainer,
                        RedactionHandlerResult preRedactionResults) {
        try {
            // Clean up section components with no entries
            cleanUpSectionComponentsWithNoEntries(xmlDocument);

            // Add empty component/section under structuredBody if none exists
            // (required to pass validation)
            addEmptySectionComponentIfNoneExists(xmlDocument);
        } catch (DocumentAccessorException e) {
            throw new RedactionHandlerException(e);
        }
    }

    public List<String> getSectionWhiteList() {
        return sectionWhiteList;
    }

    /**
     * Adds the empty section component if none exists.
     *
     * @param xmlDocument the xml document
     * @throws DocumentAccessorException the document accessor exception
     */
    private void addEmptySectionComponentIfNoneExists(Document xmlDocument)
            throws DocumentAccessorException {
        final Optional<Node> structuredBody = documentAccessor.getNode(
                xmlDocument, XPATH_NO_COMPONENT_IN_STRUCTURED_BODY);
        structuredBody.ifPresent(sb -> {
            final Element emptyComponent = xmlDocument.createElementNS(
                    URN_HL7_V3, TAG_COMPONENT);
            final Element emptySection = xmlDocument.createElementNS(
                    URN_HL7_V3, TAG_SECTION);
            emptyComponent.appendChild(emptySection);
            sb.appendChild(emptyComponent);
        });
    }

    /**
     * Clean up section components with no entries.
     *
     * @param xmlDocument the xml document
     * @throws DocumentAccessorException the document accessor exception
     */
    private void cleanUpSectionComponentsWithNoEntries(Document xmlDocument)
            throws DocumentAccessorException {
        // Find all empty sections
        final List<Node> emptySectionComponents = documentAccessor.getNodeListAsStream(
                xmlDocument, XPATH_SECTION_COMPONENT_WITH_NO_ENTRY).collect(toList());
        // Find the required sections out of the empty sections
        final List<Node> requiredButEmptySectionComponents = emptySectionComponents.stream()
                .filter(this::isRequired)
                .collect(toList());
        // Remove the empty sections that are not required
        emptySectionComponents.stream()
                .filter(section -> !requiredButEmptySectionComponents.contains(section))
                .forEach(this::nullSafeRemove);
        // Set @nullFlavor=NI and add "No information" text on required but empty sections
        requiredButEmptySectionComponents.stream()
                .flatMap(this::componentToSection)
                .peek(section -> section.getAttributes().setNamedItem(createNullFlavorNIAttribute(xmlDocument)))
                .flatMap(this::sectionToText)
                .forEach(text -> {
                    nullSafeRemoveChildNodes(text);
                    Node textNode = createTextNode(xmlDocument, "No information");
                    text.appendChild(textNode);
                });
    }

    private Node createNullFlavorNIAttribute(Document xmlDocument) {
        final Attr nullFlavor = xmlDocument.createAttributeNS(URN_HL7_V3, "nullFlavor");
        nullFlavor.setValue("NI");
        return nullFlavor;
    }

    private Node createTextNode(Document xmlDocument, String data) {
        return xmlDocument.createTextNode(data);
    }

    private boolean isRequired(Node componentNode) {
        return componentToSection(componentNode)
                .flatMap(this::sectionToCode)
                .map(code -> code.getAttributes().getNamedItem("code"))
                .filter(Objects::nonNull)
                .map(Node::getNodeValue)
                .filter(Objects::nonNull)
                .filter(sectionWhiteList::contains)
                .findAny().isPresent();
    }

    private Stream<Node> componentToSection(Node componentNode) {
        return getChildElementNodeWithName(componentNode, "section");
    }

    private Stream<Node> sectionToCode(Node sectionNode) {
        return getChildElementNodeWithName(sectionNode, "code");
    }

    private Stream<Node> sectionToText(Node sectionNode) {
        return getChildElementNodeWithName(sectionNode, "text");
    }

    private Stream<Node> getChildElementNodeWithName(Node node, String elementName) {
        return Optional.ofNullable(node)
                .map(Node::getChildNodes)
                .map(DocumentAccessor::toNodeStream)
                .orElseGet(Stream::empty)
                .filter(child -> Node.ELEMENT_NODE == child.getNodeType())
                .filter(child -> elementName.equals(child.getNodeName()));
    }
}
