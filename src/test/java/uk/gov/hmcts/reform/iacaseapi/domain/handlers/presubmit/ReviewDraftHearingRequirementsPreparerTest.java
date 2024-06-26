package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ReviewDraftHearingRequirementsPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private UserDetails userDetails;
    @Mock
    private UserDetailsHelper userDetailsHelper;
    @Mock
    private List<IdValue<WitnessDetails>> witnessDetails;
    @Mock
    private List<IdValue<InterpreterLanguage>> interpreterLanguage;

    private ReviewDraftHearingRequirementsPreparer reviewDraftHearingRequirementsPreparer;

    @BeforeEach
    public void setUp() {
        reviewDraftHearingRequirementsPreparer =
            new ReviewDraftHearingRequirementsPreparer(userDetails, userDetailsHelper);

        when(callback.getEvent()).thenReturn(Event.REVIEW_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_review_hearing_requirements() {
        when(asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.TRIBUNAL_CASEWORKER);

        witnessDetails = Arrays.asList(
            new IdValue<>("1", new WitnessDetails("Witness1")),
            new IdValue<>("2", new WitnessDetails("Witness2"))
        );

        interpreterLanguage = Arrays.asList(
            new IdValue<>("1", new InterpreterLanguage("Irish", "N/A"))
        );

        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));
        when(asylumCase.read(INTERPRETER_LANGUAGE)).thenReturn(Optional.of(interpreterLanguage));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(WITNESS_DETAILS);
        verify(asylumCase, times(1)).read(INTERPRETER_LANGUAGE);

        verify(asylumCase, times(1)).write(
            WITNESS_DETAILS_READONLY,
            "Name\t\tWitness1\nName\t\tWitness2");
        verify(asylumCase, times(1)).write(
            INTERPRETER_LANGUAGE_READONLY,
            "Language\t\tIrish\nDialect\t\t\tN/A\n");
    }

    @Test
    void should_set_outside_evidence_out_of_country_default_for_old_cases() {
        when(asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_EVIDENCE_FROM_OUTSIDE_UK_OOC)).thenReturn(null);
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.TRIBUNAL_CASEWORKER);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(
            IS_EVIDENCE_FROM_OUTSIDE_UK_OOC,
            YesOrNo.NO);

        verify(asylumCase, times(1)).write(APPEAL_OUT_OF_COUNTRY, YesOrNo.NO);
    }

    @Test
    void should_set_outside_evidence_in_country_default_for_old_cases() {
        when(asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_EVIDENCE_FROM_OUTSIDE_UK_IN_COUNTRY)).thenReturn(null);
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.TRIBUNAL_CASEWORKER);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(
            IS_EVIDENCE_FROM_OUTSIDE_UK_IN_COUNTRY,
            YesOrNo.NO);
    }

    @Test
    void should_return_error_on_review_flag_is_missing() {
        when(asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)).thenReturn(Optional.empty());
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.TRIBUNAL_CASEWORKER);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        assertEquals(callbackResponse.getErrors().iterator().next(),
            "The case is already listed, you can't request hearing requirements");

        verify(asylumCase, times(0)).read(WITNESS_DETAILS);
        verify(asylumCase, times(0)).read(INTERPRETER_LANGUAGE);
    }

    @Test
    void should_return_error_on_reviewing_hearing_requirements_twice() {
        when(asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.TRIBUNAL_CASEWORKER);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        assertEquals("You've made an invalid request. The hearing requirements have already been reviewed.",
            callbackResponse.getErrors().iterator().next());

        verify(asylumCase, times(0)).read(WITNESS_DETAILS);
        verify(asylumCase, times(0)).read(INTERPRETER_LANGUAGE);
    }

    @Test
    void should_return_error_on_reviewing_hearing_requirements_if_submitted_when_case_was_ada() {
        when(asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(ADA_HEARING_REQUIREMENTS_SUBMITTED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.TRIBUNAL_CASEWORKER);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        assertEquals("You've made an invalid request. The hearing requirements have already been reviewed.",
            callbackResponse.getErrors().iterator().next());

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        assertThatThrownBy(() -> reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> reviewDraftHearingRequirementsPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewDraftHearingRequirementsPreparer.canHandle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewDraftHearingRequirementsPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_return_error_when_judge_tries_to_trigger_event_on_nonAda_case() {
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.JUDGE);
        when(asylumCase.read(AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        assertEquals("This option is not available. You can only review hearing requirements for accelerated detained appeals.",
            callbackResponse.getErrors().iterator().next());

    }

    @Test
    void judge_should_review_hearing_requirements_for_ada_case() {
        when(asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.JUDGE);
        when(asylumCase.read(AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        witnessDetails = Arrays.asList(
            new IdValue<>("1", new WitnessDetails("Witness1")),
            new IdValue<>("2", new WitnessDetails("Witness2"))
        );

        interpreterLanguage = Arrays.asList(
            new IdValue<>("1", new InterpreterLanguage("Irish", "N/A"))
        );

        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));
        when(asylumCase.read(INTERPRETER_LANGUAGE)).thenReturn(Optional.of(interpreterLanguage));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewDraftHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(WITNESS_DETAILS);
        verify(asylumCase, times(1)).read(INTERPRETER_LANGUAGE);

        verify(asylumCase, times(1)).write(
            WITNESS_DETAILS_READONLY,
            "Name\t\tWitness1\nName\t\tWitness2");
        verify(asylumCase, times(1)).write(
            INTERPRETER_LANGUAGE_READONLY,
            "Language\t\tIrish\nDialect\t\t\tN/A\n");
    }
}
