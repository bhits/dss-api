
package gov.samhsa.mhc.brms.service.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="clinicalFactXmlString" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "clinicalFactXmlString"
})
@XmlRootElement(name = "assertAndExecuteClinicalFactsRequest")
public class AssertAndExecuteClinicalFactsRequest {

    @XmlElement(required = true)
    protected String clinicalFactXmlString;

    /**
     * Gets the value of the clinicalFactXmlString property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClinicalFactXmlString() {
        return clinicalFactXmlString;
    }

    /**
     * Sets the value of the clinicalFactXmlString property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClinicalFactXmlString(String value) {
        this.clinicalFactXmlString = value;
    }

}
