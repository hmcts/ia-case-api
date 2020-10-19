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

    private Optional<String> attendingJudge;
    private Optional<String> attendingAppellant;
    private Optional<String> attendingHomeOfficeLegalRepresentative;
    private Optional<HoursAndMinutes> actualCaseHearingLength;
    private String ariaListingReference;
    private HearingCentre listCaseHearingCentre;
    private String listCaseHearingDate;
    private String listCaseHearingLength;
    private Optional<List<IdValue<HearingRecordingDocument>>> hearingRecordingDocuments;
    private String appealDecision;
    private List<IdValue<DocumentWithMetadata>> finalDecisionAndReasonsDocuments;
    private List<IdValue<DocumentWithMetadata>> hearingRequirements;

    private PreviousHearing() {
        // noop -- for deserializer
    }

    public PreviousHearing(
        Optional<String> attendingJudge,
        Optional<String> attendingAppellant,
        Optional<String> attendingHomeOfficeLegalRepresentative,
        Optional<HoursAndMinutes> actualCaseHearingLength,
        String ariaListingReference,
        HearingCentre listCaseHearingCentre,
        String listCaseHearingDate,
        String listCaseHearingLength,
        Optional<List<IdValue<HearingRecordingDocument>>> hearingRecordingDocuments,
        String appealDecision,
        List<IdValue<DocumentWithMetadata>> finalDecisionAndReasonsDocuments,
        List<IdValue<DocumentWithMetadata>> hearingRequirements

    ) {
        requireNonNull(ariaListingReference);
        requireNonNull(listCaseHearingCentre);
        requireNonNull(listCaseHearingDate);
        requireNonNull(listCaseHearingLength);
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

    public Optional<String> getAttendingJudge() {
        return attendingJudge;
    }

    public Optional<String> getAttendingAppellant() {
        return attendingAppellant;
    }

    public Optional<String> getAttendingHomeOfficeLegalRepresentative() {
        return attendingHomeOfficeLegalRepresentative;
    }

    public Optional<HoursAndMinutes> getActualCaseHearingLength() {
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

    public Optional<List<IdValue<HearingRecordingDocument>>> getHearingRecordingDocuments() {
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
