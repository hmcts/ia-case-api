package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

class ApplyForCostsTest {
    private TypesOfAppliedCosts typesOfAppliedCosts = TypesOfAppliedCosts.UNREASONABLE_COSTS;
    private String argumentsAndEvidenceDetails = "Test details";
    private List<IdValue<Document>> argumentsAndEvidenceDocuments =
            List.of(new IdValue<>("1",
                    new Document("http://localhost/documents/123456",
                            "http://localhost/documents/123456",
                            "DocumentName.pdf")));
    private List<IdValue<Document>> scheduleOfCostsDocuments = Collections.emptyList();
    private YesOrNo applyForCostsHearingType = YesOrNo.YES;
    private String applyForCostsHearingTypeExplanation = "Test explanation";
    private String applyForCostsDecision = "Test decision";
    private String applyForCostsApplicantType = "Test applicant type";
    private String respondentToCostsOrder = "Respondent test type";
    private String applyForCostsCreationDate = "2020-09-21";

    private ApplyForCosts applyForCosts = new ApplyForCosts(
            typesOfAppliedCosts,
            argumentsAndEvidenceDetails,
            argumentsAndEvidenceDocuments,
            scheduleOfCostsDocuments,
            applyForCostsHearingType,
            applyForCostsHearingTypeExplanation,
            applyForCostsDecision,
            applyForCostsApplicantType,
            applyForCostsCreationDate,
            respondentToCostsOrder
    );

    @Test
    void should_hold_onto_values() {
        assertEquals(typesOfAppliedCosts, applyForCosts.getTypesOfAppliedCosts());
        assertEquals(argumentsAndEvidenceDetails, applyForCosts.getArgumentsAndEvidenceDetails());
        assertEquals(argumentsAndEvidenceDocuments, applyForCosts.getArgumentsAndEvidenceDocuments());
        assertEquals(scheduleOfCostsDocuments, applyForCosts.getScheduleOfCostsDocuments());
        assertEquals(applyForCostsHearingType, applyForCosts.getApplyForCostsHearingType());
        assertEquals(applyForCostsHearingTypeExplanation, applyForCosts.getApplyForCostsHearingTypeExplanation());
        assertEquals(applyForCostsDecision, applyForCosts.getApplyForCostsDecision());
        assertEquals(applyForCostsApplicantType, applyForCosts.getApplyForCostsApplicantType());
        assertEquals(applyForCostsCreationDate, applyForCosts.getApplyForCostsCreationDate());
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> new ApplyForCosts(
                null,
                argumentsAndEvidenceDetails,
                argumentsAndEvidenceDocuments,
                scheduleOfCostsDocuments,
                applyForCostsHearingType,
                applyForCostsHearingTypeExplanation,
                applyForCostsDecision,
                applyForCostsApplicantType,
                applyForCostsCreationDate,
                respondentToCostsOrder))
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new ApplyForCosts(
                typesOfAppliedCosts,
                argumentsAndEvidenceDetails,
                null,
                scheduleOfCostsDocuments,
                applyForCostsHearingType,
                applyForCostsHearingTypeExplanation,
                applyForCostsDecision,
                applyForCostsApplicantType,
                applyForCostsCreationDate,
                respondentToCostsOrder))
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new ApplyForCosts(
                typesOfAppliedCosts,
                argumentsAndEvidenceDetails,
                argumentsAndEvidenceDocuments,
                scheduleOfCostsDocuments,
                null,
                applyForCostsHearingTypeExplanation,
                applyForCostsDecision,
                applyForCostsApplicantType,
                applyForCostsCreationDate,
                respondentToCostsOrder))
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new ApplyForCosts(
                typesOfAppliedCosts,
                argumentsAndEvidenceDetails,
                argumentsAndEvidenceDocuments,
                scheduleOfCostsDocuments,
                applyForCostsHearingType,
                null,
                applyForCostsDecision,
                applyForCostsApplicantType,
                applyForCostsCreationDate,
                respondentToCostsOrder))
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new ApplyForCosts(
                typesOfAppliedCosts,
                argumentsAndEvidenceDetails,
                argumentsAndEvidenceDocuments,
                scheduleOfCostsDocuments,
                applyForCostsHearingType,
                applyForCostsHearingTypeExplanation,
                null,
                applyForCostsApplicantType,
                applyForCostsCreationDate,
                respondentToCostsOrder))
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new ApplyForCosts(
                typesOfAppliedCosts,
                argumentsAndEvidenceDetails,
                argumentsAndEvidenceDocuments,
                scheduleOfCostsDocuments,
                applyForCostsHearingType,
                applyForCostsHearingTypeExplanation,
                applyForCostsDecision,
                null,
                applyForCostsCreationDate,
                respondentToCostsOrder))
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new ApplyForCosts(
                typesOfAppliedCosts,
                argumentsAndEvidenceDetails,
                argumentsAndEvidenceDocuments,
                scheduleOfCostsDocuments,
                applyForCostsHearingType,
                applyForCostsHearingTypeExplanation,
                applyForCostsDecision,
                applyForCostsApplicantType,
                null,
                respondentToCostsOrder))
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new ApplyForCosts(
                typesOfAppliedCosts,
                argumentsAndEvidenceDetails,
                argumentsAndEvidenceDocuments,
                scheduleOfCostsDocuments,
                applyForCostsHearingType,
                applyForCostsHearingTypeExplanation,
                applyForCostsDecision,
                applyForCostsApplicantType,
                applyForCostsCreationDate,
                null))
                .isExactlyInstanceOf(NullPointerException.class);
    }
}

