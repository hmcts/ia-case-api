package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HoursAndMinutes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@EqualsAndHashCode
@ToString
public class PreviousHearing {

    @Getter
    private Optional<String> attendingJudge;

    @Getter
    private Optional<String> attendingAppellant;

    @Getter
    private Optional<String> attendingHomeOfficeLegalRepresentative;

    @Getter
    private Optional<HoursAndMinutes> actualCaseHearingLength;

    @Getter
    private String ariaListingReference;

    @Getter
    private String listCaseHearingDate;

    @Getter
    private String listCaseHearingLength;

    private HearingCentre listCaseHearingCentre;
    private String appealDecision;
    private List<IdValue<DocumentWithMetadata>> finalDecisionAndReasonsDocuments;

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
        String appealDecision,
        List<IdValue<DocumentWithMetadata>> finalDecisionAndReasonsDocuments

    ) {
        requireNonNull(appealDecision);
        requireNonNull(finalDecisionAndReasonsDocuments);

        this.attendingJudge = attendingJudge;
        this.attendingAppellant = attendingAppellant;
        this.attendingHomeOfficeLegalRepresentative = attendingHomeOfficeLegalRepresentative;
        this.actualCaseHearingLength = actualCaseHearingLength;
        this.ariaListingReference = ariaListingReference;
        this.listCaseHearingCentre = listCaseHearingCentre;
        this.listCaseHearingDate = listCaseHearingDate;
        this.listCaseHearingLength = listCaseHearingLength;
        this.appealDecision = appealDecision;
        this.finalDecisionAndReasonsDocuments = finalDecisionAndReasonsDocuments;
    }

    public HearingCentre getListCaseHearingCentre() {
        requireNonNull(listCaseHearingCentre);
        return listCaseHearingCentre;
    }

    public String getAppealDecision() {
        requireNonNull(appealDecision);
        return appealDecision;
    }

    public List<IdValue<DocumentWithMetadata>> getFinalDecisionAndReasonsDocuments() {
        requireNonNull(finalDecisionAndReasonsDocuments);
        return finalDecisionAndReasonsDocuments;
    }

}
