package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

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
    String subTypeKey;
    String flagComment;
    String subTypeValue;
    YesOrNo hearingRelevant;
    String dateTimeCreated;
    String dateTimeModified;
    List<IdValue<String>> path;
    String otherDescription;

}
