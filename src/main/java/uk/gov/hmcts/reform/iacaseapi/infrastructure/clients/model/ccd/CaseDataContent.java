package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CaseDataContent {

    private Event event;
    private String eventToken;
    private boolean ignoreWarning;
    private Map<String, Object> data;
}
