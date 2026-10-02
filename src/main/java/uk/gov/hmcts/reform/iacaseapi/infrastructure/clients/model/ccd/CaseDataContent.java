package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CaseDataContent {

    private Event event;
    @JsonProperty("event_token")
    private String eventToken;
    @JsonProperty("ignore_warning")
    private boolean ignoreWarning;
    private Map<String, Object> data;
}
