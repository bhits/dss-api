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
package gov.samhsa.c2s.dss.service.document;

import gov.samhsa.mhc.common.document.transformer.XmlTransformer;
import gov.samhsa.mhc.common.log.Logger;
import gov.samhsa.mhc.common.log.LoggerFactory;
import gov.samhsa.mhc.common.util.StringURIResolver;
import gov.samhsa.c2s.dss.service.exception.DocumentSegmentationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.transform.URIResolver;
import java.util.Optional;

/**
 * The Class DocumentFactModelExtractorImpl.
 */
@Service
public class DocumentFactModelExtractorImpl implements
        DocumentFactModelExtractor {

    /**
     * The Constant EXTRACT_CLINICAL_FACTS_XSL.
     */
    private static final String EXTRACT_CLINICAL_FACTS_XSL = "extractClinicalFacts.xsl";

    /**
     * The Constant PARAM_XACML_RESULT.
     */
    private static final String PARAM_XACML_RESULT = "xacmlResult";

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory
            .getLogger(this.getClass());

    /**
     * The xml transformer.
     */
    @Autowired
    private XmlTransformer xmlTransformer;

    public DocumentFactModelExtractorImpl() {
    }

    /**
     * Instantiates a new document fact model extractor impl.
     *
     * @param xmlTransformer
     *            the xml transformer
     */
    @Autowired
    public DocumentFactModelExtractorImpl(XmlTransformer xmlTransformer) {
        super();
        this.xmlTransformer = xmlTransformer;
    }

    /*
     * (non-Javadoc)
     *
     * @see gov.samhsa.c2s.dss.service.document
     * .DocumentFactModelExtractor#extractFactModel(java.lang.String,
     * java.lang.String)
     */
    @Override
    public String extractFactModel(String document, String enforcementPolicies) {

        try {
            final String xslUrl = Thread.currentThread().getContextClassLoader()
                    .getResource(EXTRACT_CLINICAL_FACTS_XSL).toString();
            final String xacmlResult = enforcementPolicies.replace("<xacmlReslt>",
                    "<xacmlReslt xmlns:\"urn:hl7-org:v3\">");
            final Optional<URIResolver> uriResolver = Optional
                    .of(new StringURIResolver()
                            .put(PARAM_XACML_RESULT, xacmlResult));
            String factModel = xmlTransformer.transform(document, xslUrl,
                    Optional.empty(), uriResolver);

            factModel = factModel
                    .replace(
                            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>",
                            "");
            logger.debug("FactModel:");
            logger.debug(factModel);
            return factModel;
        } catch (final Exception e) {
            throw new DocumentSegmentationException(e.getMessage(), e);
        }
    }
}
