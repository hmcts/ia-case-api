package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;

@Getter
@Setter
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseFlagValue {

    private CaseFlagValue() {
        // noop -- for deserializer
    }

    String name;
    String status;
    String flagCode;
    YesOrNo hearingRelevant;
    String dateTimeCreated;
    String dateTimeModified;

}
