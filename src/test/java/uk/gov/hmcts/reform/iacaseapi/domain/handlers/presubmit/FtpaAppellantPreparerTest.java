package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
class FtpaAppellantPreparerTest {

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

    private FtpaAppellantPreparer ftpaAppellantPreparer;


    @BeforeEach
    public void setUp() {
        ftpaAppellantPreparer =
            new FtpaAppellantPreparer(dateProvider, 14,28, featureToggler);
    }

    @Test
    void should_perform_mid_event_and_set_out_of_date_submission_state_no() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        final String appealDate = dateProvider.now().minusDays(1).toString();
        when(asylumCase.read(APPEAL_DATE)).thenReturn(Optional.of(appealDate));

        ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, YesOrNo.NO);

    }

    @Test
    void should_perform_mid_event_and_set_out_of_date_submission_state_no_for_ooc_removal_of_client_decision_type() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        final String appealDate = dateProvider.now().minusDays(1).toString();
        when(asylumCase.read(APPEAL_DATE)).thenReturn(Optional.of(appealDate));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
            .thenReturn(Optional.of(OutOfCountryDecisionType.REMOVAL_OF_CLIENT));

        ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, YesOrNo.NO);

    }

    @Test
    void should_perform_mid_event_and_set_out_of_date_submission_state_no_when_no_appeal_date() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_DATE)).thenReturn(Optional.empty());

        ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, YesOrNo.NO);

    }


    @Test
    void should_perform_mid_event_and_set_out_of_date_submission_state_to_yes() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        final String appealDate = dateProvider.now().minusDays(29).toString();
        when(asylumCase.read(APPEAL_DATE)).thenReturn(Optional.of(appealDate));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
            .thenReturn(Optional.of(OutOfCountryDecisionType.REMOVAL_OF_CLIENT));

        ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, YesOrNo.YES);

    }

    @Test
    void should_perform_mid_event_and_set_out_of_date_submission_state_to_yes_for_ooc_removal_of_client_decision_type() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        final String appealDate = dateProvider.now().minusDays(29).toString();
        when(asylumCase.read(APPEAL_DATE)).thenReturn(Optional.of(appealDate));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
            .thenReturn(Optional.of(OutOfCountryDecisionType.REMOVAL_OF_CLIENT));

        ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, YesOrNo.YES);

    }

    @Test
    void should_throw_error_of_appeal_is_already_submitted() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED)).thenReturn(Optional.of("Yes"));

        final PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        assertEquals("You've already submitted an application. "
            + "You can only make one application at a time.", callbackResponse.getErrors().iterator().next());

        verify(asylumCase, never()).write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, YesOrNo.YES);
        verify(asylumCase, never()).read(APPEAL_DATE);
        verify(dateProvider, never()).now();

    }

    @Test
    void should_clear_existing_fields_for_ftpa_reheard_case() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED)).thenReturn(Optional.of("Yes"));
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(dateProvider.now()).thenReturn(LocalDate.now());
        final String appealDate = dateProvider.now().minusDays(15).toString();
        when(asylumCase.read(APPEAL_DATE)).thenReturn(Optional.of(appealDate));

        final PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).clear(FTPA_APPELLANT_GROUNDS_DOCUMENTS);
        verify(asylumCase, times(1)).clear(FTPA_APPELLANT_EVIDENCE_DOCUMENTS);
        verify(asylumCase).write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, YesOrNo.YES);

    }

    @Test
    void should_not_clear_existing_fields_when_not_a_ftpa_reheard() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED)).thenReturn(Optional.of("Yes"));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        final PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).clear(FTPA_APPELLANT_GROUNDS_DOCUMENTS);
        verify(asylumCase, times(0)).clear(FTPA_APPELLANT_EVIDENCE_DOCUMENTS);

    }

    @Test
    void should_clear_existing_fields_after_ftpa_loop() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED)).thenReturn(Optional.empty());

        final PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).clear(FTPA_APPELLANT_GROUNDS_DOCUMENTS);
        verify(asylumCase, times(1)).clear(FTPA_APPELLANT_EVIDENCE_DOCUMENTS);

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = ftpaAppellantPreparer.canHandle(callbackStage, callback);

                if (event == Event.APPLY_FOR_FTPA_APPELLANT
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

        assertThatThrownBy(() -> ftpaAppellantPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaAppellantPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaAppellantPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
