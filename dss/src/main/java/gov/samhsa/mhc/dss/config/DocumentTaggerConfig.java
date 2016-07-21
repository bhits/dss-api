package gov.samhsa.mhc.dss.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "mhc.dss.DocumentTaggerImpl")
public class DocumentTaggerConfig {

    private List<CustomSection> additionalSections = new ArrayList<>();

    public List<CustomSection> getAdditionalSections() {
        return additionalSections;
    }

    public CustomSectionList getAdditionalSectionsAsCustomSectionList() {
        return new CustomSectionList(getAdditionalSections());
    }
}
