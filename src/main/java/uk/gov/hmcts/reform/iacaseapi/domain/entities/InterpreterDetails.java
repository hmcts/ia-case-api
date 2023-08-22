package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
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

}
