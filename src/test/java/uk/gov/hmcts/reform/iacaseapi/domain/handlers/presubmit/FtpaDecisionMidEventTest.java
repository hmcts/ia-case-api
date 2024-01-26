package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_DECISION_LISTING_VISIBLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_DECISION_REASONS_NOTES_VISIBLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_DECISION_REASONS_VISIBLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPELLANT_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPLICANT_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_DECISION_LISTING_VISIBLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_DECISION_REASONS_NOTES_VISIBLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_DECISION_REASONS_VISIBLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_RESPONDENT_SUBMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class FtpaDecisionMidEventTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private FtpaDecisionMidEvent ftpaDecisionMidEvent;

    @BeforeEach
    public void setUp() {
        ftpaDecisionMidEvent = new FtpaDecisionMidEvent();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getState()).thenReturn(State.FTPA_SUBMITTED);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = ftpaDecisionMidEvent.canHandle(callbackStage, callback);

                if ( event == Event.DECIDE_FTPA_APPLICATION
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
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> ftpaDecisionMidEvent.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> ftpaDecisionMidEvent.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> ftpaDecisionMidEvent.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaDecisionMidEvent.canHandle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_throw_if_ftpa_applicant_type_missing() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        assertThatThrownBy(() -> ftpaDecisionMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .hasMessage("FtpaApplicantType is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_error_when_there_is_no_ftpa_appellant_application_to_record_decision() {

        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.APPELLANT.toString()));
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors).containsOnly(
            "You've made an invalid request. There is no appellant FTPA application to record the decision.");
    }

    @Test
    void should_error_when_there_is_no_ftpa_respondent_application_to_record_decision() {

        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors).containsOnly(
            "You've made an invalid request. There is no respondent FTPA application to record the decision.");
    }

    @Test
    void should_successfully_record_ftpa_appellant_decision() {

        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
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
    void should_successfully_record_ftpa_respondent_decision() {

        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
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
    void should_successfully_set_ftpa_appellant_decision_reasons_notes_visibility_when_granted() {

        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.APPELLANT.toString()));
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of(FtpaDecisionOutcomeType.GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_APPELLANT_DECISION_REASONS_VISIBLE, YesOrNo.YES);
    }

    @Test
    void should_successfully_set_ftpa_appellant_decision_reasons_notes_visibility_when_partially_granted() {

        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.APPELLANT.toString()));
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of(FtpaDecisionOutcomeType.PARTIALLY_GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_APPELLANT_DECISION_REASONS_VISIBLE, YesOrNo.YES);
    }

    @Test
    void should_successfully_set_ftpa_respondent_decision_reasons_notes_visibility_when_granted() {

        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of(FtpaDecisionOutcomeType.GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_RESPONDENT_DECISION_REASONS_VISIBLE, YesOrNo.YES);
    }

    @Test
    void should_successfully_set_ftpa_respondent_decision_reasons_notes_visibility_when_partially_granted() {

        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of(FtpaDecisionOutcomeType.PARTIALLY_GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_RESPONDENT_DECISION_REASONS_VISIBLE, YesOrNo.YES);
    }

    @ParameterizedTest
    @CsvSource({
        "APPELLANT, GRANTED",
        "APPELLANT, PARTIALLY_GRANTED",
        "APPELLANT, REFUSED",
        "APPELLANT, APPLICATION_NOT_ADMITTED",
        "RESPONDENT, GRANTED",
        "RESPONDENT, PARTIALLY_GRANTED",
        "RESPONDENT, REFUSED",
        "RESPONDENT, APPLICATION_NOT_ADMITTED",
    })
    void should_successfully_set_appellant_notice_of_decision_set_aside_visibility_when_different_statuses(
        Parties party,
        DecideFtpaApplicationType selection
    ) {
        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(party.toString()));

        when(asylumCase.read(party.equals(Parties.APPELLANT) ?
            FTPA_APPELLANT_SUBMITTED : FTPA_RESPONDENT_SUBMITTED, String.class))
            .thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(party.equals(Parties.APPELLANT) ?
            FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE : FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of(selection.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(party.equals(Parties.APPELLANT) ?
            FTPA_APPELLANT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE :
            FTPA_RESPONDENT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE, YesOrNo.YES);
    }


    @Test
    void should_successfully_set_respondent_notice_of_decision_set_aside_visibility_when_granted() {

        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of(DecideFtpaApplicationType.GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_RESPONDENT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE, YesOrNo.YES);
    }

    @Test
    void should_successfully_set_respondent_notice_of_decision_set_aside_visibility_when_partially_granted() {

        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of(FtpaDecisionOutcomeType.PARTIALLY_GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_RESPONDENT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE, YesOrNo.YES);
    }

    @Test
    void should_successfully_set_respondent_notice_of_decision_set_aside_visibility_when_refused() {

        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of(FtpaDecisionOutcomeType.REFUSED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_RESPONDENT_NOTICE_OF_DECISION_SET_ASIDE_VISIBLE, YesOrNo.YES);
    }


    @Test
    void should_successfully_set_appellant_decision_reasons_notes_visibility_when_granted() {

        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.APPELLANT.toString()));
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of(DecideFtpaApplicationType.GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_APPELLANT_DECISION_REASONS_NOTES_VISIBLE, YesOrNo.YES);
    }

    @Test
    void should_successfully_set_respondent_decision_reasons_notes_visibility_when_granted() {

        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of(DecideFtpaApplicationType.GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_RESPONDENT_DECISION_REASONS_NOTES_VISIBLE, YesOrNo.YES);
    }

    @Test
    void should_successfully_set_appellant_decision_reasons_notes_visibility_when_partially_granted() {

        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.APPELLANT.toString()));
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of(DecideFtpaApplicationType.PARTIALLY_GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_APPELLANT_DECISION_REASONS_NOTES_VISIBLE, YesOrNo.YES);
    }

    @Test
    void should_successfully_set_respondent_decision_reasons_notes_visibility_when_partially_granted() {

        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of(DecideFtpaApplicationType.PARTIALLY_GRANTED.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_RESPONDENT_DECISION_REASONS_NOTES_VISIBLE, YesOrNo.YES);
    }


    @Test
    void should_successfully_set_appellant_decision_listing_visibility_when_reheard_rule35() {

        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.APPELLANT.toString()));
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of(DecideFtpaApplicationType.REHEARD_RULE35.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_APPELLANT_DECISION_LISTING_VISIBLE, YesOrNo.YES);
    }

    @Test
    void should_successfully_set_respondent_decision_listing_visibility_when_reheard_rule35() {

        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));

        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of(DecideFtpaApplicationType.REHEARD_RULE35.toString()));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
        verify(asylumCase).write(FTPA_RESPONDENT_DECISION_LISTING_VISIBLE, YesOrNo.YES);
    }

    @ParameterizedTest
    @CsvSource({
        "FTPA_APPELLANT_SUBMITTED, APPELLANT, FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, REHEARD_RULE35",
        "FTPA_APPELLANT_SUBMITTED, APPELLANT, FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, REMADE_RULE32",
        "FTPA_RESPONDENT_SUBMITTED, RESPONDENT, FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, REHEARD_RULE35",
        "FTPA_RESPONDENT_SUBMITTED, RESPONDENT, FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, REMADE_RULE32",
    })
    void should_return_error_when_rj_select_appellant_or_respondent_for_reheard_rule35_rule32_or_remade_rule32_in_aip(
        AsylumCaseFieldDefinition ftpaSubmitted,
        Parties party,
        AsylumCaseFieldDefinition ftpaRjDecisionOutcomeType,
        DecideFtpaApplicationType selection
    ) {
        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(asylumCase.read(ftpaSubmitted, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(party.toString()));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(ftpaRjDecisionOutcomeType, String.class))
            .thenReturn(Optional.of(selection.toString()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors).containsOnly("For Legal Representative Journey Only");
    }

    @ParameterizedTest
    @CsvSource({
        "FTPA_APPELLANT_SUBMITTED, APPELLANT, FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, GRANTED",
        "FTPA_APPELLANT_SUBMITTED, APPELLANT, FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, PARTIALLY_GRANTED",
        "FTPA_APPELLANT_SUBMITTED, APPELLANT, FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, REFUSED",
        "FTPA_APPELLANT_SUBMITTED, APPELLANT, FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, APPLICATION_NOT_ADMITTED",
        "FTPA_RESPONDENT_SUBMITTED, RESPONDENT, FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, GRANTED",
        "FTPA_RESPONDENT_SUBMITTED, RESPONDENT, FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, PARTIALLY_GRANTED",
        "FTPA_RESPONDENT_SUBMITTED, RESPONDENT, FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, REFUSED",
        "FTPA_RESPONDENT_SUBMITTED, RESPONDENT, FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, APPLICATION_NOT_ADMITTED",
    })
    void should_record_when_rj_select_appellant_or_respondent_for_non_reheard_rule35_rule32_or_remade_rule32_in_aip(
        AsylumCaseFieldDefinition ftpaSubmitted,
        Parties party,
        AsylumCaseFieldDefinition ftpaRjDecisionOutcomeType,
        DecideFtpaApplicationType selection
    ) {
        when(callback.getEvent()).thenReturn(Event.DECIDE_FTPA_APPLICATION);
        when(asylumCase.read(ftpaSubmitted, String.class)).thenReturn(Optional.of(YesOrNo.YES.toString()));
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of(party.toString()));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(ftpaRjDecisionOutcomeType, String.class))
            .thenReturn(Optional.of(selection.toString()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaDecisionMidEvent.handle(MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
    }

}
