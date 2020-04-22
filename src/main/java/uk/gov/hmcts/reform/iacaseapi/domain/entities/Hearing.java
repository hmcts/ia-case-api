package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import lombok.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HoursAndMinutes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Hearing {

    // attending
    private String attendingJudge;
    private String attendingAppellant;
    private String attendingAppellantsLegalRepresentative;
    private String attendingHomeOfficeLegalRepresentative;
    private HoursAndMinutes actualCaseHearingLength;

    // hearing details
    private String ariaListingReference;
    private HearingCentre listCaseHearingCentre;
    private String listCaseHearingLength;
    private String listCaseHearingDate;

    // recordings
    private List<IdValue<HearingRecordingDocument>> recordingDocuments;

    // other
    private HearingType hearingType;
    private String dateAdded;
}