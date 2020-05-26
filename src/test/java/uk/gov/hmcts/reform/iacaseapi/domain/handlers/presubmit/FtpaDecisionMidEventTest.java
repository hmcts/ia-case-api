package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FtpaResidentJudgeDecisionOutcomeType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class FtpaDecisionMidEventTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private FtpaDecisionMidEvent ftpaDecisionMidEvent;

    @Before
    public void setUp() {
        ftpaDecisionMidEvent = new FtpaDecisionMidEvent();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getState()).thenReturn(State.FTPA_SUBMITTED);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = ftpaDecisionMidEvent.canHandle(callbackStage, callback);

                if ((event == Event.LEADERSHIP_JUDGE_FTPA_DECISION || event == Event.RESIDENT_JUDGE_FTPA_DECISION)
                    && callbackStage == MID_EVENT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> ftpaDecisionMidEvent.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> ftpaDecisionMidEvent.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> ftpaDecisionMidEvent.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaDecisionMidEvent.canHandle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_throw_if_ftpa_applicant_type_missing() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.LEADERSHIP_JUDGE_FTPA_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        assertThatThrownBy(() -> ftpaDecisionMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .hasMessage("FtpaApplicantType is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_error_when_there_is_no_ftpa_appellant_application_to_record_decision() {

        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.APPELLANT.toString()));
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors).containsOnly("You've made an invalid request. There is no appellant FTPA application to record the decision.");
    }

    @Test
    public void should_error_when_there_is_no_ftpa_respondent_application_to_record_decision() {

        when(callback.getEvent()).thenReturn(Event.LEADERSHIP_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors).containsOnly("You've made an invalid request. There is no respondent FTPA application to record the decision.");
    }

    @Test
    public void should_successfully_record_ftpa_appellant_decision() {

        when(callback.getEvent()).thenReturn(Event.LEADERSHIP_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.APPELLANT.toString()));
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    public void should_successfully_record_ftpa_respondent_decision() {

        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    public void should_successfully_set_ftpa_appellant_decision_reasons_notes_visibility_when_granted() {

        when(callback.getEvent()).thenReturn(Event.LEADERSHIP_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.APPELLANT.toString()));
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_APPELLANT_DECISION_REASONS_VISIBLE, YesOrNo.YES);
    }

    @Test
    public void should_successfully_set_ftpa_appellant_decision_reasons_notes_visibility_when_partially_granted() {

        when(callback.getEvent()).thenReturn(Event.LEADERSHIP_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.APPELLANT.toString()));
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.PARTIALLY_GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_APPELLANT_DECISION_REASONS_VISIBLE, YesOrNo.YES);
    }

    @Test
    public void should_successfully_set_ftpa_respondent_decision_reasons_notes_visibility_when_granted() {

        when(callback.getEvent()).thenReturn(Event.LEADERSHIP_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_RESPONDENT_DECISION_REASONS_VISIBLE, YesOrNo.YES);
    }

    @Test
    public void should_successfully_set_ftpa_respondent_decision_reasons_notes_visibility_when_partially_granted() {

        when(callback.getEvent()).thenReturn(Event.LEADERSHIP_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.PARTIALLY_GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_RESPONDENT_DECISION_REASONS_VISIBLE, YesOrNo.YES);
    }

    @Test
    public void should_successfully_set_appellant_notice_of_decision_set_aside_visibility_when_granted() {

        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.APPELLANT.toString()));
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(FtpaResidentJudgeDecisionOutcomeType.GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_APPELLANT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE, YesOrNo.YES);
    }

    @Test
    public void should_successfully_set_appellant_notice_of_decision_set_aside_visibility_when_partially_granted() {

        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.APPELLANT.toString()));
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(FtpaResidentJudgeDecisionOutcomeType.PARTIALLY_GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_APPELLANT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE, YesOrNo.YES);
    }

    @Test
    public void should_successfully_set_appellant_notice_of_decision_set_aside_visibility_when_refused() {

        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.APPELLANT.toString()));
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(FtpaResidentJudgeDecisionOutcomeType.REFUSED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_APPELLANT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE, YesOrNo.YES);
    }

    @Test
    public void should_successfully_set_respondent_notice_of_decision_set_aside_visibility_when_granted() {

        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(FtpaResidentJudgeDecisionOutcomeType.GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_RESPONDENT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE, YesOrNo.YES);
    }

    @Test
    public void should_successfully_set_respondent_notice_of_decision_set_aside_visibility_when_partially_granted() {

        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.PARTIALLY_GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_RESPONDENT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE, YesOrNo.YES);
    }

    @Test
    public void should_successfully_set_respondent_notice_of_decision_set_aside_visibility_when_refused() {

        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.REFUSED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_RESPONDENT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE, YesOrNo.YES);
    }


    @Test
    public void should_successfully_set_appellant_decision_objections_page_visibility_when_set_aside_is_yes() {

        when(callback.getEvent()).thenReturn(Event.LEADERSHIP_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.APPELLANT.toString()));
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(IS_FTPA_APPELLANT_NOTICE_OF_DECISION_SET_ASIDE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_APPELLANT_DECISION_OBJECTIONS_VISIBLE, YesOrNo.YES);
    }

    @Test
    public void should_successfully_set_respondent_decision_objections_page_visibility_when_set_aside_is_yes() {

        when(callback.getEvent()).thenReturn(Event.LEADERSHIP_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(IS_FTPA_RESPONDENT_NOTICE_OF_DECISION_SET_ASIDE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_RESPONDENT_DECISION_OBJECTIONS_VISIBLE, YesOrNo.YES);
    }

    @Test
    public void should_successfully_set_appellant_decision_objections_page_visibility_when_reheard_rule35() {

        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.APPELLANT.toString()));
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(FtpaResidentJudgeDecisionOutcomeType.REHEARD_RULE35.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_APPELLANT_DECISION_OBJECTIONS_VISIBLE, YesOrNo.YES);
    }

    @Test
    public void should_successfully_set_respondent_decision_objections_page_visibility_when_reheard_rule35() {

        when(callback.getEvent()).thenReturn(Event.LEADERSHIP_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(FtpaResidentJudgeDecisionOutcomeType.REHEARD_RULE35.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_RESPONDENT_DECISION_OBJECTIONS_VISIBLE, YesOrNo.YES);
    }

    @Test
    public void should_successfully_set_appellant_decision_reasons_notes_visibility_when_granted() {

        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.APPELLANT.toString()));
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(FtpaResidentJudgeDecisionOutcomeType.GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_APPELLANT_DECISION_REASONS_NOTES_VISIBLE, YesOrNo.YES);
    }

    @Test
    public void should_successfully_set_respondent_decision_reasons_notes_visibility_when_granted() {

        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(FtpaResidentJudgeDecisionOutcomeType.GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_RESPONDENT_DECISION_REASONS_NOTES_VISIBLE, YesOrNo.YES);
    }

    @Test
    public void should_successfully_set_appellant_decision_reasons_notes_visibility_when_partially_granted() {

        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.APPELLANT.toString()));
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(FtpaResidentJudgeDecisionOutcomeType.PARTIALLY_GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_APPELLANT_DECISION_REASONS_NOTES_VISIBLE, YesOrNo.YES);
    }

    @Test
    public void should_successfully_set_respondent_decision_reasons_notes_visibility_when_partially_granted() {

        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(FtpaResidentJudgeDecisionOutcomeType.PARTIALLY_GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_RESPONDENT_DECISION_REASONS_NOTES_VISIBLE, YesOrNo.YES);
    }


    @Test
    public void should_successfully_set_appellant_decision_listing_visibility_when_reheard_rule35() {

        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.APPELLANT.toString()));
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(FtpaResidentJudgeDecisionOutcomeType.REHEARD_RULE35.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_APPELLANT_DECISION_LISTING_VISIBLE, YesOrNo.YES);
    }

    @Test
    public void should_successfully_set_respondent_decision_listing_visibility_when_reheard_rule35() {

        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(FtpaResidentJudgeDecisionOutcomeType.REHEARD_RULE35.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_RESPONDENT_DECISION_LISTING_VISIBLE, YesOrNo.YES);
    }

    @Test
    public void should_successfully_set_appellant_decision_listing_visibility_when_reheard_rule32() {

        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.APPELLANT.toString()));
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(FtpaResidentJudgeDecisionOutcomeType.REHEARD_RULE32.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_APPELLANT_DECISION_LISTING_VISIBLE, YesOrNo.YES);
    }

    @Test
    public void should_successfully_set_respondent_decision_listing_visibility_when_reheard_rule32() {

        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.of(FtpaResidentJudgeDecisionOutcomeType.REHEARD_RULE32.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_RESPONDENT_DECISION_LISTING_VISIBLE, YesOrNo.YES);
    }

}
