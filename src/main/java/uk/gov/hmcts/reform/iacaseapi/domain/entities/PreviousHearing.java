package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HoursAndMinutes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@EqualsAndHashCode
@ToString
public class PreviousHearing {

    private String attendingJudge;
    private Optional<String> attendingAppellant;
    private Optional<String> attendingHomeOfficeLegalRepresentative;
    private HoursAndMinutes actualCaseHearingLength;
    private String ariaListingReference;
    private HearingCentre listCaseHearingCentre;
    private String listCaseHearingDate;
    private String listCaseHearingLength;
    private List<IdValue<HearingRecordingDocument>> hearingRecordingDocuments;
    private String appealDecision;
    private List<IdValue<DocumentWithMetadata>> finalDecisionAndReasonsDocuments;
    private List<IdValue<DocumentWithMetadata>> hearingRequirements;

    private PreviousHearing() {
        // noop -- for deserializer
    }

    public PreviousHearing(
        String attendingJudge,
        Optional<String> attendingAppellant,
        Optional<String> attendingHomeOfficeLegalRepresentative,
        HoursAndMinutes actualCaseHearingLength,
        String ariaListingReference,
        HearingCentre listCaseHearingCentre,
        String listCaseHearingDate,
        String listCaseHearingLength,
        List<IdValue<HearingRecordingDocument>> hearingRecordingDocuments,
        String appealDecision,
        List<IdValue<DocumentWithMetadata>> finalDecisionAndReasonsDocuments,
        List<IdValue<DocumentWithMetadata>> hearingRequirements

    ) {
        requireNonNull(attendingJudge);
        requireNonNull(actualCaseHearingLength);
        requireNonNull(ariaListingReference);
        requireNonNull(listCaseHearingCentre);
        requireNonNull(listCaseHearingDate);
        requireNonNull(listCaseHearingLength);
        requireNonNull(hearingRecordingDocuments);
        requireNonNull(appealDecision);
        requireNonNull(finalDecisionAndReasonsDocuments);
        requireNonNull(hearingRequirements);

        this.attendingJudge = attendingJudge;
        this.attendingAppellant = attendingAppellant;
        this.attendingHomeOfficeLegalRepresentative = attendingHomeOfficeLegalRepresentative;
        this.actualCaseHearingLength = actualCaseHearingLength;
        this.ariaListingReference = ariaListingReference;
        this.listCaseHearingCentre = listCaseHearingCentre;
        this.listCaseHearingDate = listCaseHearingDate;
        this.listCaseHearingLength = listCaseHearingLength;
        this.hearingRecordingDocuments = hearingRecordingDocuments;
        this.appealDecision = appealDecision;
        this.finalDecisionAndReasonsDocuments = finalDecisionAndReasonsDocuments;
        this.hearingRequirements = hearingRequirements;
    }

    public String getAttendingJudge() {
        requireNonNull(attendingAppellant);
        return attendingJudge;
    }

    public Optional<String> getAttendingAppellant() {
        return attendingAppellant;
    }

    public Optional<String> getAttendingHomeOfficeLegalRepresentative() {
        return attendingHomeOfficeLegalRepresentative;
    }

    public HoursAndMinutes getActualCaseHearingLength() {
        requireNonNull(actualCaseHearingLength);
        return actualCaseHearingLength;
    }

    public String getAriaListingReference() {
        requireNonNull(ariaListingReference);
        return ariaListingReference;
    }

    public HearingCentre getListCaseHearingCentre() {
        requireNonNull(listCaseHearingCentre);
        return listCaseHearingCentre;
    }

    public String getListCaseHearingDate() {
        requireNonNull(listCaseHearingDate);
        return listCaseHearingDate;
    }

    public String getListCaseHearingLength() {
        requireNonNull(listCaseHearingLength);
        return listCaseHearingLength;
    }

    public List<IdValue<HearingRecordingDocument>> getHearingRecordingDocuments() {
        requireNonNull(hearingRecordingDocuments);
        return hearingRecordingDocuments;
    }

    public String getAppealDecision() {
        requireNonNull(appealDecision);
        return appealDecision;
    }

    public List<IdValue<DocumentWithMetadata>> getFinalDecisionAndReasonsDocuments() {
        requireNonNull(finalDecisionAndReasonsDocuments);
        return finalDecisionAndReasonsDocuments;
    }

    public List<IdValue<DocumentWithMetadata>> getHearingRequirements() {
        requireNonNull(hearingRequirements);
        return hearingRequirements;
    }
}
