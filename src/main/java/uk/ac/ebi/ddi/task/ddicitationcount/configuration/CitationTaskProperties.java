package uk.ac.ebi.ddi.task.ddicitationcount.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("citationcount")
public class CitationTaskProperties {

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    private String configType;
}
