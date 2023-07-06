package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WitnessDetails {

    private String witnessName;
    private String witnessFamilyName;

    public WitnessDetails(String witnessName) {
        this.witnessName = witnessName;
    }
}
