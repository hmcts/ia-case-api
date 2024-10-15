package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HoursAndMinutes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

class PreviousHearingTest {

    private final Optional<String> attendingJudge = Optional.of("Judge Joe");
    private final Optional<String> attendingAppellant = Optional.of("Joe Bloggs");
    private final Optional<String> attendingHomeOfficeLegalRepresentative = Optional.of("Mr Cliff Evans");
    private final Optional<HoursAndMinutes> actualCaseHearingLength = Optional.of(new HoursAndMinutes("4", "30"));
    private final String ariaListingReference = "123456";
    private final HearingCentre listCaseHearingCentre = HearingCentre.NEWPORT;
    private final String listCaseHearingDate = "13/10/2020";
    private final String listCaseHearingLength = "6 hours";
    private final String appealDecision = "Dismissed";

    private final Document doc = new Document(
        "documentUrl",
        "binaryUrl",
        "documentFilename");

    private final DocumentWithMetadata decisionAndReasonsDocument = new DocumentWithMetadata(
        new Document(
            "documentUrl",
            "binaryUrl",
            "documentFilename"),
        "description",
        "dateUploaded",
        DocumentTag.FINAL_DECISION_AND_REASONS_PDF
    );

    private final List<IdValue<DocumentWithMetadata>> allFinalDecisionAndReasonsDocuments = asList(
        new IdValue<DocumentWithMetadata>(
            "1",
            decisionAndReasonsDocument
        )
    );

    private PreviousHearing previousHearing = new PreviousHearing(
        attendingJudge,
        attendingAppellant,
        attendingHomeOfficeLegalRepresentative,
        actualCaseHearingLength,
        ariaListingReference,
        listCaseHearingCentre,
        listCaseHearingDate,
        listCaseHearingLength,
        appealDecision,
        allFinalDecisionAndReasonsDocuments
    );

    @Test
    void should_hold_onto_values() {
        assertEquals(attendingJudge, previousHearing.getAttendingJudge());
        assertEquals(attendingAppellant, previousHearing.getAttendingAppellant());
        assertEquals(attendingHomeOfficeLegalRepresentative,
            previousHearing.getAttendingHomeOfficeLegalRepresentative());
        assertEquals(actualCaseHearingLength, previousHearing.getActualCaseHearingLength());
        assertEquals(ariaListingReference, previousHearing.getAriaListingReference());
        assertEquals(listCaseHearingCentre, previousHearing.getListCaseHearingCentre());
        assertEquals(listCaseHearingDate, previousHearing.getListCaseHearingDate());
        assertEquals(listCaseHearingLength, previousHearing.getListCaseHearingLength());
        assertEquals(appealDecision, previousHearing.getAppealDecision());
        assertEquals(allFinalDecisionAndReasonsDocuments, previousHearing.getFinalDecisionAndReasonsDocuments());
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new PreviousHearing(
            attendingJudge,
            attendingAppellant,
            attendingHomeOfficeLegalRepresentative,
            actualCaseHearingLength,
            null,
            listCaseHearingCentre,
            listCaseHearingDate,
            listCaseHearingLength,
            appealDecision,
            allFinalDecisionAndReasonsDocuments))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new PreviousHearing(
            attendingJudge,
            attendingAppellant,
            attendingHomeOfficeLegalRepresentative,
            actualCaseHearingLength,
            ariaListingReference,
            listCaseHearingCentre,
            listCaseHearingDate,
            null,
            appealDecision,
            allFinalDecisionAndReasonsDocuments))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new PreviousHearing(
            attendingJudge,
            attendingAppellant,
            attendingHomeOfficeLegalRepresentative,
            actualCaseHearingLength,
            ariaListingReference,
            listCaseHearingCentre,
            listCaseHearingDate,
            listCaseHearingLength,
            null,
            allFinalDecisionAndReasonsDocuments))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new PreviousHearing(
            attendingJudge,
            attendingAppellant,
            attendingHomeOfficeLegalRepresentative,
            actualCaseHearingLength,
            ariaListingReference,
            listCaseHearingCentre,
            listCaseHearingDate,
            listCaseHearingLength,
            appealDecision,
            null))
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
