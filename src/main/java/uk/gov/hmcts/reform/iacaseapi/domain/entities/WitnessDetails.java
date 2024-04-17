package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WitnessDetails {

    private String witnessPartyId;
    private String witnessName;
    private String witnessFamilyName;
    private YesOrNo isWitnessDeleted;

    public WitnessDetails() {
    }

    public WitnessDetails(String witnessName) {
        this.witnessName = witnessName;
    }

    public WitnessDetails(String witnessName, String witnessFamilyName) {
        this.witnessName = witnessName;
        this.witnessFamilyName = witnessFamilyName;
    }

    public String buildWitnessFullName() {
        String givenNames = getWitnessName() == null ? " " : getWitnessName();
        String familyName = getWitnessFamilyName() == null ? " " : getWitnessFamilyName();

        return !(givenNames.isBlank() || familyName.isBlank()) ? givenNames + " " + familyName : givenNames;
    }
}
