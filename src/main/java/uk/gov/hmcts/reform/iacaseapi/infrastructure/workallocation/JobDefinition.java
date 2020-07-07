package uk.gov.hmcts.reform.iacaseapi.infrastructure.workallocation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobDefinition {
    private String activityId;

    private JobDefinition() {
    }

    public String getActivityId() {
        return activityId;
    }
}
