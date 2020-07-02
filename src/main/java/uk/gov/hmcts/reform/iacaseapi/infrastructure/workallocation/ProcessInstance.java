package uk.gov.hmcts.reform.iacaseapi.infrastructure.workallocation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessInstance {
    private String id;
    private String processDefinitionKey;

    public String getId() {
        return id;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    @Override
    public String toString() {
        return "ProcessInstance{" +
                "id='" + id + '\'' +
                ", processDefinitionKey='" + processDefinitionKey + '\'' +
                '}';
    }
}
