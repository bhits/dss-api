package gov.samhsa.mhc.dss.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "customSectionList", namespace = "urn:hl7-org:v3")
public class CustomSectionList {

    @XmlElement(name = "customSection", namespace = "urn:hl7-org:v3")
    private List<CustomSection> customSectionList;
}
