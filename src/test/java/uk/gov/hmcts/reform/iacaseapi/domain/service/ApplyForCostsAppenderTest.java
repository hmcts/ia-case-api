package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplyForCosts;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TypesOfAppliedCosts;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ApplyForCostsAppenderTest {
    @Mock
    private UserDetails userDetails;
    @Mock
    private UserDetailsHelper userDetailsHelper;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private IdValue<ApplyForCosts> applyForCostsById1;
    @Mock
    private IdValue<ApplyForCosts> applyForCostsById2;

    private String typesOfAppliedCosts = TypesOfAppliedCosts.UNREASONABLE_COSTS.toString();
    private String typesOfAppliedCostsDesc = TypesOfAppliedCosts.WASTED_COSTS.toString();
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
    private String applyForCostsApplicantTypeHM = "Home office";
    private String applyForCostsApplicantTypeLegalRep = "Legal representative";
    private String applyForCostsApplicantTypeJudge = "Tribunal";
    private String applyForCostsRespondentRoleLr = "Legal representative";
    private String applyForCostsRespondentRoleHo = "Home office";
    private String respondentToCostsOrder = "Test name of LegalRep";
    private String respondentToCostsOrderHomeOffice = "Home office";
    private String applyForCostsCreationDate = "2020-09-21";
    private ApplyForCostsAppender applyForCostsAppender;
    private String applyForCostsOotExplanation = "Test explanation";
    private String consideringReason = "Test reason";
    private List<IdValue<Document>> ootUploadEvidenceDocuments =
        List.of(new IdValue<>("1",
            new Document("http://localhost/documents/123456",
                "http://localhost/documents/123456",
                "DocumentName.pdf")));
    private YesOrNo isApplyForCostsOot = YesOrNo.YES;

    @BeforeEach
    public void setUp() {
        applyForCostsAppender =
            new ApplyForCostsAppender(userDetails, userDetailsHelper, dateProvider);
    }

    @Test
    void should_append_the_new_apply_for_costs_app_in_first_position() {

        when(dateProvider.now()).thenReturn(LocalDate.of(2020, 9, 21));
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.HOME_OFFICE_GENERIC);

        ApplyForCosts existingApplyForCosts1 = mock(ApplyForCosts.class);
        when(applyForCostsById1.getValue()).thenReturn(existingApplyForCosts1);

        ApplyForCosts existingApplyForCosts2 = mock(ApplyForCosts.class);
        when(applyForCostsById2.getValue()).thenReturn(existingApplyForCosts2);

        List<IdValue<ApplyForCosts>> existingAppliesForCosts = List.of(applyForCostsById1, applyForCostsById2);

        List<IdValue<ApplyForCosts>> allAppliesForCosts = applyForCostsAppender.append(
            existingAppliesForCosts,
            typesOfAppliedCosts,
            argumentsAndEvidenceDetails,
            argumentsAndEvidenceDocuments,
            scheduleOfCostsDocuments,
            applyForCostsHearingType,
            applyForCostsHearingTypeExplanation,
            applyForCostsDecision,
            respondentToCostsOrder,
            applyForCostsOotExplanation,
            ootUploadEvidenceDocuments,
            isApplyForCostsOot
        );

        assertNotNull(allAppliesForCosts);
        assertEquals(3, allAppliesForCosts.size());

        assertEquals("3", allAppliesForCosts.get(0).getId());
        assertEquals("2", allAppliesForCosts.get(1).getId());
        assertEquals("1", allAppliesForCosts.get(2).getId());

        assertEquals(typesOfAppliedCosts, allAppliesForCosts.get(0).getValue().getAppliedCostsType());
        assertEquals(argumentsAndEvidenceDetails, allAppliesForCosts.get(0).getValue().getArgumentsAndEvidenceDetails());
        assertEquals(argumentsAndEvidenceDocuments, allAppliesForCosts.get(0).getValue().getArgumentsAndEvidenceDocuments());
        assertEquals(scheduleOfCostsDocuments, allAppliesForCosts.get(0).getValue().getScheduleOfCostsDocuments());
        assertEquals(applyForCostsHearingType, allAppliesForCosts.get(0).getValue().getApplyForCostsHearingType());
        assertEquals(applyForCostsHearingTypeExplanation, allAppliesForCosts.get(0).getValue().getApplyForCostsHearingTypeExplanation());
        assertEquals(applyForCostsDecision, allAppliesForCosts.get(0).getValue().getApplyForCostsDecision());
        assertEquals(applyForCostsApplicantTypeHM, allAppliesForCosts.get(0).getValue().getApplyForCostsApplicantType());
        assertEquals(applyForCostsCreationDate, allAppliesForCosts.get(0).getValue().getApplyForCostsCreationDate());
        assertEquals(respondentToCostsOrder, allAppliesForCosts.get(0).getValue().getRespondentToCostsOrder());
        assertEquals(applyForCostsRespondentRoleLr, allAppliesForCosts.get(0).getValue().getApplyForCostsRespondentRole());

        assertEquals(existingApplyForCosts1, allAppliesForCosts.get(1).getValue());
        assertEquals(existingApplyForCosts2, allAppliesForCosts.get(2).getValue());
    }

    @Test
    void should_append_the_new_consider_making_costs_order_app_in_first_position() {

        when(dateProvider.now()).thenReturn(LocalDate.of(2020, 9, 21));
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.JUDGE);

        ApplyForCosts existingApplyForCosts1 = mock(ApplyForCosts.class);
        when(applyForCostsById1.getValue()).thenReturn(existingApplyForCosts1);

        ApplyForCosts existingApplyForCosts2 = mock(ApplyForCosts.class);
        when(applyForCostsById2.getValue()).thenReturn(existingApplyForCosts2);

        List<IdValue<ApplyForCosts>> existingAppliesForCosts = List.of(applyForCostsById1, applyForCostsById2);

        List<IdValue<ApplyForCosts>> allAppliesForCosts = applyForCostsAppender.append(
            existingAppliesForCosts,
            applyForCostsDecision,
            typesOfAppliedCosts,
            consideringReason,
            argumentsAndEvidenceDocuments,
            respondentToCostsOrder
        );

        assertNotNull(allAppliesForCosts);
        assertEquals(3, allAppliesForCosts.size());

        assertEquals("3", allAppliesForCosts.get(0).getId());
        assertEquals("2", allAppliesForCosts.get(1).getId());
        assertEquals("1", allAppliesForCosts.get(2).getId());

        assertEquals(applyForCostsDecision, allAppliesForCosts.get(0).getValue().getApplyForCostsDecision());
        assertEquals(typesOfAppliedCosts, allAppliesForCosts.get(0).getValue().getAppliedCostsType());
        assertEquals(applyForCostsApplicantTypeJudge, allAppliesForCosts.get(0).getValue().getApplyForCostsApplicantType());
        assertEquals(consideringReason, allAppliesForCosts.get(0).getValue().getTribunalConsideringReason());
        assertEquals(argumentsAndEvidenceDocuments, allAppliesForCosts.get(0).getValue().getJudgeEvidenceForCostsOrder());
        assertEquals(applyForCostsCreationDate, allAppliesForCosts.get(0).getValue().getApplyForCostsCreationDate());
        assertEquals(respondentToCostsOrder, allAppliesForCosts.get(0).getValue().getRespondentToCostsOrder());
        assertEquals(respondentToCostsOrder, allAppliesForCosts.get(0).getValue().getApplyForCostsRespondentRole());

        assertEquals(existingApplyForCosts1, allAppliesForCosts.get(1).getValue());
        assertEquals(existingApplyForCosts2, allAppliesForCosts.get(2).getValue());
    }

    @Test
    void should_return_new_apply_for_costs_app_if_no_existing() {
        when(dateProvider.now()).thenReturn(LocalDate.of(2020, 9, 21));
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.LEGAL_REPRESENTATIVE);

        List<IdValue<ApplyForCosts>> existingAppliesForCosts = Collections.emptyList();

        List<IdValue<ApplyForCosts>> allAppliesForCosts = applyForCostsAppender.append(
            existingAppliesForCosts,
            typesOfAppliedCosts,
            argumentsAndEvidenceDetails,
            argumentsAndEvidenceDocuments,
            scheduleOfCostsDocuments,
            applyForCostsHearingType,
            applyForCostsHearingTypeExplanation,
            applyForCostsDecision,
            respondentToCostsOrder,
            applyForCostsOotExplanation,
            ootUploadEvidenceDocuments,
            isApplyForCostsOot
        );

        assertNotNull(allAppliesForCosts);
        assertEquals(1, allAppliesForCosts.size());

        assertEquals(typesOfAppliedCosts, allAppliesForCosts.get(0).getValue().getAppliedCostsType());
        assertEquals(argumentsAndEvidenceDetails, allAppliesForCosts.get(0).getValue().getArgumentsAndEvidenceDetails());
        assertEquals(argumentsAndEvidenceDocuments, allAppliesForCosts.get(0).getValue().getArgumentsAndEvidenceDocuments());
        assertEquals(scheduleOfCostsDocuments, allAppliesForCosts.get(0).getValue().getScheduleOfCostsDocuments());
        assertEquals(applyForCostsHearingType, allAppliesForCosts.get(0).getValue().getApplyForCostsHearingType());
        assertEquals(applyForCostsHearingTypeExplanation, allAppliesForCosts.get(0).getValue().getApplyForCostsHearingTypeExplanation());
        assertEquals(applyForCostsDecision, allAppliesForCosts.get(0).getValue().getApplyForCostsDecision());
        assertEquals(applyForCostsApplicantTypeLegalRep, allAppliesForCosts.get(0).getValue().getApplyForCostsApplicantType());
        assertEquals(applyForCostsCreationDate, allAppliesForCosts.get(0).getValue().getApplyForCostsCreationDate());
        assertEquals(respondentToCostsOrderHomeOffice, allAppliesForCosts.get(0).getValue().getRespondentToCostsOrder());
        assertEquals(applyForCostsRespondentRoleHo, allAppliesForCosts.get(0).getValue().getApplyForCostsRespondentRole());
    }

    @Test
    void should_not_allow_null_values() {
        List<IdValue<ApplyForCosts>> existingAppliesForCosts = Collections.emptyList();

        assertThatThrownBy(() -> applyForCostsAppender.append(
            null,
            typesOfAppliedCosts,
            argumentsAndEvidenceDetails,
            argumentsAndEvidenceDocuments,
            scheduleOfCostsDocuments,
            applyForCostsHearingType,
            applyForCostsHearingTypeExplanation,
            applyForCostsDecision,
            respondentToCostsOrder,
            applyForCostsOotExplanation,
            ootUploadEvidenceDocuments,
            isApplyForCostsOot))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsAppender.append(
            existingAppliesForCosts,
            null,
            argumentsAndEvidenceDetails,
            argumentsAndEvidenceDocuments,
            scheduleOfCostsDocuments,
            applyForCostsHearingType,
            applyForCostsHearingTypeExplanation,
            applyForCostsDecision,
            respondentToCostsOrder,
            applyForCostsOotExplanation,
            ootUploadEvidenceDocuments,
            isApplyForCostsOot))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsAppender.append(
            existingAppliesForCosts,
            typesOfAppliedCosts,
            null,
            argumentsAndEvidenceDocuments,
            scheduleOfCostsDocuments,
            applyForCostsHearingType,
            applyForCostsHearingTypeExplanation,
            applyForCostsDecision,
            respondentToCostsOrder,
            applyForCostsOotExplanation,
            ootUploadEvidenceDocuments,
            isApplyForCostsOot))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsAppender.append(
            existingAppliesForCosts,
            typesOfAppliedCosts,
            argumentsAndEvidenceDetails,
            null,
            scheduleOfCostsDocuments,
            applyForCostsHearingType,
            applyForCostsHearingTypeExplanation,
            applyForCostsDecision,
            respondentToCostsOrder,
            applyForCostsOotExplanation,
            ootUploadEvidenceDocuments,
            isApplyForCostsOot))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsAppender.append(
            existingAppliesForCosts,
            typesOfAppliedCosts,
            argumentsAndEvidenceDetails,
            argumentsAndEvidenceDocuments,
            null,
            applyForCostsHearingType,
            applyForCostsHearingTypeExplanation,
            applyForCostsDecision,
            respondentToCostsOrder,
            applyForCostsOotExplanation,
            ootUploadEvidenceDocuments,
            isApplyForCostsOot))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsAppender.append(
            existingAppliesForCosts,
            typesOfAppliedCosts,
            argumentsAndEvidenceDetails,
            argumentsAndEvidenceDocuments,
            scheduleOfCostsDocuments,
            null,
            applyForCostsHearingTypeExplanation,
            applyForCostsDecision,
            respondentToCostsOrder,
            applyForCostsOotExplanation,
            ootUploadEvidenceDocuments,
            isApplyForCostsOot))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsAppender.append(
            existingAppliesForCosts,
            typesOfAppliedCosts,
            argumentsAndEvidenceDetails,
            argumentsAndEvidenceDocuments,
            scheduleOfCostsDocuments,
            applyForCostsHearingType,
            null,
            applyForCostsDecision,
            respondentToCostsOrder,
            applyForCostsOotExplanation,
            ootUploadEvidenceDocuments,
            isApplyForCostsOot))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsAppender.append(
            existingAppliesForCosts,
            typesOfAppliedCosts,
            argumentsAndEvidenceDetails,
            argumentsAndEvidenceDocuments,
            scheduleOfCostsDocuments,
            applyForCostsHearingType,
            applyForCostsHearingTypeExplanation,
            null,
            respondentToCostsOrder,
            applyForCostsOotExplanation,
            ootUploadEvidenceDocuments,
            isApplyForCostsOot))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsAppender.append(
            existingAppliesForCosts,
            typesOfAppliedCosts,
            argumentsAndEvidenceDetails,
            argumentsAndEvidenceDocuments,
            scheduleOfCostsDocuments,
            applyForCostsHearingType,
            applyForCostsHearingTypeExplanation,
            applyForCostsDecision,
            null,
            applyForCostsOotExplanation,
            ootUploadEvidenceDocuments,
            isApplyForCostsOot))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsAppender.append(
            existingAppliesForCosts,
            typesOfAppliedCosts,
            argumentsAndEvidenceDetails,
            argumentsAndEvidenceDocuments,
            scheduleOfCostsDocuments,
            applyForCostsHearingType,
            applyForCostsHearingTypeExplanation,
            applyForCostsDecision,
            respondentToCostsOrder,
            null,
            ootUploadEvidenceDocuments,
            isApplyForCostsOot))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_allow_null_values_for_judge_created_costs() {
        List<IdValue<ApplyForCosts>> existingAppliesForCosts = Collections.emptyList();

        assertThatThrownBy(() -> applyForCostsAppender.append(
            null,
            applyForCostsDecision,
            typesOfAppliedCosts,
            consideringReason,
            argumentsAndEvidenceDocuments,
            respondentToCostsOrder
        ))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsAppender.append(
            existingAppliesForCosts,
            null,
            typesOfAppliedCosts,
            consideringReason,
            argumentsAndEvidenceDocuments,
            respondentToCostsOrder
        ))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsAppender.append(
            existingAppliesForCosts,
            applyForCostsDecision,
            null,
            consideringReason,
            argumentsAndEvidenceDocuments,
            respondentToCostsOrder
        ))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsAppender.append(
            existingAppliesForCosts,
            applyForCostsDecision,
            typesOfAppliedCosts,
            null,
            argumentsAndEvidenceDocuments,
            respondentToCostsOrder
        ))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsAppender.append(
            existingAppliesForCosts,
            applyForCostsDecision,
            typesOfAppliedCosts,
            consideringReason,
            argumentsAndEvidenceDocuments,
            null
        ))
            .isExactlyInstanceOf(NullPointerException.class);
    }

}