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
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class FtpaRespondentPreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private DateProvider dateProvider;

    FtpaRespondentPreparer ftpaRespondentPreparer;


    @BeforeEach
    void setUp() {

        ftpaRespondentPreparer =
            new FtpaRespondentPreparer(dateProvider, 14);
    }

    @Test
    void should_set_out_of_date_submission_state_no() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        final String appealDate = dateProvider.now().minusDays(1).toString();
        when(asylumCase.read(APPEAL_DATE)).thenReturn(Optional.of(appealDate));

        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED)).thenReturn(Optional.of("No"));

        ftpaRespondentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(FTPA_RESPONDENT_SUBMISSION_OUT_OF_TIME, YesOrNo.NO);

    }

    @Test
    void should_set_out_of_date_submission_state_no_when_no_appeal_date() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_DATE)).thenReturn(Optional.empty());

        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED)).thenReturn(Optional.of("No"));

        ftpaRespondentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(FTPA_RESPONDENT_SUBMISSION_OUT_OF_TIME, YesOrNo.NO);

    }

    @Test
    void should_set_out_of_date_submission_state_to_yes() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        final String appealDate = dateProvider.now().minusDays(15).toString();
        when(asylumCase.read(APPEAL_DATE)).thenReturn(Optional.of(appealDate));

        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED)).thenReturn(Optional.of("No"));

        ftpaRespondentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(FTPA_RESPONDENT_SUBMISSION_OUT_OF_TIME, YesOrNo.YES);

    }

    @Test
    void should_throw_error_of_appeal_is_already_submitted() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_RESPONDENT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(FTPA_RESPONDENT_SUBMITTED)).thenReturn(Optional.of("Yes"));

        final PreSubmitCallbackResponse<AsylumCase> callbackResponse = ftpaRespondentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        assertEquals("You've already submitted an application. "
                     + "You can only make one application at a time.", callbackResponse.getErrors().iterator().next());

        verify(asylumCase, never()).write(FTPA_RESPONDENT_SUBMISSION_OUT_OF_TIME, YesOrNo.YES);
        verify(asylumCase, never()).read(APPEAL_DATE);
        verify(dateProvider, never()).now();

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
