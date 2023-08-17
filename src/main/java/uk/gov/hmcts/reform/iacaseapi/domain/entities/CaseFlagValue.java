package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseFlagValue {

    String name;
    String status;
    String flagCode;
    YesOrNo hearingRelevant;
    String dateTimeCreated;
    String dateTimeModified;
    InterpreterLanguageRefData appelantSpokenLanguage;

}
