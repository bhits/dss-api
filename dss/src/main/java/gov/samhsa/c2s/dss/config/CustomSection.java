package gov.samhsa.c2s.dss.config;


import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.UUID;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomSection {

    @XmlElement(namespace = "urn:hl7-org:v3")
    private String id = UUID.randomUUID().toString();

    @XmlElement(namespace = "urn:hl7-org:v3")
    private String code;

    @XmlElement(namespace = "urn:hl7-org:v3")
    private String codeSystem;

    @XmlElement(namespace = "urn:hl7-org:v3")
    private String codeSystemName;

    @XmlElement(namespace = "urn:hl7-org:v3")
    private String displayName;

    @XmlElement(namespace = "urn:hl7-org:v3")
    private String title;

    @XmlElement(namespace = "urn:hl7-org:v3")
    private String text;
}
