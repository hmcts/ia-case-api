package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MissingSupplementaryInfo {

    @JsonProperty("ccd_case_numbers")
    private List<String> ccdCaseNumbers;

}
