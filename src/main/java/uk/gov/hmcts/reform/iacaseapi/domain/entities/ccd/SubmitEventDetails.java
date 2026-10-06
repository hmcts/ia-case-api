package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmitEventDetails {

    private long id;
    private String jurisdiction;
    private State state;
    private Map<String, Object> data;
    @JsonProperty("callback_response_status_code")
    private int callbackResponseStatusCode;
    @JsonProperty("callback_response_status")
    private String callbackResponseStatus;
}
