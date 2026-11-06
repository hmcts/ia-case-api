package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupplementaryInfo {

    @JsonProperty("ccd_case_number")
    private String ccdCaseNumber;
    @JsonProperty("supplementary_details")
    private SupplementaryDetails supplementaryDetails;

}
