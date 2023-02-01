package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
@JsonIgnoreProperties(value = "dateTimeCreated", ignoreUnknown = true)
public class CaseFlagValue {
    private String name;
    private String status;
    private String flagCode;
    private YesOrNo hearingRelevant;

}
