package gov.samhsa.c2s.brms.service.dto;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for anonymous complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ruleExecutionResponseContainer" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="rulesFired" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "ruleExecutionResponseContainer",
        "rulesFired"
})
@XmlRootElement(name = "assertAndExecuteClinicalFactsResponse")
public class AssertAndExecuteClinicalFactsResponse {

    @XmlElement(required = true)
    protected String ruleExecutionResponseContainer;
    @XmlElement(required = true)
    protected String rulesFired;

    /**
     * Gets the value of the ruleExecutionResponseContainer property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRuleExecutionResponseContainer() {
        return ruleExecutionResponseContainer;
    }

    /**
     * Sets the value of the ruleExecutionResponseContainer property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRuleExecutionResponseContainer(String value) {
        this.ruleExecutionResponseContainer = value;
    }

    /**
     * Gets the value of the rulesFired property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRulesFired() {
        return rulesFired;
    }

    /**
     * Sets the value of the rulesFired property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRulesFired(String value) {
        this.rulesFired = value;
    }

}
