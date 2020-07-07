package uk.gov.hmcts.reform.iacaseapi.infrastructure.workallocation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class Job {
    private String id;
    private String jobDefinitionId;

    private Job() {
    }

    public String getId() {
        return id;
    }

    public String getJobDefinitionId() {
        return jobDefinitionId;
    }

    @Override
    public String toString() {
        return "Job{" +
                "id='" + id + '\'' +
                ", jobDefinitionId='" + jobDefinitionId + '\'' +
                '}';
    }
}
