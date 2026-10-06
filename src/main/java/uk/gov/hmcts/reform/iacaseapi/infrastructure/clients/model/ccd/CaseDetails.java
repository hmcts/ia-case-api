package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseDetails {

    private Long id;
    private String state;
    @JsonProperty("case_data")
    private Map<String, Object> caseData;
}
