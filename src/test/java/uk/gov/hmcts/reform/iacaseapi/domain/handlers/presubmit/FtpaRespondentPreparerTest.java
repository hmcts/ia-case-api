package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class FtpaRespondentPreparerTest {

    private static final int FTPA_DUE_IN_DAYS_OOC = 28;
    private static final int FTPA_DUE_IN_DAYS_UK = 14;
    private static final LocalDate FTPA_DUE_DATE_OOC = LocalDate.now().plusDays(FTPA_DUE_IN_DAYS_OOC);
    private static final LocalDate FTPA_DUE_DATE_UK = LocalDate.now().plusDays(FTPA_DUE_IN_DAYS_UK);

    private static final LocalDate MISSED_FTPA_SUBMISSION_DATE = LocalDate.now().minusDays(1);

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private FeatureToggler featureToggler;

    private FtpaRespondentPreparer ftpaRespondentPreparer;


    @BeforeEach
    public void setUp() {
        ftpaRespondentPreparer =
            new FtpaRespondentPreparer(dateProvider, featureToggler);

        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED)).thenReturn(Optional.of("No"));
    }

    @Test
    void should_set_out_of_date_submission_state_no() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        when(asylumCase.read(FTPA_APPLICATION_DEADLINE, String.class)).thenReturn(Optional.of(FTPA_DUE_DATE_UK.toString()));

        ftpaRespondentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(FTPA_RESPONDENT_SUBMISSION_OUT_OF_TIME, YesOrNo.NO);

    }

    @Test
    void should_set_out_of_date_submission_state_no_for_ooc_removal_of_client_decision_type() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        when(asylumCase.read(FTPA_APPLICATION_DEADLINE, String.class)).thenReturn(Optional.of(FTPA_DUE_DATE_OOC.toString()));

        ftpaRespondentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(FTPA_RESPONDENT_SUBMISSION_OUT_OF_TIME, YesOrNo.NO);

    }

    @Test
    void should_throw_when_ftpa_application_deadline_date_not_present() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPLICATION_DEADLINE, String.class)).thenReturn(Optional.empty());

        RequiredFieldMissingException thrown = assertThrows(
                RequiredFieldMissingException.class,
                () -> ftpaRespondentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback),
                    "Expected handler to throw RequiredFieldMissingException due to ftpaApplicationDeadline not present"
        );

        assertTrue(thrown.getMessage().contentEquals("FTPA application deadline missing."));

        assertNotNull(callback);

        verify(asylumCase, never()).write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, YesOrNo.YES);
        verify(asylumCase, never()).write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, YesOrNo.NO);
        verify(dateProvider, never()).now();

    }

    @Test
    void should_set_out_of_date_submission_state_to_yes_for_ooc_removal_of_client_decision_type() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
                .thenReturn(Optional.of(OutOfCountryDecisionType.REMOVAL_OF_CLIENT));
        when(asylumCase.read(FTPA_APPLICATION_DEADLINE, String.class)).thenReturn(Optional.of(MISSED_FTPA_SUBMISSION_DATE.toString()));

        ftpaRespondentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(FTPA_RESPONDENT_SUBMISSION_OUT_OF_TIME, YesOrNo.YES);
    }

    @Test
    void should_set_out_of_date_submission_state_to_yes() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());

        when(asylumCase.read(FTPA_APPLICATION_DEADLINE, String.class)).thenReturn(Optional.of(MISSED_FTPA_SUBMISSION_DATE.toString()));

        ftpaRespondentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(FTPA_RESPONDENT_SUBMISSION_OUT_OF_TIME, YesOrNo.YES);

    }

    @Test
    void should_throw_error_of_appeal_is_already_submitted() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED)).thenReturn(Optional.of("Yes"));

        final PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaRespondentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        assertEquals("You've already submitted an application. "
            + "You can only make one application at a time.", callbackResponse.getErrors().iterator().next());

        verify(asylumCase, never()).write(FTPA_RESPONDENT_SUBMISSION_OUT_OF_TIME, YesOrNo.YES);
        verify(asylumCase, never()).read(APPEAL_DATE);
        verify(dateProvider, never()).now();

    }

    @Test
    void should_clear_existing_fields_for_ftpa_reheard_case() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED)).thenReturn(Optional.of("Yes"));
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(dateProvider.now()).thenReturn(LocalDate.now());
        final String ftpaApplicationDeadline = dateProvider.now().minusDays(15).toString();
        when(asylumCase.read(FTPA_APPLICATION_DEADLINE, String.class)).thenReturn(Optional.of(ftpaApplicationDeadline));

        final PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaRespondentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).clear(FTPA_RESPONDENT_GROUNDS_DOCUMENTS);
        verify(asylumCase, times(1)).clear(FTPA_RESPONDENT_EVIDENCE_DOCUMENTS);
        verify(asylumCase).write(FTPA_RESPONDENT_SUBMISSION_OUT_OF_TIME, YesOrNo.YES);

    }

    @Test
    void should_not_clear_existing_fields_when_not_a_ftpa_reheard() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED)).thenReturn(Optional.of("Yes"));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        final PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaRespondentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).clear(FTPA_RESPONDENT_GROUNDS_DOCUMENTS);
        verify(asylumCase, times(0)).clear(FTPA_RESPONDENT_EVIDENCE_DOCUMENTS);

    }

    @Test
    void should_not_clear_existing_fields_when_feature_flag_disabled() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED)).thenReturn(Optional.of("Yes"));

        final PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaRespondentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).clear(FTPA_RESPONDENT_GROUNDS_DOCUMENTS);
        verify(asylumCase, times(0)).clear(FTPA_RESPONDENT_EVIDENCE_DOCUMENTS);

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> ftpaRespondentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> ftpaRespondentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = ftpaRespondentPreparer.canHandle(callbackStage, callback);

                if (event == Event.APPLY_FOR_FTPA_RESPONDENT
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> ftpaRespondentPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaRespondentPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaRespondentPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaRespondentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
