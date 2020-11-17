package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;


@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class FtpaAppellantPreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private DateProvider dateProvider;
    @Mock private FeatureToggler featureToggler;

    private FtpaAppellantPreparer ftpaAppellantPreparer;


    @Before
    public void setUp() {
        ftpaAppellantPreparer =
            new FtpaAppellantPreparer(dateProvider, 14, featureToggler);
    }

    @Test
    public void should_perform_mid_event_and_set_out_of_date_submission_state_no() {

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
    public void should_perform_mid_event_and_set_out_of_date_submission_state_no_when_no_appeal_date() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_DATE)).thenReturn(Optional.empty());

        ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, YesOrNo.NO);

    }

    @Test
    public void should_perform_mid_event_and_set_out_of_date_submission_state_to_yes() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        final String appealDate = dateProvider.now().minusDays(15).toString();
        when(asylumCase.read(APPEAL_DATE)).thenReturn(Optional.of(appealDate));

        ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, YesOrNo.YES);

    }

    @Test
    public void should_throw_error_of_appeal_is_already_submitted() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED)).thenReturn(Optional.of("Yes"));

        final PreSubmitCallbackResponse<AsylumCase> callbackResponse = ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        assertEquals("You've already submitted an application. "
                     + "You can only make one application at a time.", callbackResponse.getErrors().iterator().next());

        verify(asylumCase, never()).write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, YesOrNo.YES);
        verify(asylumCase, never()).read(APPEAL_DATE);
        verify(dateProvider, never()).now();

    }

    @Test
    public void should_clear_existing_fields_for_ftpa_reheard_case() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED)).thenReturn(Optional.of("Yes"));
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(dateProvider.now()).thenReturn(LocalDate.now());
        final String appealDate = dateProvider.now().minusDays(15).toString();
        when(asylumCase.read(APPEAL_DATE)).thenReturn(Optional.of(appealDate));

        final PreSubmitCallbackResponse<AsylumCase> callbackResponse = ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).clear(FTPA_APPELLANT_GROUNDS_DOCUMENTS);
        verify(asylumCase, times(1)).clear(FTPA_APPELLANT_EVIDENCE_DOCUMENTS);
        verify(asylumCase).write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, YesOrNo.YES);

    }

    @Test
    public void should_not_clear_existing_fields_when_not_a_ftpa_reheard() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED)).thenReturn(Optional.of("Yes"));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        final PreSubmitCallbackResponse<AsylumCase> callbackResponse = ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).clear(FTPA_APPELLANT_GROUNDS_DOCUMENTS);
        verify(asylumCase, times(0)).clear(FTPA_APPELLANT_EVIDENCE_DOCUMENTS);

    }

    @Test
    public void should_not_clear_existing_fields_when_feature_flag_disabled() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_APPELLANT_SUBMITTED)).thenReturn(Optional.of("Yes"));

        final PreSubmitCallbackResponse<AsylumCase> callbackResponse = ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).clear(FTPA_APPELLANT_GROUNDS_DOCUMENTS);
        verify(asylumCase, times(0)).clear(FTPA_APPELLANT_EVIDENCE_DOCUMENTS);

    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

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
    public void should_not_allow_null_arguments() {

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
