package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

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
public class CaseDataContent {

    @JsonProperty("case_reference")
    private String caseReference;
    private Map<String, Object> data;
    private Map<String, Object> event;
    @JsonProperty("event_token")
    private String eventToken;
    @JsonProperty("ignore_warning")
    private boolean ignoreWarning;
}
