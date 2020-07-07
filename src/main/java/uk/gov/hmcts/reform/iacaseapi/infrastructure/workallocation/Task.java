package uk.gov.hmcts.reform.iacaseapi.infrastructure.workallocation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class Task {
    private String id;
    private String executionId;

    public String getId() {
        return id;
    }

    public String getExecutionId() {
        return executionId;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", executionId='" + executionId + '\'' +
                '}';
    }
}
