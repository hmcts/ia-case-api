package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterpreterDetails {

    private String interpreterId;
    private String interpreterBookingRef;
    private String interpreterGivenNames;
    private String interpreterFamilyName;
    private String interpreterPhoneNumber;
    private String interpreterEmail;
    private String interpreterNote;

    public String buildInterpreterFullName() {
        String givenNames = getInterpreterGivenNames() == null ? " " : getInterpreterGivenNames();
        String familyName = getInterpreterFamilyName() == null ? " " : getInterpreterFamilyName();

        return !(givenNames.isBlank() || familyName.isBlank()) ? givenNames + " " + familyName : givenNames;
    }

}
