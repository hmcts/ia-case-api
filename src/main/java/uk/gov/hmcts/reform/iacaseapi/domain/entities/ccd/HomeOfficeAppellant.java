package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import java.time.LocalDate;

import lombok.Data;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;


@Data
public class HomeOfficeAppellant {

    private String familyName;
    private String givenNames;
    private LocalDate dateOfBirth;
    private String nationality;
    private YesOrNo roa;
    private YesOrNo asylumSupport;
    private YesOrNo hoFeeWaiver;
    private String language;
    private YesOrNo interpreterNeeded;

}
