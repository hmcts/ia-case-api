package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import lombok.Data;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@Data
public class HomeOfficeAppellant {

    private String pp;
    private String familyName;
    private String givenNames;
    private String dateOfBirth;
    private String nationality;
    private YesOrNo roa;
    private YesOrNo asylumSupport;
    private YesOrNo hoFeeWaiver;
    private String language;
    private YesOrNo interpreterNeeded;

}
